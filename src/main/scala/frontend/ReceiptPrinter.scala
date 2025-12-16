package frontend

import engine.LedgerEvent
import ast._
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.nio.file.{Files, Paths, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets.UTF_8

object ReceiptPrinter {

  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())

  /**
    * Stampa una "ricevuta" leggibile per gli eventi eseguiti e opzionalmente la salva in file (append).
    * Se `saveTo` è Some(path) scrive in append nel file specificato.
    */
  def printReceipts(events: List[LedgerEvent], saveTo: Option[Path] = None): Unit = {
    if (events.isEmpty) return

    // Calcola larghezze colonne per allineamento
    def s(v: BigDecimal): String = v.bigDecimal.stripTrailingZeros.toPlainString
    val rows = events.map { e =>
      val action = e.action match { case ast.Buy => "Comprato"; case ast.Sell => "Venduto" }
      val qty = s(e.qty)
      val price = s(e.price)
      val total = s(e.notional)
      val when = fmt.format(Instant.ofEpochMilli(e.ts))
      (action, e.symbol, qty, price, total, when)
    }

    val actionW = (rows.map(_._1.length) :+ "Azione".length).max
    val symW    = (rows.map(_._2.length) :+ "Simbolo".length).max
    val qtyW    = (rows.map(_._3.length) :+ "Quantità".length).max
    val pxW     = (rows.map(_._4.length) :+ "Prezzo".length).max
    val totW    = (rows.map(_._5.length) :+ "Totale".length).max

    val header = f"%-${actionW}s  %- ${symW}s  %${qtyW}s  %${pxW}s  %${totW}s  %s".format("Azione","Simbolo","Quantità","Prezzo","Totale","Ora")

    val bodyLines = rows.map { case (action, symbol, qty, price, total, when) =>
      f"%-${actionW}s  %- ${symW}s  %${qtyW}s  %${pxW}s  %${totW}s  %s".format(action, symbol, qty, price, total, when)
    }

    // Print to stdout
    println("\n=== Ricevuta ordini eseguiti ===")
    println(header)
    bodyLines.foreach(println)
    println()

    // Optionally append to file
    saveTo.foreach { path =>
      try {
        val all = (Seq(header) ++ bodyLines).mkString("\n") + "\n"
        if (!Files.exists(path)) {
          val parent = path.getParent
          if (parent != null && !Files.exists(parent)) Files.createDirectories(parent)
          Files.writeString(path, all, UTF_8, StandardOpenOption.CREATE)
        } else {
          Files.writeString(path, all, UTF_8, StandardOpenOption.APPEND)
        }
      } catch { case e: Exception =>
        System.err.println(s"Warning: unable to write receipts to ${path.toString}: ${e.getMessage}")
      }
    }
  }
}
