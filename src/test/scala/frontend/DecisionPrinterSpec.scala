package frontend

import engine._
import org.scalatest.funsuite.AnyFunSuite

class DecisionPrinterSpec extends AnyFunSuite {
  private val plan = ProgramEvaluator.evaluate(
    "BUY QTY 1 OF A IF 0 && PRICE(UNKNOWN) > 0; SELL QTY 1 OF A IF 1", InMemoryMarketData()).plan

  test("rendering distinguishes false conditions and unevaluated expressions") {
    val lines = DecisionPrinter.explain(Some(plan))
    assert(lines.exists(_.contains("Trade 1: [SKIP]")))
    assert(lines.exists(_.contains("=> false")))
    assert(lines.exists(line => line.contains("NOT EVALUATED") && line.contains("AND short-circuit")))
    assert(lines.exists(_.contains("Trade 2: [EXECUTE]")))
  }

  test("one-based selection and invalid requests have clear results") {
    assert(DecisionPrinter.explain(Some(plan), Some("2")).exists(_.contains("Trade 2:")))
    assert(!DecisionPrinter.explain(Some(plan), Some("2")).exists(_.contains("Trade 1:")))
    for (number <- List("0", "-1", "3", "abc", "99999999999999999"))
      assert(DecisionPrinter.explain(Some(plan), Some(number)).head == "Trade number must be between 1 and 2.")
    assert(DecisionPrinter.explain(None).head.contains("No plan"))
    assert(DecisionPrinter.explain(Some(ExecutionPlan(Nil, None))).head.contains("No trade decisions"))
  }

  test("TUI explain dispatch preserves portfolio, session and paste buffer") {
    val result = SophieTui.simulateSession(Seq(
      ":pf new 100", ":set price A 10", "BUY QTY 1 OF A IF 1", "",
      ":explain", ":explain 1", ":explain invalid", ":pf apply"))
    assert(result._1("A") == 1)
    assert(result._2.get.trades.head.explanation.isDefined)
  }
}
