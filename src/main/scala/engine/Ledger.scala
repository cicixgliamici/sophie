package engine

import upickle.default._
import java.nio.file.{Files, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets.UTF_8

/**
  * Ledger
  * ------
  * Small persistence layer that records executed trades as append-only events.
  * Keeping this layer narrow makes it easy to swap file storage for a database
  * later without touching the execution engine.
  *
  * Tips for readers:
  *   - Events are stored as NDJSON for easy streaming and grepping.
  *   - `Ledger` is an interface; `FileLedger` is a minimal default implementation
  *     used by the TUI and tests. Swapping in-memory or cloud-backed ledgers only
  *     requires implementing the two methods.
  */
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

/**
  * FileLedger
  * ----------
  * Default implementation of `Ledger` that persists events to a file as NDJSON.
  * Each event is appended to the end of the file, and the file is created if it
  * doesn't exist.
  *
  * @param path Path to the file where events will be stored.
  */
final case class FileLedger(path: Path) extends Ledger {
  /**
    * Append a new event to the ledger.
    *
    * @param e The event to append.
    */
  override def append(e: LedgerEvent): Unit = {
    val line = write(e) + "\n" // NDJSON
    Files.writeString(path, line, UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }

  /**
    * Read all events from the ledger.
    *
    * @return A vector of all events in the ledger.
    */
  override def readAll(): Vector[LedgerEvent] = {
    import scala.jdk.CollectionConverters._
    if (!Files.exists(path)) Vector.empty
    else Files.readAllLines(path, UTF_8).asScala
      .filter(_.trim.nonEmpty).toVector.map(s => read[LedgerEvent](s))
  }
}
