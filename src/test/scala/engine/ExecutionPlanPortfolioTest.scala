package engine

import org.scalatest.funsuite.AnyFunSuite
import ast._
import frontend.SophieParserFacade

class ExecutionPlanPortfolioTest extends AnyFunSuite {

  private def eval(src: String): ExecutionPlan =
    Evaluator.evaluate(SophieParserFacade.parseString(src), InMemoryMarketData())

  test("Program without PORTFOLIO produces portfolio = None") {
    val src = """BUY 1 EUR OF A IF 1 > 0;"""
    val plan = eval(src)
    assert(plan.portfolio.isEmpty)
  }

  test("Only the first PORTFOLIO is kept if multiple are present") {
    val src =
      """PORTFOLIO = 100 EUR OF A + 200 EUR OF B;
        |PORTFOLIO = 999 EUR OF C;  // should be ignored by Evaluator
        |BUY 1 EUR OF A IF 1 > 0;
        |""".stripMargin

    val plan = eval(src)
    val pf   = plan.portfolio.getOrElse(fail("Expected a PortfolioPlan"))
    assert(pf.allocations.map(a => a.symbol -> a.value.amount).toList ==
      List("A" -> BigDecimal(100), "B" -> BigDecimal(200)))
  }
}
