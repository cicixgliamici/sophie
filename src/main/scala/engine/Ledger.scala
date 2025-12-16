package engine

import upickle.default._
import java.nio.file.{Files, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets.UTF_8

final case class LedgerEvent(
                              ts: Long,
                              action: ast.TradeAction,
                              symbol: String,
                              qty: BigDecimal,
                              price: BigDecimal,
                              notional: BigDecimal,
                              source: String,
                              note: String
                            )
object LedgerEvent {
  implicit val tradeActionRW: ReadWriter[ast.TradeAction] =
    readwriter[String].bimap[ast.TradeAction](
      { case ast.Buy => "BUY"; case ast.Sell => "SELL" },
      { case "BUY" => ast.Buy; case "SELL" => ast.Sell; case x => throw new Exception(s"Unknown: $x") }
    )
  implicit val rw: ReadWriter[LedgerEvent] = macroRW
}

trait Ledger {
  def append(e: LedgerEvent): Unit
  def readAll(): Vector[LedgerEvent]
}

final case class FileLedger(path: Path) extends Ledger {
  override def append(e: LedgerEvent): Unit = {
    val line = write(e) + "\n" // NDJSON
    Files.writeString(path, line, UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }
  override def readAll(): Vector[LedgerEvent] = {
    if (!Files.exists(path)) return Vector.empty
    import scala.jdk.CollectionConverters._
    Files.readAllLines(path, UTF_8).asScala
      .filter(_.trim.nonEmpty).toVector.map(read[LedgerEvent])
  }
}
