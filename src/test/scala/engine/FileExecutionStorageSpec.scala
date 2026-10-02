package engine

import ast._
import java.nio.file.{Files, Path}
import java.time.{Clock, Instant, ZoneOffset}
import org.scalatest.funsuite.AnyFunSuite
import upickle.default.{read, write}

class FileExecutionStorageSpec extends AnyFunSuite {
  private val initial = PortfolioState(Map("A" -> BigDecimal(1)), BigDecimal(100))
  private val market = InMemoryMarketData(prices = Map("A" -> BigDecimal(10)))
  private val order = Instruction("buy", Buy, "A", BigDecimal(2), None, "test")
  private val event = LedgerEvent(123, Buy, "A", BigDecimal(2), BigDecimal(10), BigDecimal(20), "test", "test")

  private def withStores(test: (FileJsonPortfolioStore, FileLedger) => Unit): Unit = {
    val directory = Files.createTempDirectory("sophie-transaction-")
    try test(FileJsonPortfolioStore(directory.resolve("portfolio.json")), FileLedger(directory.resolve("ledger.ndjson")))
    finally {
      val paths = Files.walk(directory)
      try paths.sorted(java.util.Comparator.reverseOrder()).forEach(path => Files.deleteIfExists(path))
      finally paths.close()
    }
  }

  test("execution equals preview and preserves the existing ledger with an injected clock") {
    withStores { (portfolio, ledger) =>
      portfolio.save(initial)
      ledger.append(event)
      val expected = Executor.preview(List(order), market, initial).toOption.get.portfolio
      val clock = Clock.fixed(Instant.ofEpochMilli(456), ZoneOffset.UTC)
      val events = Executor.run(List(order), market, portfolio, ledger, clock = clock)
      assert(portfolio.load() == expected)
      assert(events.head.ts == 456)
      assert(ledger.readAll() == Vector(event, events.head))
    }
  }

  test("failure during the second target write restores both original files") {
    withStores { (portfolio, ledger) =>
      portfolio.save(initial)
      ledger.append(event)
      val oldPortfolio = Files.readString(portfolio.path)
      val oldLedger = Files.readString(ledger.path)
      val failLedger: (Path, String) => Unit = (path, text) => {
        if (path == ledger.path.toAbsolutePath.normalize()) throw new java.io.IOException("Injected ledger failure")
        FileExecutionStorage.writeAtomically(path, text)
      }
      intercept[java.io.IOException] {
        FileExecutionStorage.withLock(portfolio.path, ledger.path) {
          FileExecutionStorage.commit(portfolio, ledger, initial.copy(cash = BigDecimal(80)), List(event), failLedger)
        }
      }
      assert(Files.readString(portfolio.path) == oldPortfolio)
      assert(Files.readString(ledger.path) == oldLedger)
      assert(!Files.exists(portfolio.path.resolveSibling("portfolio.json.transaction.json")))
    }
  }

  test("failure on a new account removes partially created files") {
    withStores { (portfolio, ledger) =>
      val failLedger: (Path, String) => Unit = (path, text) => {
        if (path == ledger.path.toAbsolutePath.normalize()) throw new java.io.IOException("Injected failure")
        FileExecutionStorage.writeAtomically(path, text)
      }
      intercept[java.io.IOException] {
        FileExecutionStorage.withLock(portfolio.path, ledger.path) {
          FileExecutionStorage.commit(portfolio, ledger, initial, List(event), failLedger)
        }
      }
      assert(!Files.exists(portfolio.path) && !Files.exists(ledger.path))
    }
  }

  test("a leftover journal restores an interrupted batch before another execution") {
    withStores { (portfolio, ledger) =>
      portfolio.save(initial)
      ledger.append(event)
      val oldPortfolio = Files.readString(portfolio.path)
      val oldLedger = Files.readString(ledger.path)
      val backup = ujson.Obj("portfolioPath" -> portfolio.path.toAbsolutePath.normalize().toString,
        "ledgerPath" -> ledger.path.toAbsolutePath.normalize().toString,
        "portfolio" -> ujson.Arr(oldPortfolio), "ledger" -> ujson.Arr(oldLedger))
      val journal = portfolio.path.resolveSibling("portfolio.json.transaction.json")
      Files.writeString(journal, backup.render())
      portfolio.save(initial.copy(cash = BigDecimal(80)))
      ledger.append(event)
      FileExecutionStorage.withLock(portfolio.path, ledger.path) {
        assert(portfolio.load() == initial)
        assert(ledger.readAll() == Vector(event))
      }
      assert(!Files.exists(journal))
    }
  }

  test("resetting an account is not persisted if the batch is rejected") {
    withStores { (portfolio, ledger) =>
      portfolio.save(initial)
      ledger.append(event)
      intercept[IllegalStateException] {
        Executor.run(List(order), market, portfolio, ledger,
          initialPortfolio = Some(PortfolioState(Map.empty, BigDecimal(0))))
      }
      assert(portfolio.load() == initial && ledger.readAll() == Vector(event))
    }
  }

  test("a second execution cannot enter the same account transaction") {
    withStores { (portfolio, ledger) =>
      FileExecutionStorage.withLock(portfolio.path, ledger.path) {
        intercept[java.nio.channels.OverlappingFileLockException] {
          Executor.run(List(order), market, portfolio, ledger)
        }
      }
    }
  }
}
