package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JackpotRateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤー専用です。");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("casino.jackpot.rate")) {
            player.sendMessage("このコマンドを実行する権限がありません。");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("使用方法: /jackpotrate <倍率>");
            return false;
        }

        Double jackpotMultiplier = validateAndParseMultiplier(player, args[0]);
        if (jackpotMultiplier == null) {
            return false;
        }

        int newJackpotAmount = (int) (Casino.getPlugin(Casino.class).getJackpotAmount() * jackpotMultiplier);
        Casino.getPlugin(Casino.class).setJackpotAmount(newJackpotAmount);
        player.sendMessage("ジャックポットの値を " + jackpotMultiplier + " 倍に増加しました。");
        return true;
    }

    private Double validateAndParseMultiplier(Player player, String arg) {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            player.sendMessage("有効な数値を入力してください。");
            return null;
        }
    }
}