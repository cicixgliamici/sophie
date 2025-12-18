package frontend

import ast._
import engine._
import scala.io.StdIn.readLine
import scala.util.control.NonFatal
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}
import scala.math.BigDecimal.RoundingMode

object SophieTui {

  // ---- State ----
  private var md: InMemoryMarketData = InMemoryMarketData()
  private var lastPlan: Option[ExecutionPlan] = None
  private var lastProgramSrc: Option[String]  = None
  private var portfolio: Map[String, BigDecimal] = Map.empty.withDefaultValue(BigDecimal(0))

  // printer default
  private val printer: TuiPrinter = DefaultPrinter

  // PortfolioManager and CommandHandler instances
  private lazy val portfolioManager: PortfolioManager = new PortfolioManager(sym => md.price(sym), printer)
  private lazy val commandHandler: CommandHandler = new CommandHandler(sym => md.price(sym), portfolioManager, printer)

  // helper: format qty with reasonable scale for display (moved to object level)
  private def fmtQty(x: BigDecimal): String = {
    try {
      x.setScale(10, RoundingMode.HALF_UP).bigDecimal.stripTrailingZeros.toPlainString
    } catch { case _: Throwable => x.bigDecimal.stripTrailingZeros.toPlainString }
  }

  def run(): Unit = {
    println("Sophie TUI - type :help for commands. Paste DSL, blank line to run.")
    var running = true
    val buf = new StringBuilder

    // helper: normalizza la riga per rilevare comandi (rimuove BOM e caratteri di controllo invisibili)
    def normalizeForCommand(line: String): String = {
      if (line == null) null
      else line.replace("\uFEFF", "").replaceAll("\\p{C}", "").trim
    }

    while (running) {
      val prompt = if (buf.nonEmpty) "... " else "sophie> "
      val raw = readLine(prompt)

      val line = normalizeForCommand(raw)

      if (line == null) running = false
      else if (line.isEmpty && buf.nonEmpty) {
        val programSrc = buf.result(); buf.clear()
        evalAndPrint(programSrc)
      } else if (line.startsWith(":")) {
        // trattiamo sempre le linee che iniziano con ':' come comandi, anche durante paste-mode
        running = handleCommand(line, buf)
      } else if (raw != null) {
        // appendiamo la riga *come è stata inserita* nel buffer (preserviamo eventuali spazi interni)
        buf.append(raw).append('\n')
      }
    }
    println("Bye.")
  }

  // -------- Command handling --------
  private def handleCommand(cmd: String, buf: StringBuilder): Boolean = {
    // delegate to CommandHandler to keep SophieTui thin
    commandHandler.handle(cmd, buf)
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
        |  :compile ir <file.json>   - compile last plan to IR (JSON)
        |  :exec ir <file.json>      - execute IR, update portfolio & ledger
        |
        |  :help
        |  :q | :quit
        |  :show md
        |  :set price <SYM> <VALUE>
        |  :set series <SYM> <FIELD> <v1,...>
        |  :set ovr <NAME> <SYM> <PERIOD> <V>
        |
        |  :pf new   - reset portfolio
        |  :pf show  - show positions and mark-to-market (if prices available)
        |  :pf apply - apply last execution plan (EXECUTE trades only)
        |
        |Paste a multi-line Sophie program; submit with an empty line.
        |Lines that start with ':' are always treated as TUI commands (even when pasting).
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
      val cleaned = if (programSrc != null) programSrc.replace("\uFEFF", "") else programSrc
      val res = ProgramEvaluator.evaluate(cleaned, md)

      // print warnings via printer
      res.warnings.foreach(w => printer.printlnLine(s"[Warning] $w. Set them with ':set price <SYM> <VALUE>' before applying the plan."))

      val plan = res.plan
      lastPlan = Some(plan)
      lastProgramSrc = Some(programSrc)

      // reuse existing prettyPrint but route outputs through printer
      printer.printlnLine("\n=== Execution Plan ===")
      if (plan.trades.isEmpty) printer.printlnLine(" - (no trades)")
      plan.trades.foreach { d =>
        val status = if (d.shouldExecute) "EXECUTE" else "SKIP"
        printer.printlnLine(s" - [$status] ${d.detail}")
      }
      plan.portfolio match {
        case Some(p) =>
          printer.printlnLine(" - [PORTFOLIO] target allocations:")
          p.allocations.foreach { a =>
            printer.printlnLine(s"   * ${fmt(a.value.amount)} ${a.value.currency} OF ${a.symbol}")
          }
        case None => ()
      }
      printer.printlnLine("")

    } catch { case NonFatal(e) => printer.printlnLine(s"[Error] ${e.getMessage}") }
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
    else {
      val rows = portfolio.toSeq.sortBy(_._1).map { case (sym, qty) =>
        md.price(sym) match {
          case Some(px) =>
            val v = qty * px
            (s" - $sym: qty=${fmtQty(qty)}  ~ value=${fmt(v)} (px=${fmt(px)})", v)
          case None => (s" - $sym: qty=${fmtQty(qty)}  (no price)", BigDecimal(0))
        }
      }
      rows.foreach { case (line, _) => println(line) }
      val total = rows.map(_._2).sum
      if (total > 0) println(s"Total M2M value ~ ${fmt(total)}")
      println()
    }
  }

  private def pfApply(): Unit = {
    lastPlan match {
      case None =>
        println("No plan to apply. Evaluate a program first.")
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) {
          println("No EXECUTE trades in last plan.")
        } else {
          val (updatedPortfolio, applied) = exec.foldLeft((portfolio, 0)) {
            case ((pfMap, applied), d) =>
              val sym = d.cmd.symbol
              val v   = d.cmd.value
              val qty: BigDecimal =
                if (v.currency == sym) v.amount
                else md.price(sym) match {
                  case Some(px) if px != 0 => v.amount / px
                  case _ =>
                    println(s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                    BigDecimal(0)
                }

              if (qty > 0) {
                val cur = pfMap(sym)
                val next = d.cmd.action match {
                  case Buy  => cur + qty
                  case Sell => (cur - qty).max(BigDecimal(0))
                }
                if (next != cur) {
                  if (d.cmd.action == Sell && cur == 0) {
                    println(s" ! Skipping SELL ${fmt(qty)} $sym - no holdings to reduce")
                    (pfMap, applied)
                  } else {
                    (pfMap.updated(sym, next), applied + 1)
                  }
                } else {
                  println(s" ! Skipping ${d.cmd.action} for $sym - no net change (qty=${fmt(qty)})")
                  (pfMap, applied)
                }
              } else {
                println(s" ! Skipping ${d.cmd.action} for $sym - computed quantity is 0")
                (pfMap, applied)
              }
          }
          portfolio = updatedPortfolio.filter { case (_, q) => q > 0 }
          println(s"Applied $applied trade(s) to portfolio.")
          pfShow()
        }
    }
  }

  // preview: simulate applying lastPlan without mutating portfolio
  private def pfPreview(): Unit = {
    lastPlan match {
      case None => println("No plan to preview. Evaluate a program first.")
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) {
          println("No EXECUTE trades in last plan.")
        } else {
          val (tmp, applied) = exec.foldLeft((portfolio, 0)) {
            case ((tmpMap, applied), d) =>
              val sym = d.cmd.symbol
              val v   = d.cmd.value
              val qty: BigDecimal =
                if (v.currency == sym) v.amount
                else md.price(sym) match {
                  case Some(px) if px != 0 => v.amount / px
                  case _ =>
                    println(s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                    BigDecimal(0)
                }

              if (qty > 0) {
                val cur = tmpMap.withDefaultValue(BigDecimal(0))(sym)
                val next = d.cmd.action match {
                  case Buy  => cur + qty
                  case Sell => (cur - qty).max(BigDecimal(0))
                }
                if (next != cur) {
                  if (d.cmd.action == Sell && cur == 0) {
                    println(s" ! Skipping SELL ${fmt(qty)} $sym - no holdings to reduce")
                    (tmpMap, applied)
                  } else {
                    (tmpMap.updated(sym, next), applied + 1)
                  }
                } else {
                  println(s" ! Skipping ${d.cmd.action} for $sym - no net change (qty=${fmt(qty)})")
                  (tmpMap, applied)
                }
              } else {
                println(s" ! Skipping ${d.cmd.action} for $sym - computed quantity is 0")
                (tmpMap, applied)
              }
          }

          println(s"Preview: would apply $applied trade(s). Resulting portfolio:")
          if (tmp.isEmpty) println(" - (empty)")
          else tmp.toSeq.sortBy(_._1).foreach { case (s,q) => println(s" - $s: qty=${fmt(q)}") }
        }
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

          Lowering.from(plan, md, source = "tui") match {
            case Left(err) => println(s"Error lowering to instructions: $err")
            case Right(instrs) =>
              val json = write(instrs, indent = 2)
              Files.writeString(Paths.get(path), json, UTF_8)
              println(s"Wrote IR instructions to $path (${instrs.size} op)")
          }
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
      val events = Executor.run(instrs, md, pfStore, ledger, source = s"ir:${Paths.get(path).getFileName}")
      println(s"Executed ${instrs.size} instruction(s). Portfolio saved, ledger appended.")
      ReceiptPrinter.printReceipts(events)
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

  // Exposed helpers for CommandHandler
  def printMarketDataPublic(): Unit = printMarketData()
  def printLast(): Unit = lastProgramSrc match { case Some(src) => printer.printlnLine("\n=== Last program source ===\n" + src); case None => printer.printlnLine("No program source available.") }

  def setPricePublic(sym: String, v: String): Unit = setPrice(sym, v)
  def setSeriesPublic(s: String, f: String, csv: String): Unit = setSeries(s, f, csv)
  def setOverridePublic(n: String, s: String, p: String, v: String): Unit = setOverride(n, s, p, v)

  def loadMdPublic(path: String): Unit = loadMd(path)
  def saveMdPublic(path: String): Unit = saveMd(path)

  def runProgPublic(path: String): Unit = runProg(path)
  def saveProgPublic(path: String): Unit = saveProg(path)

  def getLastPlan: Option[ExecutionPlan] = lastPlan
  def compileIrPublic(path: String): Unit = compileIr(path)
  def execIrPublic(path: String): Unit = execIr(path)
  def evalAndPrintPublic(src: String): Unit = evalAndPrint(src)

  /**
   * Simulate a TUI session programmatically.
   * Returns the final portfolio and lastPlan for assertions in tests.
   * This helper is intended for tests only.
   */
  def simulateSession(inputs: Seq[String]): (Map[String, BigDecimal], Option[engine.ExecutionPlan]) = {
    // reset transient state to have a clean session (isolate MD and portfolio)
    md = InMemoryMarketData()
    portfolioManager.reset()
    lastPlan = None
    lastProgramSrc = None

    val buf = new StringBuilder

    def normalize(line: String): String = {
      if (line == null) null
      else line.replace("\uFEFF", "").replaceAll("\\p{C}", "").trim
    }

    // detect if inputs contain a termination (null) and process only until then
    val hasStop = inputs.exists(raw => normalize(raw) == null)
    val toProcess = if (hasStop) inputs.takeWhile(raw => normalize(raw) != null) else inputs

    // process inputs functionally
    toProcess.foreach { raw =>
      val line = normalize(raw)
      // DEBUG LOGS
      println(s"[simulateSession] raw='$raw' normalized='${line}' bufBefore='${buf.result()}'")
      if (line == null) ()
      else if (line.isEmpty && buf.nonEmpty) {
        val programSrc = buf.result(); buf.clear()
        println(s"[simulateSession] EVALUATE program:\n$programSrc")
        // use existing evalAndPrint which sets lastPlan/lastProgramSrc
        evalAndPrint(programSrc)
      } else if (line.startsWith(":")) {
        println(s"[simulateSession] HANDLE COMMAND: $line")
        // treat as command even during paste-mode
        handleCommand(line, buf)
      } else {
        buf.append(raw).append('\n')
        println(s"[simulateSession] APPENDED to buffer, now='${buf.result()}'")
      }
    }

    if (hasStop) (portfolioManager.getPortfolio, lastPlan)
    else {
      // If inputs ended but buffer still has program, evaluate it (mimic user pressing blank line at end)
      if (buf.nonEmpty) {
        val programSrc = buf.result(); buf.clear()
        evalAndPrint(programSrc)
      }

      (portfolioManager.getPortfolio, lastPlan)
    }
  }

}
