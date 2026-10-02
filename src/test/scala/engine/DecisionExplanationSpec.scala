package engine

import ast._
import frontend.SophieParserFacade
import org.scalatest.funsuite.AnyFunSuite
import upickle.default.{read, write}

class DecisionExplanationSpec extends AnyFunSuite {
  private def decision(condition: String, md: MarketData = InMemoryMarketData()): TradeDecision =
    Evaluator.evaluate(SophieParserFacade.parseString(s"BUY QTY 1 OF A IF $condition"), md).trades.head

  test("comparison traces retain both values, operator, result and indicator provenance") {
    val md = InMemoryMarketData(indicatorOverrides = Map(IndicatorKey("RSI", "A", 14) -> BigDecimal(28)))
    val trade = decision("RSI(A, 14) < 30", md)
    val trace = trade.explanation.get
    assert(trade.shouldExecute && trace.result.contains(true))
    assert(trace.expression == "RSI(A, 14) < 30")
    assert(trace.operands.map(_.value) == Vector(BigDecimal(28), BigDecimal(30)))
    assert(trace.operands.head.source == "indicator-override")
  }

  test("false comparisons remain evaluated rather than skipped") {
    val trace = decision("3 != 3").explanation.get
    assert(trace.result.contains(false) && trace.skippedReason.isEmpty)
    assert(trace.operands.map(_.value) == Vector(BigDecimal(3), BigDecimal(3)))
  }

  test("AND and OR record unevaluated branches without any market lookup") {
    val forbidden = new MarketData {
      def price(symbol: String): Option[BigDecimal] = fail("Skipped prices must never be read")
      def series(symbol: String, field: String): Option[Vector[BigDecimal]] = fail("Skipped series must never be read")
      override def indicatorOverride(name: String, symbol: String, period: Int): Option[BigDecimal] =
        fail("Skipped indicators must never be read")
    }
    for ((condition, result, operator) <- List(
      ("0 && (PRICE(UNKNOWN) > 0 || RSI(UNKNOWN, 14) < 30)", false, "AND"),
      ("1 || (UNKNOWN.volume > 0 && RSI(UNKNOWN, 14) < 30)", true, "OR"))) {
      val trace = decision(condition, forbidden).explanation.get
      assert(trace.result.contains(result))
      val skipped = trace.children(1)
      assert(skipped.result.isEmpty && skipped.operands.isEmpty && skipped.children.isEmpty)
      assert(skipped.expression.contains("UNKNOWN"))
      assert(skipped.skippedReason.exists(_.startsWith(s"$operator short-circuit")))
    }
  }

  test("arithmetic traces preserve nested grouping and existing division precision") {
    val trace = decision("(1 + 2) * (1 / 3) < 2").explanation.get
    val expression = trace.operands.head
    assert(expression.value == BigDecimal("0.9999999999"))
    assert(expression.children.map(_.value) == Vector(BigDecimal(3), BigDecimal("0.3333333333")))
    assert(expression.children(1).children.map(_.value) == Vector(BigDecimal(1), BigDecimal(3)))
    assert(trace.expression == "((1 + 2) * (1 / 3)) < 2")
  }

  test("computed indicators and latest series values are identified separately") {
    val md = InMemoryMarketData(seriesData = Map(
      ("A", "close") -> Vector[BigDecimal](1, 2, 3), ("A", "volume") -> Vector[BigDecimal](4, 10)))
    val trace = decision("MAVG(A, 2) < A.volume", md).explanation.get
    assert(trace.operands.map(_.value) == Vector(BigDecimal("2.5"), BigDecimal(10)))
    assert(trace.operands.map(_.source) == Vector("indicator-series", "series-latest"))
  }

  test("capturing and rendering a value never performs a second quote lookup") {
    var lookups = 0
    val md = new MarketData {
      def price(symbol: String): Option[BigDecimal] = { lookups += 1; Some(BigDecimal(lookups)) }
      def series(symbol: String, field: String): Option[Vector[BigDecimal]] = None
    }
    val trade = decision("PRICE(A) = 1", md)
    val plan = ExecutionPlan(List(trade), None)
    val first = frontend.DecisionPrinter.explain(Some(plan))
    assert(first == frontend.DecisionPrinter.explain(Some(plan)))
    assert(first.exists(_.contains("PRICE(A) = 1")))
    assert(lookups == 1)
  }

  test("unconditional trades have an explicit explanation") {
    val trade = Evaluator.evaluate(SophieParserFacade.parseString("BUY QTY 1 OF A"), InMemoryMarketData()).trades.head
    assert(trade.explanation.get.result.contains(true))
    assert(trade.explanation.get.expression.contains("unconditional"))
    assert(trade.explanation.get.operands.isEmpty)
  }

  test("reports and IR retain exact traces through JSON including skipped branches") {
    val trade = decision("1 || PRICE(UNKNOWN) > 0")
    val plan = ExecutionPlan(List(trade), None)
    val report = DecisionReport.from(plan)
    assert(read[DecisionReport](write(report)) == report)
    val instruction = Lowering.from(plan, InMemoryMarketData()).toOption.get.head
    assert(read[Instruction](write(instruction)).explanation == trade.explanation)
    assert(report.trades.head.number == 1 && report.trades.head.conditionMet)
  }

  test("legacy instructions and ledger events without explanations still deserialize") {
    val instruction = Instruction("old", Buy, "A", BigDecimal(1), Some(BigDecimal(2)), "legacy")
    val oldInstruction = ujson.read(write(instruction))
    oldInstruction.obj.remove("explanation")
    assert(read[Instruction](oldInstruction).explanation.isEmpty)
    val event = LedgerEvent(1, Buy, "A", BigDecimal(1), BigDecimal(2), BigDecimal(2), "test", "legacy")
    val oldEvent = ujson.read(write(event))
    oldEvent.obj.remove("explanation")
    assert(read[LedgerEvent](oldEvent).explanation.isEmpty)
  }
}
