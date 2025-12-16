package parser

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime._
import scala.io.Source

class ResourceProgramsTest extends AnyFunSuite {
  private def parseString(s: String): Unit = {
    val lexer = new sophieLexer(CharStreams.fromString(s))
    val parser = new sophieParser(new CommonTokenStream(lexer))
    parser.program()
  }
  private def fromRes(path: String): String =
    Source.fromResource(path).mkString

  test("buy_ok.sophie parses") {
    parseString(fromRes("programs/buy_ok.sophie"))
  }

  test("portfolio_ok.sophie parses") {
    parseString(fromRes("programs/portfolio_ok.sophie"))
  }
}
