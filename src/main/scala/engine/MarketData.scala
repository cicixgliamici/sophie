package engine

import scala.collection.immutable.Map

final case class IndicatorKey(name: String, symbol: String, period: Int)

/**
  * MarketData
  * ----------
  * This trait abstracts access to market data for the evaluation engine.
  *
  * Purpose:
  *   - Provides methods to retrieve spot prices, historical series, and indicator overrides.
  *   - Allows the evaluation logic to be decoupled from the underlying data source.
  *
  * Structure:
  *   - Defines methods for price lookup, time series retrieval, and indicator overrides.
  *   - Includes a convenience method for fetching the latest value in a series.
  *
  * Why this layer?
  *   - Enables testing and simulation by allowing different implementations (e.g., in-memory, live feeds).
  *   - Keeps the evaluation logic independent from data storage and retrieval details.
  */
trait MarketData {
  /** Spot price (e.g., from a quote feed). */
  def price(symbol: String): Option[BigDecimal]

  /** Historical series for a given field (e.g., "close", "volume"). */
  def series(symbol: String, field: String): Option[Vector[BigDecimal]]

  /** Optional overrides for indicators (useful in tests/demos). */
  def indicatorOverride(name: String, symbol: String, period: Int): Option[BigDecimal] = None

  /** Convenience: latest datapoint for a series field. */
  final def latest(symbol: String, field: String): Option[BigDecimal] =
    series(symbol, field).flatMap(_.lastOption)
}

/**
  * InMemoryMarketData
  * ------------------
  * Simple in-memory implementation of MarketData, useful for tests and demos.
  *
  * Structure:
  *   - Stores prices, series data, and indicator overrides in immutable maps.
  *   - All data is provided at construction and does not change at runtime.
  *
  * Peculiarities:
  *   - Indicator overrides are looked up case-insensitively for the indicator name.
  *   - Efficient for small datasets and unit testing.
  */
final case class InMemoryMarketData(
                                     prices: Map[String, BigDecimal] = Map.empty,
                                     seriesData: Map[(String, String), Vector[BigDecimal]] = Map.empty,
                                     indicatorOverrides: Map[IndicatorKey, BigDecimal] = Map.empty
                                   ) extends MarketData {
  def price(symbol: String): Option[BigDecimal] = prices.get(symbol)
  def series(symbol: String, field: String): Option[Vector[BigDecimal]] = seriesData.get(symbol -> field)
  override def indicatorOverride(name: String, symbol: String, period: Int): Option[BigDecimal] =
    indicatorOverrides.get(IndicatorKey(name.toUpperCase, symbol, period))
}
