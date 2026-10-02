package engine

import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}
import scala.util.control.NonFatal
import upickle.default._

/** Recoverable file transactions for the local, single-account simulator. */
object FileExecutionStorage {
  private case class Backup(portfolioPath: String, ledgerPath: String,
                            portfolio: Option[String], ledger: Option[String])
  private object Backup { implicit val rw: ReadWriter[Backup] = macroRW }

  private def journalPath(portfolio: Path): Path =
    portfolio.resolveSibling(portfolio.getFileName.toString + ".transaction.json")

  private def contents(path: Path): Option[String] =
    if (Files.exists(path)) Some(Files.readString(path, UTF_8)) else None

  /** Replacing individual files prevents readers from seeing truncated JSON/NDJSON. */
  private[engine] def writeAtomically(path: Path, text: String): Unit = {
    val absolute = path.toAbsolutePath.normalize()
    Files.createDirectories(absolute.getParent)
    val temporary = Files.createTempFile(absolute.getParent, ".sophie-", ".tmp")
    try {
      Files.writeString(temporary, text, UTF_8)
      // Fail rather than silently downgrade on filesystems without atomic replacement.
      Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } finally Files.deleteIfExists(temporary)
  }

  /** The portfolio lock also excludes executions using a different ledger for this account. */
  def withLock[A](portfolio: Path, ledger: Path)(operation: => A): A = {
    val account = portfolio.toAbsolutePath.normalize()
    require(account != ledger.toAbsolutePath.normalize(), "Portfolio and ledger paths must differ")
    Files.createDirectories(account.getParent)
    val lockPath = account.resolveSibling(account.getFileName.toString + ".lock")
    val channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
    try {
      val lock = channel.tryLock()
      if (lock == null) throw new IllegalStateException("Portfolio is being executed by another process")
      try {
        recover(account, ledger.toAbsolutePath.normalize())
        operation
      } finally lock.release()
    } finally channel.close()
  }

  /** A leftover journal means the commit did not finish; restore both original files. */
  private[engine] def recover(portfolio: Path, ledger: Path): Unit = {
    val journal = journalPath(portfolio)
    if (Files.exists(journal)) {
      val backup = read[Backup](Files.readString(journal, UTF_8))
      require(backup.portfolioPath == portfolio.toAbsolutePath.normalize().toString &&
        backup.ledgerPath == ledger.toAbsolutePath.normalize().toString,
        "Pending transaction belongs to a different portfolio/ledger pair")
      restore(portfolio, backup.portfolio)
      restore(ledger, backup.ledger)
      Files.delete(journal)
    }
  }

  private def restore(path: Path, original: Option[String]): Unit = original match {
    case Some(text) => writeAtomically(path, text)
    case None => Files.deleteIfExists(path)
  }

  /** Called under the account lock, after every trade has passed validation. */
  private[engine] def commit(portfolio: FileJsonPortfolioStore, ledger: FileLedger,
             state: PortfolioState, events: List[LedgerEvent],
             persist: (Path, String) => Unit = writeAtomically): Unit = {
    val account = portfolio.path.toAbsolutePath.normalize()
    val history = ledger.path.toAbsolutePath.normalize()
    val backup = Backup(account.toString, history.toString, contents(account), contents(history))
    val previous = backup.ledger.getOrElse("")
    val separator = if (previous.nonEmpty && !previous.endsWith("\n")) "\n" else ""
    val nextLedger = previous + separator + events.map(event => write(event) + "\n").mkString
    // Publish the journal before changing either target; deletion is the commit point.
    writeAtomically(journalPath(account), write(backup))
    try {
      val snapshot = frontend.PortfolioJson.PortfolioJ(state.positions, Some(state.cash), state.currency)
      persist(account, write(snapshot, indent = 2))
      persist(history, nextLedger)
      Files.delete(journalPath(account))
    } catch {
      case NonFatal(failure) =>
        // Preserve the original error and leave the journal available if rollback also fails.
        try recover(account, history)
        catch { case NonFatal(recoveryFailure) => failure.addSuppressed(recoveryFailure) }
        throw failure
    }
  }
}
