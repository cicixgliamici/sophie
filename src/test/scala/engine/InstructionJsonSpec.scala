package engine

import org.scalatest.funsuite.AnyFunSuite
import upickle.default._

class InstructionJsonSpec extends AnyFunSuite {
  test("Instruction JSON roundtrip and TradeAction codec") {
    val inst = Instruction("id-1", ast.Buy, "MSFT", BigDecimal(2), Some(BigDecimal(100)), "note")
    val json = write(inst)
    val parsed = read[Instruction](json)
    assert(parsed.id == inst.id)
    assert(parsed.action == inst.action)
    assert(parsed.qty == inst.qty)
  }
}

