package net.hotamachisubaru.casino.GUI;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CasinoGUI implements Listener { // プラグインのインスタンスを保持するための変数
    private final JavaPlugin plugin;
    private static final String CASINO_GUI_TITLE = "カジノゲーム選択";

    public CasinoGUI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static ItemStack createMenuItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openCasinoGUI(Player player) {
        org.bukkit.inventory.Inventory casinoGUI = Bukkit.createInventory(null, 9, CASINO_GUI_TITLE);
        casinoGUI.setItem(0, createMenuItem(Material.DIAMOND, "スロットマシン"));
        casinoGUI.setItem(1, createMenuItem(Material.EMERALD, "ルーレット"));
        casinoGUI.setItem(2, createMenuItem(Material.GOLD_INGOT, "ブラックジャック"));

        Bukkit.getPluginManager().registerEvents(this, plugin); // リスナー登録を行う
        player.openInventory(casinoGUI);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        org.bukkit.inventory.Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.ENCHANTING_TABLE) {
                event.setCancelled(true); // エンチャントテーブルからのアイテム取り出しをキャンセルする
                player.sendMessage("エンチャントテーブルからのアイテム取り出しは禁止されています。");
                // アイテムを元の状態に戻す処理などを行う場合はここに追加
            }
        }
    }
}