package cli

import org.scalatest.funsuite.AnyFunSuite
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
  * Sanity test for the TUI simulation runner.
  *
  * This test runs `cli.RunTuiSim.main` which performs a few non-interactive
  * simulated TUI sessions and prints summaries to stdout. The test captures
  * stdout and checks that the runner printed completion and portfolio summary
  * lines.
  */
class RunTuiSimSpec extends AnyFunSuite {
  test("RunTuiSim prints expected outputs for simulations") {
    val out = new ByteArrayOutputStream()
    val ps = new PrintStream(out)

    Console.withOut(ps) {
      cli.RunTuiSim.main(Array.empty)
    }

    val txt = out.toString("UTF-8")
    assert(txt.contains("TUI simulations completed."), s"Did not find completion text in: $txt")
    assert(txt.contains("Portfolio positions"), s"Did not find portfolio summary in: $txt")
  }
}
