package engine

import org.scalatest.funsuite.AnyFunSuite
import scala.math.BigDecimal
import testhelpers.TestHelpers._
import ast._
import engine._

// Tests for IR Lowering
// - Ensures that `Lowering.from` correctly transforms ExecutionPlan into IR instructions
// - Verifies error handling when conversion requires a market price that is missing or zero
class IRLoweringSpec extends AnyFunSuite {

  // If no market price is available for conversion, lowering should return Left with a clear message
  test("Lowering.from returns Left when price missing for currency conversion") {
    val plan = ExecutionPlan(List(mkTradeDecision(Buy, "ABC", BigDecimal(100), "EUR")), None)
    val md = InMemoryMarketData(prices = Map.empty, seriesData = Map.empty, indicatorOverrides = Map.empty)

    val res = Lowering.from(plan, md, source = "test")
    assert(res.isLeft)
    assert(res.left.get.contains("Missing PRICE(ABC)"))
  }

  // If market price is zero, lowering should report a specific error (cannot divide by zero)
  test("Lowering.from returns Left when price is zero") {
    val plan = ExecutionPlan(List(mkTradeDecision(Buy, "Z", BigDecimal(50), "EUR")), None)
    val md = InMemoryMarketData(prices = Map("Z" -> BigDecimal(0)), seriesData = Map.empty, indicatorOverrides = Map.empty)

    val res = Lowering.from(plan, md, source = "test")
    assert(res.isLeft)
    assert(res.left.get.contains("PRICE(Z) is zero"))
  }

  // When price is available, lowering should succeed and quantities computed correctly
  test("Lowering.from returns Right when price available") {
    val plan = ExecutionPlan(List(mkTradeDecision(Buy, "MSFT", BigDecimal(200), "EUR")), None)
    val md = InMemoryMarketData(prices = Map("MSFT" -> BigDecimal(100)), seriesData = Map.empty, indicatorOverrides = Map.empty)

    val res = Lowering.from(plan, md, source = "test")
    assert(res.isRight)
    val instrs = res.right.get
    assert(instrs.nonEmpty)
    val instr = instrs.head
    // 200 EUR / 100 = qty 2
    assert(instr.qty == BigDecimal(2))
    assert(instr.action == Buy)
  }

  test("Lowering.from keeps explicit quantities without requiring prices") {
    // This covers the new ByQuantity branch introduced by the trade-consideration refactor.
    val cmd = TradeCmd(Buy, ByQuantity(BigDecimal(3)), "MSFT", AlwaysTrue)
    val plan = ExecutionPlan(List(TradeDecision(cmd, shouldExecute = true, "BUY QTY 3 OF MSFT")), None)
    val md = InMemoryMarketData()

    val res = Lowering.from(plan, md, source = "test")
    assert(res.isRight)
    val instr = res.toOption.get.head
    assert(instr.qty == BigDecimal(3))
    assert(instr.symbol == "MSFT")
  }
}
