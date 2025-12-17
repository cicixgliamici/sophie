package engine

import upickle.default._
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets.UTF_8

trait PortfolioStore {
  def load(): PortfolioState
  def save(pf: PortfolioState): Unit
}

final case class PortfolioState(positions: Map[String, BigDecimal], cash: BigDecimal) {
  def withDefaults: PortfolioState = copy(positions = positions.withDefaultValue(BigDecimal(0)))
  def onlyPositivePositions: PortfolioState = copy(positions = positions.filter(_._2 > 0))
}

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
