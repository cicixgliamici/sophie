package frontend

import ast.{Program, SophieAstBuilder}
import org.antlr.v4.runtime._
import parser.{sophieLexer, sophieParser}

import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

/**
  * Thin facade around the generated ANTLR parser that returns domain AST nodes.
  *
  * Keeping construction and error-handling logic here avoids duplication between
  * CLI/TUI and test code, and ensures all entry points throw consistent
  * exceptions when syntax errors are found.
  */
object SophieParserFacade {

  /** Parse in-memory source code into a strongly-typed [[ast.Program]]. */
  def parseString(input: String): Program = {
    val lexer  = new sophieLexer(CharStreams.fromString(input))
    val tokens = new CommonTokenStream(lexer)
    val parser = new sophieParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(ThrowingErrorListener)

    val tree = parser.program()
    SophieAstBuilder.fromProgram(tree)
  }

  /** Load a UTF-8 file from disk and delegate to [[parseString]]. */
  def parseFile(path: Path): Program = {
    val content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    parseString(content)
  }
}

object ThrowingErrorListener extends BaseErrorListener {
  override def syntaxError(
                            recognizer: Recognizer[_, _],
                            offendingSymbol: Any,
                            line: Int,
                            charPositionInLine: Int,
                            msg: String,
                            e: RecognitionException
                          ): Unit = {
    val at = s"line $line:$charPositionInLine"
    throw new IllegalArgumentException(s"Syntax error at $at: $msg")
  }
}
