package frontend

trait TuiPrinter {
  def printlnLine(s: String): Unit
}

object DefaultPrinter extends TuiPrinter {
  // Thin wrapper around println so tests can swap in a buffered printer
  // without touching the rest of the TUI code.
  override def printlnLine(s: String): Unit = Predef.println(s)
}
