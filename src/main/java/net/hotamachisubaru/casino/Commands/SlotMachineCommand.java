package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Slot.SlotMachine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SlotMachineCommand implements CommandExecutor {

    private static final String PLAYER_ONLY_MESSAGE = "このコマンドはプレイヤーのみ使用可能です。";
    private static final String INVALID_BET_AMOUNT_MESSAGE = "賭け金は整数で入力してください。";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PLAYER_ONLY_MESSAGE);
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(INVALID_BET_AMOUNT_MESSAGE);
            return false;
        }

        Player player = (Player) sender;

        try {
            int betAmount = Integer.parseInt(args[0]);
            SlotMachine.openSlotGUI(player, betAmount);
        } catch (NumberFormatException e) {
            player.sendMessage(INVALID_BET_AMOUNT_MESSAGE);
            return false;
        }

        return true;
    }
}