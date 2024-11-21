package net.hotamachisubaru.casino.Listener

import net.hotamachisubaru.casino.Blackjack.Game
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class GameListener(private val game: Game) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.getView().getTitle() == "ブラックジャック: アクションを選択") {
            handleBlackjackAction(event)
        }
    }

    private fun handleBlackjackAction(event: InventoryClickEvent) {
        val clickedItem = event.getCurrentItem()
        if (clickedItem == null || !clickedItem.hasItemMeta()) return

        val itemName = clickedItem.getItemMeta().getDisplayName()
        when (itemName) {
            "ヒット" -> game.hit()
            "スタンド" -> game.stand()
        }
    }
}
