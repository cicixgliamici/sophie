package engine

import upickle.default._
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets.UTF_8

trait PortfolioStore {
  def load(): Map[String, BigDecimal]
  def save(pf: Map[String, BigDecimal]): Unit
}

final case class FileJsonPortfolioStore(path: Path) extends PortfolioStore {
  import frontend.PortfolioJson._
  override def load(): Map[String, BigDecimal] =
    if (!Files.exists(path)) Map.empty.withDefaultValue(BigDecimal(0))
    else {
      val json = Files.readString(path, UTF_8)
      val pj   = read[PortfolioJ](json)
      pj.positions.withDefaultValue(BigDecimal(0))
    }

  override def save(pf: Map[String, BigDecimal]): Unit = {
    val json = write(PortfolioJ(pf.filter(_._2 > 0)), indent = 2)
    Files.writeString(path, json, UTF_8)
  }
}
