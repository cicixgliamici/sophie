package ast

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import parser.{sophieLexer, sophieParser}

class SophieAstBuilderQuantitySpec extends AnyFunSuite {
  test("quantity consideration produces a ByQuantity AST node") {
    val src = "BUY QTY 100 OF MSFT;"
    val lex = new sophieLexer(CharStreams.fromString(src))
    val par = new sophieParser(new CommonTokenStream(lex))
    val prog = par.program()
    assert(par.getNumberOfSyntaxErrors == 0)

    val ast = SophieAstBuilder.fromProgram(prog)
    ast.statements match {
      case List(TradeCmd(action, consideration, symbol, cond)) =>
        assert(action == Buy)
        assert(symbol == "MSFT")
        assert(consideration == ByQuantity(BigDecimal(100)))
      case other => fail(s"Unexpected AST: $other")
    }
  }
}

