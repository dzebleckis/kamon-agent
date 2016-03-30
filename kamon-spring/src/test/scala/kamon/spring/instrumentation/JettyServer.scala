package kamon.spring.instrumentation

import java.net.InetSocketAddress
import java.net.InetSocketAddress
import javax.servlet.Servlet

import kamon.Kamon
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ ServletContextHandler, ServletHolder }
import org.scalatest.BeforeAndAfterAll

class JettyServer(socketAddress: InetSocketAddress = new InetSocketAddress(8080)) {
  val server = new Server(socketAddress)
  val context = new ServletContextHandler(server, "/")

  //def start(clazz: Class[_ <: Servlet]): this.type = start(new ServletHolder(clazz))

  //def start[T <: Servlet](servlet: T, path: Option[String] = None): this.type = start(new ServletHolder(servlet), path)

  def start(servletHolder: ServletHolder, path: Option[String] = None): this.type = {
    val servlet = servletHolder
    servlet.setAsyncSupported(true)
    context.addServlet(servlet, path getOrElse "/")
    server.start()
    this
  }

  def stop(): this.type = {
    server.stop()
    this
  }

  def join(): this.type = {
    server.join()
    this
  }

  //def withContext(f: ServletContextHandler => Unit) = {
  //  f(context)
  //}

}

trait JettySupport {
  self: BeforeAndAfterAll ⇒

  // def servletClass: Class[_ <: Servlet]

  var jetty: JettyServer = _
  var port: Int = 8000

  //override protected def beforeAll(): Unit = {
  //  jetty = new JettyServer(new InetSocketAddress(port)).start(servletClass)
  //  Kamon.start()
  //}

  override protected def afterAll(): Unit = {
    Kamon.shutdown()
    jetty.stop()
  }
}
