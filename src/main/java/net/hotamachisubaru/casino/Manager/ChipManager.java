package net.hotamachisubaru.casino.Manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class ChipManager {
    private final File file;
    private final FileConfiguration config;

    public ChipManager() {
        file = new File("plugins/Casino/chips.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    // チップを取得
    public int getChips(Player player) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Casino/player.yml"));
        return config.getInt("players." + player.getUniqueId() + ".chips", 0);
    }

    public void setChips(Player player, int amount) {
        File file = new File("plugins/Casino/player.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("players." + player.getUniqueId() + ".chips", amount);
        try {
            config.save(file);
            Bukkit.getLogger().info("[デバッグ] player.yml を正常に保存しました。");
        } catch (IOException e) {
            Bukkit.getLogger().warning("[エラー] player.yml の保存に失敗しました。");
            e.printStackTrace();
        }
    }



    // チップを追加
    public void addChips(Player player, int amount) {
        int currentChips = getChips(player);
        config.set(player.getUniqueId().toString(), currentChips + amount);
        saveConfig();
    }

    // チップを保存
    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

