package frontend

import org.scalatest.funsuite.AnyFunSuite
import engine._

class PortfolioManagerPureSpec extends AnyFunSuite {
  private val manager = new PortfolioManager()
  private val market = InMemoryMarketData(prices = Map("MSFT" -> BigDecimal(10)))
  private def plan(source: String): ExecutionPlan = ProgramEvaluator.evaluate(source, market).plan

  test("preview, apply and IR execution use the same cash and position transitions") {
    val initial = PortfolioState(Map.empty, BigDecimal(100))
    val batch = plan("BUY 60 EUR OF MSFT; SELL QTY 2 OF MSFT")
    val preview = manager.purePreviewPlan(Some(batch), market.price, initial)
    val applied = manager.pureApplyPlan(Some(batch), market.price, initial)
    val instructions = Lowering.from(batch, market).toOption.get
    val engine = Executor.preview(instructions, market, initial).toOption.get
    assert(preview._1 == applied._1)
    assert(applied._1 == engine.portfolio)
    assert(applied._2 == 2)
    assert(applied._1.positions("MSFT") == 4)
    assert(applied._1.cash == 60)
    assert(initial.cash == 100 && initial.positions.isEmpty)
    assert(manager.previewPlan(Some(batch), market.price, initial)._1 == initial)
  }

  test("an invalid later sell rejects the entire plan in preview and apply") {
    val initial = PortfolioState(Map.empty, BigDecimal(100))
    val batch = plan("BUY QTY 1 OF MSFT; SELL QTY 2 OF MSFT")
    for (result <- List(manager.purePreviewPlan(Some(batch), market.price, initial),
                        manager.pureApplyPlan(Some(batch), market.price, initial))) {
      assert(result._1 == initial && result._2 == 0)
      assert(result._3.exists(_.contains("insufficient holdings")))
    }
  }

  test("buy requires cash and quantity trades also require a price") {
    val batch = plan("BUY QTY 1 OF MSFT")
    val unfunded = manager.pureApplyPlan(Some(batch), market.price, manager.empty)
    assert(unfunded._2 == 0 && unfunded._3.exists(_.contains("insufficient cash")))
    val funded = manager.empty.copy(cash = BigDecimal(100))
    val noPrice = manager.pureApplyPlan(Some(batch), _ => None, funded)
    assert(noPrice._1 == funded && noPrice._3.exists(_.contains("Missing PRICE(MSFT)")))
  }

  test("missing prices and non-positive prices leave the portfolio unchanged") {
    val batch = plan("BUY 50 EUR OF MSFT")
    val initial = manager.empty.copy(cash = BigDecimal(100))
    for (price <- List(None, Some(BigDecimal(0)), Some(BigDecimal(-1)))) {
      val result = manager.pureApplyPlan(Some(batch), _ => price, initial)
      assert(result._1 == initial && result._2 == 0)
    }
  }

  test("invalid initial funding in TUI preserves the existing account") {
    val inputs = Seq(":pf new 100", ":pf new -1", ":pf new invalid",
      ":set price MSFT 10", "BUY QTY 10 OF MSFT", "", ":pf apply")
    assert(SophieTui.simulateSession(inputs)._1("MSFT") == 10)
  }
}
