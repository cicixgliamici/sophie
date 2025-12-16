package ast

import org.scalatest.funsuite.AnyFunSuite
import frontend.SophieParserFacade

class AstPrecedenceParensTest extends AnyFunSuite {

  test("OR has lower precedence than AND in the AST structure") {
    val src = """BUY 1 EUR OF X IF 1 > 0 || 2 > 1 && 0 > 1;"""
    val ast = SophieParserFacade.parseString(src)
    val trade = ast.statements.head.asInstanceOf[TradeCmd]
    trade.condition match {
      case Or(_, And(_, _)) => succeed
      case other            => fail(s"Expected Or(_, And(_, _)), got: $other")
    }
  }

  test("Parentheses are preserved in AST with Parens") {
    val src = """BUY 1 EUR OF X IF (1 > 0 || 1 > 2) && 0 > 1;"""
    val ast = SophieParserFacade.parseString(src)
    val trade = ast.statements.head.asInstanceOf[TradeCmd]
    trade.condition match {
      case And(Parens(inner), _) =>
        inner match {
          case Or(_, _) => succeed
          case other    => fail(s"Expected Or inside Parens, got: $other")
        }
      case other => fail(s"Expected And(Parens(_), _), got: $other")
    }
  }
}