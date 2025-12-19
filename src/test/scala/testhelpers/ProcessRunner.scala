package testhelpers

import java.nio.file.Path
import java.io.{InputStream, BufferedReader, InputStreamReader}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

case class ProcessResult(exitCode: Int, stdout: String, stderr: String)

object ProcessRunner {
  def runJavaMain(mainClass: String, args: Seq[String], cwd: Option[Path] = None, timeout: Duration = 30.seconds): ProcessResult = {
    val javaBin = sys.props.get("java.home").map(p => s"$p${java.io.File.separator}bin${java.io.File.separator}java").getOrElse("java")
    val classpath = System.getProperty("java.class.path")
    val cmd = Seq(javaBin, "-cp", classpath, mainClass) ++ args
    // Diagnostic to stderr so sbt shows it in test logs
    Console.err.println(s"[ProcessRunner] CMD: ${cmd.mkString(" ")}")
    Console.err.println(s"[ProcessRunner] CLASSPATH: $classpath")
    val pb = new ProcessBuilder(cmd.asJava)
    cwd.foreach(p => pb.directory(p.toFile))
    val process = pb.start()

    val outF = Future { readStream(process.getInputStream) }
    val errF = Future { readStream(process.getErrorStream) }

    val finished = process.waitFor(timeout.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
    if (!finished) {
      process.destroyForcibly()
      throw new RuntimeException("Process timed out")
    }
    val code = process.exitValue()
    val out = Await.result(outF, 5.seconds)
    val err = Await.result(errF, 5.seconds)
    ProcessResult(code, out, err)
  }

  private def readStream(is: InputStream): String = {
    val br = new BufferedReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))
    val sb = new StringBuilder
    var line = br.readLine()
    while (line != null) {
      sb.append(line).append(System.lineSeparator())
      line = br.readLine()
    }
    sb.toString()
  }
}
