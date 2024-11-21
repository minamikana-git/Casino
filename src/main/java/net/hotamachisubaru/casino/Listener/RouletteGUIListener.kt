package net.hotamachisubaru.casino.Listener

import net.hotamachisubaru.casino.Casino
import net.hotamachisubaru.casino.Roulette.Roulette
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class RouletteGUIListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.getView().getTitle() == "ルーレットホイール") {
            event.setCancelled(true) // GUI操作をキャンセル

            if (event.getCurrentItem() != null && event.getCurrentItem()!!.getItemMeta() != null) {
                val bet = event.getCurrentItem()!!.getItemMeta().getDisplayName()
                val player = event.getWhoClicked() as Player
                val plugin = Casino.getPlugin<Casino>(Casino::class.java)

                // 賭け金を取得してルーレットを実行
                val betAmount = plugin.getBetManager().getBet(player)
                Roulette().executeRoulette(player, bet, betAmount)

                // 賭け金をクリアしてインベントリを閉じる
                plugin.getBetManager().clearBet(player)
                player.closeInventory()
            }
        }
    }
}
