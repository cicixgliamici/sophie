package frontend

import upickle.default._
import frontend.MdJsonCodec.bigDecimalRW // riusa il codec BigDecimal tollerante

object PortfolioJson {
  final case class PortfolioJ(
      positions: Map[String, BigDecimal],
      cash: Option[BigDecimal] = None // optional for backward compatibility
  )
  implicit val pfRw: ReadWriter[PortfolioJ] = macroRW
}
