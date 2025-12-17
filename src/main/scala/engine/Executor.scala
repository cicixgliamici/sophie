package engine

import ast._

object Executor {
  /**
    * Executes the list of [[Instruction]] produced by the planner.
    *
    * This stage is intentionally separated from `Evaluator` because it simulates
    * what would happen in the real world when applying a trading plan:
    * - reads the persisted portfolio (or initializes it if missing);
    * - resolves the price to use for each instruction (explicit price or from MarketData);
    * - updates holdings and records a ledger event for every movement so we keep an
    *   auditable trail.
    * We return produced events so callers and tests can inspect or print them.
    */
  def run(instructions: List[Instruction], md: MarketData, pf: PortfolioStore, ledger: Ledger, source: String = "repl"): List[LedgerEvent] = {
    var pfState = pf.load().withDefaults
    var portfolio = pfState.positions
    var produced = List.empty[LedgerEvent]


    // Priority: if the instruction carries an explicit price we use it;
    // otherwise we look at current MarketData. Missing prices are fatal
    // because we would not be able to value the trade.
    instructions.foreach { instr =>
      val px = instr.price.orElse(md.price(instr.symbol))
        .getOrElse(throw new IllegalStateException(s"Missing PRICE(${instr.symbol}) to execute instruction ${instr.id}"))

      val notional = instr.qty * px
      val cur = portfolio(instr.symbol)

      // Executor works with quantities, not cash: BUY increases the position,
      // SELL reduces it down to zero to avoid negative inventory (simplified
      // model for the project).
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
      produced ::= event
    }

    // Persist only positive positions to keep the file tidy.
    pfState = pfState.copy(positions = portfolio).onlyPositivePositions
    pf.save(pfState)
    produced.reverse
  }
}
