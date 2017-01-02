package kamon.spring.instrumentation.advisor

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import kamon.Kamon
import kamon.agent.libs.net.bytebuddy.asm.Advice.{ Argument, OnMethodEnter, OnMethodExit }
import kamon.spring.SpringExtension
import kamon.spring.instrumentation.mixin.TraceContextAwareExtension
import kamon.trace.Tracer

class RequestMappingAdvisor
object RequestMappingAdvisor {
  @OnMethodEnter
  def onEnter(): Unit = {
    println("Entering to annotated method!")
  }

  //@OnMethodExit
  //def onExit(): Unit = Tracer.currentContext.finish()

}
