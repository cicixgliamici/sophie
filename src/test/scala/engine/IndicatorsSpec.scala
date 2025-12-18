package engine

import org.scalatest.funsuite.AnyFunSuite
import scala.math.BigDecimal

// Tests for basic indicator functions via Indicators.compute
// - Verify SMA on exact window
// - Ensure EMA computes without error on a flat series
// - Check RSI behavior on strictly increasing series (should be 100)
// - STDDEV of identical numbers should be 0
class IndicatorsSpec extends AnyFunSuite {

  test("sma on exact n points returns mean") {
    val xs = Vector(BigDecimal(1), BigDecimal(2), BigDecimal(3))
    val res = Indicators.compute("MAVG", xs, 3)
    assert(res == BigDecimal(2))
  }

  test("ema handles minimal case and doesn't blow up") {
    val xs = Vector(BigDecimal(10), BigDecimal(10), BigDecimal(10), BigDecimal(10))
    val res = Indicators.compute("EMA", xs, 3)
    assert(res.isInstanceOf[BigDecimal])
  }

  test("rsi returns 100 on strictly increasing series") {
    val xs = Vector(BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(4), BigDecimal(5), BigDecimal(6))
    val res = Indicators.compute("RSI", xs, 3)
    assert(res == BigDecimal(100))
  }

  test("stddev of identical numbers is zero") {
    val xs = Vector.fill(5)(BigDecimal(4))
    val res = Indicators.compute("STDDEV", xs, 5)
    assert(res == BigDecimal(0))
  }
}
