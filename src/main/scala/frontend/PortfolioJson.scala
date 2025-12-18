package frontend

import upickle.default._
import frontend.MdJsonCodec.bigDecimalRW // riusa il codec BigDecimal tollerante

object PortfolioJson {
  /**
    * Minimal JSON schema for persisting the portfolio state on disk.
    * It intentionally mirrors the in-memory structure (positions + optional cash)
    * so that the TUI and CLI can read/write the file with very little ceremony.
    * The `cash` field is optional to stay backward compatible with older files.
    */
  final case class PortfolioJ(
      positions: Map[String, BigDecimal],
      cash: Option[BigDecimal] = None // optional for backward compatibility
  )
  implicit val pfRw: ReadWriter[PortfolioJ] = macroRW
}
