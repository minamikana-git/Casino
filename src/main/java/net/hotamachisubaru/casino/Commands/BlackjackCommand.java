package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Blackjack.Game;
import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlackjackCommand implements CommandExecutor {

    private static final double DEFAULT_BET_AMOUNT = 100.0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤー専用です。");
            return true;
        }

        Player player = (Player) sender;
        Economy economy = initializeEconomy();
        double betAmount = DEFAULT_BET_AMOUNT;

        double playerBalance = economy.getBalance(player);
        player.sendMessage("現在の所持金: " + playerBalance);

        if (playerBalance < betAmount) {
            sendPlayerMessage(player, "所持金が不足しています。ブラックジャックを開始できません。");
            return true;
        }

        Game blackjackGame = new Game(player, economy, betAmount);
        blackjackGame.startGame(betAmount);
        return true;
    }

    private Economy initializeEconomy() {
        // Economyの初期化（Vaultが正しくセットアップされている必要があります）
        return Casino.getEconomy();
    }

    private void sendPlayerMessage(Player player, String message) {
        player.sendMessage(message);
    }
}