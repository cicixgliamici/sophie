package engine

import ast.{Buy, Sell}

/** A resolved fill is data only; preview and persistence consume the same result. */
final case class TradeFill(instruction: Instruction, price: BigDecimal) {
  // Unlimited multiplication avoids creating or losing cash through context rounding.
  def notional: BigDecimal = BigDecimal(instruction.qty.bigDecimal.multiply(price.bigDecimal))
}

final case class TradingResult(portfolio: PortfolioState, fills: Vector[TradeFill])

object TradingEngine {
  /** Validate the entire ordered batch without changing state or writing events. */
  def simulate(instructions: List[Instruction], price: String => Option[BigDecimal],
               initial: PortfolioState): Either[String, TradingResult] = {
    if (!Set("EUR", "USD", "GBP", "BTC")(initial.currency)) Left("Unsupported account currency")
    else if (initial.cash < 0 || initial.positions.values.exists(_ < 0))
      Left("Portfolio cash and positions must be non-negative")
    else instructions.foldLeft[Either[String, TradingResult]](Right(TradingResult(initial, Vector.empty))) {
      case (previous, instruction) => previous.flatMap { result =>
        resolve(instruction, price).flatMap { fill =>
          applyFill(result.portfolio, fill).map(next => TradingResult(next, result.fills :+ fill))
        }
      }
    }
  }

  private def resolve(instruction: Instruction, price: String => Option[BigDecimal]): Either[String, TradeFill] = {
    if (instruction.qty <= 0) Left(s"Quantity for ${instruction.symbol} must be positive")
    else instruction.price.orElse(price(instruction.symbol)) match {
      case None => Left(s"Missing PRICE(${instruction.symbol})")
      case Some(value) if value <= 0 => Left(s"PRICE(${instruction.symbol}) must be positive")
      case Some(value) => Right(TradeFill(instruction, value))
    }
  }

  private def applyFill(state: PortfolioState, fill: TradeFill): Either[String, PortfolioState] = {
    val order = fill.instruction
    val held = state.positions.getOrElse(order.symbol, BigDecimal(0))
    if (order.currency.exists(_ != state.currency))
      Left(s"Currency mismatch: order is ${order.currency.get}, account is ${state.currency}; FX conversion is not supported")
    else order.action match {
      case Buy if fill.notional > state.cash => Left(s"BUY ${order.symbol}: insufficient cash (need ${fill.notional}, have ${state.cash})")
      case Sell if order.qty > held => Left(s"SELL ${order.symbol}: insufficient holdings (need ${order.qty}, have $held)")
      case action =>
        // Account balances use exact addition/subtraction even for very large holdings.
        val quantity = balance(held, order.qty, subtract = action == Sell)
        val cash = balance(state.cash, fill.notional, subtract = action == Buy)
        Right(state.copy(positions = state.positions.updated(order.symbol, quantity), cash = cash).onlyPositivePositions.withDefaults)
    }
  }
  private def balance(current: BigDecimal, movement: BigDecimal, subtract: Boolean): BigDecimal =
    BigDecimal(if (subtract) current.bigDecimal.subtract(movement.bigDecimal)
      else current.bigDecimal.add(movement.bigDecimal))

}
