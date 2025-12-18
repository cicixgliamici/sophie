/**
 * Entry point for the Sophie application in Text User Interface (TUI) mode.
 * It serves as the launcher for the interactive console interface of the software.
 */

import frontend.SophieTui

object TuiMain {
  def main(args: Array[String]): Unit = {
    SophieTui.run()
  }
}
