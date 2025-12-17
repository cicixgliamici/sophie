package parser

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.misc.ParseCancellationException

class SyntaxErrorTest extends AnyFunSuite {

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

  private def parseExpectingFailure(input: String): Unit = {
    val lexer  = new sophieLexer(CharStreams.fromString(input))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ThrowingErrorListener)

    val tokens = new CommonTokenStream(lexer)
    val p      = new sophieParser(tokens)

    // forza il bail (niente recovery)
    p.removeErrorListeners()
    p.addErrorListener(ThrowingErrorListener)
    p.setErrorHandler(new BailErrorStrategy())

    // qui ci aspettiamo l'eccezione
    p.program()
  }

  private def parseAllowSuccess(input: String): Unit = {
    val lexer  = new sophieLexer(CharStreams.fromString(input))
    val tokens = new CommonTokenStream(lexer)
    val p      = new sophieParser(tokens)
    // use default error handler/listeners; if parse fails an exception will be thrown
    p.program()
  }

  test("missing IF condition after trade is now allowed") {
    val src = "BUY 100 EUR OF MSFT;"
    // parsing should succeed with the updated grammar
    parseAllowSuccess(src)
  }

  test("illegal arithmetic at top-level (outside condition)") {
    val bad = "1 + 2;"
    intercept[ParseCancellationException] {
      parseExpectingFailure(bad)
    }
  }
}
