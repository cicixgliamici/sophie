package frontend

trait TuiPrinter {
  def printlnLine(s: String): Unit
}

object DefaultPrinter extends TuiPrinter {
  override def printlnLine(s: String): Unit = Predef.println(s)
}

