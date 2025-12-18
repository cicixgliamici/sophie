package frontend

import SophieTui.{PasteBuffer, SessionState}
import engine.PortfolioState

sealed trait TuiCommand
object TuiCommand {
  case object Quit extends TuiCommand
  case object Help extends TuiCommand
  case object ShowMd extends TuiCommand
  case object ShowLast extends TuiCommand
  case class SetPrice(sym: String, value: String) extends TuiCommand
  case class SetSeries(sym: String, field: String, csv: String) extends TuiCommand
  case class SetOverride(name: String, sym: String, period: String, value: String) extends TuiCommand
  case class LoadMd(path: String) extends TuiCommand
  case class SaveMd(path: String) extends TuiCommand
  case class RunProg(path: String) extends TuiCommand
  case class SaveProg(path: String) extends TuiCommand
  case object ResetPortfolio extends TuiCommand
  case object ShowPortfolio extends TuiCommand
  case object ApplyPlan extends TuiCommand
  case object PreviewPlan extends TuiCommand
  case class SavePortfolio(path: String) extends TuiCommand
  case class LoadPortfolio(path: String) extends TuiCommand
  case class CompileIr(path: String) extends TuiCommand
  case class ExecIr(path: String) extends TuiCommand
  case object EndBuffer extends TuiCommand
  case object AbortBuffer extends TuiCommand
  case class Unknown(raw: String, reason: String) extends TuiCommand
}

case class CommandResult(
  continue: Boolean,
  session: SessionState,
  portfolio: PortfolioState,
  buffer: PasteBuffer,
  log: Vector[String]
)

trait TuiActions {
  def help: Vector[String]
  def showMd(session: SessionState): Vector[String]
  def showLast(session: SessionState): Vector[String]
  def setPrice(session: SessionState, sym: String, v: String): (SessionState, Vector[String])
  def setSeries(session: SessionState, s: String, f: String, csv: String): (SessionState, Vector[String])
  def setOverride(session: SessionState, n: String, s: String, p: String, v: String): (SessionState, Vector[String])
  def loadMd(session: SessionState, path: String): (SessionState, Vector[String])
  def saveMd(session: SessionState, path: String): (SessionState, Vector[String])
  def runProg(session: SessionState, path: String): (SessionState, Vector[String])
  def saveProg(session: SessionState, path: String): (SessionState, Vector[String])
  def compileIr(session: SessionState, path: String): (SessionState, Vector[String])
  def execIr(session: SessionState, path: String): (SessionState, Vector[String])
  def evalBuffer(session: SessionState, buf: PasteBuffer): (SessionState, Vector[String])
}

class CommandHandler(actions: TuiActions, portfolioManager: PortfolioManager) {

  private def parse(cmd: String): TuiCommand = cmd.split("\\s+").toList match {
    case List(":q") | List(":quit")              => TuiCommand.Quit
    case List(":help")                            => TuiCommand.Help
    case List(":show", "md")                     => TuiCommand.ShowMd
    case List(":show", "last")                   => TuiCommand.ShowLast
    case List(":set", "price", sym, v)           => TuiCommand.SetPrice(sym, v)
    case List(":set", "series", s, f, csv)       => TuiCommand.SetSeries(s, f, csv)
    case List(":set", "ovr", n, s, p, v)         => TuiCommand.SetOverride(n, s, p, v)
    case List(":load", "md", path)               => TuiCommand.LoadMd(path)
    case List(":save", "md", path)               => TuiCommand.SaveMd(path)
    case List(":run", "prog", path)              => TuiCommand.RunProg(path)
    case List(":save", "prog", path)             => TuiCommand.SaveProg(path)
    case List(":pf", "new")                      => TuiCommand.ResetPortfolio
    case List(":pf", "show")                     => TuiCommand.ShowPortfolio
    case List(":pf", "apply")                    => TuiCommand.ApplyPlan
    case List(":pf", "preview")                  => TuiCommand.PreviewPlan
    case List(":pf", "save", path)               => TuiCommand.SavePortfolio(path)
    case List(":pf", "load", path)               => TuiCommand.LoadPortfolio(path)
    case List(":compile", "ir", path)            => TuiCommand.CompileIr(path)
    case List(":exec", "ir", path)               => TuiCommand.ExecIr(path)
    case List(":end")                             => TuiCommand.EndBuffer
    case List(":abort")                           => TuiCommand.AbortBuffer
    case other                                     => TuiCommand.Unknown(cmd, s"Unknown command or arity: ${other.mkString(" ")}")
  }

  def handle(raw: String, session: SessionState, portfolio: PortfolioState, buf: PasteBuffer): CommandResult = {
    val cmd = parse(raw)
    val baseLog = Vector.empty[String]

    cmd match {
      case TuiCommand.Quit => CommandResult(continue = false, session, portfolio, buf, baseLog)

      case TuiCommand.Help => CommandResult(true, session, portfolio, buf, actions.help)

      case TuiCommand.ShowMd => CommandResult(true, session, portfolio, buf, actions.showMd(session))

      case TuiCommand.ShowLast => CommandResult(true, session, portfolio, buf, actions.showLast(session))

      case TuiCommand.SetPrice(sym, v) =>
        val (nextSession, log) = actions.setPrice(session, sym, v)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.SetSeries(s, f, csv) =>
        val (nextSession, log) = actions.setSeries(session, s, f, csv)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.SetOverride(n, s, p, v) =>
        val (nextSession, log) = actions.setOverride(session, n, s, p, v)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.LoadMd(path) =>
        val (nextSession, log) = actions.loadMd(session, path)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.SaveMd(path) =>
        val (nextSession, log) = actions.saveMd(session, path)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.RunProg(path) =>
        val (nextSession, log) = actions.runProg(session, path)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.SaveProg(path) =>
        val (nextSession, log) = actions.saveProg(session, path)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.ResetPortfolio =>
        val (nextPortfolio, log) = portfolioManager.reset()
        CommandResult(true, session, nextPortfolio, buf, log)

      case TuiCommand.ShowPortfolio =>
        val log = portfolioManager.show(portfolio, sym => session.md.price(sym))
        CommandResult(true, session, portfolio, buf, log)

      case TuiCommand.ApplyPlan =>
        val (nextPortfolio, log) = portfolioManager.applyPlan(session.lastPlan, sym => session.md.price(sym), portfolio)
        CommandResult(true, session, nextPortfolio, buf, log)

      case TuiCommand.PreviewPlan =>
        val (nextPortfolio, log) = portfolioManager.previewPlan(session.lastPlan, sym => session.md.price(sym), portfolio)
        CommandResult(true, session, nextPortfolio, buf, log)

      case TuiCommand.SavePortfolio(path) =>
        val log = portfolioManager.save(portfolio, path)
        CommandResult(true, session, portfolio, buf, log)

      case TuiCommand.LoadPortfolio(path) =>
        val (nextPortfolio, log) = portfolioManager.load(path)
        CommandResult(true, session, nextPortfolio, buf, log)

      case TuiCommand.CompileIr(path) =>
        val (nextSession, log) = actions.compileIr(session, path)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.ExecIr(path) =>
        val (nextSession, log) = actions.execIr(session, path)
        CommandResult(true, nextSession, portfolio, buf, log)

      case TuiCommand.EndBuffer =>
        if (buf.nonEmpty) {
          val (nextSession, log) = actions.evalBuffer(session, buf)
          CommandResult(true, nextSession, portfolio, PasteBuffer.empty, log)
        } else CommandResult(true, session, portfolio, buf, Vector(":end: no program in buffer"))

      case TuiCommand.AbortBuffer =>
        val log = if (buf.nonEmpty) Vector(":abort: paste buffer cleared") else Vector(":abort: no program in buffer")
        CommandResult(true, session, portfolio, PasteBuffer.empty, log)

      case TuiCommand.Unknown(raw, reason) =>
        CommandResult(true, session, portfolio, buf, Vector(s"Unknown command: $raw  (try :help) [$reason]"))
    }
  }
}
