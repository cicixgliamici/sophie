import frontend.SophieParserFacade
import ast._
import engine._
import util.SLF4JLogger

object Main {
  private val printer = frontend.DefaultPrinter

  private def planLines(plan: engine.ExecutionPlan): Vector[String] = {
    val header = Vector("\n=== Execution Plan ===")
    val trades = plan.trades.toVector.map { d =>
      val status = if (d.shouldExecute) "EXECUTE" else "SKIP"
      s" - [$status] ${d.detail}"
    }
    val portfolioLines = plan.portfolio.toSeq.flatMap { p =>
      Vector(" - [PORTFOLIO] target allocations:") ++ p.allocations.map { a => s"   * ${a.value.amount} ${a.value.currency} OF ${a.symbol}" }
    }.toVector
    header ++ trades ++ portfolioLines
  }

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
    try {
      val program: Program = SophieParserFacade.parseString(sample)
      printer.printlnLine(program.toString)

      // 2) Demo market data:
      val md = InMemoryMarketData(
        prices = Map(
          "MSFT" -> BigDecimal(320),
          "BTC"  -> BigDecimal(62000)
        ),
        seriesData = Map(
          ("BTC", "volume") -> Vector[BigDecimal](500000, 800000, 2_000_000)
        ),
        indicatorOverrides = Map(
          IndicatorKey("RSI",  "MSFT", 14) -> BigDecimal(25),
          IndicatorKey("MAVG", "MSFT", 50) -> BigDecimal(350),
          IndicatorKey("STDDEV", "BTC", 20)-> BigDecimal(70000)
        )
      )

      // 3) Evaluate -> plan
      val plan = Evaluator.evaluate(program, md)

      // 4) Print plan via printer
      planLines(plan).foreach(printer.printlnLine)
    } catch {
      case ex: Throwable =>
        SLF4JLogger.error(s"Main failed: ${ex.getMessage}", ex)
        throw ex
    }
  }
}
