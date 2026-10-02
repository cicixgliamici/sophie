package engine

import ast._
import org.scalatest.funsuite.AnyFunSuite

class TradingEngineSpec extends AnyFunSuite {
  private def order(action: TradeAction, qty: BigDecimal, price: Option[BigDecimal] = None): Instruction =
    Instruction("test", action, "A", qty, price, "test")
  private val market = InMemoryMarketData(prices = Map("A" -> BigDecimal(3)))

  test("selling existing holdings can fund the following buy") {
    val initial = PortfolioState(Map("A" -> BigDecimal(2)), BigDecimal(0))
    val result = Executor.preview(List(order(Sell, 2), order(Buy, 1)), market, initial).toOption.get
    assert(result.portfolio.cash == 3)
    assert(result.portfolio.positions("A") == 1)
    assert(result.fills.map(_.notional) == Vector(BigDecimal(6), BigDecimal(3)))
  }

  test("reject non-positive quantities, prices, cash and holdings") {
    val funded = PortfolioState(Map.empty, BigDecimal(10))
    for (qty <- List(BigDecimal(0), BigDecimal(-1)))
      assert(Executor.preview(List(order(Buy, qty)), market, funded).isLeft)
    for (price <- List(BigDecimal(0), BigDecimal(-1)))
      assert(Executor.preview(List(order(Buy, 1, Some(price))), market, funded).isLeft)
    assert(Executor.preview(Nil, market, funded.copy(cash = BigDecimal(-1))).isLeft)
    assert(Executor.preview(Nil, market, funded.copy(positions = Map("A" -> BigDecimal(-1)))).isLeft)
  }

  test("a notional buy with a recurring quotient cannot exceed its budget") {
    val cmd = TradeCmd(Buy, ByValue(Value(BigDecimal(1), "EUR")), "A", AlwaysTrue)
    val plan = ExecutionPlan(List(TradeDecision(cmd, true, "test")), None)
    val instructions = Lowering.from(plan, market).toOption.get
    val result = Executor.preview(instructions, market, PortfolioState(Map.empty, BigDecimal(1))).toOption.get
    assert(result.portfolio.cash >= 0)
    assert(result.portfolio.cash + result.fills.head.notional == 1)
  }

  test("lowered prices stay fixed if the market changes before execution") {
    val cmd = TradeCmd(Buy, ByValue(Value(BigDecimal(6), "EUR")), "A", AlwaysTrue)
    val plan = ExecutionPlan(List(TradeDecision(cmd, true, "test")), None)
    val instructions = Lowering.from(plan, market).toOption.get
    assert(instructions == Lowering.from(plan, market).toOption.get)
    val changed = InMemoryMarketData(prices = Map("A" -> BigDecimal(100)))
    val result = Executor.preview(instructions, changed, PortfolioState(Map.empty, BigDecimal(6))).toOption.get
    assert(result.portfolio.cash == 0 && result.fills.head.price == 3)
  }
  test("foreign-currency notionals are rejected instead of silently treated as account cash") {
    val foreign = order(Buy, 1).copy(currency = Some("USD"))
    assert(Executor.preview(List(foreign), market, PortfolioState(Map.empty, BigDecimal(10))).isLeft)
    val account = PortfolioState(Map.empty, BigDecimal(10), "USD")
    val result = Executor.preview(List(foreign), market, account).toOption.get
    assert(result.portfolio.cash == 7 && result.portfolio.currency == "USD")
  }

  test("cash movements remain exact when the account has more than 34 significant digits") {
    val initial = PortfolioState(Map.empty, BigDecimal("1000000000000000000000000000000000000.01"))
    val result = Executor.preview(List(order(Buy, 1, Some(BigDecimal("0.01")))), market, initial).toOption.get
    assert(result.portfolio.cash.bigDecimal == initial.cash.bigDecimal.subtract(new java.math.BigDecimal("0.01")))
  }

}
