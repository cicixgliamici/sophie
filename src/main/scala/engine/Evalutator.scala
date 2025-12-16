package engine

import ast._
import scala.math.BigDecimal.RoundingMode

/**
  * Evaluator
  * ---------
  * This object is responsible for evaluating a Sophie AST program against a snapshot of MarketData.
  *
  * Purpose:
  *   - Traverses the AST and computes the result of each statement (trades and portfolio allocations).
  *   - Decides whether trade commands should be executed based on their conditions.
  *   - Computes indicator values (e.g., SMA, EMA, RSI) as needed for expressions.
  *
  * Structure:
  *   - The main entry point is `evaluate`, which processes all statements in the program.
  *   - Helper methods recursively evaluate conditions and arithmetic expressions.
  *   - Contains a nested `Indicators` object for basic financial indicator calculations.
  *
  * Why another layer?
  *   - Decouples the AST structure from the evaluation logic and market data access.
  *   - Centralizes all runtime logic for executing Sophie programs.
  *   - Makes it easier to test, extend, and maintain the evaluation semantics.
  */

final case class TradeDecision(cmd: TradeCmd, shouldExecute: Boolean, detail: String)
final case class PortfolioPlan(allocations: List[Allocation])
final case class ExecutionPlan(trades: List[TradeDecision], portfolio: Option[PortfolioPlan])

object Evaluator {

  /** Top-level entry: evaluate all statements. */
  def evaluate(program: Program, md: MarketData): ExecutionPlan = {
    // Evaluate all trade commands and collect their execution decisions
    val tradeDecisions = program.statements.collect { case t: TradeCmd => decide(t, md) }
    // Only one portfolio command is allowed; take the first if present
    val portfolioPlan  = program.statements.collectFirst { case p: PortfolioCmd => PortfolioPlan(p.allocations) }
    ExecutionPlan(tradeDecisions, portfolioPlan)
  }

  /** Decide whether a trade should execute (i.e., IF condition holds). */
  private def decide(cmd: TradeCmd, md: MarketData): TradeDecision = {
    val ok = evalCondition(cmd.condition, md)
    val what = cmd.action match { case Buy => "BUY"; case Sell => "SELL" }
    val reason =
      if (ok) s"$what ${cmd.value.amount} ${cmd.value.currency} OF ${cmd.symbol} — condition met"
      else    s"$what ${cmd.value.amount} ${cmd.value.currency} OF ${cmd.symbol} — condition NOT met"
    TradeDecision(cmd, ok, reason)
  }

  // -------------------------
  // Conditions and operands
  // -------------------------

  /**
    * Recursively evaluates a boolean condition in the AST.
    * Supports logical operators and parenthesis.
    */
  private def evalCondition(c: Condition, md: MarketData): Boolean = c match {
    case Comparison(l, op, r) =>
      val lv = evalOperand(l, md)
      val rv = evalOperand(r, md)
      // Compare the two operands using the specified comparison operator
      op match {
        case GT  => lv >  rv
        case LT  => lv <  rv
        case EQ  => lv == rv
        case NEQ => lv != rv
      }
    case And(a, b)   => evalCondition(a, md) && evalCondition(b, md)
    case Or(a, b)    => evalCondition(a, md) || evalCondition(b, md)
    case Parens(cc)  => evalCondition(cc, md)
  }

  /**
    * Recursively evaluates an arithmetic operand in the AST.
    * Supports literals, price lookups, series operations, indicators, and arithmetic expressions.
    */
  private def evalOperand(o: Operand, md: MarketData): BigDecimal = o match {
    case NumberLiteral(v)                 => v
    case Price(sym)                       => md.price(sym).getOrElse(err(s"Missing PRICE($sym)"))
    case SeriesOperation(sym, field)      => md.latest(sym, field).getOrElse(err(s"Missing $sym.$field"))
    case AggFunc(name, symbol, periodDec) =>
      val n = toIntExactPeriod(periodDec, s"$name($symbol, $periodDec)")
      // Prefer explicit override; otherwise compute from CLOSE series
      md.indicatorOverride(name, symbol, n).getOrElse {
        val closes = md.series(symbol, "close").getOrElse(err(s"$name($symbol, $n) needs $symbol.close series"))
        Indicators.compute(name, closes, n)
      }
    case Binary(op, l, r) =>
      val a = evalOperand(l, md)
      val b = evalOperand(r, md)
      // Evaluate the arithmetic operation
      op match {
        case Add => a + b
        case Sub => a - b
        case Mul => a * b
        case Div =>
          if (b == 0) err("Division by zero")
          else (a / b).setScale(10, RoundingMode.HALF_UP) // keep numeric stability
      }
  }

  /** Throws an exception with the given message. Used for error handling. */
  private def err[T](msg: String): T = throw new IllegalStateException(msg)

  /**
    * Converts a BigDecimal to Int only if it's an exact integer and in Int range.
    * Used for indicator periods.
    */
  private def toIntExactPeriod(bd: BigDecimal, label: String): Int = {
    val min = BigDecimal(Int.MinValue)
    val max = BigDecimal(Int.MaxValue)
    // isWhole is available on scala.math.BigDecimal
    if (bd.isWhole && bd >= min && bd <= max) bd.toInt
    else err(s"$label: period must be an integer within Int range")
  }

}

/**
  * Indicators
  * ----------
  * Provides basic indicator implementations over close prices.
  * Used by the evaluator to compute values for expressions like SMA, EMA, STDDEV, RSI.
  */
object Indicators {

  /**
    * Dispatches to the correct indicator implementation based on the name.
    * All indicator names are case-insensitive.
    */
  def compute(name: String, closes: Vector[BigDecimal], n: Int): BigDecimal =
    name.toUpperCase match {
      case "MAVG"  => sma(closes, n)
      case "EMA"   => ema(closes, n)
      case "STDDEV"=> stddev(closes, n)
      case "RSI"   => rsi(closes, n)
      case other   => throw new IllegalArgumentException(s"Unknown indicator: $other")
    }

  /** Returns the last n elements of the vector, or throws if not enough data. */
  private def tailN(xs: Vector[BigDecimal], n: Int): Vector[BigDecimal] =
    if (xs.length >= n) xs.takeRight(n) else throw new IllegalArgumentException(s"Need $n points, got ${xs.length}")

  /** Computes the mean of a vector of numbers. */
  private def mean(xs: Vector[BigDecimal]): BigDecimal =
    if (xs.isEmpty) BigDecimal(0) else xs.sum / BigDecimal(xs.length)

  /** Simple Moving Average over last n closes. */
  def sma(closes: Vector[BigDecimal], n: Int): BigDecimal =
    mean(tailN(closes, n))

  /**
    * Exponential Moving Average (K = 2/(n+1)). Seeded with SMA of first n.
    * Uses a standard EMA formula, starting with the SMA as the initial value.
    */
  def ema(closes: Vector[BigDecimal], n: Int): BigDecimal = {
    if (closes.length < n) throw new IllegalArgumentException(s"Need $n points, got ${closes.length}")
    val alpha = 2.0 / (n + 1)
    val seed  = sma(closes.take(n), n).toDouble
    val emaSeq = closes.drop(n).foldLeft(seed) { (prev, price) =>
      alpha * price.toDouble + (1 - alpha) * prev
    }
    BigDecimal(emaSeq)
  }

  /**
    * Population standard deviation of last n closes.
    * Uses the population formula (not sample).
    */
  def stddev(closes: Vector[BigDecimal], n: Int): BigDecimal = {
    val xs  = tailN(closes, n)
    val mu  = mean(xs)
    val varPop = xs.map(x => (x - mu) * (x - mu)).sum / BigDecimal(xs.length)
    sqrtBD(varPop)
  }

  /**
    * Wilder's RSI over last n closes.
    * Implements the standard RSI calculation with Wilder's smoothing.
    */
  def rsi(closes: Vector[BigDecimal], n: Int): BigDecimal = {
    if (closes.length <= n) throw new IllegalArgumentException(s"Need > $n points, got ${closes.length}")
    val deltas = closes.sliding(2).toVector.collect { case Vector(a,b) => b - a }
    val gains  = deltas.map(d => if (d > 0) d else BigDecimal(0))
    val losses = deltas.map(d => if (d < 0) -d else BigDecimal(0))

    // Wilder smoothing: start with SMA over first n
    val avgGain0 = mean(gains.take(n))
    val avgLoss0 = mean(losses.take(n))

    def smooth(prevGain: BigDecimal, prevLoss: BigDecimal, dGain: BigDecimal, dLoss: BigDecimal): (BigDecimal, BigDecimal) = {
      val g = (prevGain * (n - 1) + dGain) / n
      val l = (prevLoss * (n - 1) + dLoss) / n
      (g, l)
    }

    val (avgGain, avgLoss) = gains.drop(n).zip(losses.drop(n)).foldLeft((avgGain0, avgLoss0)) {
      case ((gPrev, lPrev), (g, l)) => smooth(gPrev, lPrev, g, l)
    }

    if (avgLoss == 0) BigDecimal(100)
    else {
      val rs  = avgGain / avgLoss
      val rsi = 100 - (100 / (1 + rs))
      rsi
    }
  }

  /**
    * Poor man's sqrt for BigDecimal — good enough for risk metrics (10 iterations).
    * Uses Newton's method for square root approximation.
    */
  private def sqrtBD(x: BigDecimal, scale: Int = 10): BigDecimal = {
    if (x <= 0) return BigDecimal(0)
    var guess = BigDecimal(Math.sqrt(x.toDouble))
    val two = BigDecimal(2)
    for (_ <- 0 until 10) guess = (guess + x / guess) / two
    guess.setScale(scale, RoundingMode.HALF_UP)
  }
}
