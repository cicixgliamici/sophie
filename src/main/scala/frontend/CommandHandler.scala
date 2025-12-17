package frontend

import scala.math.BigDecimal

class CommandHandler(priceLookup: String => Option[BigDecimal], portfolioManager: PortfolioManager, printer: TuiPrinter) {

  // helper to expose price lookup to portfolio manager
  private def priceFn(sym: String): Option[BigDecimal] = priceLookup(sym)

  def handle(cmd: String, buf: StringBuilder): Boolean = {
    val parts = cmd.split("\\s+").toList
    parts match {
      case List(":q") | List(":quit") => false
      case List(":help") =>
        printer.printlnLine(
          "Commands:\n  :pf save <path.json>\n  :pf load <path.json>\n  :load md <path.json>\n  :save md <path.json>\n  :run  prog <file.sophie>\n  :save prog <file.sophie>\n  :compile ir <file.json>   - compile last plan to IR (JSON)\n  :exec ir <file.json>      - execute IR, update portfolio & ledger\n  :help\n  :q | :quit\n  :show md\n  :set price <SYM> <VALUE>\n  :set series <SYM> <FIELD> <v1,...>\n  :set ovr <NAME> <SYM> <PERIOD> <V>\n  :pf new   - reset portfolio\n  :pf show  - show positions and mark-to-market\n  :pf apply - apply last execution plan\n")
        true

      case List(":show", "md") =>
        SophieTui.printMarketDataPublic(); true

      case List(":show", "last") =>
        SophieTui.printLast(); true

      case List(":set", "price", sym, v) =>
        SophieTui.setPricePublic(sym, v); true

      case List(":set", "series", s, f, csv) =>
        SophieTui.setSeriesPublic(s, f, csv); true

      case List(":set", "ovr", n, s, p, v) =>
        SophieTui.setOverridePublic(n, s, p, v); true

      case List(":load", "md", path) =>
        SophieTui.loadMdPublic(path); true
      case List(":save", "md", path) =>
        SophieTui.saveMdPublic(path); true

      case List(":run", "prog", path) =>
        SophieTui.runProgPublic(path); true
      case List(":save", "prog", path) =>
        SophieTui.saveProgPublic(path); true

      case List(":pf", "new") =>
        portfolioManager.reset(); true
      case List(":pf", "show") =>
        portfolioManager.show(); true
      case List(":pf", "apply") =>
        portfolioManager.applyPlan(SophieTui.getLastPlan(), priceFn); true
      case List(":pf", "preview") =>
        portfolioManager.previewPlan(SophieTui.getLastPlan(), priceFn); true
      case List(":pf", "save", path) =>
        portfolioManager.save(path); true
      case List(":pf", "load", path) =>
        portfolioManager.load(path); true

      case List(":compile", "ir", path) =>
        SophieTui.compileIrPublic(path); true
      case List(":exec", "ir", path) =>
        SophieTui.execIrPublic(path); true

      case List(":end") =>
        if (buf.nonEmpty) {
          val programSrc = buf.result(); buf.clear()
          SophieTui.evalAndPrintPublic(programSrc)
        } else printer.printlnLine(":end: no program in buffer")
        true

      case List(":abort") =>
        if (buf.nonEmpty) { buf.clear(); printer.printlnLine(":abort: paste buffer cleared") }
        else printer.printlnLine(":abort: no program in buffer")
        true

      case _ => printer.printlnLine(s"Unknown command: $cmd  (try :help)"); true
    }
  }
}
