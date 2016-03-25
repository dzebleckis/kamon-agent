package kamon.spring

import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

import akka.actor.ReflectiveDynamicAccess
import kamon.Kamon

object SpringExtension {

  private val config = Kamon.config.getConfig("kamon.spring")
  private val dynamic = new ReflectiveDynamicAccess(getClass.getClassLoader)

  private val nameGeneratorFQN = config.getString("name-generator")
  private val nameGenerator: NameGenerator = dynamic.createInstanceFor[NameGenerator](nameGeneratorFQN, Nil).get

  def generateTraceName(httpRequest: HttpServletRequest) = nameGenerator.generateTraceName(httpRequest)

}

trait NameGenerator {
  def generateTraceName(httpServletRequest: HttpServletRequest): String
  def generateSpringSegmentName(servletRequest: ServletRequest): String
}

class DefaultNameGenerator extends NameGenerator {
  override def generateTraceName(httpRequest: HttpServletRequest): String = s"${httpRequest.getMethod}:${httpRequest.getRequestURI}"
  override def generateSpringSegmentName(servletRequest: ServletRequest): String = "spring-async"
}
