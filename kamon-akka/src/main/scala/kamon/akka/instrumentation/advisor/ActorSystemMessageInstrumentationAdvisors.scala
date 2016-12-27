/*
 * =========================================================================================
 * Copyright © 2013-2016 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package akka.kamon.instrumentation.advisor

import akka.dispatch.sysmsg.EarliestFirstSystemMessageList
import kamon.agent.libs.net.bytebuddy.asm.Advice.{ Enter, OnMethodEnter, OnMethodExit, This }
import kamon.trace.{ EmptyTraceContext, TraceContext, TraceContextAware, Tracer }

/**
 * Advisor for akka.actor.RepointableActorRef::point
 */
class PointMethodAdvisor
object PointMethodAdvisor {
  @OnMethodEnter
  def onEnter(@This repointableActorRef: TraceContextAware): Unit = {
    Tracer.setCurrentContext(repointableActorRef.traceContext)
  }

  @OnMethodExit
  def onExit(): Unit = Tracer.currentContext.finish()
}

/**
 * Advisor for akka.actor.ActorCell::invokeAll
 */
class InvokeAllMethodAdvisor
object InvokeAllMethodAdvisor {
  @OnMethodEnter
  def onEnter(messages: EarliestFirstSystemMessageList): Unit = {
    if (messages.nonEmpty) {
      val ctx = messages.head.asInstanceOf[TraceContextAware].traceContext
      Tracer.setCurrentContext(ctx)
    }
  }

  @OnMethodExit
  def onExit(): Unit = {
    Tracer.currentContext.finish()
  }
}
