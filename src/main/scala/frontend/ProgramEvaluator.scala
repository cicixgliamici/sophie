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
  * The TUI/CLI call this object so that parsing, semantic evaluation, and warning
  * detection (currently: missing market prices for currency conversion) stay
  * centralized and can be re-used in integration tests.
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
