package frontend

import engine.InMemoryMarketData
import org.scalatest.funsuite.AnyFunSuite

class ProgramEvaluatorWarningsSpec extends AnyFunSuite {

  // Quantity-based trades are already executable, so missing market prices
  // should not produce conversion warnings.
  test("ProgramEvaluator does not warn for quantity-based trades when prices are missing") {
    val md = InMemoryMarketData()
    val result = ProgramEvaluator.evaluate("BUY QTY 3 OF MSFT;", md)

    assert(result.warnings.isEmpty)
    assert(result.plan.trades.head.shouldExecute)
  }

  // Value-based trades still need PRICE(symbol) to convert the notional amount
  // into an executable quantity, so the helper should surface a warning.
  test("ProgramEvaluator warns for value-based trades when conversion price is missing") {
    val md = InMemoryMarketData()
    val result = ProgramEvaluator.evaluate("BUY 100 EUR OF MSFT;", md)

    assert(result.warnings.exists(_.contains("Missing PRICE for symbol(s): MSFT")))
  }
}
