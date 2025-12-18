package ast

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import parser.{sophieLexer, sophieParser}

class SophieAstBuilderQuantitySpec extends AnyFunSuite {
  test("quantity consideration produces Value with currency == symbol") {
    val src = "BUY QTY 100 OF MSFT;"
    val lex = new sophieLexer(CharStreams.fromString(src))
    val par = new sophieParser(new CommonTokenStream(lex))
    val prog = par.program()
    assert(par.getNumberOfSyntaxErrors == 0)

    val ast = SophieAstBuilder.fromProgram(prog)
    ast.statements match {
      case List(TradeCmd(action, value, symbol, cond)) =>
        assert(action == Buy)
        assert(symbol == "MSFT")
        // Quantity was specified as QTY 100; we expect the AST Value to have
        // amount=100 and currency == symbol to signal direct quantity.
        assert(value.amount == BigDecimal(100))
        assert(value.currency == "MSFT")
      case other => fail(s"Unexpected AST: $other")
    }
  }
}

