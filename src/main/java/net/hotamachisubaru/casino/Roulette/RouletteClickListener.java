package net.hotamachisubaru.casino.Roulette;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RouletteClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        // クリックしたインベントリがルーレットホイールかどうかをチェック
        if (clickedInventory == null || !event.getView().getTitle().equals("ルーレットホイール")) return;

        // クリックをキャンセルしてインベントリが変更されないようにする
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getItemMeta() != null) {
            String bet = clickedItem.getItemMeta().getDisplayName();
            executeRoulette(player, bet);
        }
    }

    private void executeRoulette(Player player, String bet) {
        // ルーレットの結果をランダムに決定
        String[] outcomes = {"赤", "黒", "緑"};
        String result = outcomes[new Random().nextInt(outcomes.length)];

        player.sendMessage("ルーレットは " + result + " に止まりました！");

        // プレイヤーの賭けと結果を比較して勝敗を決定
        if (bet.equals(result)) {
            player.sendMessage("おめでとう！勝ちました！");
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5)); // 勝利報酬としてダイヤモンドを5つ与える
        } else {
            player.sendMessage("残念、負けました。次の挑戦をお待ちしています！");
        }
    }
}
