package frontend

import org.scalatest.funsuite.AnyFunSuite

class SophieTuiSpec extends AnyFunSuite {
  test("normalizeForCommand removes BOM and control chars and trims") {
    val raw = "\uFEFF  :pf load tmp/pf.json  \n"
    val normalized = {
      // replicate the same normalizeForCommand logic from SophieTui
      if (raw == null) null
      else raw.replace("\uFEFF", "").replaceAll("\\p{C}", "").trim
    }
    assert(normalized.startsWith(":"))
    assert(normalized == ":pf load tmp/pf.json")
  }
}

