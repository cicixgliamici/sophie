import org.scalatest.funsuite.AnyFunSuite
import util.SLF4JLogger

class LoggerSpec extends AnyFunSuite {
  test("SLF4JLogger info/warn/error do not throw") {
    SLF4JLogger.info("test info")
    SLF4JLogger.warn("test warn")
    SLF4JLogger.error("test error")
    SLF4JLogger.error("test error with throwable", new RuntimeException("boom"))
    assert(true) // If no exception thrown, test passes
  }
}

