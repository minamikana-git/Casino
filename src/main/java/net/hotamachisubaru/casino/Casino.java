package net.hotamachisubaru.casino;

import net.hotamachisubaru.casino.listener.GUIListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class Casino extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
    }

    private void setupCommands() {

    }



    @Override
    public void onDisable() {
        // Plugin停止時の処理を追加します

        // 必要に応じて設定を保存します
        saveConfig();
        // 必要に応じて他のリソースをクリーンアップします

    }


}
