package cli

import frontend.SophieTui

object RunTuiSim {
  def main(args: Array[String]): Unit = {
    println("=== TUI Simulation 1: simple buy and apply ===")
    val inputs1 = Seq(
      ":set price MSFT 320",
      "BUY 1500 EUR OF MSFT;",
      "",
      ":pf apply"
    )
    val (pf1, plan1) = SophieTui.simulateSession(inputs1)
    println(s"Portfolio positions: $pf1")
    println(s"Last plan present: ${plan1.isDefined}")
    println()

    println("=== TUI Simulation 2: paste mode and command handling ===")
    val inputs2 = Seq(
      ":set price MSFT 100",
      "BUY 100 EUR OF MSFT;",
      ":show last",
      "",
      ":pf apply"
    )
    val (pf2, plan2) = SophieTui.simulateSession(inputs2)
    println(s"Portfolio positions: $pf2")
    println(s"Last plan present: ${plan2.isDefined}")
    println()

    println("=== TUI Simulation 3: missing price -> skip apply ===")
    val inputs3 = Seq(
      "BUY 100 EUR OF ABC;",
      "",
      ":pf preview",
      ":pf apply"
    )
    val (pf3, plan3) = SophieTui.simulateSession(inputs3)
    println(s"Portfolio positions: $pf3")
    println(s"Last plan present: ${plan3.isDefined}")
    println()

    println("TUI simulations completed.")
  }
}

