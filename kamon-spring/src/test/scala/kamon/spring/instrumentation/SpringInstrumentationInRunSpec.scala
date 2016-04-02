package kamon.spring.instrumentation

import java.net.InetSocketAddress
import javax.servlet.Servlet
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

import kamon.Kamon
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ ServletContextHandler, ServletHolder }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.{ RequestMapping, RequestMethod, RestController }
import org.springframework.web.context.{ ContextLoaderListener, WebApplicationContext }
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.{ EnableWebMvc, ResourceHandlerRegistry, WebMvcConfigurerAdapter }

class SpringInstrumentationInRunSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with KamonSpec {

  val CONTEXT_PATH = "kamon.spring.instrumentation"

  val CONFIG_LOCATION = "kamon.spring.instrumentation.config"
  val DEFAULT_PROFILE = "test"

  //  val webApplicationContext: WebApplicationContext = {
  //    val annotationContext: AnnotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext()
  //    annotationContext.setConfigLocation(CONFIG_LOCATION)
  //    annotationContext.getEnvironment.setDefaultProfiles(DEFAULT_PROFILE)
  //    annotationContext
  //  }

  private val MAPPING_URL: String = "/*"

  var port: Int = 8000

  var server: Server = _

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

    println(s"Starting server at port $port")

    val server = new Server(new InetSocketAddress(port))

    server.setHandler(getServletContextHandler(getContext))

    server.start()

    println(s"Server started at port $port")

    //    jetty = new JettyServer(new InetSocketAddress(port))
    //
    //    jetty.context.setErrorHandler(null)
    //    jetty.context.setContextPath(CONTEXT_PATH)
    //    jetty.context.addEventListener(new ContextLoaderListener(webApplicationContext))
    //    //println(s"********* PATH: ${new ClassPathResource("/").getPath}")
    //    //println(s"********* PATH: ${new ClassPathResource("/").getURI}")
    //    //println(s"********* PATH: ${new ClassPathResource("resources").getURI}")
    //    //jetty.context.setResourceBase(new ClassPathResource("resources/webapp").getURI.toString)
    //
    //    jetty.start(new ServletHolder(new DispatcherServlet(webApplicationContext)), Some("/*"))
    Kamon.start()
  }

  override protected def afterAll(): Unit = {
    Kamon.shutdown()
    server.stop()
  }

  private def getServletContextHandler(context: WebApplicationContext): ServletContextHandler = {
    val contextHandler: ServletContextHandler = new ServletContextHandler()
    contextHandler.setErrorHandler(null)
    contextHandler.setContextPath(CONTEXT_PATH)
    contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), MAPPING_URL)
    contextHandler.addEventListener(new ContextLoaderListener(context))
    contextHandler.setResourceBase(new ClassPathResource("webapp").getURI.toString)
    contextHandler
  }

  private def getContext: WebApplicationContext = {
    val context: AnnotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext()
    context.setConfigLocation(CONFIG_LOCATION)
    context.getEnvironment.setDefaultProfiles(DEFAULT_PROFILE)
    context
  }

  //  private def oldBeforeAll(): Unit = {
  //    jetty = new JettyServer(new InetSocketAddress(port))
  //
  //    jetty.context.setErrorHandler(null)
  //    jetty.context.setContextPath(CONTEXT_PATH)
  //    jetty.context.addEventListener(new ContextLoaderListener(webApplicationContext))
  //    //println(s"********* PATH: ${new ClassPathResource("/").getPath}")
  //    //println(s"********* PATH: ${new ClassPathResource("/").getURI}")
  //    //println(s"********* PATH: ${new ClassPathResource("resources").getURI}")
  //    //jetty.context.setResourceBase(new ClassPathResource("resources/webapp").getURI.toString)
  //
  //    jetty.start(new ServletHolder(new DispatcherServlet(webApplicationContext)), Some("/*"))
  //    Kamon.start()
  //  }
}

//@Configuration
//@EnableWebMvc
//class WebMvcConfig extends WebMvcConfigurerAdapter {
//  //override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
//  //  registry.addResourceHandler("servlet-get/**").addResourceLocations("images/")
//  //}
//  println("Init WebMvcConfig")
//}

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