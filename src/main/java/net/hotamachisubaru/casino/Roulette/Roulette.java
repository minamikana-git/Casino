package net.hotamachisubaru.casino.Roulette;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static net.hotamachisubaru.casino.Manager.PlayerDataManager.plugin;

public class Roulette {



    public void executeRoulette(Player player, String bet, int betAmount) {
        Casino plugin = Casino.getPlugin(Casino.class);

        if (plugin.getChips(player) < betAmount) {
            player.sendMessage("チップが足りません。最低 " + betAmount + " チップが必要です。");
            return;
        }



        // ルーレットの結果をランダムに決定
        List<String> outcomes = new ArrayList<>(Arrays.asList("赤", "黒", "緑"));
        String result = outcomes.get(new Random().nextInt(outcomes.size()));
        player.sendMessage("ルーレットは " + result + " に止まりました！");

        // プレイヤーの賭けと結果を比較して勝敗を決定
        if (bet.equals(result)) {
            player.sendMessage("おめでとう！勝ちました！");
            plugin.addChips(player, betAmount * 2); // 勝利時は2倍のチップを与える


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

    // チップ数を player.yml に保存
    public void setChips(Player player, int amount) {
        File playerFile = new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        config.set("chips", amount);
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("チップ数の保存に失敗しました: " + e.getMessage());
        }
    }

    // チップ数を player.yml から読み込む
    public int getChips(Player player) {
        File playerFile = new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            return 0; // ファイルがなければ 0 チップ
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        return config.getInt("chips", 0);
    }


}