package kamon.spring.instrumentation

import java.net.InetSocketAddress
import javax.servlet.http.HttpServletResponse

import kamon.Kamon
import kamon.spring.instrumentation.server.JettyServer
import kamon.util.logger.LazyLogger
import org.apache.http.client.methods.{ CloseableHttpResponse, HttpGet }
import org.apache.http.impl.client.HttpClients
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ ServletContextHandler, ServletHolder }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import org.springframework.web.bind.annotation.{ RequestMapping, RequestMethod, RestController }
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.context.{ ContextLoaderListener, WebApplicationContext }
import org.springframework.web.servlet.DispatcherServlet

class SpringInstrumentationSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with KamonSpec {

  val log = LazyLogger(classOf[SpringInstrumentationSpec])

  val CONTEXT_PATH = "kamon.spring.instrumentation"
  val CONFIG_LOCATION = "kamon.spring.instrumentation.config"
  val DEFAULT_PROFILE = "test"

  private val MAPPING_URL: String = "/*"

  var port: Int = 8000

  var server: Server = _

  "should propagate the TraceContext and record http server metrics for all processed requests" in {
    val httpclient = HttpClients.createDefault()
    val get = new HttpGet(s"http://localhost:$port/servlet-get")
    val notFound = new HttpGet(s"http://localhost:$port/servlet-not-found")
    val error = new HttpGet(s"http://localhost:$port/servlet-error")

    for (_ ← 1 to 10) {
      closeAtEnd(httpclient.execute(get)) { httpResponse: CloseableHttpResponse ⇒
        Thread.sleep(3000)
      }
    }

    for (_ ← 1 to 5) {
      closeAtEnd(httpclient.execute(notFound)) { httpResponse: CloseableHttpResponse ⇒
        Thread.sleep(3000)
      }
      closeAtEnd(httpclient.execute(error)) { httpResponse: CloseableHttpResponse ⇒
        Thread.sleep(3000)
      }
    }

    val getSnapshot = takeSnapshotOf("GET:/servlet-get", "trace")
    getSnapshot.histogram("elapsed-time").get.numberOfMeasurements should be(10)

    val metrics = takeSnapshotOf("servlet", "http-server")
    metrics.counter("GET:/servlet-get_200").get.count should be(10)
    metrics.counter("GET:/servlet-not-found_404").get.count should be(5)
    metrics.counter("GET:/servlet-error_500").get.count should be(5)
    metrics.counter("200").get.count should be(10)
    metrics.counter("404").get.count should be(5)
    metrics.counter("500").get.count should be(5)
  }

  override protected def beforeAll(): Unit = {
    val webApplicationContext: WebApplicationContext = getContext
    val dispatcherServlet: DispatcherServlet = new DispatcherServlet(webApplicationContext)

    server = new Server(new InetSocketAddress(port))

    val servletContextHandler: ServletContextHandler = new ServletContextHandler
    val servletHolder: ServletHolder = new ServletHolder(dispatcherServlet)

    servletContextHandler.addServlet(servletHolder, "/")

    servletContextHandler.setErrorHandler(null)
    servletContextHandler.addEventListener(new ContextLoaderListener(webApplicationContext))

    server.setHandler(servletContextHandler)
    server.start()

    log.info(s"Server started at port $port")
    Kamon.start()
  }

  override protected def afterAll(): Unit = {
    Kamon.shutdown()
    server.stop()
  }

  private def getContext: WebApplicationContext = {
    val context: AnnotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext()
    context.setConfigLocation(CONFIG_LOCATION)
    context.getEnvironment.setDefaultProfiles(DEFAULT_PROFILE)
    context
  }

  private def closeAtEnd(httpResponse: CloseableHttpResponse)(f: CloseableHttpResponse ⇒ _) = {
    try {
      f(httpResponse)
    } finally {
      httpResponse.close()
    }
  }
}

@RestController
class TestController {

  @RequestMapping(value = Array("/servlet-get"), method = Array(RequestMethod.GET))
  def get: String = "ok response"

  @RequestMapping(value = Array("/servlet-not-found"), method = Array(RequestMethod.GET))
  def getNotFound(response: HttpServletResponse): String = {
    response.setStatus(404)
    "not found response"
  }

  @RequestMapping(value = Array("/servlet-error"), method = Array(RequestMethod.GET))
  def getError(response: HttpServletResponse): String = {
    response.setStatus(500)
    "error response"
  }
}
