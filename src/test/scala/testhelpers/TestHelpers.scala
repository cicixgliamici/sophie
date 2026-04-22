package testhelpers

import frontend.TuiPrinter
import scala.math.BigDecimal
import ast._
import engine._

object TestHelpers {
  object DummyPrinter extends TuiPrinter { def printlnLine(s: String): Unit = () }

  def mkTradeDecision(action: TradeAction, sym: String, amount: BigDecimal, currency: String, shouldExecute: Boolean = true): TradeDecision = {
    val cmd = TradeCmd(action = action, consideration = ByValue(Value(amount, currency)), symbol = sym, condition = AlwaysTrue)
    TradeDecision(cmd, shouldExecute, s"${if(action==Buy) "BUY" else "SELL"} $amount $currency OF $sym")
  }
}
