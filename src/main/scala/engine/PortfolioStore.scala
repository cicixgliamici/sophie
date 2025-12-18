package engine

import upickle.default._
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets.UTF_8

/**
  * Minimal persistence interface used by the engine to store portfolio state.
  *
  * Abstracting behind this trait keeps the executor/test code agnostic to the
  * concrete storage mechanism (file system, in‑memory, etc.).
  */
trait PortfolioStore {
  /** Load the persisted portfolio (or an empty/default one if none exists). */
  def load(): PortfolioState

  /** Persist the provided portfolio snapshot. */
  def save(pf: PortfolioState): Unit
}

/** In‑memory representation of a portfolio used across the app and tests. */
final case class PortfolioState(positions: Map[String, BigDecimal], cash: BigDecimal) {
  /** Ensure lookups for missing symbols default to zero rather than throwing. */
  def withDefaults: PortfolioState = copy(positions = positions.withDefaultValue(BigDecimal(0)))

  /** Drop zero/negative positions so persisted files stay compact. */
  def onlyPositivePositions: PortfolioState = copy(positions = positions.filter(_._2 > 0))
}

/**
  * File-backed JSON implementation of [[PortfolioStore]].
  *
  * Keeps the on-disk format in sync with the TUI/CLI by reusing the
  * `PortfolioJson` codecs, and ensures directories are created lazily by the
  * caller (tests typically do this).
  */
final case class FileJsonPortfolioStore(path: Path) extends PortfolioStore {
  import frontend.PortfolioJson._

  override def load(): PortfolioState =
    if (!Files.exists(path)) PortfolioState(Map.empty.withDefaultValue(BigDecimal(0)), BigDecimal(0))
    else {
      val json = Files.readString(path, UTF_8)
      val pj   = read[PortfolioJ](json)
      PortfolioState(
        positions = pj.positions.withDefaultValue(BigDecimal(0)),
        cash = pj.cash.getOrElse(BigDecimal(0))
      )
    }

  override def save(pf: PortfolioState): Unit = {
    val json = write(PortfolioJ(pf.positions.filter(_._2 > 0), Some(pf.cash)), indent = 2)
    Files.writeString(path, json, UTF_8)
  }
}
