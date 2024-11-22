package net.hotamachisubaru.casino.Vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Vault {
    private Economy economy = null;

    public Vault() {
        if (!setupEconomy()) {
            Bukkit.getLogger().warning("Vaultのセットアップに失敗しました。");
        }
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vaultプラグインが見つかりません。");
            return false;
        }

        var rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().warning("Economyサービスが登録されていません。");
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean has(Player player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (economy != null && economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public void deposit(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public Economy getEconomy() {
        return economy;
    }
}