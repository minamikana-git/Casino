package net.hotamachisubaru.casino.economy

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object EconomyHelper {
    var economy: Economy? = null
        private set

    // Economyプラグインの初期化
    fun setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return
        }

        val rsp = Bukkit.getServicesManager().getRegistration<Economy?>(Economy::class.java)
        if (rsp != null) {
            economy = rsp.getProvider()
        }
    }

    // プレイヤーの所持金を取得
    fun getBalance(player: Player?): Double {
        if (economy != null) {
            return economy!!.getBalance(player)
        }
        return 0.0
    }

    // プレイヤーの所持金を設定（必要に応じて）
    fun setBalance(player: Player?, amount: Double) {
        if (economy != null) {
            economy!!.depositPlayer(player, amount)
        }
    }
}
