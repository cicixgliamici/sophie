package engine

import org.scalatest.funsuite.AnyFunSuite
import ast._
import frontend.SophieParserFacade

class AggFuncComputeTest extends AnyFunSuite {

  private def planOf(src: String, md: MarketData): ExecutionPlan = {
    val prog = SophieParserFacade.parseString(src)
    Evaluator.evaluate(prog, md)
  }

  test("MAVG computes from close when no override (exactly n points)") {
    // closes = 1,2,3 → MAVG(3) = 2.0
    val md = InMemoryMarketData(seriesData = Map(("A","close") -> Vector[BigDecimal](1,2,3)))
    val src =
      """BUY 1 EUR OF A IF MAVG(A, 3) > 1.9 && MAVG(A, 3) < 2.1;"""
    assert(planOf(src, md).trades.head.shouldExecute)
  }

  test("AggFunc fails when series too short") {
    val md = InMemoryMarketData(seriesData = Map(("A","close") -> Vector[BigDecimal](1,2))) // only 2 points
    val src = """BUY 1 EUR OF A IF MAVG(A, 3) > 0;"""
    assertThrows[IllegalArgumentException] {
      planOf(src, md)
    }
  }

  test("indicatorOverride takes precedence over computation (name case-insensitive)") {
    // closes would give MAVG=2.0, but override 'mavg' forces a much larger value.
    val md = InMemoryMarketData(
      prices = Map("A" -> BigDecimal(100)),
      seriesData = Map(("A","close") -> Vector[BigDecimal](1,2,3)),
      indicatorOverrides = Map(IndicatorKey("mavg","A",3) -> BigDecimal(1000))
    )
    val src = """BUY 1 EUR OF A IF MAVG(A, 3) > PRICE(A);"""
    val d = planOf(src, md).trades.head
    assert(d.shouldExecute) // uses override (1000 > 100), not the computed 2.0
  }
}