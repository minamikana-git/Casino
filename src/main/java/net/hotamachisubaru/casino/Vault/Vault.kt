package net.hotamachisubaru.casino.Vault

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Vault {
    var economy: Economy? = null
        private set

    init {
        if (!setupEconomy()) {
            Bukkit.getLogger().warning("Vaultのセットアップに失敗しました。")
        }
    }

    private fun setupEconomy(): Boolean {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vaultプラグインが見つかりません。")
            return false
        }

        val rsp = Bukkit.getServer().getServicesManager().getRegistration<Economy?>(Economy::class.java)
        if (rsp == null) {
            Bukkit.getLogger().warning("Economyサービスが登録されていません。")
            return false
        }

        economy = rsp.getProvider()
        return economy != null
    }

    fun has(player: Player?, amount: Double): Boolean {
        return economy != null && economy!!.has(player, amount)
    }

    fun withdraw(player: Player?, amount: Double): Boolean {
        if (economy != null && economy!!.has(player, amount)) {
            economy!!.withdrawPlayer(player, amount)
            return true
        }
        return false
    }

    fun deposit(player: Player?, amount: Double) {
        if (economy != null) {
            economy!!.depositPlayer(player, amount)
        }
    }
}
