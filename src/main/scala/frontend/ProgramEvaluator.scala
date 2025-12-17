package frontend

import engine._
import frontend.MdJsonCodec

case class EvaluationResult(plan: ExecutionPlan, warnings: Seq[String])

object ProgramEvaluator {
  def evaluate(programSrc: String, md: engine.MarketData): EvaluationResult = {
    val program = SophieParserFacade.parseString(programSrc)
    val plan = Evaluator.evaluate(program, md)
    // detect missing prices needed for conversion
    val missingPrices: Seq[String] = plan.trades.flatMap { d =>
      val sym = d.cmd.symbol
      val v = d.cmd.value
      if (v.currency != sym && md.price(sym).isEmpty) Some(sym) else None
    }.distinct
    val warnings = if (missingPrices.nonEmpty) Seq(s"Missing PRICE for symbol(s): ${missingPrices.mkString(", ")}") else Seq.empty
    EvaluationResult(plan, warnings)
  }
}

