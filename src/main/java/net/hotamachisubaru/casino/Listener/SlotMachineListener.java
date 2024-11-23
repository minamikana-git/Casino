package net.hotamachisubaru.casino.Listener;

import org.bukkit.entity.Player;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class SlotMachineListener implements Listener {
    private void resetSlotMachine(Inventory inventory) {
        inventory.clear();
        Logger.getLogger("SlotMachineLogger").info("Slot machine inventory has been reset.");
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = player.getOpenInventory().getTopInventory();

        // スロットマシンのインベントリの場合、操作をキャンセル
        if (topInventory != null && "スロットマシン".equals(topInventory.getType())) {
            if (clickedInventory != null && clickedInventory.equals(topInventory)) {
                event.setCancelled(true); // アイテム移動禁止
                player.sendMessage("スロットマシンのアイテムは動かせません！");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory closedInventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        // スロットマシンが閉じられた場合の処理
        if (closedInventory != null && "スロットマシン".equals(closedInventory.getType())) {
            player.sendMessage("スロットマシンが閉じられました！");
            resetSlotMachine(closedInventory);
            Logger.getLogger("SlotMachineLogger").info("Slot machine inventory closed by " + player.getName());
        }
    }


}
