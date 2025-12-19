package testhelpers

object NoExit {
  /**
    * Best-effort placeholder for intercepting System.exit in tests. Modern JVMs
    * often prohibit installing a custom SecurityManager; handling the full
    * interception reliably across JVM versions requires low-level APIs and
    * is environment-specific.
    *
    * To keep tests portable we intentionally throw UnsupportedOperationException
    * here; tests that need to be robust should accept that exception as a
    * valid environment-specific outcome (see usages in `SophieCliIntegrationSpec`).
    */
  def withNoExit[A](block: => A): A = {
    throw new UnsupportedOperationException("withNoExit not available in this JVM")
  }
}
