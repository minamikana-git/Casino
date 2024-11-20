package net.hotamachisubaru.casino.Roulette

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class RouletteClickListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.getWhoClicked() !is Player) return

        val player = event.getWhoClicked() as Player
        val clickedInventory = event.getClickedInventory()

        // クリックしたインベントリがルーレットホイールかどうかをチェック
        if (clickedInventory == null || event.getView().getTitle() != "ルーレットホイール") return

        // クリックをキャンセルしてインベントリが変更されないようにする
        event.setCancelled(true)

        val clickedItem = event.getCurrentItem()
        if (clickedItem != null && clickedItem.getItemMeta() != null) {
            val bet = clickedItem.getItemMeta().getDisplayName()
            executeRoulette(player, bet)
        }
    }

    private fun executeRoulette(player: Player, bet: String) {
        // ルーレットの結果をランダムに決定
        val outcomes = arrayOf<String>("赤", "黒", "緑")
        val result = outcomes[Random().nextInt(outcomes.size)]

        player.sendMessage("ルーレットは " + result + " に止まりました！")

        // プレイヤーの賭けと結果を比較して勝敗を決定
        if (bet == result) {
            player.sendMessage("おめでとう！勝ちました！")
            player.getInventory().addItem(ItemStack(Material.DIAMOND, 5)) // 勝利報酬としてダイヤモンドを5つ与える
        } else {
            player.sendMessage("残念、負けました。次の挑戦をお待ちしています！")
        }
    }
}
