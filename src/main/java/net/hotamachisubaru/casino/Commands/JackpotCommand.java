package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JackpotCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("casino.jackpot")) {
            sender.sendMessage("権限がありません。");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage("使用方法: /setjackpot <amount>");
            return false;
        }

        int jackpotAmount;
        try {
            jackpotAmount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage("正しい数値を入力してください。");
            return false;
        }

        Casino plugin = Casino.getPlugin(Casino.class);
        plugin.setJackpotAmount(jackpotAmount);
        sender.sendMessage("ジャックポットの値を " + jackpotAmount + " に設定しました。");

        return true;
    }
}
