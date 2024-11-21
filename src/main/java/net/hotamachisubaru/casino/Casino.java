package net.hotamachisubaru.casino;

import net.hotamachisubaru.casino.Commands.*;
import net.hotamachisubaru.casino.GUI.CasinoGUI;
import net.hotamachisubaru.casino.Listener.BlackjackChatListener;
import net.hotamachisubaru.casino.Listener.CasinoListener;
import net.hotamachisubaru.casino.Listener.RouletteGUIListener;
import net.hotamachisubaru.casino.Manager.BetManager;
import net.hotamachisubaru.casino.Listener.RouletteClickListener;
import net.hotamachisubaru.casino.Vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Casino extends JavaPlugin implements CommandExecutor {
    private Vault vault;
    private static Economy economy;
    private FileConfiguration slotConfig;
    private FileConfiguration rouletteConfig;
    private FileConfiguration chipsConfig;
    private FileConfiguration jackpotConfig;
    private static Casino instance;
    private int minimumBet;
    private int maximumBet;
    private BetManager betManager;
    private List<Material> slotItems = new ArrayList<>();


    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        vault = new Vault();
        instance = this;
        setupCasino();
        setupEconomy();
        CasinoGUI casinoGUI = new CasinoGUI(this); // CasinoGUI インスタンスを作成
        setupCommands();
        registerEvents();
        betManager = new BetManager();
    }

    private void setupCasino() {
        // config.ymlの設定読み込み
        reloadConfig();

        // slot_itemsの読み込み
        List<String> items = getConfig().getStringList("slot_items");
        slotItems.clear();
        for (String itemName : items) {
            Material material = Material.getMaterial(itemName);
            if (material != null) {
                slotItems.add(material);
            } else {
                getLogger().warning("無効なアイテム名: " + itemName);
            }
        }

        // 最低・最大チップの設定読み込み
        minimumBet = getConfig().getInt("minimum_bet", 10);  // デフォルト値は10
        maximumBet = getConfig().getInt("maximum_bet", 10000); // デフォルト値は10000
    }

    public int getJackpotAmount() {
        return getConfig().getInt("jackpot_amount", 5000);
    }

    public void addToJackpot(int amount) {
        int currentAmount = getJackpotAmount();
        getConfig().set("jackpot_amount", currentAmount + amount);
        saveConfig();
    }

    public void resetJackpot() {
        getConfig().set("jackpot_amount", 5000);  // リセット時の初期金額
        saveConfig();
    }
    public FileConfiguration getRouletteConfig() {
        return rouletteConfig;
    }

    public List<Material> getSlotItems() {
        return slotItems;
    }

    public int getMinimumBet() {
        return minimumBet;
    }

    public int getMaximumBet() {
        return maximumBet;
    }

    public FileConfiguration getJackpotConfig() {
        return jackpotConfig;
    }

    public Vault getVault() {
        return vault;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public BetManager getBetManager() {
        return betManager;
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new RouletteClickListener(), this);
        getServer().getPluginManager().registerEvents(new CasinoListener(), this);
        getServer().getPluginManager().registerEvents(new BlackjackChatListener(this),this);
        CasinoGUI casinoGUI = new CasinoGUI(this);// CasinoGUI のインスタンスを生成し、プラグインのインスタンスを渡す
        getServer().getPluginManager().registerEvents(casinoGUI, this);
        getServer().getPluginManager().registerEvents(new RouletteGUIListener(), this);
        getServer().getPluginManager().registerEvents(new RouletteClickListener(), this);
    }

    public Casino () {
        instance = this;
    }

    public static Casino getInstance() {
        return instance;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("§4エラー：Vaultプラグインが見つかりませんでした。");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("§4エラー:Economyサービスプロバイダが登録されていません。");
            return false;
        }
        economy = rsp.getProvider();
        if (economy == null) {
            getLogger().warning("§4エラー：Economyサービスが見つかりません。");
            return false;
        }
        return true;
    }

    private void setupCommands() {

        getCommand("roulette").setExecutor(new RouletteCommand());
        getCommand("slot").setExecutor(new SlotMachineCommand());
        getCommand("buychips").setExecutor(new BuyChip(this));
        getCommand("blackjack").setExecutor(new BlackjackCommand());
        getCommand("casino").setExecutor(new OpenCasinoCommand());
    }




    public void addChips(Player player, int amount) {
        String playerUUID = player.getUniqueId().toString();
        int currentChips = chipsConfig.getInt("players." + playerUUID + ".chips", 0);
        chipsConfig.set("players." + playerUUID + ".chips", currentChips + amount);
        saveChipsConfig();
        player.sendMessage("あなたは " + amount + " チップを獲得しました！現在のチップ数: " + (currentChips + amount));
    }

    public void removeChips(Player player, int amount) {
        String playerUUID = player.getUniqueId().toString();
        int currentChips = chipsConfig.getInt("players." + playerUUID + ".chips", 0);
        if (currentChips >= amount) {
            chipsConfig.set("players." + playerUUID + ".chips", currentChips - amount);
            saveChipsConfig();
            player.sendMessage("あなたは " + amount + " チップを失いました。現在のチップ数: " + (currentChips - amount));
        } else {
            player.sendMessage("チップが足りません。");
        }
    }

    public int getChips(Player player) {
        String playerUUID = player.getUniqueId().toString();
        return chipsConfig.getInt("players." + playerUUID + ".chips", 0);
    }

    private void saveChipsConfig() {
        try {
            chipsConfig.save(new File(getDataFolder(), "chips.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
