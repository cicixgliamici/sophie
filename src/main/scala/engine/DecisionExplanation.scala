package engine

import upickle.default._

/** Values are captured during evaluation, so explaining never reads market data again. */
final case class OperandTrace(expression: String, value: BigDecimal, source: String,
                              children: Vector[OperandTrace] = Vector.empty)
object OperandTrace {
  // Decimal strings preserve the exact value for JSON consumers that use binary floats.
  implicit val decimalRW: ReadWriter[BigDecimal] =
    readwriter[String].bimap[BigDecimal](_.toString, text => BigDecimal(text))
  implicit val rw: ReadWriter[OperandTrace] = macroRW
}

/** None means unevaluated, rather than false; skipped branches must carry a reason. */
final case class ConditionTrace(expression: String, result: Option[Boolean],
                                operands: Vector[OperandTrace] = Vector.empty,
                                children: Vector[ConditionTrace] = Vector.empty,
                                skippedReason: Option[String] = None) {
  require(result.isDefined != skippedReason.isDefined, "A condition must be evaluated or explicitly skipped")
}
object ConditionTrace { implicit val rw: ReadWriter[ConditionTrace] = macroRW }

final case class ExplainedTrade(number: Int, action: String, symbol: String,
                                conditionMet: Boolean, summary: String,
                                explanation: Option[ConditionTrace])
object ExplainedTrade { implicit val rw: ReadWriter[ExplainedTrade] = macroRW }

/** This report explains selection by conditions; account validation happens separately. */
final case class DecisionReport(schemaVersion: Int, trades: Vector[ExplainedTrade])
object DecisionReport {
  implicit val rw: ReadWriter[DecisionReport] = macroRW

  def from(plan: ExecutionPlan): DecisionReport = DecisionReport(1,
    plan.trades.zipWithIndex.map { case (decision, index) =>
      val action = decision.cmd.action match { case ast.Buy => "BUY"; case ast.Sell => "SELL" }
      ExplainedTrade(index + 1, action, decision.cmd.symbol, decision.shouldExecute,
        decision.detail, decision.explanation)
    }.toVector)
}
