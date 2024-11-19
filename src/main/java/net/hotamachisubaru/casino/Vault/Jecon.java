package net.hotamachisubaru.casino.Vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Jecon {
    private static Economy econ = null;

    public Jecon() {
        if (!setupEconomy()) {
            throw new IllegalStateException("Vault 経済プラグインが見つかりません!");
        }
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean hasBalance(Player player, double amount) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        return econ.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        if (hasBalance(player, amount)) {
            econ.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public void deposit(Player player, double amount) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        econ.depositPlayer(player, amount);
    }
}