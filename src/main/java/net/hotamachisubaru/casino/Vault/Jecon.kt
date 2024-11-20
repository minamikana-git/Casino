package net.hotamachisubaru.casino.Vault

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Jecon {
    init {
        check(setupEconomy()) { "Vault 経済プラグインが見つかりません!" }
    }

    private fun setupEconomy(): Boolean {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false
        }
        val rsp = Bukkit.getServer().getServicesManager().getRegistration<Economy?>(Economy::class.java)
        if (rsp == null) {
            return false
        }
        econ = rsp.getProvider()
        return econ != null
    }

    fun hasBalance(player: Player, amount: Double): Boolean {
        requireNotNull(player) { "player cannot be null" }
        return econ!!.has(player, amount)
    }

    fun withdraw(player: Player, amount: Double): Boolean {
        requireNotNull(player) { "player cannot be null" }
        if (hasBalance(player, amount)) {
            econ!!.withdrawPlayer(player, amount)
            return true
        }
        return false
    }

    fun deposit(player: Player, amount: Double) {
        requireNotNull(player) { "player cannot be null" }
        econ!!.depositPlayer(player, amount)
    }

    companion object {
        private var econ: Economy? = null
    }
}