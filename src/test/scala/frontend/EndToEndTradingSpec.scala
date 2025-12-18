package frontend

import engine.*
import frontend.SophieParserFacade
import org.scalatest.funsuite.AnyFunSuite

import scala.math.BigDecimal

class EndToEndTradingSpec extends AnyFunSuite {

  test("simple buy and sell program applied via PortfolioManager") {
    val md = InMemoryMarketData(prices = Map("MSFT" -> BigDecimal(100)), seriesData = Map.empty, indicatorOverrides = Map.empty)
    // Sample program string: buy 100 EUR of MSFT
    val prog = "BUY 100 EUR OF MSFT"
    val res = ProgramEvaluator.evaluate(prog, md)
    val plan = res.plan
    val pm = new PortfolioManager()

    // apply plan via PortfolioManager
    val (afterBuy, _) = pm.applyPlan(Some(plan), sym => md.price(sym), pm.empty)
    val pf = afterBuy.positions
    // 100 EUR / 100 = qty 1
    assert(pf("MSFT") == BigDecimal(1))

    // Now add a sell instruction in program
    val prog2 = "SELL 50 EUR OF MSFT"
    val res2 = ProgramEvaluator.evaluate(prog2, md)
    val plan2 = res2.plan
    val (afterSell, _) = pm.applyPlan(Some(plan2), sym => md.price(sym), afterBuy)
    val pf2 = afterSell.positions
    // selling 50 EUR at price 100 reduces qty by 0.5 (from 1 -> 0.5)
    assert(pf2("MSFT") == BigDecimal(0.5))
  }
}
