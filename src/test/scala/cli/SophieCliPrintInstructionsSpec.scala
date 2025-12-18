package cli

import org.scalatest.funsuite.AnyFunSuite
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
  * Test that the CLI prints lowered IR as JSON when invoked with
  * `--print-instructions`. The test captures stdout to assert that the
  * expected JSON structure (array / instruction fields) is present.
  */
class SophieCliPrintInstructionsSpec extends AnyFunSuite {
  test("CLI --print-instructions prints lowered IR JSON to stdout") {
    val out = new ByteArrayOutputStream()
    val ps = new PrintStream(out)

    val args = Array(
      "--file", "src/test/resources/programs/buy_ok.sophie",
      "--md", "src/main/resources/md_demo.json",
      "--print-instructions"
    )

    Console.withOut(ps) {
      cli.SophieCli.main(args)
    }

    val txt = out.toString("UTF-8")
    // Expect JSON array or instruction fields
    assert(txt.contains("["), s"Expected JSON array in output, got: $txt")
    assert(txt.toLowerCase.contains("action") || txt.contains("id"), s"Expected instruction fields in output, got: $txt")
  }
}
