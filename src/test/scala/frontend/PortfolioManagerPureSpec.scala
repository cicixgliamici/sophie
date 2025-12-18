package frontend

import org.scalatest.funsuite.AnyFunSuite
import engine._
import ast._
import scala.math.BigDecimal
import testhelpers.TestHelpers._

// Tests for PortfolioManager pure transition functions
// - These exercises the pure state transition helpers (`pureApplyPlan` and `purePreviewPlan`)
// - We cover typical and edge cases: applying buys, attempting sells with no holdings,
//   preview behavior (non-mutating), and missing price handling.
// This makes it easy to test portfolio logic without side-effects or file IO.
class PortfolioManagerPureSpec extends AnyFunSuite {

  // Verify that applying a BUY instruction increases holdings by the expected quantity
  test("pureApplyPlan applies buys and updates state") {
    val pm = new PortfolioManager(_ => Some(BigDecimal(10)), DummyPrinter)
    val initial = PortfolioState(Map("A" -> BigDecimal(1)), BigDecimal(0))
    val plan = ExecutionPlan(List(mkTradeDecision(Buy, "A", BigDecimal(20), "EUR")), None)

    val (newState, applied, msgs) = pm.pureApplyPlan(Some(plan), _ => Some(BigDecimal(10)), initial)
    assert(applied == 1)
    // amount 20 EUR at price 10 -> qty 2
    assert(newState.positions("A") == BigDecimal(3))
    assert(msgs.exists(_.contains("Applied 1 trade(s)")))
  }

  // Ensure SELL is skipped when there are no holdings to reduce
  test("pureApplyPlan skips sell when no holdings") {
    val pm = new PortfolioManager(_ => None, DummyPrinter)
    val initial = PortfolioState(Map.empty.withDefaultValue(BigDecimal(0)), BigDecimal(0))
    val plan = ExecutionPlan(List(mkTradeDecision(Sell, "X", BigDecimal(10), "EUR")), None)

    val (newState, applied, msgs) = pm.pureApplyPlan(Some(plan), _ => None, initial)
    assert(applied == 0)
    assert(newState.positions.isEmpty)
    assert(msgs.exists(m => m.contains("computed quantity is 0") || m.contains("Skipping SELL")))
  }

  // Preview should return the new state without mutating the original portfolio passed in
  test("purePreviewPlan does not mutate state") {
    val pm = new PortfolioManager(_ => Some(BigDecimal(5)), DummyPrinter)
    val initial = PortfolioState(Map("B" -> BigDecimal(2)), BigDecimal(0))
    val plan = ExecutionPlan(List(mkTradeDecision(Buy, "B", BigDecimal(50), "EUR")), None)

    val (previewState, applied, msgs) = pm.purePreviewPlan(Some(plan), _ => Some(BigDecimal(5)), initial)
    // 50 EUR / 5 -> qty 10, so new qty would be 12
    assert(applied == 1)
    assert(previewState.positions("B") == BigDecimal(12))
    // original state unchanged
    assert(initial.positions("B") == BigDecimal(2))
  }

  // Missing market price should result in no applied trades and a clear message
  test("pureApplyPlan handles missing price with message") {
    val pm = new PortfolioManager(_ => None, DummyPrinter)
    val initial = PortfolioState(Map.empty.withDefaultValue(BigDecimal(0)), BigDecimal(0))
    val plan = ExecutionPlan(List(mkTradeDecision(Buy, "Y", BigDecimal(100), "EUR")), None)

    val (newState, applied, msgs) = pm.pureApplyPlan(Some(plan), _ => None, initial)
    assert(applied == 0)
    assert(msgs.exists(_.contains("Missing PRICE(Y)")))
  }
}
