package engine

import ast._
import upickle.default._

/**
  * Intermediate Representation (IR)
  * --------------------------------
  * Bridges the gap between a high-level `ExecutionPlan` and a serialized, portable
  * instruction list. The IR layer keeps execution details stable and auditable even
  * if the planner or evaluator evolve.
  *
  * Design notes:
  *   - `Instruction` is intentionally data-only: no side effects, easy to persist.
  *   - `Lowering.from` filters out SKIP trades, converts money amounts to share
  *     quantities, and tags each instruction with a deterministic-ish id for
  *     traceability.
  *   - JSON codecs are defined locally so the IR stays self-contained and can be
  *     used by CLI tools or tests without importing unrelated modules.
  */
final case class Instruction(
                              id: String,
                              action: TradeAction,
                              symbol: String,
                              qty: BigDecimal,            // quantity to move (not money)
                              price: Option[BigDecimal],  // optional fixed price; if None, resolve at execution
                              note: String
                            )

object Instruction {
  implicit val tradeActionRW: ReadWriter[TradeAction] =
    readwriter[String].bimap[TradeAction](
      { case Buy => "BUY"; case Sell => "SELL" },
      {
        case "BUY"  => Buy
        case "SELL" => Sell
        case x      => throw new Exception(s"Unknown action: $x")
      }
    )
  implicit val rw: ReadWriter[Instruction] = macroRW
}

object Lowering {
  /** Convert an ExecutionPlan into concrete Instructions (filters EXECUTE only). */
  def from(plan: ExecutionPlan, md: MarketData, source: String = "repl"): List[Instruction] = {
    val exec = plan.trades.filter(_.shouldExecute)
    exec.zipWithIndex.map { case (dec, idx) =>
      val cmd = dec.cmd
      val qty =
        if (cmd.value.currency == cmd.symbol) cmd.value.amount
        else {
          val px = md.price(cmd.symbol)
            .getOrElse(throw new IllegalStateException(s"Missing PRICE(${cmd.symbol}) to convert ${cmd.value.amount} ${cmd.value.currency} to qty"))
          if (px == 0) throw new IllegalStateException(s"PRICE(${cmd.symbol}) is zero")
          (cmd.value.amount / px)
        }
      val id = s"$source-$idx-${System.nanoTime()}"
      Instruction(id, cmd.action, cmd.symbol, qty, price = None, note = dec.detail)
    }
  }
}
