package engine

import org.scalatest.funsuite.AnyFunSuite
import ast._
import frontend.SophieParserFacade

class MarketDataSemanticsTest extends AnyFunSuite {

  private def planOf(src: String, md: MarketData): ExecutionPlan =
    Evaluator.evaluate(SophieParserFacade.parseString(src), md)

  test("SeriesOperation(symbol.field) uses the latest datapoint") {
    val md = InMemoryMarketData(seriesData = Map(("A","volume") -> Vector[BigDecimal](10, 20, 99)))
    val src = """BUY 1 EUR OF A IF A.volume > 50;"""
    assert(planOf(src, md).trades.head.shouldExecute) // 99 > 50
  }

  test("Division uses scale 10 and compares as expected") {
    // Evaluator sets scale=10 for division; 1/3 ≈ 0.3333333333
    val src =
      """BUY 1 EUR OF X IF (1 / 3) > 0.3333333332 && (1 / 3) < 0.3333333334;"""
    val plan = planOf(src, InMemoryMarketData())
    assert(plan.trades.head.shouldExecute)
  }
}
