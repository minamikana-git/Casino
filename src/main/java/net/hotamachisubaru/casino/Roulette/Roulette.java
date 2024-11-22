package net.hotamachisubaru.casino.Roulette;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Roulette {
    private void addToJackpotFromBet(int betAmount) {
        Casino plugin = Casino.getPlugin(Casino.class);
        double increaseRate = plugin.getConfig().getDouble("jackpot_increase_rate", 0.05);
        int amountToAdd = (int) (betAmount * increaseRate);
        plugin.addToJackpot(amountToAdd);
    }

    private void checkJackpot(Player player) {
        Casino plugin = Casino.getPlugin(Casino.class);
        double winChance = plugin.getConfig().getDouble("jackpot_win_chance", 0.01);

        // ランダムに抽選
        if (Math.random() < winChance) {
            int jackpotAmount = plugin.getJackpotAmount();
            player.sendMessage("おめでとうございます！ジャックポットを当てました！獲得額: " + jackpotAmount + " チップ！");
            plugin.addChips(player, jackpotAmount);
            plugin.resetJackpot();
        } else {
            player.sendMessage("ジャックポットには当選しませんでしたが、また挑戦してください！");
        }
    }

    public void executeRoulette(Player player, String bet, int betAmount) {
        Casino plugin = Casino.getPlugin(Casino.class);

        if (plugin.getChips(player) < betAmount) {
            player.sendMessage("チップが足りません。最低 " + betAmount + " チップが必要です。");
            return;
        }

        // 賭け金の一部をジャックポットに加算
        addToJackpotFromBet(betAmount);

        // ルーレットの結果をランダムに決定
        List<String> outcomes = new ArrayList<>(Arrays.asList("赤", "黒", "緑"));
        String result = outcomes.get(new Random().nextInt(outcomes.size()));
        player.sendMessage("ルーレットは " + result + " に止まりました！");

        // プレイヤーの賭けと結果を比較して勝敗を決定
        if (bet.equals(result)) {
            player.sendMessage("おめでとう！勝ちました！");
            plugin.addChips(player, betAmount * 2); // 勝利時は2倍のチップを与える

            // ジャックポットの抽選
            checkJackpot(player);
        } else {
            player.sendMessage("残念、負けました。次の挑戦をお待ちしています！");
            plugin.removeChips(player, betAmount);
        }
    }

    public static void openRouletteGUI(Player player) {
        org.bukkit.inventory.Inventory rouletteGUI = Bukkit.createInventory(null, 9, "ルーレットホイール");

        // 賭けのオプションを設定
        rouletteGUI.setItem(0, createBetItem("赤", Material.RED_WOOL));
        rouletteGUI.setItem(1, createBetItem("黒", Material.BLACK_WOOL));
        rouletteGUI.setItem(2, createBetItem("緑", Material.GREEN_WOOL));
        // 他のスロットは空にしておく
        for (int i = 3; i <= 8; i++) {
            rouletteGUI.setItem(i, new ItemStack(Material.AIR));
        }

        player.openInventory(rouletteGUI);
    }

    private static ItemStack createBetItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}