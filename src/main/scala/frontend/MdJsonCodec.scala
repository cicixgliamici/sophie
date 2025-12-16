/**
 * MdJsonCodec provides JSON serialization and deserialization utilities for in-memory market data.
 *
 * This object defines compact, human-friendly JSON schemas for market data, including prices, time series,
 * and indicator overrides. It uses uPickle for (de)serialization and handles conversions between the
 * internal InMemoryMarketData representation and its JSON form.
 *
 * Key features:
 *  - Custom ReadWriter for BigDecimal to support both numeric and string JSON representations.
 *  - Flattens series data as "SYMBOL.field" -> [values...] for simplicity.
 *  - Converts between internal and JSON case classes for indicator overrides and market data.
 *  - Ensures schema is concise and easy to edit by hand.
 */
package frontend

import engine._
import upickle.default._
import ujson._

/** JSON schema kept tiny and human-friendly.
 * series is stored as "SYMBOL.field" -> [values...]
 */
object MdJsonCodec {

  // Accetta sia "123.45" (string) sia 123.45 (number) e scrive come stringa
  implicit val bigDecimalRW: ReadWriter[BigDecimal] =
    readwriter[ujson.Value].bimap[BigDecimal](
      bd => ujson.Str(bd.toString),
      {
        case ujson.Str(s) => BigDecimal(s)
        case ujson.Num(n) => BigDecimal(n)
        case x            => throw new Exception(s"Expected number or numeric string, got: $x")
      }
    )

  // ----- JSON shapes -----
  final case class IndicatorOverrideJ(name: String, symbol: String, period: Int, value: BigDecimal)
  final case class MarketDataJ(
                                prices: Map[String, BigDecimal],
                                series: Map[String, Vector[BigDecimal]],
                                indicatorOverrides: Seq[IndicatorOverrideJ]
                              )

  implicit val ovRw: ReadWriter[IndicatorOverrideJ] = macroRW
  implicit val mdRw: ReadWriter[MarketDataJ]        = macroRW

  // ----- Conversions -----
  def toJ(md: InMemoryMarketData): MarketDataJ =
    MarketDataJ(
      prices = md.prices,
      series = md.seriesData.map { case ((sym, field), vec) => s"$sym.$field" -> vec },
      indicatorOverrides = md.indicatorOverrides.toSeq.map {
        case (IndicatorKey(n, s, p), v) => IndicatorOverrideJ(n, s, p, v)
      }
    )

  def fromJ(j: MarketDataJ): InMemoryMarketData = {
    val seriesData =
      j.series.map { case (k, vec) =>
        val dot = k.lastIndexOf('.')
        require(dot > 0 && dot < k.length - 1, s"Invalid series key: $k (expected SYMBOL.field)")
        (k.substring(0, dot), k.substring(dot + 1)) -> vec
      }
    val overrides =
      j.indicatorOverrides.map(o => IndicatorKey(o.name.toUpperCase, o.symbol, o.period) -> o.value).toMap

    InMemoryMarketData(
      prices = j.prices,
      seriesData = seriesData,
      indicatorOverrides = overrides
    )
  }
}