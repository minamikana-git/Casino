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

        int jackpotAmount = plugin.getJackpotAmount();
        player.sendMessage("現在のジャックポット: " + jackpotAmount + " チップ");

        if (args.length < 1) {
            player.sendMessage("使用方法: /roulette <amount>");
            return true;
        }

        int betAmount = parseBetAmount(args[0], player);
        if (betAmount == -1) return true;

        int minBet = plugin.getConfig().getInt("roulette.min_bet");
        int maxBet = plugin.getConfig().getInt("roulette.max_bet");
        if (isBetAmountOutsideRange(betAmount, minBet, maxBet, player)) return true;

        if (!hasEnoughChips(player, betAmount, plugin)) return true;

        plugin.getBetManager().setBet(player, betAmount);
        Roulette.openRouletteGUI(player);

        return true;
    }

    private int parseBetAmount(String arg, Player player) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            player.sendMessage("賭け金は正しい数値で指定してください。");
            return -1;
        }
    }

    private boolean isBetAmountOutsideRange(int betAmount, int minBet, int maxBet, Player player) {
        if (betAmount < minBet || betAmount > maxBet) {
            player.sendMessage("賭け金は " + minBet + " 以上 " + maxBet + " 以下で指定してください。");
            return true;
        }
        return false;
    }

    private boolean hasEnoughChips(Player player, int betAmount, Casino plugin) {
        int playerChips = plugin.getChips(player);
        if (playerChips < betAmount) {
            player.sendMessage("チップが足りません。現在のチップ数: " + playerChips);
            return false;
        }
        return true;
    }
}
