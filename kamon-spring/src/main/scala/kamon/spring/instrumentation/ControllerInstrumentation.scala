package kamon.spring.instrumentation

import kamon.agent.libs.net.bytebuddy.description.method.MethodDescription
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatcher.Junction
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatchers
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatchers.{ isAbstract, named, not, takesArguments }
import kamon.agent.libs.net.bytebuddy.pool.TypePool
import kamon.agent.scala.KamonInstrumentation
import kamon.spring.instrumentation.advisor.RequestMappingAdvisor

class ControllerInstrumentation extends KamonInstrumentation {
  private val typePool = TypePool.Default.ofClassPath

  val requestMappingName = "org.springframework.web.bind.annotation.RequestMapping"

  /**
   * org.springframework.web.bind.annotation.RequestMapping
   */
  val requestMappingMethod: Junction[MethodDescription] = ElementMatchers.isAnnotatedWith(typePool.describe(requestMappingName).resolve())
  //.and(takesArguments[MethodDescription](classOf[HttpServletRequest], classOf[HttpServletResponse]))
  //.and(not(isAbstract()))

  annotatedWith("org.springframework.web.bind.annotation.RestController") { builder ⇒
    builder
      .withAdvisorFor(requestMappingMethod, classOf[RequestMappingAdvisor])
      .build()
  }

}
