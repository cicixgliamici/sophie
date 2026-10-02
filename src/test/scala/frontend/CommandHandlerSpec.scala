package frontend

import org.scalatest.funsuite.AnyFunSuite
import engine.InMemoryMarketData
import SophieTui.{PasteBuffer, SessionState}

/**
  * Quick smoke test for the TUI command dispatcher. The goal is to verify that
  * known portfolio commands are accepted and that unknown ones do not crash the
  * REPL loop (they should simply return true after printing a message).
  */
class CommandHandlerSpec extends AnyFunSuite {
  test("CommandHandler handles pf commands and unknown gracefully") {
    val pm = new PortfolioManager()
    val dummyActions = new TuiActions {
      private val emptyLog = Vector.empty[String]
      def help                                 = emptyLog
      def showMd(session: SessionState)        = emptyLog
      def showLast(session: SessionState)      = emptyLog
      def setPrice(session: SessionState, sym: String, v: String) = (session, emptyLog)
      def setSeries(session: SessionState, s: String, f: String, csv: String) = (session, emptyLog)
      def setOverride(session: SessionState, n: String, s: String, p: String, v: String) = (session, emptyLog)
      def loadMd(session: SessionState, path: String)                                     = (session, emptyLog)
      def saveMd(session: SessionState, path: String)                                     = (session, emptyLog)
      def runProg(session: SessionState, path: String)                                    = (session, emptyLog)
      def saveProg(session: SessionState, path: String)                                   = (session, emptyLog)
      def compileIr(session: SessionState, path: String)                                  = (session, emptyLog)
      def execIr(session: SessionState, path: String, portfolio: engine.PortfolioState) = (session, portfolio, emptyLog)
      def evalBuffer(session: SessionState, buf: PasteBuffer)                             = (session, emptyLog)
    }
    val ch = new CommandHandler(dummyActions, pm)

    val session   = SessionState(InMemoryMarketData(), None, None)
    val portfolio = pm.empty
    val buf       = PasteBuffer.empty

    assert(ch.handle(":pf new", session, portfolio, buf).continue)
    assert(ch.handle(":pf show", session, portfolio, buf).continue)
    val temporary = java.nio.file.Files.createTempFile("sophie-command-", ".json")
    try {
      assert(ch.handle(s":pf save $temporary", session, portfolio, buf).continue)
      assert(ch.handle(s":pf load $temporary", session, portfolio, buf).continue)
    } finally java.nio.file.Files.deleteIfExists(temporary)
    assert(ch.handle(":pf apply", session, portfolio, buf).continue)
    // Explaining a plan must not alter an active paste buffer or the account.
    val plan = ProgramEvaluator.evaluate("BUY QTY 1 OF A IF 1", InMemoryMarketData()).plan
    val evaluatedSession = session.copy(lastPlan = Some(plan))
    val pendingBuffer = PasteBuffer(Vector("BUY QTY 2 OF A"))
    val explained = ch.handle(":explain 1", evaluatedSession, portfolio, pendingBuffer)
    assert(explained.session == evaluatedSession && explained.portfolio == portfolio)
    assert(explained.buffer == pendingBuffer && explained.log.exists(_.contains("Trade 1:")))
    // Unknown commands should not abort the loop; they return true after printing help.
    assert(ch.handle(":unknowncmd", session, portfolio, buf).continue)
  }
}
