package engine

import ast._

/**
  * ProgramValidator
  * ----------------
  *
  * This module introduces an explicit **validation phase** between parsing and
  * evaluation. The goal is to fail fast on semantic issues that can be detected
  * statically from the AST, before we touch runtime market data.
  *
  * Why this exists:
  *  - It gives users clearer error messages at "compile time" (program check time)
  *    instead of late runtime failures.
  *  - It provides a single place where language constraints are documented in code.
  *  - It keeps the Evaluator focused on execution semantics rather than policy checks.
  *
  * Current checks implemented (first step):
  *  1) At most one PORTFOLIO command per program.
  *  2) Indicator periods must be positive whole integers in Int range.
  *  3) Obvious literal division-by-zero expressions are rejected early.
  */
object ProgramValidator {

  /** Severity used by diagnostics. Kept extensible for future warning/info checks. */
  sealed trait Severity
  case object Error extends Severity

  /**
    * Structured validation diagnostic.
    *
    * @param code machine-friendly identifier (useful for tests/tooling)
    * @param message human-friendly explanation shown to users
    * @param severity currently always Error; model kept generic for future growth
    */
  final case class ValidationDiagnostic(code: String, message: String, severity: Severity)

  /**
    * Exception thrown when one or more validation diagnostics with Error severity
    * are present. This gives callers a typed way to distinguish validation
    * failures from parser/runtime failures.
    */
  final case class ProgramValidationException(diagnostics: Vector[ValidationDiagnostic])
      extends IllegalArgumentException(
        diagnostics.map(d => s"[${d.code}] ${d.message}").mkString("Validation failed:\n - ", "\n - ", "")
      )

  /**
    * Run all validation rules and return diagnostics.
    *
    * Keep this function pure: output only depends on the AST input.
    */
  def validate(program: Program): Vector[ValidationDiagnostic] = {
    val portfolioDiagnostics = validatePortfolioMultiplicity(program)
    val statementDiagnostics = program.statements.flatMap(validateStatement).toVector
    portfolioDiagnostics ++ statementDiagnostics
  }

  /** Validate and throw immediately when errors are present. */
  def validateOrThrow(program: Program): Unit = {
    val diagnostics = validate(program)
    if (diagnostics.nonEmpty) throw ProgramValidationException(diagnostics)
  }

  /** Rule: only one PORTFOLIO statement is accepted in the current language semantics. */
  private def validatePortfolioMultiplicity(program: Program): Vector[ValidationDiagnostic] = {
    val portfolioCount = program.statements.count(_.isInstanceOf[PortfolioCmd])
    if (portfolioCount <= 1) Vector.empty
    else Vector(
      ValidationDiagnostic(
        code = "VAL-PORTFOLIO-MULTIPLE",
        message = s"Program declares $portfolioCount PORTFOLIO blocks; only one is currently supported.",
        severity = Error
      )
    )
  }

  /** Validate each statement and recurse where needed. */
  private def validateStatement(stmt: Statement): Vector[ValidationDiagnostic] = stmt match {
    case t: TradeCmd     => validateCondition(t.condition)
    case p: PortfolioCmd => p.allocations.toVector.flatMap(a => validateOperand(a.value.amount))
  }

  /** Validate logical conditions by recursively checking nested operands/conditions. */
  private def validateCondition(cond: Condition): Vector[ValidationDiagnostic] = cond match {
    case AlwaysTrue         => Vector.empty
    case Comparison(l, _, r)=> validateOperand(l) ++ validateOperand(r)
    case And(a, b)          => validateCondition(a) ++ validateCondition(b)
    case Or(a, b)           => validateCondition(a) ++ validateCondition(b)
    case Parens(c)          => validateCondition(c)
  }

  /**
    * Validate arithmetic/indicator operands.
    *
    * Note: this phase intentionally checks only rules that are independent from
    * market snapshots. Missing prices/series remain runtime concerns for now.
    */
  private def validateOperand(op: Operand): Vector[ValidationDiagnostic] = op match {
    case NumberLiteral(_)            => Vector.empty
    case Price(_)                    => Vector.empty
    case SeriesOperation(_, _)       => Vector.empty

    case AggFunc(name, symbol, period) =>
      validateIndicatorPeriod(name, symbol, period)

    case Binary(Div, left, NumberLiteral(v)) if v == 0 =>
      // We can detect this statically because the denominator is literally zero.
      validateOperand(left) :+ ValidationDiagnostic(
        code = "VAL-DIV-BY-ZERO-LITERAL",
        message = "Division by zero detected in expression with literal denominator 0.",
        severity = Error
      )

    case Binary(_, left, right) =>
      validateOperand(left) ++ validateOperand(right)
  }

  /** Ensure indicator period follows current DSL rule: positive whole Int value. */
  private def validateIndicatorPeriod(name: String, symbol: String, period: BigDecimal): Vector[ValidationDiagnostic] = {
    val min = BigDecimal(Int.MinValue)
    val max = BigDecimal(Int.MaxValue)

    if (!period.isWhole || period < min || period > max) {
      Vector(
        ValidationDiagnostic(
          code = "VAL-INDICATOR-PERIOD-NON-INTEGER",
          message = s"$name($symbol, $period) requires an integer period within Int range.",
          severity = Error
        )
      )
    } else if (period.toInt <= 0) {
      Vector(
        ValidationDiagnostic(
          code = "VAL-INDICATOR-PERIOD-NON-POSITIVE",
          message = s"$name($symbol, $period) requires a strictly positive period.",
          severity = Error
        )
      )
    } else Vector.empty
  }
}
