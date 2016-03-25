package kamon.spring.instrumentation.advisor

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import kamon.Kamon
import kamon.agent.libs.net.bytebuddy.asm.Advice.{Argument, OnMethodEnter, OnMethodExit}
import kamon.spring.SpringExtension
import kamon.spring.instrumentation.mixin.TraceContextAwareExtension
import kamon.trace.Tracer

/**
  * Advisor for org.springframework.web.servlet.DispatcherServlet::doDispatch
  */
class DoDispatchMethodAdvisor
object DoDispatchMethodAdvisor {
  @OnMethodEnter
  def onEnter(@Argument(0) request: HttpServletRequest, @Argument(1) response: HttpServletResponse): Unit = request match {
    case httpRequest: HttpServletRequest ⇒
      val traceName = SpringExtension.generateTraceName(httpRequest)
      val traceContext = Kamon.tracer.newContext(traceName)

      Tracer.setCurrentContext(traceContext)
      response.asInstanceOf[TraceContextAwareExtension].traceContext(traceContext)
    case other ⇒
      println("I didn't know this could happen :(")
  }

  @OnMethodExit
  def onExit(): Unit = Tracer.currentContext.finish()
}
