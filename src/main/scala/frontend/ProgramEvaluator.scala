package frontend

import engine._
import frontend.MdJsonCodec

/**
  * Result returned by ProgramEvaluator: the execution plan plus any warnings
  * that should be surfaced to the user (e.g. missing prices).
  */
case class EvaluationResult(plan: ExecutionPlan, warnings: Seq[String])

/**
  * High-level helper that parses Sophie source and turns it into an ExecutionPlan.
  *
  * Responsibilities & notes:
  *  - Parse the source code into a Program AST (via `SophieParserFacade`).
  *  - Evaluate the AST into an `ExecutionPlan` using the `engine.Evaluator`.
  *  - Compute lightweight warnings (e.g. missing market prices required to
  *    convert currency-amount values into quantities) and return them alongside
  *    the plan. Warnings are *non-fatal*: users can still preview/inspect the
  *    plan and fix market data before applying.
  *
  * Why this helper exists:
  *  - The TUI and the CLI share the same steps: parse -> evaluate -> warnings.
  *  - Centralizing this logic keeps consistent behaviour and messaging across
  *    interactive and batch use-cases.
  */
object ProgramEvaluator {
  def evaluate(programSrc: String, md: engine.MarketData): EvaluationResult = {
    val program = SophieParserFacade.parseString(programSrc)
    val plan = Evaluator.evaluate(program, md)

    // Detect trades that reference a currency different from the symbol but
    // cannot be converted because the price feed is missing.
    val missingPrices: Seq[String] = plan.trades.flatMap { d =>
      val sym = d.cmd.symbol
      val v = d.cmd.value
      if (v.currency != sym && md.price(sym).isEmpty) Some(sym) else None
    }.distinct

    val warnings = if (missingPrices.nonEmpty) Seq(s"Missing PRICE for symbol(s): ${missingPrices.mkString(", ")}") else Seq.empty
    EvaluationResult(plan, warnings)
  }
}
