package engine

import ast._
import frontend.SophieParserFacade
import org.scalatest.funsuite.AnyFunSuite

class EvaluatorShortCircuitTest extends AnyFunSuite {

  private def plan(src: String, md: MarketData): ExecutionPlan =
    Evaluator.evaluate(SophieParserFacade.parseString(src), md)

  test("OR short-circuits so missing data on the right does not fail") {
    val src = """BUY 1 EUR OF MSFT IF 1 < 2 || PRICE(UNKNOWN) > 0;"""
    val md  = InMemoryMarketData(prices = Map("MSFT" -> 1))

    val decision = plan(src, md).trades.headOption.getOrElse(fail("expected a trade decision"))
    assert(decision.shouldExecute)
  }

  test("AND evaluates the right side and surfaces missing data errors") {
    val src = """BUY 1 EUR OF MSFT IF 1 < 2 && PRICE(UNKNOWN) > 0;"""
    val md  = InMemoryMarketData(prices = Map("MSFT" -> 1))

    val ex = intercept[IllegalStateException] {
      plan(src, md)
    }
    assert(ex.getMessage.contains("Missing PRICE(UNKNOWN)"))
  }

  test("ExecutionPlan keeps trade decisions alongside a portfolio block") {
    val src =
      """BUY 100 USD OF MSFT IF PRICE(MSFT) > 0;
        |PORTFOLIO = 50 USD OF MSFT + 25 USD OF IBM;
        |""".stripMargin
    val md  = InMemoryMarketData(prices = Map("MSFT" -> 120))

    val evaluated = plan(src, md)
    val trade = evaluated.trades.headOption.getOrElse(fail("expected a trade decision"))
    assert(trade.shouldExecute)

    val pfPlan = evaluated.portfolio.getOrElse(fail("expected a portfolio plan"))
    assert(pfPlan.allocations.map(_.symbol).toSet == Set("MSFT", "IBM"))
  }
}
