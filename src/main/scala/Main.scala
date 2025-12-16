import frontend.SophieParserFacade
import ast._
import engine._

object Main {
  def main(args: Array[String]): Unit = {
    val sample =
      """BUY 1500 EUR OF MSFT IF RSI(MSFT, 14) < 30 && MAVG(MSFT, 50) > PRICE(MSFT);
        |SELL 0.5 BTC OF BTC IF BTC.volume > 1000000 && STDDEV(BTC, 20) > PRICE(BTC);
        |
        |PORTFOLIO =
        |  6000 EUR OF VWCE
        |  + 2000 USD OF AAPL
        |  + 0.1 BTC OF BTC;
        |""".stripMargin

    // 1) Parse -> AST
    val program: Program = SophieParserFacade.parseString(sample)
    println(program) // keep the quick AST print

    // 2) Demo market data:
    //    - Override indicators so we don't need huge close-series in the demo.
    //    - Provide a volume series for BTC to satisfy BTC.volume > 1_000_000.
    val md = InMemoryMarketData(
      prices = Map(
        "MSFT" -> BigDecimal(320),
        "BTC"  -> BigDecimal(62000)
      ),
      seriesData = Map(
        ("BTC", "volume") -> Vector[BigDecimal](500000, 800000, 2_000_000)
      ),
      indicatorOverrides = Map(
        IndicatorKey("RSI",  "MSFT", 14) -> BigDecimal(25),   // RSI < 30 -> true
        IndicatorKey("MAVG", "MSFT", 50) -> BigDecimal(350),  // MAVG > PRICE -> true
        IndicatorKey("STDDEV", "BTC", 20)-> BigDecimal(70000) // STDDEV > PRICE -> true
      )
    )

    // 3) Evaluate -> plan
    val plan = Evaluator.evaluate(program, md)

    // 4) Pretty-ish output
    println("\n=== Execution Plan ===")
    plan.trades.foreach { d =>
      val status = if (d.shouldExecute) "EXECUTE" else "SKIP"
      println(s" - [$status] ${d.detail}")
    }
    plan.portfolio.foreach { p =>
      println(" - [PORTFOLIO] target allocations:")
      p.allocations.foreach { a =>
        println(s"   * ${a.value.amount} ${a.value.currency} OF ${a.symbol}")
      }
    }
  }
}
