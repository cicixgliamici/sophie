package engine

import ast._

/** Canonical expressions preserve grouping without depending on the parser's source text. */
object ExpressionText {
  def number(value: BigDecimal): String = value.bigDecimal.stripTrailingZeros.toPlainString

  def operand(value: Operand): String = value match {
    case NumberLiteral(amount) => number(amount)
    case Price(symbol) => s"PRICE($symbol)"
    case SeriesOperation(symbol, field) => s"$symbol.$field"
    case AggFunc(name, symbol, period) => s"$name($symbol, ${number(period)})"
    case Binary(operator, left, right) => s"(${operand(left)} ${arithmetic(operator)} ${operand(right)})"
  }

  def condition(value: Condition): String = value match {
    case AlwaysTrue => "No IF condition (unconditional)"
    case Comparison(left, operator, right) => s"${operand(left)} ${comparison(operator)} ${operand(right)}"
    case And(left, right) => s"(${condition(left)} && ${condition(right)})"
    case Or(left, right) => s"(${condition(left)} || ${condition(right)})"
    case Parens(inner) => s"(${condition(inner)})"
  }

  private def arithmetic(operator: ArithOp): String = operator match {
    case Add => "+"; case Sub => "-"; case Mul => "*"; case Div => "/"
  }

  private def comparison(operator: CompOp): String = operator match {
    case GT => ">"; case LT => "<"; case EQ => "="; case NEQ => "!="
  }
}
