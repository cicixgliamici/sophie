package frontend

import org.scalatest.funsuite.AnyFunSuite
import scala.math.BigDecimal

// Simulate a TUI session programmatically using `SophieTui.simulateSession`.
// This avoids interactive stdin and exercises TUI commands and program evaluation
// in a deterministic way: we set a price, paste a BUY program, run it and apply the plan.
class SophieTuiSimSpec extends AnyFunSuite {
  test("simulate a TUI session: set price, buy and apply using simulateSession") {
    val inputs = Seq(
      ":pf new",
      ":set price MSFT 100",
      "BUY 100 EUR OF MSFT",
      "", // blank line to run program
      ":pf apply",
      null // end session
    )

    val (pf, plan) = SophieTui.simulateSession(inputs)
    // Expect the buy of 100 EUR at price 100 results in quantity 1
    assert(pf("MSFT") == BigDecimal(1))

    // restore demo price to avoid interfering with other tests
    SophieTui.setPricePublic("MSFT", "350")
  }
}
