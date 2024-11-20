package net.hotamachisubaru.casino.GUI

import net.hotamachisubaru.casino.Casino
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class CasinoGUI(plugin: Casino) : Listener {
    private val plugin: JavaPlugin // プラグインのインスタンスを保持するための変数

    init {
        this.plugin = plugin
    }

    fun openCasinoGUI(player: Player) {
        val casinoGUI = Bukkit.createInventory(null, 9, "カジノゲーム選択")

        // スロットマシンのアイコンを設定
        val slotMachineItem: ItemStack = createMenuItem(Material.DIAMOND, "スロットマシン")
        casinoGUI.setItem(0, slotMachineItem)

        // ルーレットのアイコンを設定
        val rouletteItem: ItemStack = createMenuItem(Material.EMERALD, "ルーレット")
        casinoGUI.setItem(1, rouletteItem)

        // ブラックジャックのアイコンを設定
        val blackjackItem: ItemStack = createMenuItem(Material.GOLD_INGOT, "ブラックジャック")
        casinoGUI.setItem(2, blackjackItem)

        Bukkit.getPluginManager().registerEvents(this, plugin) // リスナー登録を行う
        player.openInventory(casinoGUI)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.getWhoClicked() as Player
        val clickedInventory = event.getClickedInventory()

        if (clickedInventory != null && clickedInventory == player.getOpenInventory().getTopInventory()) {
            val clickedItem = event.getCurrentItem()
            if (clickedItem != null && clickedItem.getType() == Material.ENCHANTING_TABLE) {
                event.setCancelled(true) // エンチャントテーブルからのアイテム取り出しをキャンセルする
                player.sendMessage("エンチャントテーブルからのアイテム取り出しは禁止されています。")
                // アイテムを元の状態に戻す処理などを行う場合はここに追加
            }
        }
    }

    companion object {
        private fun createMenuItem(material: Material, displayName: String?): ItemStack {
            val item = ItemStack(material)
            val meta = item.getItemMeta()
            if (meta != null) {
                meta.setDisplayName(displayName)
                item.setItemMeta(meta)
            }
            return item
        }
    }
}
