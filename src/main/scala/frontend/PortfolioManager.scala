package frontend

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}
import frontend.PortfolioJson
import scala.util.control.NonFatal
import scala.math.BigDecimal.RoundingMode
import engine._
import ast._

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

  // apply an optional plan; returns number of applied trades
  def applyPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal]): Int = {
    planOpt match {
      case None => printer.printlnLine("No plan to apply. Evaluate a program first."); 0
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) { printer.printlnLine("No EXECUTE trades in last plan."); 0 }
        else {
          var applied = 0
          exec.foreach { d =>
            val sym = d.cmd.symbol
            val v   = d.cmd.value
            val qty: BigDecimal =
              if (v.currency == sym) v.amount
              else mdPriceFn(sym) match {
                case Some(px) if px != 0 => v.amount / px
                case _ =>
                  printer.printlnLine(s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                  BigDecimal(0)
              }
            if (qty > 0) {
              val cur = positions(sym)
              val next = d.cmd.action match {
                case ast.Buy  => cur + qty
                case ast.Sell => (cur - qty).max(BigDecimal(0))
              }
              if (next != cur) {
                if (d.cmd.action == ast.Sell && cur == 0) {
                  printer.printlnLine(s" ! Skipping SELL ${fmt(qty)} $sym - no holdings to reduce")
                } else {
                  portfolio = portfolio.copy(positions = portfolio.positions.updated(sym, next))
                  applied += 1
                }
              } else {
                printer.printlnLine(s" ! Skipping ${d.cmd.action} for $sym - no net change (qty=${fmt(qty)})")
              }
            } else {
              printer.printlnLine(s" ! Skipping ${d.cmd.action} for $sym - computed quantity is 0")
            }
          }
          portfolio = portfolio.copy(positions = portfolio.positions.filter { case (_, q) => q > 0 })
          printer.printlnLine(s"Applied $applied trade(s) to portfolio.")
          show()
          applied
        }
    }
  }

  // preview without mutating internal portfolio
  def previewPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal]): Map[String, BigDecimal] = {
    planOpt match {
      case None => printer.printlnLine("No plan to preview. Evaluate a program first."); Map.empty
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) { printer.printlnLine("No EXECUTE trades in last plan."); Map.empty }
        else {
          var applied = 0
          var tmp = portfolio
          exec.foreach { d =>
            val sym = d.cmd.symbol
            val v   = d.cmd.value
            val qty: BigDecimal =
              if (v.currency == sym) v.amount
              else mdPriceFn(sym) match {
                case Some(px) if px != 0 => v.amount / px
                case _ =>
                  printer.printlnLine(s" ! Missing PRICE($sym) - cannot convert ${fmt(v.amount)} ${v.currency} to quantity")
                  BigDecimal(0)
              }
            if (qty > 0) {
              val cur = tmp.positions.withDefaultValue(BigDecimal(0))(sym)
              val next = d.cmd.action match {
                case ast.Buy  => cur + qty
                case ast.Sell => (cur - qty).max(BigDecimal(0))
              }
              if (next != cur) {
                if (d.cmd.action == ast.Sell && cur == 0) printer.printlnLine(s" ! Skipping SELL ${fmt(qty)} $sym - no holdings to reduce")
                else { tmp = tmp.copy(positions = tmp.positions.updated(sym, next)); applied += 1 }
              } else printer.printlnLine(s" ! Skipping ${d.cmd.action} for $sym - no net change (qty=${fmt(qty)})")
            } else printer.printlnLine(s" ! Skipping ${d.cmd.action} for $sym - computed quantity is 0")
          }
          printer.printlnLine(s"Preview: would apply $applied trade(s). Resulting portfolio:")
          if (tmp.positions.isEmpty) printer.printlnLine(" - (empty)")
          else tmp.positions.toSeq.sortBy(_._1).foreach { case (s,q) => printer.printlnLine(s" - $s: qty=${fmt(q)}") }
          tmp
        }
    }
  }
}
