package net.hotamachisubaru.casino.Manager;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final JavaPlugin plugin;
    private final File playerFile;
    private final FileConfiguration playerData;

    public PlayerDataManager() {
        this.plugin = JavaPlugin.getPlugin(Casino.class); // プラグインインスタンス
        this.playerFile = new File(plugin.getDataFolder(), "player.yml");
        this.playerData = YamlConfiguration.loadConfiguration(playerFile);
    }

    private void saveConfig() {
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPlayerChipsData(UUID playerUUID, String key, int value) {
        playerData.set(playerUUID.toString() + key, value);
        saveConfig();
    }

    private int getPlayerChipsData(UUID playerUUID, String key, int defaultValue) {
        return playerData.getInt(playerUUID.toString() + key, defaultValue);
    }

    // プレイヤーのチップ数を保存
    public void savePlayerChips(Player player, int chips) {
        UUID playerUUID = player.getUniqueId();
        setPlayerChipsData(playerUUID, ".chips", chips);
    }

    // プレイヤーのチップ数を読み込む
    public int getPlayerChips(Player player) {
        UUID playerUUID = player.getUniqueId();
        return getPlayerChipsData(playerUUID, ".chips", 0); // デフォルトは0
    }

    // プレイヤーのデータをリセット
    public void resetPlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerData.set(playerUUID.toString(), null);
        saveConfig();
    }
}