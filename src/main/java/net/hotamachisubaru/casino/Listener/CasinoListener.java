package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Slot.SlotMachine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CasinoListener implements Listener {

    private static final String CASINO_GAME_TITLE = "カジノゲーム選択";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();

        if (inventory == null || !event.getView().getTitle().equals(CASINO_GAME_TITLE)) return;
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        String itemName = item.getItemMeta().getDisplayName();
        if (itemName == null) return;

        handleItemClick(player, itemName, inventory);
    }

    private void handleItemClick(Player player, String itemName, Inventory inventory) {
        switch (itemName) {
            case "スロットマシン":
                player.sendMessage("いくら賭けますか？");
                player.sendMessage("金額をチャットで入力してください。");
                SlotMachine.waitForBetAmount(player, betAmount -> SlotMachine.openSlotGUI(player, betAmount));
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