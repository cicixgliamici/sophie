package util

import org.slf4j.LoggerFactory

object SLF4JLogger extends Logger {
  private val logger = LoggerFactory.getLogger("sophie")
  override def info(msg: String): Unit = logger.info(msg)
  override def warn(msg: String): Unit = logger.warn(msg)
  override def error(msg: String): Unit = logger.error(msg)
  override def error(msg: String, t: Throwable): Unit = logger.error(msg, t)
}

