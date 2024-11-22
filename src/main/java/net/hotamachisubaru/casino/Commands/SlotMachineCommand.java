package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Slot.SlotMachine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SlotMachineCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            SlotMachine.openSlotGUI(player, Integer.parseInt(args[0]));
            return true;
        }
        sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
        return false;
    }
}

