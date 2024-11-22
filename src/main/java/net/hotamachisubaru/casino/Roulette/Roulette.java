package net.hotamachisubaru.casino.Roulette;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Roulette {
    private final Plugin plugin;
    private final Casino casinoPlugin;

    public Roulette(Plugin plugin) {
        this.plugin = plugin;
        this.casinoPlugin = Casino.getPlugin(Casino.class);
    }

    public static void openRouletteGUI(Player player) {
        // Implement roulette GUI opening logic here
    }

    private void sendBetResultMessage(Player player, String bet, String result, int betAmount) {
        player.sendMessage("ルーレットは " + result + " に止まりました！");

        if (bet.equals(result)) {
            player.sendMessage("おめでとう！勝ちました！");
            casinoPlugin.addChips(player, betAmount * 2);
        } else {
            player.sendMessage("残念、負けました。次の挑戦をお待ちしています！");
            casinoPlugin.removeChips(player, betAmount);
        }
    }

    public void execute(Player player, String bet, int betAmount) {
        if (casinoPlugin.getChips(player) < betAmount) {
            player.sendMessage("チップが足りません。最低 " + betAmount + " チップが必要です。");
            return;
        }

        List<String> outcomes = Arrays.asList("赤", "黒", "緑");
        String result = outcomes.get(new Random().nextInt(outcomes.size()));

        sendBetResultMessage(player, bet, result, betAmount);
    }

    public static void openGUI(Player player) {
        org.bukkit.inventory.Inventory rouletteGUI = Bukkit.createInventory(null, 9, "ルーレットホイール");

        rouletteGUI.setItem(0, BetItem.create("赤", Material.RED_WOOL));
        rouletteGUI.setItem(1, BetItem.create("黒", Material.BLACK_WOOL));
        rouletteGUI.setItem(2, BetItem.create("緑", Material.GREEN_WOOL));

        for (int i = 3; i <= 8; i++) {
            rouletteGUI.setItem(i, new ItemStack(Material.AIR));
        }
        player.openInventory(rouletteGUI);
    }

    public void executeRoulette(Player player, String bet, Integer betAmount) {
        if (casinoPlugin.getChips(player) < betAmount) {
            player.sendMessage("チップが足りません。最低 " + betAmount + " チップが必要です。");
            return;
        }

        List<String> outcomes = Arrays.asList("赤", "黒", "緑");
        String result = outcomes.get(new Random().nextInt(outcomes.size()));

        sendBetResultMessage(player, bet, result, betAmount);
    }

    private static class BetItem {
        static ItemStack create(String name, Material material) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                item.setItemMeta(meta);
            }
            return item;
        }
    }

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

    public int getChips(Player player) {
        File playerFile = new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            return 0;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        return config.getInt("chips", 0);
    }
}