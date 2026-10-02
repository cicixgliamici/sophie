package engine

import ast._
import scala.math.BigDecimal.RoundingMode

/** Evaluate and explain in one traversal, preserving lookup order and short-circuiting. */
object ConditionEvaluator {
  /** Return a recorded condition tree; evaluated runtime errors still fail the program. */
  def evaluate(condition: Condition, md: MarketData): ConditionTrace = condition match {
    case AlwaysTrue => ConditionTrace(ExpressionText.condition(condition), Some(true))
    case Comparison(left, operator, right) =>
      val leftTrace = operand(left, md)
      val rightTrace = operand(right, md)
      ConditionTrace(ExpressionText.condition(condition), Some(compare(operator, leftTrace.value, rightTrace.value)),
        operands = Vector(leftTrace, rightTrace))
    case And(left, right) => logical(condition, left, right, md, shortCircuitValue = false)
    case Or(left, right) => logical(condition, left, right, md, shortCircuitValue = true)
    case Parens(inner) =>
      val child = evaluate(inner, md)
      ConditionTrace(ExpressionText.condition(condition), child.result, children = Vector(child))
  }

  private def logical(condition: Condition, left: Condition, right: Condition,
                      md: MarketData, shortCircuitValue: Boolean): ConditionTrace = {
    val leftTrace = evaluate(left, md)
    val leftResult = leftTrace.result.get
    val rightTrace = if (leftResult == shortCircuitValue) {
      val operator = if (shortCircuitValue) "OR" else "AND"
      // Describe the skipped expression without evaluating any of its operands.
      ConditionTrace(ExpressionText.condition(right), None,
        skippedReason = Some(s"$operator short-circuit: left condition is $leftResult"))
    } else evaluate(right, md)
    val result = rightTrace.result.getOrElse(leftResult)
    ConditionTrace(ExpressionText.condition(condition), Some(result), children = Vector(leftTrace, rightTrace))
  }

  private def compare(operator: CompOp, left: BigDecimal, right: BigDecimal): Boolean = operator match {
    case GT => left > right; case LT => left < right; case EQ => left == right; case NEQ => left != right
  }

  private def operand(expression: Operand, md: MarketData): OperandTrace = expression match {
    case NumberLiteral(value) => trace(expression, value, "literal")
    case Price(symbol) =>
      trace(expression, md.price(symbol).getOrElse(error(s"Missing PRICE($symbol)")), "market-price")
    case SeriesOperation(symbol, field) =>
      trace(expression, md.latest(symbol, field).getOrElse(error(s"Missing $symbol.$field")), "series-latest")
    case AggFunc(name, symbol, period) => indicator(expression, name, symbol, period, md)
    case Binary(operator, left, right) =>
      val leftTrace = operand(left, md)
      val rightTrace = operand(right, md)
      OperandTrace(ExpressionText.operand(expression), arithmetic(operator, leftTrace.value, rightTrace.value),
        "arithmetic", Vector(leftTrace, rightTrace))
  }

  private def indicator(expression: Operand, name: String, symbol: String,
                        period: BigDecimal, md: MarketData): OperandTrace = {
    if (!period.isWhole || !period.isValidInt || period <= 0)
      error(s"$name($symbol, $period): period must be a positive integer within Int range")
    val window = period.toInt
    md.indicatorOverride(name, symbol, window) match {
      case Some(value) => trace(expression, value, "indicator-override")
      case None =>
        val closes = md.series(symbol, "close").getOrElse(error(s"$name($symbol, $window) needs $symbol.close series"))
        trace(expression, Indicators.compute(name, closes, window), "indicator-series")
    }
  }

  private def arithmetic(operator: ArithOp, left: BigDecimal, right: BigDecimal): BigDecimal = operator match {
    case Add => left + right; case Sub => left - right; case Mul => left * right
    // Preserve the language's existing division precision in both value and explanation.
    case Div => if (right == 0) error("Division by zero") else (left / right).setScale(10, RoundingMode.HALF_UP)
  }

  private def trace(expression: Operand, value: BigDecimal, source: String): OperandTrace =
    OperandTrace(ExpressionText.operand(expression), value, source)

  private def error(message: String): Nothing = throw new IllegalStateException(message)
}
