package frontend

import engine.InMemoryMarketData
import org.scalatest.funsuite.AnyFunSuite

/**
  * Integration-like tests to ensure ProgramEvaluator runs semantic validation
  * before delegating to runtime evaluation.
  */
class ProgramEvaluatorValidationSpec extends AnyFunSuite {

  test("ProgramEvaluator fails early on multiple PORTFOLIO commands") {
    val src =
      """
        |PORTFOLIO = 100 EUR OF MSFT;
        |PORTFOLIO = 100 EUR OF AAPL;
        |""".stripMargin

    val ex = intercept[engine.ProgramValidator.ProgramValidationException] {
      ProgramEvaluator.evaluate(src, InMemoryMarketData())
    }

    assert(ex.diagnostics.exists(_.code == "VAL-PORTFOLIO-MULTIPLE"))
  }
}
