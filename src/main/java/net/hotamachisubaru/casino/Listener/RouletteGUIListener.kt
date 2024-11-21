package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Roulette.Roulette;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RouletteGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("ルーレットホイール")) {
            event.setCancelled(true); // GUI操作をキャンセル

            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                String bet = event.getCurrentItem().getItemMeta().getDisplayName();
                Player player = (Player) event.getWhoClicked();
                Casino plugin = Casino.getPlugin(Casino.class);

                // 賭け金を取得してルーレットを実行
                int betAmount = plugin.getBetManager().getBet(player);
                new Roulette().executeRoulette(player, bet, betAmount);

                // 賭け金をクリアしてインベントリを閉じる
                plugin.getBetManager().clearBet(player);
                player.closeInventory();
            }
        }
    }
}
