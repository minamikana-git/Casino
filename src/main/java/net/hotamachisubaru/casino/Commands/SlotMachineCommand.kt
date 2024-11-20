package net.hotamachisubaru.casino.Commands

import net.hotamachisubaru.casino.Slot.SlotMachine.openSlotGUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SlotMachineCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<String?>?): Boolean {
        if (sender is Player) {
            val player = sender
            openSlotGUI(player)
            return true
        }
        sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。")
        return false
    }
}

