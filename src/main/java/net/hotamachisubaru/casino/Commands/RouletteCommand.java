package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Roulette.Roulette;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RouletteCommand implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみが実行できます。");
            return true;
        }

        Player player = (Player) sender;
        Casino plugin = Casino.getPlugin(Casino.class);

        // 現在のジャックポット枚数を取得
        int jackpotAmount = plugin.getJackpotAmount();
        player.sendMessage("現在のジャックポット: " + jackpotAmount + " チップ");

        // 引数が不足している場合
        if (args.length < 1) {
            player.sendMessage("使用方法: /roulette <amount>");
            return true;
        }

        // 賭け金の確認
        int betAmount;
        try {
            betAmount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("賭け金は正しい数値で指定してください。");
            return true;
        }

        // 賭け金が最低・最大金額に収まるか確認
        int minBet = plugin.getConfig().getInt("roulette.min_bet");
        int maxBet = plugin.getConfig().getInt("roulette.max_bet");

        if (betAmount < minBet || betAmount > maxBet) {
            player.sendMessage("賭け金は " + minBet + " 以上 " + maxBet + " 以下で指定してください。");
            return true;
        }

        // プレイヤーのチップを確認
        if (plugin.getChips(player) < betAmount) {
            player.sendMessage("チップが足りません。現在のチップ数: " + plugin.getChips(player));
            return true;
        }

        // 賭け金を保存してGUIを開く
        plugin.getBetManager().setBet(player, betAmount);
        Roulette.openRouletteGUI(player);
        return true;
    }
}