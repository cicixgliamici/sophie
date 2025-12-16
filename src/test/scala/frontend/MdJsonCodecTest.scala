package frontend

import org.scalatest.funsuite.AnyFunSuite
import engine._
import upickle.default._

class MdJsonCodecTest extends AnyFunSuite {

  test("Round-trip: InMemoryMarketData -> JSON -> InMemoryMarketData") {
    val md0 = InMemoryMarketData(
      prices = Map("A" -> BigDecimal("1.23"), "B" -> BigDecimal("45")),
      seriesData = Map(("A","close") -> Vector[BigDecimal](1,2,3)),
      indicatorOverrides = Map(IndicatorKey("RSI","A",14) -> BigDecimal(55))
    )
    val j  = MdJsonCodec.toJ(md0)
    val md1 = MdJsonCodec.fromJ(j)
    assert(md1 == md0)
  }

  test("BigDecimal accepts both number and string in JSON") {
    // manually craft a MarketDataJ that would be encoded with strings,
    // then decode back and ensure values are preserved.
    val j = MdJsonCodec.MarketDataJ(
      prices = Map("A" -> BigDecimal("1.50")), // will be written as string
      series = Map("A.close" -> Vector(BigDecimal("10.0"), BigDecimal(11))),
      indicatorOverrides = Seq(MdJsonCodec.IndicatorOverrideJ("EMA","A",3, BigDecimal("2.25")))
    )
    val jsonStr = write(j)               // -> JSON (values as strings due to custom RW)
    val jBack   = read[MdJsonCodec.MarketDataJ](jsonStr)
    val md      = MdJsonCodec.fromJ(jBack)
    assert(md.prices("A") == BigDecimal("1.50"))
    assert(md.series("A","close").exists(_.last == BigDecimal(11)))
    assert(md.indicatorOverrides(IndicatorKey("EMA","A",3)) == BigDecimal("2.25"))
  }

  test("Invalid series key format (no dot) raises a clear error") {
    val bad = MdJsonCodec.MarketDataJ(
      prices = Map.empty,
      series = Map("Aclose" -> Vector(BigDecimal(1))), // invalid: missing "A.close"
      indicatorOverrides = Seq.empty
    )
    assertThrows[IllegalArgumentException] {
      MdJsonCodec.fromJ(bad) // require(dot > 0 && dot < k.length - 1, ...)
    }
  }
}