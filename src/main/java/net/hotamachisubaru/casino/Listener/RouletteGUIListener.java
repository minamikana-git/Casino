package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Manager.BetManager;
import net.hotamachisubaru.casino.Roulette.Roulette;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RouletteGUIListener implements Listener {

    net.hotamachisubaru.casino.Manager.BetManager betManager;


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("ルーレットホイール")) {
            event.setCancelled(true); // GUI操作をキャンセル
            handleRouletteAction(event);
        }
    }

    private void handleRouletteAction(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        String bet = event.getCurrentItem().getItemMeta().getDisplayName();
        Player player = (Player) event.getWhoClicked();
        Casino plugin = Casino.getPlugin(Casino.class);

        // Validate and obtain bet amount, then execute roulette
        BetManager betManager = plugin.getBetManager();
        Integer betAmount = betManager.getBetAmount(player);
        if (betAmount == null) {
            player.sendMessage("賭け金が設定されていません。");
            return;
        }
        new Roulette(plugin).executeRoulette(player, bet, betAmount);

        // Clear bet amount and close inventory
        betManager.clearBet(player);
        player.closeInventory();
    }
}