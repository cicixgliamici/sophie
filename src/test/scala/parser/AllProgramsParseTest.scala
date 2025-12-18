+6
-0

package parser

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/**
  * Safety net that ensures every example program bundled in test resources is
  * still valid according to the current grammar. This makes sure we do not ship
  * broken samples when evolving the language.
  */
class AllProgramsParseTest extends AnyFunSuite {
  test("all .sophie resources parse") {
    val dir = Paths.get("src/test/resources/programs")
    val files = Files.list(dir).iterator().asScala.filter(_.toString.endsWith(".sophie")).toList
    assert(files.nonEmpty, s"Nessun file .sophie in $dir")

    files.foreach { p =>
      val text = new String(Files.readAllBytes(p), "UTF-8")
      val lex  = new sophieLexer(CharStreams.fromString(text))
      val par  = new sophieParser(new CommonTokenStream(lex))
      par.program()
      // Each file must parse with zero syntax errors; we do not assert semantics here.
      assert(par.getNumberOfSyntaxErrors == 0, s"Syntax errors in ${p.getFileName}")
    }
  }
}
