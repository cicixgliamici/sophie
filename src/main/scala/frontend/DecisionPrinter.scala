package frontend

import engine._

/** Rendering uses captured values only; a new quote cannot change an old explanation. */
object DecisionPrinter {
  def explain(plan: Option[ExecutionPlan], number: Option[String] = None): Vector[String] = plan match {
    case None => Vector("No plan to explain. Evaluate a program first.")
    case Some(value) =>
      val report = DecisionReport.from(value)
      number match {
        case None => render(report.trades)
        case Some(raw) => scala.util.Try(raw.toInt).toOption match {
          case Some(index) if index >= 1 && index <= report.trades.size => render(Vector(report.trades(index - 1)))
          case _ => Vector(s"Trade number must be between 1 and ${report.trades.size}.")
        }
      }
  }

  private def render(trades: Vector[ExplainedTrade]): Vector[String] = {
    if (trades.isEmpty) Vector("No trade decisions to explain.")
    else Vector("Decision explanations (conditions select trades; account checks happen when applying):") ++
      trades.flatMap { trade =>
        val status = if (trade.conditionMet) "EXECUTE" else "SKIP"
        Vector(s"Trade ${trade.number}: [$status] ${trade.summary}") ++
          trade.explanation.map(condition(_, "  ")).getOrElse(Vector("  No recorded explanation."))
      }
  }

  private def condition(trace: ConditionTrace, indent: String): Vector[String] = {
    val outcome = trace.result.map(_.toString).getOrElse("NOT EVALUATED")
    val reason = trace.skippedReason.map(text => s" ($text)").getOrElse("")
    Vector(s"$indent${trace.expression} => $outcome$reason") ++
      trace.operands.flatMap(operand(_, indent + "  ")) ++
      trace.children.flatMap(condition(_, indent + "  "))
  }

  private def operand(trace: OperandTrace, indent: String): Vector[String] = {
    val origin = trace.source match {
      case "indicator-override" => " [indicator override]"
      case "indicator-series" => " [computed from close series]"
      case "series-latest" => " [latest series value]"
      case _ => ""
    }
    Vector(s"$indent${trace.expression} = ${ExpressionText.number(trace.value)}$origin") ++
      trace.children.flatMap(operand(_, indent + "  "))
  }
}
