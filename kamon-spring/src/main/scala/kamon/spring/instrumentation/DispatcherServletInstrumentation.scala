package kamon.spring.instrumentation

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import kamon.agent.libs.net.bytebuddy.description.method.MethodDescription
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatcher.Junction
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatchers._
import kamon.agent.scala.KamonInstrumentation
import kamon.spring.instrumentation.advisor.DoDispatchMethodAdvisor
import kamon.spring.instrumentation.mixin.HttpServletResponseMixin

class DispatcherServletInstrumentation extends KamonInstrumentation {

  /**
    * org.springframework.web.servlet.DispatcherServlet.doDispatch
    */
  val doDispatchMethod: Junction[MethodDescription] = named("doDispatch")
    .and(takesArguments[MethodDescription](classOf[HttpServletRequest], classOf[HttpServletResponse]))
    .and(not(isAbstract()))

  forSubtypeOf("org.springframework.web.servlet.DispatcherServlet") { builder =>
    builder
      .withAdvisorFor(doDispatchMethod, classOf[DoDispatchMethodAdvisor])
      .build()
  }

  forSubtypeOf("javax.servlet.http.HttpServletResponse") { builder =>
    builder
      .withMixin(classOf[HttpServletResponseMixin])
      .build()
  }

}
