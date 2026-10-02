package frontend

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import upickle.default.{read, write}
import scala.util.control.NonFatal
import scala.math.BigDecimal.RoundingMode
import engine._

class PortfolioManager() {
  // Stateless helper that provides pure portfolio transitions and combines them
  // with persistence/printing helpers. TUI callers thread `PortfolioState`
  // explicitly to keep command handling referentially transparent.
  //
  // Design note (functional style): this class intentionally exposes pure
  // functions that accept a `PortfolioState` and return a new `PortfolioState`
  // (plus informational messages). Side-effects (file I/O) are implemented in
  // separate methods (`save`, `load`) so that the core transformation logic
  // remains easy to test and reason about.
  //
  // What would change in an imperative design?
  //  - The manager would hold a mutable `var state: PortfolioState` field.
  //  - Methods like `applyPlan` would update that internal state and return
  //    Unit or a side-effecting status. Testing would require setting up and
  //    inspecting object state, increasing coupling and making reasoning
  //    about dataflow harder. The functional approach makes state explicit;
  //    the imperative approach hides it and centralises mutation.

  val empty: PortfolioState = PortfolioState(Map.empty.withDefaultValue(BigDecimal(0)), BigDecimal(0))

  private def fmt(x: BigDecimal): String = x.bigDecimal.stripTrailingZeros.toPlainString
  private def fmtQty(x: BigDecimal): String = try { x.setScale(10, RoundingMode.HALF_UP).bigDecimal.stripTrailingZeros.toPlainString } catch { case _: Throwable => x.bigDecimal.stripTrailingZeros.toPlainString }

  def reset(): (PortfolioState, Vector[String]) =
    (empty, Vector("Portfolio reset."))

  /**
    * Render a portfolio state to human-friendly lines using a price lookup function.
    *
    * Functional responsibility: this is a pure rendering helper. It accepts a
    * `PortfolioState` and a pure price function and returns textual lines. No
    * mutations or I/O are performed here.
    */
  def show(state: PortfolioState, priceFn: String => Option[BigDecimal]): Vector[String] = {
    val header = Vector("\n=== Portfolio ===")
    val base =
      if (state.positions.isEmpty) Vector(" - (empty)")
      else Vector.empty[String]
    val cash = if (state.cash != 0) Vector(s" - cash: ${fmt(state.cash)} ${state.currency}") else Vector.empty[String]

    val (total, lines) = state.positions.toSeq
      .sortBy(_._1)
      .foldLeft((BigDecimal(0), Vector.empty[String])) { case ((acc, accLines), (sym, qty)) =>
        val (line, delta) = priceFn(sym) match {
          case Some(px) =>
            val v = qty * px
            (s"$sym: qty=${fmtQty(qty)}  ~ value=${fmt(v)} (px=${fmt(px)})", v)
          case None => (s"$sym: qty=${fmtQty(qty)}  (no price)", BigDecimal(0))
        }
        (acc + delta, accLines :+ s" - $line")
      }

    val totals = if (total > 0 || state.cash != 0) Vector(s"Total M2M value ~ ${fmt(total + state.cash)}", "") else Vector("")
    header ++ base ++ cash ++ lines ++ totals
  }

  /**
    * Persist portfolio state to JSON on disk.
    * NOTE: this method performs I/O — we keep it separate from pure logic so
    * tests can exercise the transformation functions without touching the
    * filesystem.
    */
  def save(state: PortfolioState, path: String): Vector[String] = {
    try {
      val p = Paths.get(path)
      Option(p.getParent).foreach(parent => if (!Files.exists(parent)) Files.createDirectories(parent))
      val json = write(PortfolioJson.PortfolioJ(state.positions.filter(_._2 > 0), Some(state.cash), state.currency), indent = 2)
      Files.writeString(p, json, UTF_8)
      Vector(s"Saved portfolio to $path")
    } catch { case NonFatal(e) => Vector(s"Error saving portfolio: ${e.getMessage}") }
  }

  def load(path: String): (PortfolioState, Vector[String]) = {
    try {
      val json = Files.readString(Paths.get(path), UTF_8)
      val pj = read[PortfolioJson.PortfolioJ](json)
      val st = PortfolioState(pj.positions.withDefaultValue(BigDecimal(0)), pj.cash.getOrElse(BigDecimal(0)), pj.currency)
      (st, Vector(s"Loaded portfolio from $path (positions=${st.positions.count(_._2>0)})"))
    } catch { case NonFatal(e) => (empty, Vector(s"Error loading portfolio: ${e.getMessage}")) }
  }

  /**
    * General plan processing helper. Accepts the plan option, a price lookup
    * function, an initial state and messages to return in the happy path.
    * Returns (newState, appliedCount, messages).
    *
    * This orchestrates the pure computations: extracting executable trades and
    * delegating to `TradingEngine`. All branching decisions are local and pure.
    */
  private def processPlan(
    planOpt: Option[ExecutionPlan],
    mdPriceFn: String => Option[BigDecimal],
    state: PortfolioState,
    emptyPlanMsg: String,
    finalMsg: Int => String
  ): (PortfolioState, Int, Vector[String]) =
    planOpt match {
      case None => (state, 0, Vector(emptyPlanMsg))
      case Some(plan) =>
        val exec = plan.trades.filter(_.shouldExecute)
        if (exec.isEmpty) (state, 0, Vector("No EXECUTE trades in last plan."))
        else {
          // Lower once and delegate every accounting rule to the shared pure engine.
          val marketData = new MarketData {
            def price(symbol: String): Option[BigDecimal] = mdPriceFn(symbol)
            def series(symbol: String, field: String): Option[Vector[BigDecimal]] = None
          }
          val result = Lowering.from(plan, marketData).flatMap { instructions =>
            TradingEngine.simulate(instructions, mdPriceFn, state)
          }
          result match {
            case Left(error) => (state, 0, Vector(s"Plan rejected: $error", finalMsg(0)))
            case Right(outcome) => (outcome.portfolio, outcome.fills.size, Vector(finalMsg(outcome.fills.size)))
          }
        }
    }

  /**
    * Public pure helper: apply a plan to a given `PortfolioState` and return
    * (newState, appliedCount, messages). This is the recommended functional
    * API to use from callers: pass the current state in and use the returned
    * state as the new value.
    */
  def pureApplyPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal], state: PortfolioState): (PortfolioState, Int, Vector[String]) =
    processPlan(planOpt, mdPriceFn, state, "No plan to apply. Evaluate a program first.", applied => s"Applied $applied trade(s) to portfolio.")

  def purePreviewPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal], state: PortfolioState): (PortfolioState, Int, Vector[String]) =
    processPlan(planOpt, mdPriceFn, state, "No plan to preview. Evaluate a program first.", applied => s"Preview: would apply $applied trade(s).")

  /**
    * A convenience wrapper that follows the older naming: given an explicit
    * `state` it returns the new state and human-friendly log lines. It is still
    * functional (no internal mutation): callers must pass and keep the state.
    */
  def applyPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal], state: PortfolioState): (PortfolioState, Vector[String]) = {
    val (newState, applied, msgs) = pureApplyPlan(planOpt, mdPriceFn, state)
    val summary = if (applied > 0) show(newState, mdPriceFn) else Vector.empty[String]
    (newState, msgs ++ summary)
  }

  def previewPlan(planOpt: Option[ExecutionPlan], mdPriceFn: String => Option[BigDecimal], state: PortfolioState): (PortfolioState, Vector[String]) = {
    val (newState, applied, msgs) = purePreviewPlan(planOpt, mdPriceFn, state)
    val previewLines =
      if (applied > 0) {
        val header = Vector("Resulting portfolio:")
        val body = if (newState.positions.isEmpty) Vector(" - (empty)") else newState.positions.toSeq.sortBy(_._1).map { case (s, q) => s" - $s: qty=${fmt(q)}" }.toVector
        header ++ body :+ s" - cash: ${fmt(newState.cash)} ${newState.currency}"
      } else Vector.empty[String]
    (state, msgs ++ previewLines)
  }

  // No stateful API: PortfolioManager is intentionally stateless. Use
  // `pureApplyPlan`/`applyPlan(plan, mdPriceFn, state)` for functional usage.
}
