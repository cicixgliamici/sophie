package frontend

import ast._
import engine._
import scala.io.StdIn.readLine
import scala.util.control.NonFatal
import scala.annotation.tailrec
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}

/*
  SophieTui
  ---------
  This object implements a small textual TUI (read-eval-print loop) used by
  the project for interactive exploration and demos. Key design goals and
  notes (useful for testing / university assignment):

  - Separation of concerns: the TUI is thin and delegates the real work to
    pure components (parser -> AST -> evaluator -> lowering). The core logic
    that transforms program + market data -> execution plan is pure and
    testable without I/O.

  - Side-effects (I/O) are isolated: methods that perform file reads/writes
    or print receipts are in `actions` or helper modules. This makes it easy
    to test the program evaluation in isolation and to simulate user
    interactions via `simulateSession`.

  - Testing the TUI:
    * Unit tests should exercise `SophieTui.simulateSession` to programmatically
      feed commands and obtain the resulting (portfolio, lastPlan). This avoids
      starting an interactive console during automated tests.
    * For manual/testing of the interactive TUI: run the TUI main (TuiMain)
      or call `SophieTui.run()` from the REPL / main runner.

  Examples (how to test): see README.md at the project root for example
  sbt commands to run the CLI and to run the TUI in interactive mode.
*/

object SophieTui {

  case class SessionState(
    md: InMemoryMarketData,
    lastPlan: Option[ExecutionPlan],
    lastProgramSrc: Option[String]
  )

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
  private lazy val portfolioManager: PortfolioManager = new PortfolioManager()

  private lazy val commandHandler: CommandHandler = new CommandHandler(actions, portfolioManager)

  def run(): Unit = {
    printer.printlnLine("Sophie TUI - type :help for commands. Paste DSL, blank line to run.")

    @tailrec
    def loop(session: SessionState, portfolio: PortfolioState, buf: PasteBuffer): Unit = {
      val prompt = if (buf.nonEmpty) "... " else "sophie> "
      val raw    = readLine(prompt)
      val line   = normalizeForCommand(raw)

      (line, raw) match {
        case (null, _) => printer.printlnLine("Bye.")
        case (l, _) if l.isEmpty && buf.nonEmpty =>
          val (nextSession, log) = evalAndCollect(buf.result, session)
          log.foreach(printer.printlnLine)
          loop(nextSession, portfolio, PasteBuffer.empty)
        case (l, _) if l != null && l.startsWith(":") =>
          val result = commandHandler.handle(l, session, portfolio, buf)
          result.log.foreach(printer.printlnLine)
          if (result.continue) loop(result.session, result.portfolio, result.buffer) else printer.printlnLine("Bye.")
        case (_, r) if r != null =>
          // appendiamo la riga *come è stata inserita* nel buffer (preserviamo eventuali spazi interni)
          loop(session, portfolio, buf.append(r))
        case _ => printer.printlnLine("Bye.")
      }
    }

    loop(SessionState(InMemoryMarketData(), None, None), portfolioManager.empty, PasteBuffer.empty)
  }

  // helper: normalizza la riga per rilevare comandi (rimuove BOM e caratteri di controllo invisibili)
  private def normalizeForCommand(line: String): String = {
    if (line == null) null
    else line.replace("\uFEFF", "").replaceAll("\\p{C}", "").trim
  }
  private val helpLines: Vector[String] =
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
      |  :show last
      |  :set price <SYM> <VALUE>
      |  :set series <SYM> <FIELD> <v1,...>
      |  :set ovr <NAME> <SYM> <PERIOD> <V>
      |
      |  :pf new   - reset portfolio
      |  :pf show  - show positions and mark-to-market (if prices available)
      |  :pf apply - apply last execution plan (EXECUTE trades only)
      |  :pf preview - preview last plan without mutating portfolio
      |
      |Paste a multi-line Sophie program; submit with an empty line.
      |Lines that start with ':' are always treated as TUI commands (even when pasting).
      |""".stripMargin.linesIterator.toVector

  private val actions: TuiActions = new TuiActions {
    override def help: Vector[String] = helpLines

    override def showMd(session: SessionState): Vector[String] = renderMarketData(session.md)

    override def showLast(session: SessionState): Vector[String] =
      session.lastProgramSrc
        .map(src => Vector("\n=== Last program source ===") ++ src.split("\n").toVector)
        .getOrElse(Vector("No program source available."))

    override def setPrice(session: SessionState, sym: String, v: String): (SessionState, Vector[String]) =
      try {
        val value = BigDecimal(v)
        val next = session.copy(md = session.md.copy(prices = session.md.prices + (sym -> value)))
        (next, Vector(s"Set PRICE($sym) = ${fmt(value)}"))
      } catch { case NonFatal(e) => (session, Vector(s"Error: ${e.getMessage}")) }

    override def setSeries(session: SessionState, s: String, f: String, csv: String): (SessionState, Vector[String]) =
      try {
        val vs    = csv.split(",").toVector.map(v => BigDecimal(v.trim))
        val next  = session.copy(md = session.md.copy(seriesData = session.md.seriesData + ((s -> f) -> vs)))
        val last  = vs.lastOption.map(fmt).getOrElse("n/a")
        (next, Vector(s"Set SERIES $s.$f (${vs.length} points, last=$last)"))
      } catch { case NonFatal(e) => (session, Vector(s"Error: ${e.getMessage}")) }

    override def setOverride(session: SessionState, n: String, s: String, p: String, v: String): (SessionState, Vector[String]) =
      try {
        val period = p.toInt
        val value  = BigDecimal(v)
        val key    = IndicatorKey(n.toUpperCase, s, period)
        val next   = session.copy(md = session.md.copy(indicatorOverrides = session.md.indicatorOverrides + (key -> value)))
        (next, Vector(s"Set OVERRIDE $n($s,$period) = ${fmt(value)}"))
      } catch {
        case _: NumberFormatException => (session, Vector("Error: PERIOD must be an integer"))
        case NonFatal(e)              => (session, Vector(s"Error: ${e.getMessage}"))
      }

    override def loadMd(session: SessionState, path: String): (SessionState, Vector[String]) =
      try {
        val p = Paths.get(path)
        val json =
          if (Files.exists(p)) Files.readString(p, UTF_8)
          else {
            val is = Option(getClass.getClassLoader.getResourceAsStream(path))
              .getOrElse(throw new IllegalArgumentException(s"Not found: $path (neither file nor classpath resource)"))
            try new String(is.readAllBytes(), UTF_8) finally is.close()
          }

        val j     = read[MdJsonCodec.MarketDataJ](json)
        val next  = session.copy(md = MdJsonCodec.fromJ(j))
        val lines = Vector(s"Loaded MarketData from $path (prices=${next.md.prices.size}, series=${next.md.seriesData.size}, overrides=${next.md.indicatorOverrides.size})")
        (next, lines)
      } catch { case e: Exception => (session, Vector(s"Error loading $path: ${e.getMessage}")) }

    override def saveMd(session: SessionState, path: String): (SessionState, Vector[String]) =
      try {
        ensureParentDir(path)
        val j    = MdJsonCodec.toJ(session.md)
        val json = write(j, indent = 2)
        Files.writeString(Paths.get(path), json, UTF_8)
        (session, Vector(s"Saved MarketData to $path"))
      } catch { case e: Exception => (session, Vector(s"Error saving $path: ${e.getMessage}")) }

    override def runProg(session: SessionState, path: String): (SessionState, Vector[String]) =
      try {
        val src = Files.readString(Paths.get(path), UTF_8)
        evalAndCollect(src, session)
      } catch { case e: Exception => (session, Vector(s"Error loading program: ${e.getMessage}")) }

    override def saveProg(session: SessionState, path: String): (SessionState, Vector[String]) =
      session.lastProgramSrc match {
        case Some(src) =>
          try {
            ensureParentDir(path)
            Files.writeString(Paths.get(path), src, UTF_8)
            (session, Vector(s"Saved last program to $path"))
          } catch { case e: Exception => (session, Vector(s"Error saving program: ${e.getMessage}")) }
        case None => (session, Vector("No program to save. Evaluate a program first."))
      }

    override def compileIr(session: SessionState, path: String): (SessionState, Vector[String]) =
      session.lastPlan match {
        case None => (session, Vector("No plan. Evaluate a program first."))
        case Some(plan) =>
          try {
            ensureParentDir(path)

            Lowering.from(plan, session.md, source = "tui") match {
              case Left(err) => (session, Vector(s"Error lowering to instructions: $err"))
              case Right(instrs) =>
                val json = write(instrs, indent = 2)
                Files.writeString(Paths.get(path), json, UTF_8)
                (session, Vector(s"Wrote IR instructions to $path (${instrs.size} op)"))
            }
          } catch { case e: Exception => (session, Vector(s"Error: ${e.getMessage}")) }
      }

    override def execIr(session: SessionState, path: String): (SessionState, Vector[String]) =
      try {
        val json   = Files.readString(Paths.get(path), UTF_8)
        val instrs = read[List[Instruction]](json)
        // default storage under ./data
        ensureParentDir("data/portfolio.json")
        ensureParentDir("data/ledger.ndjson")
        val pfStore = FileJsonPortfolioStore(Paths.get("data/portfolio.json"))
        val ledger  = FileLedger(Paths.get("data/ledger.ndjson"))
        val events  = Executor.run(instrs, session.md, pfStore, ledger, source = s"ir:${Paths.get(path).getFileName}")
        ReceiptPrinter.printReceipts(events)
        (session, Vector(s"Executed ${instrs.size} instruction(s). Portfolio saved, ledger appended."))
      } catch { case e: Exception => (session, Vector(s"Error executing IR: ${e.getMessage}")) }

    override def evalBuffer(session: SessionState, buf: PasteBuffer): (SessionState, Vector[String]) =
      evalAndCollect(buf.result, session)
  }

  private def evalAndCollect(programSrc: String, session: SessionState): (SessionState, Vector[String]) = {
    try {
      val cleaned = if (programSrc != null) programSrc.replace("\uFEFF", "") else programSrc
      val res     = ProgramEvaluator.evaluate(cleaned, session.md)
      val plan    = res.plan
      val next    = session.copy(lastPlan = Some(plan), lastProgramSrc = Some(programSrc))

      val header  = Vector("\n=== Execution Plan ===")
      val trades  = if (plan.trades.isEmpty) Vector(" - (no trades)") else plan.trades.toVector.map { d =>
        val status = if (d.shouldExecute) "EXECUTE" else "SKIP"
        s" - [$status] ${d.detail}"
      }
      val portfolioLines = plan.portfolio match {
        case Some(p) =>
          Vector(" - [PORTFOLIO] target allocations:") ++ p.allocations.map { a =>
            s"   * ${fmt(a.value.amount)} ${a.value.currency} OF ${a.symbol}"
          }
        case None => Vector.empty[String]
      }

      val warnings = res.warnings.map(w => s"[Warning] $w. Set them with ':set price <SYM> <VALUE>' before applying the plan.").toVector

      (next, warnings ++ header ++ trades ++ portfolioLines :+ "")
    } catch { case NonFatal(e) => (session, Vector(s"[Error] ${e.getMessage}")) }
  }

  private def renderMarketData(md: InMemoryMarketData): Vector[String] = {
    val pricesHeader = Vector("Prices:")
    val pricesBody =
      if (md.prices.isEmpty) Vector("  (empty)")
      else md.prices.toSeq.sortBy(_._1).map { case (s, v) => f"  - $s%-6s = ${fmt(v)}" }.toVector

    val seriesHeader = Vector("", "Series (symbol.field -> last value):")
    val seriesBody =
      if (md.seriesData.isEmpty) Vector("  (empty)")
      else md.seriesData.toSeq.sortBy { case ((s, f), _) => s"$s.$f" }.map {
        case ((s, f), vec) =>
          val last = vec.lastOption.map(fmt).getOrElse("n/a")
          f"  - $s.$f%-14s len=${vec.length}%d last=$last"
      }.toVector

    val overridesHeader = Vector("", "Indicator overrides:")
    val overridesBody =
      if (md.indicatorOverrides.isEmpty) Vector("  (empty)")
      else md.indicatorOverrides.toSeq.sortBy(kv => (kv._1.name, kv._1.symbol, kv._1.period)).map {
        case (IndicatorKey(n, s, p), v) => s"  - $n($s,$p) = ${fmt(v)}"
      }.toVector

    pricesHeader ++ pricesBody ++ seriesHeader ++ seriesBody ++ overridesHeader ++ overridesBody :+ ""
  }

  private def fmt(x: BigDecimal): String =
    x.bigDecimal.stripTrailingZeros.toPlainString

  private def ensureParentDir(path: String): Unit = {
    val p = Paths.get(path)
    Option(p.getParent).foreach(parent => if (!Files.exists(parent)) Files.createDirectories(parent))
  }

  /**
    * Simulate a TUI session programmatically.
    *
    * Purpose: provide a deterministic, non-interactive way to test TUI
    * behaviour. The function accepts a sequence of input lines (commands and
    * program lines) and returns the final portfolio positions and the last
    * execution plan produced. Tests call this helper to assert behaviour.
    *
    * Example usage in tests:
    *   val inputs = Seq(":set price MSFT 350", "BUY 100 EUR OF MSFT;", "", ":pf apply")
    *   val (portfolio, lastPlan) = SophieTui.simulateSession(inputs)
    *
    * This keeps the interactive loop separate from automated tests (no
    * System.in reads during unit tests).
    */
  def simulateSession(inputs: Seq[String]): (Map[String, BigDecimal], Option[engine.ExecutionPlan]) = {
    @tailrec
    def process(rem: Seq[String], session: SessionState, portfolio: PortfolioState, buf: PasteBuffer): (SessionState, PortfolioState, PasteBuffer) =
      rem match {
        case Nil =>
          if (buf.nonEmpty) {
            val (nextSession, _) = evalAndCollect(buf.result, session)
            (nextSession, portfolio, PasteBuffer.empty)
          } else (session, portfolio, buf)
        case raw +: tail =>
          val line = normalizeForCommand(raw)
          line match {
            case null => (session, portfolio, buf)
            case l if l.isEmpty && buf.nonEmpty =>
              val (nextSession, _) = evalAndCollect(buf.result, session)
              process(tail, nextSession, portfolio, PasteBuffer.empty)
            case l if l != null && l.startsWith(":") =>
              val result = commandHandler.handle(l, session, portfolio, buf)
              process(tail, result.session, result.portfolio, result.buffer)
            case _ => process(tail, session, portfolio, buf.append(raw))
          }
      }

    val (finalSession, finalPortfolio, _) = process(inputs, SessionState(InMemoryMarketData(), None, None), portfolioManager.empty, PasteBuffer.empty)

    (finalPortfolio.positions, finalSession.lastPlan)
  }
}
