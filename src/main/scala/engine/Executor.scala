package engine

import ast._

object Executor {
  /** Execute the list of instructions: update portfolio and append to ledger. */
  def run(instructions: List[Instruction], md: MarketData, pf: PortfolioStore, ledger: Ledger, source: String = "repl"): Unit = {
    var portfolio = pf.load().withDefaultValue(BigDecimal(0))

    instructions.foreach { instr =>
      val px = instr.price.orElse(md.price(instr.symbol))
        .getOrElse(throw new IllegalStateException(s"Missing PRICE(${instr.symbol}) to execute instruction ${instr.id}"))

      val notional = instr.qty * px
      val cur = portfolio(instr.symbol)
      val next = instr.action match {
        case Buy  => cur + instr.qty
        case Sell => (cur - instr.qty).max(BigDecimal(0))
      }
      portfolio = portfolio.updated(instr.symbol, next)

      val event = LedgerEvent(
        ts = java.time.Instant.now().toEpochMilli,
        action = instr.action,
        symbol = instr.symbol,
        qty = instr.qty,
        price = px,
        notional = notional,
        source = source,
        note = instr.note
      )
      ledger.append(event)
    }

    pf.save(portfolio.filter(_._2 > 0))
  }
}
