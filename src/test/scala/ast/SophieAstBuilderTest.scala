package ast

import org.scalatest.funsuite.AnyFunSuite
import frontend.SophieParserFacade
import engine.{Evaluator, InMemoryMarketData}

class SophieAstBuilderTest extends AnyFunSuite {

  test("trade + portfolio are mapped to the expected AST") {
    val src =
      """BUY 1500 EUR OF MSFT IF RSI(MSFT, 14) < 30 && MAVG(MSFT, 50) > PRICE(MSFT);
        |PORTFOLIO = 6000 EUR OF VWCE + 2000 USD OF AAPL + 0.1 BTC OF BTC;
        |""".stripMargin

    val ast = SophieParserFacade.parseString(src)

    val expected = Program(List(
      TradeCmd(
        action = Buy,
        consideration = ByValue(Value(BigDecimal(1500), "EUR")),
        symbol = "MSFT",
        condition = And(
          Comparison(AggFunc("RSI","MSFT",BigDecimal(14)), LT, NumberLiteral(30)),
          Comparison(AggFunc("MAVG","MSFT",BigDecimal(50)), GT, Price("MSFT"))
        )
      ),
      PortfolioCmd(List(
        Allocation(Value(BigDecimal(6000),"EUR"), "VWCE"),
        Allocation(Value(BigDecimal(2000),"USD"), "AAPL"),
        Allocation(Value(BigDecimal(0.1),"BTC"), "BTC")
      ))
    ))

    assert(ast == expected)
  }

  test("series operation and price are mapped") {
    val src =
      """SELL 1 USD OF BTC IF BTC.volume > PRICE(BTC);"""
    val ast = SophieParserFacade.parseString(src)
    val t = ast.statements.head.asInstanceOf[TradeCmd]
    assert(t.symbol == "BTC")
    assert(t.condition.isInstanceOf[And] == false) // single comparison
  }

  test("BUY without IF is parsed and treated as AlwaysTrue") {
    val src = "BUY 1500 EUR OF MSFT;"
    val ast = SophieParserFacade.parseString(src)
    val t = ast.statements.head.asInstanceOf[TradeCmd]
    assert(t.action == Buy)
    assert(t.consideration == ByValue(Value(BigDecimal(1500), "EUR")))
    assert(t.symbol == "MSFT")
    assert(t.condition == AlwaysTrue)
  }

  test("Evaluator executes BUY without IF") {
    val src = "BUY 1500 EUR OF MSFT;"
    val program = SophieParserFacade.parseString(src)
    val md = InMemoryMarketData() // empty market data is fine; AlwaysTrue does not need prices
    val plan = Evaluator.evaluate(program, md)
    assert(plan.trades.nonEmpty)
    assert(plan.trades.head.shouldExecute)
  }
}
