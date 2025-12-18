package frontend

import ast._
import engine._
import scala.io.StdIn.readLine
import scala.util.control.NonFatal
import scala.annotation.tailrec
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}

object SophieTui {

  // ---- State ----
  private case class SessionState(
    md: InMemoryMarketData,
    lastPlan: Option[ExecutionPlan],
    lastProgramSrc: Option[String]
  )

  private var state: SessionState = SessionState(InMemoryMarketData(), None, None)

  private def updateState(f: SessionState => SessionState): Unit = state = f(state)
  private def md: InMemoryMarketData                          = state.md
  private def lastPlan: Option[ExecutionPlan]                 = state.lastPlan
  private def lastProgramSrc: Option[String]                  = state.lastProgramSrc

  private[frontend] case class PasteBuffer(lines: Vector[String] = Vector.empty) {
    def nonEmpty: Boolean = lines.nonEmpty
    def append(raw: String): PasteBuffer = copy(lines = lines :+ raw)
    def result: String = lines.mkString("", "\n", if (lines.nonEmpty) "\n" else "")
    def cleared: PasteBuffer = PasteBuffer(Vector.empty)
  }

  private[frontend] object PasteBuffer {
    val empty: PasteBuffer = PasteBuffer(Vector.empty)
  }

  // printer default
  private val printer: TuiPrinter = DefaultPrinter

  // PortfolioManager and CommandHandler instances
  private lazy val portfolioManager: PortfolioManager = new PortfolioManager(sym => md.price(sym), printer)
  private lazy val commandHandler: CommandHandler = new CommandHandler(sym => md.price(sym), portfolioManager, printer)

  def run(): Unit = {
    println("Sophie TUI - type :help for commands. Paste DSL, blank line to run.")

    @tailrec
    def loop(buf: PasteBuffer): Unit = {
      val prompt = if (buf.nonEmpty) "... " else "sophie> "
      val raw    = readLine(prompt)
      val line   = normalizeForCommand(raw)

      (line, raw) match {
        case (null, _) => println("Bye.")
        case (l, _) if l.isEmpty && buf.nonEmpty =>
          evalAndPrint(buf.result)
          loop(PasteBuffer.empty)
        case (l, _) if l != null && l.startsWith(":") =>
          val (keepGoing, nextBuf) = handleCommand(l, buf)
          if (keepGoing) loop(nextBuf) else println("Bye.")
        case (_, r) if r != null =>
          // appendiamo la riga *come è stata inserita* nel buffer (preserviamo eventuali spazi interni)
          loop(buf.append(r))
        case _ => println("Bye.")
      }
    }

    loop(PasteBuffer.empty)
  }

  // -------- Command handling --------
  private def handleCommand(cmd: String, buf: PasteBuffer): (Boolean, PasteBuffer) =
    commandHandler.handle(cmd, buf)

  // helper: normalizza la riga per rilevare comandi (rimuove BOM e caratteri di controllo invisibili)
  private def normalizeForCommand(line: String): String = {
    if (line == null) null
    else line.replace("\uFEFF", "").replaceAll("\\p{C}", "").trim
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
      updateState(_.copy(md = MdJsonCodec.fromJ(j)))
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
      updateState(st => st.copy(md = st.md.copy(prices = st.md.prices + (sym -> v))))
      println(s"Set PRICE($sym) = ${fmt(v)}")
    } catch { case NonFatal(e) => println(s"Error: ${e.getMessage}") }

  private def setSeries(sym: String, field: String, valuesCsv: String): Unit =
    try {
      val vs = valuesCsv.split(",").toVector.map(s => BigDecimal(s.trim))
      updateState(st => st.copy(md = st.md.copy(seriesData = st.md.seriesData + ((sym -> field) -> vs))))
      val last = vs.lastOption.map(fmt).getOrElse("n/a")
      println(s"Set SERIES $sym.$field (${vs.length} points, last=$last)")
    } catch { case NonFatal(e) => println(s"Error: ${e.getMessage}") }

  private def setOverride(name: String, sym: String, periodStr: String, valueStr: String): Unit =
    try {
      val p = periodStr.toInt
      val v = BigDecimal(valueStr)
      val key = IndicatorKey(name.toUpperCase, sym, p)
      updateState(st => st.copy(md = st.md.copy(indicatorOverrides = st.md.indicatorOverrides + (key -> v))))
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
      updateState(_.copy(lastPlan = Some(plan), lastProgramSrc = Some(programSrc)))

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
@@ -441,75 +304,52 @@ object SophieTui {
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
    updateState(_ => SessionState(InMemoryMarketData(), None, None))
    portfolioManager.reset()

    @tailrec
    def process(rem: Seq[String], buf: PasteBuffer): PasteBuffer = rem match {
      case Nil =>
        if (buf.nonEmpty) { evalAndPrint(buf.result); PasteBuffer.empty } else buf
      case raw +: tail =>
        val line = normalizeForCommand(raw)
        line match {
          case null => buf
          case l if l.isEmpty && buf.nonEmpty =>
            evalAndPrint(buf.result)
            process(tail, PasteBuffer.empty)
          case l if l != null && l.startsWith(":") =>
            val (_, nextBuf) = handleCommand(l, buf)
            process(tail, nextBuf)
          case _ => process(tail, buf.append(raw))
        }
    }

    process(inputs, PasteBuffer.empty)

    (portfolioManager.getPortfolio, lastPlan)
  }

}
