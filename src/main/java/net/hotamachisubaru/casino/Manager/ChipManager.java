package net.hotamachisubaru.casino.Manager;

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
        return config.getInt(player.getUniqueId().toString(), 0);
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

