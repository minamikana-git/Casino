package net.hotamachisubaru.casino.Listener

import net.hotamachisubaru.casino.Slot.SlotMachine
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class CasinoListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.getWhoClicked() !is Player) return
        val player = event.getWhoClicked() as Player
        val clickedInventory = event.getClickedInventory()

        if (clickedInventory == null || event.getView().getTitle() != "カジノゲーム選択") return

        event.setCancelled(true)

        val clickedItem = event.getCurrentItem()
        if (clickedItem == null || clickedItem.getItemMeta() == null) return

        val itemName = clickedItem.getItemMeta().getDisplayName()
        when (itemName) {
            "スロットマシン" -> SlotMachine.openSlotGUI(player)
            "ルーレット" -> player.performCommand("roulette")
            "ブラックジャック" -> player.performCommand("blackjack")
            else -> {}
        }
    }
}

