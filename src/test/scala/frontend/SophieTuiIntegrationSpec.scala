package frontend

import org.scalatest.funsuite.AnyFunSuite
import engine.{FileLedger, InMemoryMarketData}
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths
import upickle.default.read

class SophieTuiIntegrationSpec extends AnyFunSuite {

  test("buy_apply_updates_portfolio") {
    // deterministic test: evaluate a program with a dedicated InMemoryMarketData and PortfolioManager
    val md = InMemoryMarketData(prices = Map("MSFT" -> BigDecimal(350)), seriesData = Map.empty, indicatorOverrides = Map.empty)
    val prog = "BUY 1500 EUR OF MSFT"
    val res = ProgramEvaluator.evaluate(prog, md)
    val plan = res.plan
    val pm = new PortfolioManager()
    val (afterState, applied, msgs) = pm.pureApplyPlan(Some(plan), sym => md.price(sym), pm.empty)
    assert(applied == 1)
    val pf = afterState.positions
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
      "", // submit the pasted program
      ":show last", // should be treated as command, not appended to program
      ":pf apply"
    )
    // simulate a session where paste mode sends a program followed by a command
    val (pf, plan) = SophieTui.simulateSession(inputs)
    // after execution the portfolio should contain MSFT from the BUY line
    assert(pf.contains("MSFT"), s"Expected MSFT in portfolio after paste-mode program: $pf")
    // ensure a plan was produced and it contains at least one trade
    assert(plan.isDefined, "Expected an execution plan to be produced from paste-mode inputs")
    assert(plan.get.trades.nonEmpty, "Expected at least one trade in the produced plan")
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

  test("save_md_and_load_md_roundtrip") {
    val tmpDir = Files.createTempDirectory("sophie_tui_md_")
    try {
      val mdPath = tmpDir.resolve("md.json")
      val inputsSave = Seq(
        ":set price MSFT 123.45",
        ":set series MSFT volume 1,2,3",
        ":set ovr RSI MSFT 14 42",
        s":save md $mdPath"
      )
      SophieTui.simulateSession(inputsSave)

      val mdJson = Files.readString(mdPath, UTF_8)
      val md = read[MdJsonCodec.MarketDataJ](mdJson)
      assert(md.prices.get("MSFT").contains(BigDecimal("123.45")), s"Expected price in saved MD: $mdJson")
      assert(md.series.contains("MSFT.volume"), s"Expected series in saved MD: $mdJson")
      assert(md.indicatorOverrides.exists(o => o.name == "RSI" && o.symbol == "MSFT" && o.period == 14), s"Expected override in saved MD: $mdJson")

      val inputsLoad = Seq(
        s":load md $mdPath",
        "BUY 100 EUR OF MSFT;",
        "",
        ":pf apply"
      )
      val (pf, _) = SophieTui.simulateSession(inputsLoad)
      assert(pf.contains("MSFT"), "Portfolio should contain MSFT after loading MD and applying program")
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }

  test("run_prog_and_save_prog_write_expected_files") {
    val tmpDir = Files.createTempDirectory("sophie_tui_prog_")
    try {
      val progPath = tmpDir.resolve("buy.sophie")
      val savedPath = tmpDir.resolve("saved.sophie")
      Files.writeString(progPath, "BUY 10 EUR OF MSFT;", UTF_8)

      val inputs = Seq(
        ":set price MSFT 10",
        s":run prog $progPath",
        ":pf apply",
        s":save prog $savedPath"
      )
      val (pf, _) = SophieTui.simulateSession(inputs)
      assert(pf.contains("MSFT"), "Portfolio should contain MSFT after :run prog + :pf apply")

      val saved = Files.readString(savedPath, UTF_8)
      assert(saved.contains("BUY 10 EUR OF MSFT"), s"Saved program mismatch: $saved")
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }

  test("compile_ir_and_exec_ir_execute_instructions") {
    val tmpDir = Files.createTempDirectory("sophie_tui_ir_")
    try {
      val irPath = tmpDir.resolve("instr.json")
      val dataPf = Paths.get("data/portfolio.json")
      val dataLedger = Paths.get("data/ledger.ndjson")
      Files.deleteIfExists(dataPf)
      Files.deleteIfExists(dataLedger)

      val inputs = Seq(
        ":set price MSFT 5",
        "BUY 10 EUR OF MSFT;",
        "",
        s":compile ir $irPath",
        s":exec ir $irPath"
      )
      SophieTui.simulateSession(inputs)

      assert(Files.exists(dataPf), "Expected portfolio.json to be written by :exec ir")
      assert(Files.exists(dataLedger), "Expected ledger.ndjson to be written by :exec ir")
      val ledgerEntries = FileLedger(dataLedger).readAll()
      assert(ledgerEntries.nonEmpty, "Expected ledger to contain entries after :exec ir")
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
      Files.deleteIfExists(Paths.get("data/portfolio.json"))
      Files.deleteIfExists(Paths.get("data/ledger.ndjson"))
    }
  }
}
