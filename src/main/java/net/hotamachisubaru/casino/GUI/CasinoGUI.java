package net.hotamachisubaru.casino.GUI;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CasinoGUI implements Listener {

    private final JavaPlugin plugin; // プラグインのインスタンスを保持するための変数

    public CasinoGUI(Casino plugin) {
        this.plugin = plugin;
    }

    public void openCasinoGUI(Player player) {
        Inventory casinoGUI = Bukkit.createInventory(null, 9, "カジノゲーム選択");

        // スロットマシンのアイコンを設定
        ItemStack slotMachineItem = createMenuItem(Material.DIAMOND, "スロットマシン");
        casinoGUI.setItem(0, slotMachineItem);

        // ルーレットのアイコンを設定
        ItemStack rouletteItem = createMenuItem(Material.EMERALD, "ルーレット");
        casinoGUI.setItem(1, rouletteItem);

        // ブラックジャックのアイコンを設定
        ItemStack blackjackItem = createMenuItem(Material.GOLD_INGOT, "ブラックジャック");
        casinoGUI.setItem(2, blackjackItem);

        Bukkit.getPluginManager().registerEvents(this, plugin); // リスナー登録を行う
        player.openInventory(casinoGUI);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.ENCHANTING_TABLE) {
                event.setCancelled(true); // エンチャントテーブルからのアイテム取り出しをキャンセルする
                player.sendMessage("エンチャントテーブルからのアイテム取り出しは禁止されています。");
                // アイテムを元の状態に戻す処理などを行う場合はここに追加
            }
        }
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
}
