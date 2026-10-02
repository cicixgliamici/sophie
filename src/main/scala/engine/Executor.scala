package engine

import java.time.Clock

object Executor {
  /** Preview resolves and validates every order before any persistence takes place. */
  def preview(instructions: List[Instruction], md: MarketData,
              initial: PortfolioState): Either[String, TradingResult] =
    TradingEngine.simulate(instructions, md.price, initial)

  /** File-backed runs share preview semantics and commit portfolio and ledger together. */
  def run(instructions: List[Instruction], md: MarketData, pf: PortfolioStore,
          ledger: Ledger, source: String = "repl", clock: Clock = Clock.systemUTC(),
          initialPortfolio: Option[PortfolioState] = None): List[LedgerEvent] =
    (pf, ledger) match {
      case (portfolio: FileJsonPortfolioStore, history: FileLedger) =>
        FileExecutionStorage.withLock(portfolio.path, history.path) {
          val result = preview(instructions, md, initialPortfolio.getOrElse(portfolio.load())).fold(
            error => throw new IllegalStateException(error), identity)
          val events = result.fills.map(fill => toEvent(fill, source, clock, result.portfolio.currency)).toList
          if (events.nonEmpty || initialPortfolio.isDefined) FileExecutionStorage.commit(portfolio, history, result.portfolio, events)
          events
        }
      // Independent save/append interfaces cannot promise an atomic commit. Reject them
      // explicitly instead of allowing a custom adapter to silently lose accounting data.
      case _ => throw new IllegalArgumentException("Execution requires transactional file storage; use preview for pure simulation")
    }

  private def toEvent(fill: TradeFill, source: String, clock: Clock, currency: String): LedgerEvent = {
    val order = fill.instruction
    LedgerEvent(clock.millis(), order.action, order.symbol, order.qty,
      fill.price, fill.notional, source, order.note, currency, order.explanation)
  }
}
