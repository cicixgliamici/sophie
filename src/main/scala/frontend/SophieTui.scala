package frontend

import ast._
import engine._
import scala.io.StdIn.readLine
import scala.util.control.NonFatal
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}
import frontend.MdJsonCodec
import frontend.PortfolioJson

object SophieTui {

  // ---- State ----
  private var md: InMemoryMarketData = InMemoryMarketData()
  private var lastPlan: Option[ExecutionPlan] = None
  private var lastProgramSrc: Option[String]  = None
  private var portfolio: Map[String, BigDecimal] = Map.empty.withDefaultValue(BigDecimal(0))

  def run(): Unit = {
    println("Sophie TUI — type :help for commands. Paste DSL, blank line to run.")
    var running = true
    val buf = new StringBuilder
    while (running) {
      val prompt = if (buf.nonEmpty) "... " else "sophie> "
      val line = readLine(prompt)

      if (line == null) running = false
      else if (line.trim.isEmpty && buf.nonEmpty) {
        val programSrc = buf.result(); buf.clear()
        evalAndPrint(programSrc)
      } else if (line.trim.startsWith(":")) {
        running = handleCommand(line.trim)
      } else buf.append(line).append('\n')
    }
    println("Bye.")
  }

  // -------- Commands --------
  private def handleCommand(cmd: String): Boolean = {
    val parts = cmd.split("\\s+").toList
    parts match {
      case List(":q") | List(":quit") => false
      case List(":help")              => printHelp(); true

      case List(":show", "md")                => printMarketData(); true
      case List(":set", "price", sym, v)      => setPrice(sym, v); true
      case List(":set", "series", s, f, csv)  => setSeries(s, f, csv); true
      case List(":set", "ovr", n, s, p, v)    => setOverride(n, s, p, v); true

      case List(":load", "md", path) => loadMd(path); true
      case List(":save", "md", path) => saveMd(path); true

      case List(":run",  "prog", path) => runProg(path); true
      case List(":save", "prog", path) => saveProg(path); true

      case List(":pf", "new")            => pfNew(); true
      case List(":pf", "show")           => pfShow(); true
      case List(":pf", "apply")          => pfApply(); true
      case List(":pf", "save", path)     => pfSave(path); true
      case List(":pf", "load", path)     => pfLoad(path); true

      case List(":compile", "ir", path) => compileIr(path); true
      case List(":exec", "ir", path)    => execIr(path); true

      case _ => println(s"Unknown command: $cmd  (try :help)"); true
    }
  }

  private def printHelp(): Unit = {
    println(
      """Commands:
        |  :pf save <path.json>
        |  :pf load <path.json>
        |  :load md <path.json>
        |  :save md <path.json>
        |  :run  prog <file.sophie>
        |  :save prog <file.sophie>
        |  :compile ir <file.json>   — compile last plan to IR (JSON)
        |  :exec ir <file.json>      — execute IR, update portfolio & ledger
        |
        |  :help
        |  :q | :quit
        |  :show md
        |  :set price <SYM> <VALUE>
        |  :set series <SYM> <FIELD> <v1,...>
        |  :set ovr <NAME> <SYM> <PERIOD> <V>
        |
        |  :pf new   — reset portfolio
        |  :pf show  — show positions and mark-to-market (if prices available)
        |  :pf apply — apply last execution plan (EXECUTE trades only)
        |
        |Paste a multi-line Sophie program; submit with an empty line.
        |""".stripMargin)
  }

  // -------- MarketData IO --------
  private def loadMd(path: String): Unit = {
    try {
      val p = Paths.get(path)
      val json =
        if (Files.exists(p)) Files.readString(p, UTF_8)
        else {
          val is = Option(getClass.getClassLoader.getResourceAsStream(path))
            .getOrElse(throw new IllegalArgumentException(
              s"Not found: $path (neither file nor classpath resource)"
            ))
          try new String(is.readAllBytes(), UTF_8) finally is.close()
        }

      val j  = read[MdJsonCodec.MarketDataJ](json)
      md = MdJsonCodec.fromJ(j)
      println(s"Loaded MarketData from $path (prices=${md.prices.size}, series=${md.seriesData.size}, overrides=${md.indicatorOverrides.size})")
    } catch { case e: Exception => println(s"Error loading $path: ${e.getMessage}") }
  }

  private def saveMd(path: String): Unit = {
    try {
      ensureParentDir(path)
      val j = MdJsonCodec.toJ(md)
      val json = write(j, indent = 2)
      Files.writeString(Paths.get(path), json, UTF_8)
      println(s"Saved MarketData to $path")
    } catch { case e: Exception => println(s"Error saving $path: ${e.getMessage}") }
  }

  // -------- Manual MD edits --------
  private def setPrice(sym: String, valueStr: String): Unit =
    try {
      val v = BigDecimal(valueStr)
      md = md.copy(prices = md.prices + (sym -> v))
      println(s"Set PRICE($sym) = ${fmt(v)}")
    } catch { case NonFatal(e) => println(s"Error: ${e.getMessage}") }

  private def setSeries(sym: String, field: String, valuesCsv: String): Unit =
    try {
      val vs = valuesCsv.split(",").toVector.map(s => BigDecimal(s.trim))
      md = md.copy(seriesData = md.seriesData + ((sym -> field) -> vs))
      val last = vs.lastOption.map(fmt).getOrElse("n/a")
      println(s"Set SERIES $sym.$field (${vs.length} points, last=$last)")
    } catch { case NonFatal(e) => println(s"Error: ${e.getMessage}") }

  private def setOverride(name: String, sym: String, periodStr: String, valueStr: String): Unit =
    try {
      val p = periodStr.toInt
      val v = BigDecimal(valueStr)
      val key = IndicatorKey(name.toUpperCase, sym, p)
      md = md.copy(indicatorOverrides = md.indicatorOverrides + (key -> v))
      println(s"Set OVERRIDE $name($sym,$p) = ${fmt(v)}")
    } catch {
      case _: NumberFormatException => println("Error: PERIOD must be an integer")
      case e: Exception             => println(s"Error: ${e.getMessage}")
    }

  // -------- Evaluation --------
  private def evalAndPrint(programSrc: String): Unit = {
    try {
      val program: Program = SophieParserFacade.parseString(programSrc)
      val plan = Evaluator.evaluate(program, md)
      lastPlan = Some(plan)
      lastProgramSrc = Some(programSrc)
      prettyPrint(plan)
    } catch { case NonFatal(e) => println(s"[Error] ${e.getMessage}") }
  }

  private def prettyPrint(plan: ExecutionPlan): Unit = {
    println("\n=== Execution Plan ===")
    if (plan.trades.isEmpty) println(" - (no trades)")
    plan.trades.foreach { d =>
      val status = if (d.shouldExecute) "EXECUTE" else "SKIP"
      println(s" - [$status] ${d.detail}")
    }
    plan.portfolio match {
      case Some(p) =>
        println(" - [PORTFOLIO] target allocations:")
        p.allocations.foreach { a =>
          println(s"   * ${fmt(a.value.amount)} ${a.value.currency} OF ${a.symbol}")
        }
      case None => ()
    }
    println()
  }

  // -------- Portfolio simulation --------
  private def pfNew(): Unit = {
    portfolio = Map.empty.withDefaultValue(BigDecimal(0))
    println("Portfolio reset.")
  }

  private def pfShow(): Unit = {
    println("\n=== Portfolio ===")
    if (portfolio.isEmpty) println(" - (empty)")
    var total: BigDecimal = 0
    portfolio.toSeq.sortBy(_._1).foreach { case (sym, qty) =>
      val line = md.price(sym) match {
        case Some(px) =>
          val v = qty * px; total += v
          s"$sym: qty=${fmt(qty)}  ~ value=${fmt(v)} (px=${fmt(px)})"
        case None =>
          s"$sym: qty=${fmt(qty)}  (no price)"
      }
      println(" - " + line)
    }
    if (total > 0) println(s"Total M2M value ≈ ${fmt(total)}")
    println()
  }

  private def pfApply(): Unit = {
    lastPlan match {
      case None =>
        println("No plan to apply. Evaluate a program first.")
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) { println("No EXECUTE trades in last plan."); return }
        var applied = 0
        exec.foreach { d =>
          val sym = d.cmd.symbol
          val v   = d.cmd.value
          val qty: BigDecimal =
            if (v.currency == sym) v.amount
            else md.price(sym) match {
              case Some(px) if px != 0 => v.amount / px
              case _ =>
                println(s" ! Missing PRICE($sym) — cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                BigDecimal(0)
            }
          if (qty > 0) {
            val cur = portfolio(sym)
            val next = d.cmd.action match {
              case Buy  => cur + qty
              case Sell => (cur - qty).max(BigDecimal(0))
            }
            portfolio = portfolio.updated(sym, next)
            applied += 1
          }
        }
        portfolio = portfolio.filter { case (_, q) => q > 0 }
        println(s"Applied $applied trade(s) to portfolio.")
        pfShow()
    }
  }

  // -------- Program IO --------
  private def runProg(path: String): Unit =
    try {
      val src = Files.readString(Paths.get(path), UTF_8)
      evalAndPrint(src)
    } catch { case e: Exception => println(s"Error loading program: ${e.getMessage}") }

  private def saveProg(path: String): Unit =
    lastProgramSrc match {
      case Some(src) =>
        try {
          ensureParentDir(path)
          Files.writeString(Paths.get(path), src, UTF_8)
          println(s"Saved last program to $path")
        } catch { case e: Exception => println(s"Error saving program: ${e.getMessage}") }
      case None =>
        println("No program to save. Evaluate a program first.")
    }

  // -------- Portfolio persistence --------
  private def pfSave(path: String): Unit = {
    try {
      ensureParentDir(path)
      val json = write(PortfolioJson.PortfolioJ(portfolio.filter(_._2 > 0)), indent = 2)
      Files.writeString(Paths.get(path), json, UTF_8)
      println(s"Saved portfolio to $path")
    } catch { case e: Exception => println(s"Error saving portfolio: ${e.getMessage}") }
  }

  private def pfLoad(path: String): Unit = {
    try {
      val json = Files.readString(Paths.get(path), UTF_8)
      val pj   = read[PortfolioJson.PortfolioJ](json)
      portfolio = pj.positions.withDefaultValue(BigDecimal(0))
      println(s"Loaded portfolio from $path (positions=${portfolio.count(_._2>0)})")
    } catch { case e: Exception => println(s"Error loading portfolio: ${e.getMessage}") }
  }

  // -------- IR / Executor integration --------
  private def compileIr(path: String): Unit = {
    lastPlan match {
      case None => println("No plan. Evaluate a program first.")
      case Some(plan) =>
        try {
          ensureParentDir(path)
          val instrs = Lowering.from(plan, md, source = "tui")
          val json = write(instrs, indent = 2)
          Files.writeString(Paths.get(path), json, UTF_8)
          println(s"Wrote IR instructions to $path (${instrs.size} op)")
        } catch { case e: Exception => println(s"Error: ${e.getMessage}") }
    }
  }

  private def execIr(path: String): Unit = {
    try {
      val json = Files.readString(Paths.get(path), UTF_8)
      val instrs = read[List[Instruction]](json)
      // default storage under ./data
      ensureParentDir("data/portfolio.json")
      ensureParentDir("data/ledger.ndjson")
      val pfStore = FileJsonPortfolioStore(Paths.get("data/portfolio.json"))
      val ledger  = FileLedger(Paths.get("data/ledger.ndjson"))
      Executor.run(instrs, md, pfStore, ledger, source = s"ir:${Paths.get(path).getFileName}")
      println(s"Executed ${instrs.size} instruction(s). Portfolio saved, ledger appended.")
    } catch { case e: Exception => println(s"Error executing IR: ${e.getMessage}") }
  }

  // -------- Utils --------
  private def printMarketData(): Unit = {
    println("Prices:")
    if (md.prices.isEmpty) println("  (empty)")
    else md.prices.toSeq.sortBy(_._1).foreach { case (s, v) => println(f"  - $s%-6s = ${fmt(v)}") }

    println("\nSeries (symbol.field -> last value):")
    if (md.seriesData.isEmpty) println("  (empty)")
    else md.seriesData.toSeq.sortBy{case ((s,f),_) => s"$s.$f"}.foreach {
      case ((s, f), vec) =>
        val last = vec.lastOption.map(fmt).getOrElse("n/a")
        println(f"  - $s.$f%-14s len=${vec.length}%d last=$last")
    }

    println("\nIndicator overrides:")
    if (md.indicatorOverrides.isEmpty) println("  (empty)")
    else md.indicatorOverrides.toSeq.sortBy(kv => (kv._1.name, kv._1.symbol, kv._1.period)).foreach {
      case (IndicatorKey(n, s, p), v) => println(s"  - $n($s,$p) = ${fmt(v)}")
    }
    println()
  }

  private def fmt(x: BigDecimal): String =
    x.bigDecimal.stripTrailingZeros.toPlainString

  private def ensureParentDir(path: String): Unit = {
    val p = Paths.get(path)
    val parent = p.getParent
    if (parent != null && !Files.exists(parent)) Files.createDirectories(parent)
  }
}
