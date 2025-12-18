import org.scalatest.funsuite.AnyFunSuite
import frontend.SophieParserFacade
import ast._
import engine._

class MainSpec extends AnyFunSuite {
  test("Main.main runs sample without throwing") {
    // Main.main prints output; calling it ensures sample path is exercised
    Main.main(Array.empty)
    assert(true) // if no exception, success
  }
}

