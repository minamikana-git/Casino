package net.hotamachisubaru.casino.Vault;

import net.hotamachisubaru.casino.economy.EconomySetupHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Vault  {
    private static final String ECONOMY_PLUGIN_NAME = "Vault";
    private Economy economy = null;

    public Vault() {
        EconomySetupHelper setupHelper = new EconomySetupHelper();
        if (!setupHelper.setupEconomy()) {
            Bukkit.getLogger().warning("Vaultのセットアップに失敗しました。");
        } else {
            economy = setupHelper.getEconomy();
        }
    }

    public boolean has(Player player, double amount) {
        return isEconomySetup() && economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public void deposit(Player player, double amount) {
        if (isEconomySetup()) {
            economy.depositPlayer(player, amount);
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    private boolean isEconomySetup() {
        return economy != null;
    }
}