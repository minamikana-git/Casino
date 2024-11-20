package net.hotamachisubaru.casino.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHelper {

    private static Economy economy;

    // Economyプラグインの初期化
    public static void setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    // プレイヤーの所持金を取得
    public static double getBalance(Player player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    // プレイヤーの所持金を設定（必要に応じて）
    public static void setBalance(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public static Economy getEconomy() {
        return economy;
    }
}
