package frontend

import org.scalatest.funsuite.AnyFunSuite

class SophieTuiIntegrationSpec extends AnyFunSuite {

  test("buy_apply_updates_portfolio") {
    val inputs = Seq(
      ":set price MSFT 350",
      "BUY 1500 EUR OF MSFT;",
      "", // blank line to submit program
      ":pf apply"
    )

    val (pf, planOpt) = SophieTui.simulateSession(inputs)

    assert(pf.contains("MSFT"), "Portfolio should contain MSFT after applying BUY")
    val actual = pf("MSFT")
    val expected = BigDecimal(1500) / BigDecimal(350)
    val diff = (actual - expected).abs
    assert(diff < BigDecimal("1e-8"), s"Quantity mismatch: actual=$actual expected=$expected diff=$diff")
  }

  test("paste_mode_commands_are_handled") {
    val inputs = Seq(
      ":set price MSFT 350",
      "BUY 100 EUR OF MSFT;",
      ":show last", // should be treated as command, not appended to program
      "", // submit program
      ":pf apply"
    )

    val (pf, planOpt) = SophieTui.simulateSession(inputs)
    val actual = pf.getOrElse("MSFT", BigDecimal(0))
    val expected = BigDecimal(100) / BigDecimal(350)
    assert((actual - expected).abs < BigDecimal("1e-8"), s"Expected approx $expected but got $actual")
  }

  test("pf_save_load_roundtrip") {
    val path = "tmp/test_pf_rt.json"
    val inputs = Seq(
      ":set price MSFT 350",
      "BUY 1500 EUR OF MSFT;",
      "",
      ":pf apply",
      s":pf save $path",
      ":pf new",
      s":pf load $path"
    )
    val (pf, _) = SophieTui.simulateSession(inputs)
    assert(pf.contains("MSFT"), "After save/load the portfolio should contain MSFT")
  }

  test("bom_in_command_is_handled") {
    val inputs = Seq(
      "\uFEFF:set price MSFT 350", // BOM before ':' should be normalized and treated as command
      "BUY 50 EUR OF MSFT;",
      "",
      ":pf apply"
    )
    val (pf, _) = SophieTui.simulateSession(inputs)
    val actual = pf.getOrElse("MSFT", BigDecimal(0))
    val expected = BigDecimal(50) / BigDecimal(350)
    assert((actual - expected).abs < BigDecimal("1e-8"), s"Expected approx $expected but got $actual")
  }

  test("multiple_buys_accumulate") {
    val inputs = Seq(
      ":set price MSFT 10",
      "BUY 100 EUR OF MSFT;",
      "BUY 50 EUR OF MSFT;",
      "",
      ":pf apply"
    )
    val (pf, _) = SophieTui.simulateSession(inputs)
    val actual = pf.getOrElse("MSFT", BigDecimal(0))
    val expected = BigDecimal(100 + 50) / BigDecimal(10)
    assert((actual - expected).abs < BigDecimal("1e-8"), s"Expected approx $expected but got $actual")
  }

  test("preview_and_apply_with_missing_price_skips_trade") {
    val inputs = Seq(
      "BUY 100 EUR OF MST;", // MST has no price
      "",
      ":pf preview",
      ":pf apply"
    )
    val (pf, _) = SophieTui.simulateSession(inputs)
    assert(pf.isEmpty, "Portfolio should remain empty when applying trades with missing prices")
  }
}
