package net.hotamachisubaru.casino.Listener

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.Random

class RouletteClickListener : Listener {

    companion object {
        private const val ROULETTE_TITLE = "ルーレットホイール"
        private val OUTCOMES = arrayOf("赤", "黒", "緑")
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = getPlayer(event) ?: return
        if (!isRouletteWheel(event)) return

        event.setCancelled(true)
        val bet = getBet(event) ?: return

        executeRoulette(player, bet)
    }

    private fun getPlayer(event: InventoryClickEvent): Player? {
        return event.getWhoClicked() as? Player
    }

    private fun isRouletteWheel(event: InventoryClickEvent): Boolean {
        return event.getClickedInventory() != null && event.getView().getTitle() == ROULETTE_TITLE
    }

    private fun getBet(event: InventoryClickEvent): String? {
        val clickedItem = event.getCurrentItem()
        return clickedItem?.itemMeta?.displayName
    }

    private fun executeRoulette(player: Player, bet: String) {
        val result = OUTCOMES[Random().nextInt(OUTCOMES.size)]
        sendMessage(player, "ルーレットは $result に止まりました！")

        if (bet == result) {
            sendMessage(player, "おめでとう！勝ちました！")
            player.inventory.addItem(ItemStack(Material.DIAMOND, 5))
        } else {
            sendMessage(player, "残念、負けました。次の挑戦をお待ちしています！")
        }
    }

    private fun sendMessage(player: Player, message: String) {
        player.sendMessage(message)
    }
}