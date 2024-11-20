package net.hotamachisubaru.casino.Commands

import net.hotamachisubaru.casino.Casino
import net.hotamachisubaru.casino.GUI.CasinoGUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class OpenCasinoCommand : CommandExecutor {
    private val casinoGUI: CasinoGUI

    init {
        this.casinoGUI = CasinoGUI(Casino.getInstance())
    }

    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<String?>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。")
            return false
        }

        val player = sender
        casinoGUI.openCasinoGUI(player) // CasinoGUI インスタンスを使用してメソッドを呼び出し
        return true
    }
}
