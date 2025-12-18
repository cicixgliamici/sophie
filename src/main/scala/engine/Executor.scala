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
    *
    * Design notes:
    *  - This component performs side-effects (reading/saving portfolio, appending ledger entries)
    *    and therefore sits at the boundary of the pure core. The instructions and market-data
    *    inputs remain pure values.
    *  - Ledger and PortfolioStore are abstracted as traits so we can inject test doubles
    *    during unit testing (see tests that use temporary files).
    */
  def run(instructions: List[Instruction], md: MarketData, pf: PortfolioStore, ledger: Ledger, source: String = "repl"): List[LedgerEvent] = {
    val pfState0 = pf.load().withDefaults

    // Fold over instructions producing (portfolio, producedEvents)
    val (finalPortfolio, producedRev) = instructions.foldLeft((pfState0.positions, List.empty[LedgerEvent])) {
      case ((portfolio, acc), instr) =>
        val px = instr.price.orElse(md.price(instr.symbol))
          .getOrElse(throw new IllegalStateException(s"Missing PRICE(${instr.symbol}) to execute instruction ${instr.id}"))

        val notional = instr.qty * px
        val cur = portfolio(instr.symbol)

        val next = instr.action match {
          case Buy  => cur + instr.qty
          case Sell => (cur - instr.qty).max(BigDecimal(0))
        }
        val updatedPortfolio = portfolio.updated(instr.symbol, next)

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

        // Keep appending to ledger as a side-effect (edge of the system)
        ledger.append(event)

        (updatedPortfolio, event :: acc)
    }

    // Persist only positive positions to keep the file tidy.
    val pfState = pfState0.copy(positions = finalPortfolio).onlyPositivePositions
    pf.save(pfState)
    producedRev.reverse
  }
}
