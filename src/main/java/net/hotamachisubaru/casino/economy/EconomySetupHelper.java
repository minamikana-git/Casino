package net.hotamachisubaru.casino.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public class EconomySetupHelper {
    private Economy economy;

    public boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vaultプラグインが見つかりません。");
            return false;
        }

        var registration = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            Bukkit.getLogger().warning("Economyサービスが登録されていません。");
            return false;
        }

        economy = registration.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }
}

