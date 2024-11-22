package net.hotamachisubaru.casino.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyHelper {
    private static Economy economy;

    // Economyプラグインの初期化
    public static void initialize() {
        EconomySetupHelper setupHelper = new EconomySetupHelper();
        if (setupHelper.setupEconomy()) {
            economy = setupHelper.getEconomy();
        }
    }

    // プレイヤーの所持金を取得
    public static double getPlayerBalance(Player player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    // プレイヤーの所持金を追加（必要に応じて）
    public static void addPlayerBalance(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static double getBalance(Player player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }
}