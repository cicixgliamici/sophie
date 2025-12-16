package parser

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream, BaseErrorListener, Recognizer, RecognitionException}
import org.antlr.v4.runtime.misc.ParseCancellationException

/**
 * End-to-end parser tests for the 'sophie' grammar.
 * We assert that valid inputs produce no syntax errors.
 *
 * IMPORTANT:
 * The grammar name is 'sophie' (lowercase), so ANTLR generates sophieLexer / sophieParser.
 */
class SophieParserSmokeTest extends AnyFunSuite {

  /** Collects syntax errors and throws on first error to keep tests simple/fast. */
  private object ThrowingErrorListener extends BaseErrorListener {
    override def syntaxError(
                              recognizer: Recognizer[_, _],
                              offendingSymbol: Object,
                              line: Int,
                              charPositionInLine: Int,
                              msg: String,
                              e: RecognitionException
                            ): Unit = {
      throw new ParseCancellationException(s"Syntax error at $line:$charPositionInLine - $msg")
    }
  }

  /** Parses a full program and fails if any syntax error occurs. */
  private def parseProgram(input: String): parser.sophieParser = {
    val lexer  = new parser.sophieLexer(CharStreams.fromString(input))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ThrowingErrorListener)

    val tokens = new CommonTokenStream(lexer)
    val parserInst = new parser.sophieParser(tokens)
    parserInst.removeErrorListeners()
    parserInst.addErrorListener(ThrowingErrorListener)

    parserInst.program()
    parserInst
  }

  test("BUY example parses without syntax errors") {
    val program =
      """BUY 1500 EUR OF MSFT IF
        |  RSI(MSFT, 14) < 30
        |  && MAVG(MSFT, 50) > PRICE(MSFT)
        |;""".stripMargin

    parseProgram(program)
    succeed  // If we got here, no syntax errors occurred.
  }

  test("SELL example parses without syntax errors") {
    // Adapted to your grammar: PRICE(MSFT) * 0.15 is valid: NUMBER is int or decimal, '*' is allowed in expressions.
    // 'operand' in comparisons accepts series_operation | price_expr | (condition), so PRICE(...) is fine on the right side too.
    val program =
      """SELL 0.5 BTC OF BTC IF
        |  BTC.volume > 1000000
        |  && STDDEV(BTC, 20) > PRICE(BTC) * 0.15
        |;""".stripMargin

    parseProgram(program)
    succeed
  }

  test("PORTFOLIO example (matches current grammar) parses without syntax errors") {
    // UPDATED: current grammar expects allocations of the form: value OF symbol
    val program =
      """PORTFOLIO =
        |  6000 EUR OF VWCE
        |  + 2000 USD OF AAPL
        |  + 0.1 BTC OF BTC;
        |""".stripMargin

    parseProgram(program)
    succeed
  }
}