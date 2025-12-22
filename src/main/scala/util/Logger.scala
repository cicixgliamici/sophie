package util

trait Logger {
  def info(msg: String): Unit
  def warn(msg: String): Unit
  def error(msg: String): Unit
  def error(msg: String, t: Throwable): Unit
}

