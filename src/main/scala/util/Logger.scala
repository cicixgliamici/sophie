package util

trait Logger {
  def info(msg: String): Unit
  def warn(msg: String): Unit
  def error(msg: String): Unit
  def error(msg: String, t: Throwable): Unit
}

// Note: ConsoleLogger was removed in favor of SLF4JLogger (Logback backend).
