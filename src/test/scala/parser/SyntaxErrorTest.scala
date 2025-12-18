package parser

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.misc.ParseCancellationException

class SyntaxErrorTest extends AnyFunSuite {

  // Custom listener that immediately throws when the lexer/parser encounters an
  // error. This keeps the assertions deterministic: no recovery, no console
  // noise during the build.
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

  // Parse utility that must fail: it wires the bail-out strategy and throwing
  // listeners so any syntax error bubbles up as an exception.
  private def parseExpectingFailure(input: String): Unit = {
    val lexer  = new sophieLexer(CharStreams.fromString(input))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ThrowingErrorListener)

    val tokens = new CommonTokenStream(lexer)
    val p      = new sophieParser(tokens)

    p.removeErrorListeners()
    p.addErrorListener(ThrowingErrorListener)
    p.setErrorHandler(new BailErrorStrategy())

    p.program()
  }

  // Parse utility that should succeed, relying on default ANTLR error handling.
  private def parseAllowSuccess(input: String): Unit = {
    val lexer  = new sophieLexer(CharStreams.fromString(input))
    val tokens = new CommonTokenStream(lexer)
    val p      = new sophieParser(tokens)
    // use default error handler/listeners; if parse fails an exception will be thrown
    p.program()
  }

  test("missing IF condition after trade is now allowed") {
    val src = "BUY 100 EUR OF MSFT;"
    // Parsing should succeed with the relaxed grammar: a trade without an IF clause is valid.
    parseAllowSuccess(src)
  }

  test("illegal arithmetic at top-level (outside condition)") {
    val bad = "1 + 2;"
    // Top-level expressions are invalid; we expect a ParseCancellationException.
    intercept[ParseCancellationException] {
      parseExpectingFailure(bad)
    }
  }
}
