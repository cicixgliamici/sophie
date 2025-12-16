package frontend

import ast.{Program, SophieAstBuilder}
import org.antlr.v4.runtime._
import parser.{sophieLexer, sophieParser}

import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

object SophieParserFacade {

  def parseString(input: String): Program = {
    val lexer  = new sophieLexer(CharStreams.fromString(input))
    val tokens = new CommonTokenStream(lexer)
    val parser = new sophieParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(ThrowingErrorListener)

    val tree = parser.program()
    SophieAstBuilder.fromProgram(tree)
  }

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
