package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Blackjack.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GameListener implements Listener {
    private final Game game;

    public GameListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("ブラックジャック: アクションを選択")) {
            handleBlackjackAction(event);
        }
    }

    private void handleBlackjackAction(InventoryClickEvent event) {
        var clickedItem = event.getCurrentItem();
        if (isItemInvalid(clickedItem)) return;

        var itemName = clickedItem.getItemMeta().getDisplayName();
        executeBlackjackAction(itemName);
    }

    private boolean isItemInvalid(ItemStack item) {
        return item == null || !item.hasItemMeta();
    }

    private void executeBlackjackAction(String itemName) {
        switch (itemName) {
            case "ヒット":
                game.hit();
                break;
            case "スタンド":
                game.stand();
                break;
        }
    }
}