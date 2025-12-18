package frontend

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}
import scala.util.control.NonFatal
import scala.math.BigDecimal.RoundingMode
import engine._

class PortfolioManager(priceFn: String => Option[BigDecimal], printer: TuiPrinter) {
  // Manages the portfolio state used by the TUI and encapsulates all
  // persistence/printing logic. This way we can reuse the same functions in
  // both the interactive interface and automated tests.

  private var portfolio: PortfolioState = PortfolioState(Map.empty.withDefaultValue(BigDecimal(0)), BigDecimal(0))
  private def positions: Map[String, BigDecimal] = portfolio.positions.withDefaultValue(BigDecimal(0))

  private def fmt(x: BigDecimal): String = x.bigDecimal.stripTrailingZeros.toPlainString
  private def fmtQty(x: BigDecimal): String = try { x.setScale(10, RoundingMode.HALF_UP).bigDecimal.stripTrailingZeros.toPlainString } catch { case _: Throwable => x.bigDecimal.stripTrailingZeros.toPlainString }

  def reset(): Unit = {
    portfolio = PortfolioState(Map.empty.withDefaultValue(BigDecimal(0)), BigDecimal(0))
    printer.printlnLine("Portfolio reset.")
  }

  def getPortfolio: Map[String, BigDecimal] = portfolio.positions
  def getPortfolioState: PortfolioState = portfolio

  def show(): Unit = {
    printer.printlnLine("\n=== Portfolio ===")
    if (portfolio.positions.isEmpty) printer.printlnLine(" - (empty)")
    if (portfolio.cash != 0) printer.printlnLine(s" - cash: ${fmt(portfolio.cash)}")
    var total: BigDecimal = 0
    portfolio.positions.toSeq.sortBy(_._1).foreach { case (sym, qty) =>
      val line = priceFn(sym) match {
        case Some(px) =>
          val v = qty * px; total += v
          s"$sym: qty=${fmtQty(qty)}  ~ value=${fmt(v)} (px=${fmt(px)})"
        case None => s"$sym: qty=${fmtQty(qty)}  (no price)"
      }
      printer.printlnLine(" - " + line)
    }
    if (total > 0 || portfolio.cash != 0) printer.printlnLine(s"Total M2M value ~ ${fmt(total + portfolio.cash)}")
    printer.printlnLine("")
  }

  def save(path: String): Unit = {
    try {
      val p = Paths.get(path)
      val parent = p.getParent
      if (parent != null && !Files.exists(parent)) Files.createDirectories(parent)
      val json = write(PortfolioJson.PortfolioJ(portfolio.positions.filter(_._2 > 0), Some(portfolio.cash)), indent = 2)
      Files.writeString(p, json, UTF_8)
      printer.printlnLine(s"Saved portfolio to $path")
    } catch { case NonFatal(e) => printer.printlnLine(s"Error saving portfolio: ${e.getMessage}") }
  }

  def load(path: String): Unit = {
    try {
      val json = Files.readString(Paths.get(path), UTF_8)
      val pj = read[PortfolioJson.PortfolioJ](json)
      portfolio = PortfolioState(pj.positions.withDefaultValue(BigDecimal(0)), pj.cash.getOrElse(BigDecimal(0)))
      printer.printlnLine(s"Loaded portfolio from $path (positions=${portfolio.positions.count(_._2>0)})")
    } catch { case NonFatal(e) => printer.printlnLine(s"Error loading portfolio: ${e.getMessage}") }
  }

  // Pure transition: apply an optional plan to a given PortfolioState and return the new state,
  // number of applied trades and a list of messages to display. This is pure and does not use printer.
  def pureApplyPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal], state: PortfolioState): (PortfolioState, Int, List[String]) = {
    planOpt match {
      case None => (state, 0, List("No plan to apply. Evaluate a program first."))
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) (state, 0, List("No EXECUTE trades in last plan."))
        else {
          val (finalState, applied, msgs) = exec.foldLeft((state, 0, List.empty[String])) {
            case ((st, appl, msgs), d) =>
              val sym = d.cmd.symbol
              val v   = d.cmd.value
              val (qty, msgs2) =
                if (v.currency == sym) (v.amount, msgs)
                else mdPriceFn(sym) match {
                  case Some(px) if px != 0 => (v.amount / px, msgs)
                  case Some(_)             => (BigDecimal(0), msgs :+ s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                  case None                => (BigDecimal(0), msgs :+ s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                }

              if (qty > 0) {
                val cur = st.positions.withDefaultValue(BigDecimal(0))(sym)
                val next = d.cmd.action match { case ast.Buy => cur + qty; case ast.Sell => (cur - qty).max(BigDecimal(0)) }
                if (next != cur) {
                  if (d.cmd.action == ast.Sell && cur == 0) (st, appl, msgs2 :+ s" ! Skipping SELL ${fmt(qty)} $sym - no holdings to reduce")
                  else (st.copy(positions = st.positions.updated(sym, next)), appl + 1, msgs2)
                } else (st, appl, msgs2 :+ s" ! Skipping ${d.cmd.action} for $sym - no net change (qty=${fmt(qty)})")
              } else (st, appl, msgs2 :+ s" ! Skipping ${d.cmd.action} for $sym - computed quantity is 0")
          }

          val finalFiltered = finalState.copy(positions = finalState.positions.filter { case (_, q) => q > 0 })
          (finalFiltered, applied, msgs :+ s"Applied $applied trade(s) to portfolio.")
        }
    }
  }

  // Pure preview: returns the resulting PortfolioState, applied count and messages, without side-effects
  def purePreviewPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal], state: PortfolioState): (PortfolioState, Int, List[String]) = {
    planOpt match {
      case None => (state, 0, List("No plan to preview. Evaluate a program first."))
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) (state, 0, List("No EXECUTE trades in last plan."))
        else {
          val (finalState, applied, msgs) = exec.foldLeft((state, 0, List.empty[String])) {
            case ((st, appl, msgs), d) =>
              val sym = d.cmd.symbol
              val v   = d.cmd.value
              val (qty, msgs2) =
                if (v.currency == sym) (v.amount, msgs)
                else mdPriceFn(sym) match {
                  case Some(px) if px != 0 => (v.amount / px, msgs)
                  case Some(_)             => (BigDecimal(0), msgs :+ s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                  case None                => (BigDecimal(0), msgs :+ s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                }

              if (qty > 0) {
                val cur = st.positions.withDefaultValue(BigDecimal(0))(sym)
                val next = d.cmd.action match { case ast.Buy => cur + qty; case ast.Sell => (cur - qty).max(BigDecimal(0)) }
                if (next != cur) {
                  if (d.cmd.action == ast.Sell && cur == 0) (st, appl, msgs2 :+ s" ! Skipping SELL ${fmt(qty)} $sym - no holdings to reduce")
                  else (st.copy(positions = st.positions.updated(sym, next)), appl + 1, msgs2)
                } else (st, appl, msgs2 :+ s" ! Skipping ${d.cmd.action} for $sym - no net change (qty=${fmt(qty)})")
              } else (st, appl, msgs2 :+ s" ! Skipping ${d.cmd.action} for $sym - computed quantity is 0")
          }

          val finalFiltered = finalState.copy(positions = finalState.positions.filter { case (_, q) => q > 0 })
          (finalFiltered, applied, msgs :+ s"Preview: would apply $applied trade(s).")
        }
    }
  }

  // apply an optional plan; returns number of applied trades
  def applyPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal]): Int = {
    val (newState, applied, msgs) = pureApplyPlan(planOpt, mdPriceFn, portfolio)
    msgs.foreach(printer.printlnLine)
    portfolio = newState
    // show resulting portfolio similar to previous behavior
    if (applied > 0) show()
    applied
  }

  // preview without mutating internal portfolio
  def previewPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal]): Map[String, BigDecimal] = {
    val (newState, applied, msgs) = purePreviewPlan(planOpt, mdPriceFn, portfolio)
    msgs.foreach(printer.printlnLine)
    if (applied > 0) {
      printer.printlnLine("Resulting portfolio:")
      if (newState.positions.isEmpty) printer.printlnLine(" - (empty)")
      else newState.positions.toSeq.sortBy(_._1).foreach { case (s,q) => printer.printlnLine(s" - $s: qty=${fmt(q)}") }
    }
    newState.positions
  }
}
