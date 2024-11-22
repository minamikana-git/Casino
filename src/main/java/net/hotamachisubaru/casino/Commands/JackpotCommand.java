package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JackpotCommand implements CommandExecutor {

    private static final String PERMISSION_MESSAGE = "権限がありません。";
    private static final String USAGE_MESSAGE = "使用方法: /setjackpot <amount>";
    private static final String INVALID_NUMBER_MESSAGE = "正しい数値を入力してください。";
    private static final String PERMISSION = "casino.jackpot";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayerWithPermission(sender)) {
            sender.sendMessage(PERMISSION_MESSAGE);
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(USAGE_MESSAGE);
            return false;
        }

        Integer jackpotAmount = parseJackpotAmount(args[0]);
        if (jackpotAmount == null) {
            sender.sendMessage(INVALID_NUMBER_MESSAGE);
            return false;
        }

        Casino plugin = Casino.getInstance();
        plugin.setJackpotAmount(jackpotAmount);
        sender.sendMessage("ジャックポットの値を " + jackpotAmount + " に設定しました。");
        return true;
    }

    private boolean isPlayerWithPermission(CommandSender sender) {
        return sender instanceof Player && sender.hasPermission(PERMISSION);
    }

    private Integer parseJackpotAmount(String amount) {
        try {
            return Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}