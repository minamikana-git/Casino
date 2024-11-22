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

    private static final JavaPlugin plugin = JavaPlugin.getPlugin(Casino.class); // プラグインインスタンス

    // player.yml ファイルのパス
    private static File playerFile = new File(plugin.getDataFolder(), "player.yml");
    private static FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

    // プレイヤーのチップ数を保存
    public static void savePlayerChips(Player player, int chips) {
        UUID playerUUID = player.getUniqueId();
        playerData.set(playerUUID.toString() + ".chips", chips);

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // プレイヤーのチップ数を読み込む
    public static int getPlayerChips(Player player) {
        UUID playerUUID = player.getUniqueId();
        return playerData.getInt(playerUUID.toString() + ".chips", 0); // デフォルトは0
    }

    // プレイヤーのデータをリセット（任意）
    public static void resetPlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerData.set(playerUUID.toString(), null);

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

