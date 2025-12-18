package frontend

import org.scalatest.funsuite.AnyFunSuite
import scala.math.BigDecimal

class CommandHandlerSpec extends AnyFunSuite {
  test("CommandHandler handles pf commands and unknown gracefully") {
    val pm = new PortfolioManager(_ => Some(BigDecimal(1)), DummyPrinter)
    val ch = new CommandHandler(sym => Some(BigDecimal(1)), pm, DummyPrinter)

    assert(ch.handle(":pf new", new StringBuilder))
    assert(ch.handle(":pf show", new StringBuilder))
    assert(ch.handle(":pf save tmp/test_pf.json", new StringBuilder))
    assert(ch.handle(":pf load tmp/test_pf.json", new StringBuilder))
    assert(ch.handle(":pf apply", new StringBuilder))
    assert(ch.handle(":unknowncmd", new StringBuilder))
  }
}

