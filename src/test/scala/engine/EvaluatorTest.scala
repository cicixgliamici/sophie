package engine

import org.scalatest.funsuite.AnyFunSuite
import ast._

class EvaluatorTest extends AnyFunSuite {

  private def planOf(src: String, md: MarketData): ExecutionPlan = {
    val prog = frontend.SophieParserFacade.parseString(src)
    Evaluator.evaluate(prog, md)
  }

  test("BUY executes when both sub-conditions hold (override indicators)") {
    val src =
      """BUY 1500 EUR OF MSFT IF RSI(MSFT, 14) < 30 && MAVG(MSFT, 50) > PRICE(MSFT);"""

    val md = InMemoryMarketData(
      prices = Map("MSFT" -> 320),
      indicatorOverrides = Map(
        IndicatorKey("RSI","MSFT",14)  -> BigDecimal(25),   // <30 true
        IndicatorKey("MAVG","MSFT",50) -> BigDecimal(350)   // >PRICE true
      )
    )

    val plan = planOf(src, md)
    assert(plan.trades.nonEmpty)
    assert(plan.trades.head.shouldExecute)
  }

  test("SELL executes when volume high and STDDEV > PRICE") {
    val src =
      """SELL 0.5 BTC OF BTC IF BTC.volume > 1000000 && STDDEV(BTC, 20) > PRICE(BTC);"""

    val md = InMemoryMarketData(
      prices = Map("BTC" -> BigDecimal(62000)),
      seriesData = Map(("BTC","volume") -> Vector[BigDecimal](100,200,2_000_001)),
      indicatorOverrides = Map(IndicatorKey("STDDEV","BTC",20) -> BigDecimal(70000))
    )

    val d = planOf(src, md).trades.head
    assert(d.shouldExecute)
  }

  test("quantity-based trades preserve QTY wording in plan details") {
    val src = """BUY QTY 3 OF MSFT IF 1;"""
    val md = InMemoryMarketData()

    // The evaluator should keep the explicit quantity form in user-facing plan details.
    val d = planOf(src, md).trades.head
    assert(d.shouldExecute)
    assert(d.detail.contains("BUY QTY 3 OF MSFT"))
  }

  test("truthy comparison: single expr acts as != 0") {
    val srcTrue  = """BUY 1 EUR OF MSFT IF PRICE(MSFT);"""
    val srcFalse = """BUY 1 EUR OF MSFT IF PRICE(MSFT);"""

    val mdTrue  = InMemoryMarketData(prices = Map("MSFT" -> 1))
    val mdFalse = InMemoryMarketData(prices = Map("MSFT" -> 0))

    assert(planOf(srcTrue,  mdTrue ).trades.head.shouldExecute)
    assert(!planOf(srcFalse, mdFalse).trades.head.shouldExecute)
  }

  test("operator precedence: AND binds tighter than OR") {
    val src1 = """BUY 1 EUR OF A IF 0 || 1 && 0;"""        // 0 || (1 && 0) == 0
    val src2 = """BUY 1 EUR OF A IF (0 || 1) && 0;"""      // (1) && 0 == 0
    val src3 = """BUY 1 EUR OF A IF 1 || 0 && 0;"""        // 1 || (0) == 1

    val md = InMemoryMarketData(prices = Map("A" -> 1)) // price not used here

    assert(!planOf(src1, md).trades.head.shouldExecute)
    assert(!planOf(src2, md).trades.head.shouldExecute)
    assert( planOf(src3, md).trades.head.shouldExecute)
  }

  test("arithmetic in comparisons works") {
    val src = """SELL 1 EUR OF X IF PRICE(X) * 0.5 < 10;"""
    val md  = InMemoryMarketData(prices = Map("X" -> BigDecimal(15)))
    assert(planOf(src, md).trades.head.shouldExecute) // 7.5 < 10
  }

  test("division by zero raises") {
    val src = """BUY 1 EUR OF X IF 1 / 0;"""
    val md  = InMemoryMarketData()
    assertThrows[IllegalStateException] {
      planOf(src, md)
    }
  }

  test("indicator period must be integer (NUMBER like 14.5 rejected at evaluation)") {
    val src = """BUY 1 EUR OF X IF RSI(X, 14.5) < 50;"""
    val md  = InMemoryMarketData(prices = Map("X" -> 1), seriesData = Map("X" -> "close" -> Vector(BigDecimal(1))))
    assertThrows[IllegalStateException] {
      planOf(src, md)
    }
  }

  test("Indicators: MAVG/EMA/STDDEV simple numeric checks") {
    val closes = Vector[BigDecimal](1,2,3,4,5)
    // MAVG last 3 of {3,4,5} = 4
    assert(Indicators.compute("MAVG", closes, 3) == BigDecimal(4))
    // EMA n=3, alpha=0.5, seed=sma(1,2,3)=2 -> step4=3 -> step5=4
    assert(Indicators.compute("EMA", closes, 3) == BigDecimal(4))
    // STDDEV of {3,4,5}: variance = 2/3, stddev ~ 0.81649658
    val s = Indicators.compute("STDDEV", closes, 3)
    assert((s - BigDecimal("0.8164965809")).abs < BigDecimal("0.000001"))
  }

  test("Indicators: RSI returns 100 on strictly increasing, 0 on strictly decreasing") {
    val inc = Vector[BigDecimal](1,2,3,4,5,6,7,8)
    val dec = Vector[BigDecimal](8,7,6,5,4,3,2,1)
    assert(Indicators.rsi(inc, 2) == BigDecimal(100))
    assert(Indicators.rsi(dec, 2) == BigDecimal(0))
  }

  test("Missing series/price produce clear errors") {
    val src1 = """BUY 1 EUR OF A IF A.volume;""" // needs latest volume
    val src2 = """BUY 1 EUR OF A IF PRICE(A);""" // needs price

    val md = InMemoryMarketData() // empty

    assertThrows[IllegalStateException] { planOf(src1, md) }
    assertThrows[IllegalStateException] { planOf(src2, md) }
  }
}
