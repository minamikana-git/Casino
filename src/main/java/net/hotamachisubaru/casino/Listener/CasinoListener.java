package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Slot.SlotMachine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CasinoListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        var clickedInventory = event.getClickedInventory();

        if (clickedInventory == null || !event.getView().getTitle().equals("カジノゲーム選択")) return;

        event.setCancelled(true);

        var clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        switch (itemName) {
            case "スロットマシン":
                SlotMachine.openSlotGUI(player);
                break;
            case "ルーレット":
                player.performCommand("roulette");
                break;
            case "ブラックジャック":
                player.performCommand("blackjack");
                break;
            default:
                break;
        }
    }
}