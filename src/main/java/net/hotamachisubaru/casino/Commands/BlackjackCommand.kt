package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Blackjack.Game;
import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlackjackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤー専用です。");
            return true;
        }

        Player player = (Player) sender;

        // Economyの初期化（Vaultが正しくセットアップされている必要があります）
        Economy economy = Casino.getEconomy();

        // 引数が1つの場合（賭け金を指定）
        double betAmount = 100.0;  // デフォルト値
        if (args.length == 1) {
            try {
                betAmount = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("無効な賭け金です。数値を入力してください。");
                return false;
            }
        }

        // プレイヤーの所持金を確認
        double balance = economy.getBalance(player);
        player.sendMessage("現在の所持金: " + balance);

        // 所持金が賭け金に足りているか確認
        if (balance < betAmount) {
            player.sendMessage("所持金が不足しています。ブラックジャックを開始できません。");
            return true;
        }

        // ゲームを開始
        Game blackjackGame = new Game(player, economy, betAmount);
        blackjackGame.startGame(betAmount);

        return true;
    }
}
