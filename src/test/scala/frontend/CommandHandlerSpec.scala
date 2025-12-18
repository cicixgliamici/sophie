package frontend

import org.scalatest.funsuite.AnyFunSuite
import scala.math.BigDecimal

/**
  * Quick smoke test for the TUI command dispatcher. The goal is to verify that
  * known portfolio commands are accepted and that unknown ones do not crash the
  * REPL loop (they should simply return true after printing a message).
  */
class CommandHandlerSpec extends AnyFunSuite {
  test("CommandHandler handles pf commands and unknown gracefully") {
    val pm = new PortfolioManager(_ => Some(BigDecimal(1)), DummyPrinter)
    val ch = new CommandHandler(sym => Some(BigDecimal(1)), pm, DummyPrinter)

    assert(ch.handle(":pf new", new StringBuilder))
    assert(ch.handle(":pf show", new StringBuilder))
    assert(ch.handle(":pf save tmp/test_pf.json", new StringBuilder))
    assert(ch.handle(":pf load tmp/test_pf.json", new StringBuilder))
    assert(ch.handle(":pf apply", new StringBuilder))
    // Unknown commands should not abort the loop; they return true after printing help.
    assert(ch.handle(":unknowncmd", new StringBuilder))
  }
}
