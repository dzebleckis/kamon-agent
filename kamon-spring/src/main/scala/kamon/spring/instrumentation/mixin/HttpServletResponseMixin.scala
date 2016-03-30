package kamon.spring.instrumentation.mixin

import kamon.trace.TraceContext

/**
 * Mixin for javax.servlet.http.HttpServletResponse
 */
class HttpServletResponseMixin extends TraceContextAwareExtension {
  @volatile private var _traceContext: TraceContext = _

  override def traceContext(): TraceContext = this._traceContext
  override def traceContext(traceContext: TraceContext): Unit = this._traceContext = traceContext
}

trait TraceContextAwareExtension {
  def traceContext(): TraceContext
  def traceContext(traceContext: TraceContext): Unit
}
