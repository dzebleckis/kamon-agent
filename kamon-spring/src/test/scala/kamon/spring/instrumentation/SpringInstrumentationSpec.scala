package kamon.spring.instrumentation

import java.net.InetSocketAddress
import javax.servlet.Servlet
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

import kamon.Kamon
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.eclipse.jetty.servlet.ServletHolder
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.{ RequestMapping, RequestMethod, RestController }
import org.springframework.web.context.{ ContextLoaderListener, WebApplicationContext }
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.{ EnableWebMvc, ResourceHandlerRegistry, WebMvcConfigurerAdapter }

class SpringInstrumentationSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with JettySupport with KamonSpec {

  val CONTEXT_PATH = "kamon.spring.instrumentation"

  val CONFIG_LOCATION = "eu.kielczewski.example.config"
  val DEFAULT_PROFILE = "test"

  val webApplicationContext: WebApplicationContext = {
    val annotationContext: AnnotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext()
    annotationContext.setConfigLocation(CONFIG_LOCATION)
    annotationContext.getEnvironment.setDefaultProfiles(DEFAULT_PROFILE)
    annotationContext
  }

  "should propagate the TraceContext and record http server metrics for all processed requests" in {
    val httpclient = HttpClients.createDefault()
    val get = new HttpGet(s"http://localhost:$port/servlet-get")
    val notFound = new HttpGet(s"http://localhost:$port/servlet-not-found")
    val error = new HttpGet(s"http://localhost:$port/servlet-error")

    for (_ ← 1 to 10) {
      httpclient.execute(get)
    }

    for (_ ← 1 to 5) {
      httpclient.execute(notFound)
      httpclient.execute(error)
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
    jetty = new JettyServer(new InetSocketAddress(port))

    jetty.context.setErrorHandler(null)
    jetty.context.setContextPath(CONTEXT_PATH)
    jetty.context.addEventListener(new ContextLoaderListener(webApplicationContext))
    //println(s"********* PATH: ${new ClassPathResource("/").getPath}")
    //println(s"********* PATH: ${new ClassPathResource("/").getURI}")
    //println(s"********* PATH: ${new ClassPathResource("resources").getURI}")
    //jetty.context.setResourceBase(new ClassPathResource("resources/webapp").getURI.toString)

    jetty.start(new ServletHolder(new DispatcherServlet(webApplicationContext)), Some("/*"))
    Kamon.start()
  }
}

@Configuration
@EnableWebMvc
class WebMvcConfig extends WebMvcConfigurerAdapter {
  //override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
  //  registry.addResourceHandler("servlet-get/**").addResourceLocations("images/")
  //}
  println("Init WebMvcConfig")
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

  println("Init TestController")
}

class TestServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = req.getRequestURI match {
    case "/servlet-not-found" ⇒ resp.setStatus(404)
    case "/servlet-error"     ⇒ resp.setStatus(500)
    case other                ⇒ resp.setStatus(200)
  }
}

class SpringApp {

}
