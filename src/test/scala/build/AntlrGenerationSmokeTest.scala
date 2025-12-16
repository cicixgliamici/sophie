package build

import org.scalatest.funsuite.AnyFunSuite

/**
 * This test suite verifies that the ANTLR-generated parser and lexer classes
 * (sophieLexer and sophieParser) are present on the test classpath.
 *
 * Purpose:
 * - Ensures that the ANTLR code generation step has run successfully.
 * - Prevents issues where the build/test process cannot find the generated sources.
 *
 * How it works:
 * - Attempts to load the generated classes by name using reflection.
 * - Fails the test if any of the required classes are missing.
 */
class AntlrGenerationSmokeTest extends AnyFunSuite {
  test("ANTLR generated classes are on the test classpath") {
    val mustLoad = Seq(
      "parser.sophieLexer",
      "parser.sophieParser"
    )
    val failures = mustLoad.flatMap { cn =>
      try { Class.forName(cn); None }
      catch { case t: Throwable => Some(s"Cannot load $cn: ${t.getClass.getSimpleName}: ${t.getMessage}") }
    }
    assert(failures.isEmpty, failures.mkString("\n"))
  }
}

