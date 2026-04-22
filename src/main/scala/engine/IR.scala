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
  // Convert the parsed trade shape into an executable quantity.
  // Explicit quantities pass through unchanged, while notional values need
  // a market price unless they are already denominated in the traded symbol.
  private def quantityFor(cmd: TradeCmd, md: MarketData): Either[String, BigDecimal] =
    cmd.consideration match {
      case ByQuantity(qty) => Right(qty)
      case ByValue(value) if value.currency == cmd.symbol =>
        Right(value.amount)
      case ByValue(value) =>
        md.price(cmd.symbol) match {
          case Some(px) if px != 0 => Right(value.amount / px)
          case Some(_)             => Left(s"PRICE(${cmd.symbol}) is zero")
          case None                => Left(s"Missing PRICE(${cmd.symbol}) to convert ${value.amount} ${value.currency} to qty")
        }
    }

  /** Convert an ExecutionPlan into concrete Instructions (filters EXECUTE only).
    * Returns an Either: Left(errorMessage) if lowering failed, or Right(listOfInstructions) on success.
    */
  def from(plan: ExecutionPlan, md: MarketData, source: String = "repl"): Either[String, List[Instruction]] = {
    val exec = plan.trades.filter(_.shouldExecute)

    // Helper: convert a single decision to Either[String, Instruction]
    def toInstr(decIdx: (TradeDecision, Int)): Either[String, Instruction] = {
      val (dec, idx) = decIdx
      val cmd = dec.cmd
      val qtyE = quantityFor(cmd, md)

      qtyE.map { qty =>
        val id = s"$source-$idx-${System.nanoTime()}"
        Instruction(id, cmd.action, cmd.symbol, qty, price = None, note = dec.detail)
      }
    }

    // Traverse exec with indexes, short-circuiting on the first Left
    val indexed = exec.zipWithIndex
    indexed.foldRight(Right(Nil): Either[String, List[Instruction]]) { case (pair, accE) =>
      for {
        acc <- accE
        instr <- toInstr(pair)
      } yield instr :: acc
    }
  }
}
