package net.hotamachisubaru.casino;

import net.hotamachisubaru.casino.Commands.*;
import net.hotamachisubaru.casino.GUI.CasinoGUI;
import net.hotamachisubaru.casino.Listener.*;
import net.hotamachisubaru.casino.Manager.BetManager;
import net.hotamachisubaru.casino.Manager.ChipManager;
import net.hotamachisubaru.casino.Vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Casino extends JavaPlugin implements CommandExecutor {
    public ChatListener chatListener = new ChatListener();
    private Vault vault;
    public static Economy economy;
    private static Casino instance;
    private int minBet;
    private int maxBet;
    private BetManager betManager;
    private final List<Material> slotItems = new ArrayList<>();


    @Override
    public void onEnable() {

        saveDefaultConfig();
        reloadConfig();
        vault = new Vault();
        instance = this;
        setupCasino();
        setupEconomy();
        setupCommands();
        chatListener = new ChatListener(); // 初期化
        registerEvents();
        betManager = new BetManager();
    }

    private void setupCasino() {
        reloadConfig();
        saveResource("player.yml", false);
        loadSlotItems();
        loadBetSettings();
    }

    private void loadSlotItems() {
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
    }

    private void loadBetSettings() {
        minBet = getConfig().getInt("minimum_bet", 10); // デフォルト値は10
        maxBet = getConfig().getInt("maximum_bet", 10000); // デフォルト値は10000
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
        getConfig().set("jackpot_amount", 5000); // リセット時の初期金額
        saveConfig();
    }

    public static List<Material> getDefaultSlotItems() {
        return Arrays.asList(
                Material.COAL,            // 石炭
                Material.IRON_INGOT,      // 鉄インゴット
                Material.GOLD_INGOT,      // 金インゴット
                Material.DIAMOND,         // ダイヤモンド
                Material.NETHERITE_SCRAP  // ネザライトのかけら
        );
    }

    public int getMinBet() {
        return minBet;
    }

    public int getMaxBet() {
        return maxBet;
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
        getServer().getPluginManager().registerEvents(new BlackjackChatListener(this), this);
        CasinoGUI casinoGUI = new CasinoGUI(this); // CasinoGUI のインスタンスを生成し、プラグインのインスタンスを渡す
        getServer().getPluginManager().registerEvents(casinoGUI, this);
        getServer().getPluginManager().registerEvents(new RouletteGUIListener(), this);
        if (chatListener == null) {
            chatListener = new ChatListener();
        }
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    public Casino() {
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
        getCommand("setjackpot").setExecutor(new JackpotCommand());
        getCommand("jackpotrate").setExecutor(new JackpotRateCommand());
        getCommand("checkchip").setExecutor(new CheckChip());
    }

    public void addChips(Player player, int amount) {
        String playerUUID = player.getUniqueId().toString();
        int currentChips = getConfig().getInt("players." + playerUUID + ".chips", 0);
        getConfig().set("players." + playerUUID + ".chips", currentChips + amount);
        saveChipsConfig();
        player.sendMessage("あなたは " + amount + " チップを獲得しました！現在のチップ数: " + (currentChips + amount));
    }

    private void saveChipsConfig() {
        try {
            getConfig().save(new File(getDataFolder(), "player.yml"));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Exception caught: " + e.getMessage());
        }
    }

    public boolean removeChips(Player player, int amount) {
        String playerUUID = player.getUniqueId().toString();
        int currentChips = getConfig().getInt("players." + playerUUID + ".chips", 0);
        // チップ数が足りているか確認
        if (currentChips >= amount) {
            getConfig().set("players." + playerUUID + ".chips", currentChips - amount);
            saveChipsConfig();
            player.sendMessage("あなたは " + amount + " チップを失いました。現在のチップ数: " + (currentChips - amount));
        } else {
            player.sendMessage("チップが足りません。");
        }
        return false;
    }

    public int getChips(Player player) {
        String playerUUID = player.getUniqueId().toString();
        return getConfig().getInt("players." + playerUUID + ".chips", 0);
    }

    public void saveConfig() {
        try {
            getConfig().save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Exception caught: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Plugin停止時の処理を追加します

        // 必要に応じて設定を保存します
        saveConfig();
        // 必要に応じて他のリソースをクリーンアップします
        chatListener = null;
        vault = null;
        betManager = null;
        instance = null;
        // その他終了時に必要なロジックを追加
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public void setJackpotAmount(int newJackpotAmount) {
        getConfig().set("jackpot_amount", newJackpotAmount);
        saveConfig();
    }

    public void setMinimumBet(int minBet) {
        this.minBet = minBet;
        getConfig().set("minimum_bet", minBet);
        saveConfig();
    }

    public void setMaximumBet(int maxBet) {
        this.maxBet = maxBet;
        getConfig().set("maximum_bet", maxBet);
        saveConfig();
    }

    public void transferChips(Player fromPlayer, Player toPlayer, int amount) {
        if (removeChips(fromPlayer, amount)) {
            addChips(toPlayer, amount);
            fromPlayer.sendMessage("あなたは " + toPlayer.getName() + " に " + amount + " チップを送信しました。");
            toPlayer.sendMessage("あなたは " + fromPlayer.getName() + " から " + amount + " チップを受け取りました。");
        } else {
            fromPlayer.sendMessage("チップの送信に失敗しました。チップの数を確認してください。");
        }
    }

    // New method
    public void processBet(Player player, int amount) {
        if (amount < getMinBet()) {
            player.sendMessage("賭け金が最小賭け金額を下回っています。");
            return;
        }

        if (amount > getMaxBet()) {
            player.sendMessage("賭け金が最大賭け金額を上回っています。");
            return;
        }

        if (!vault.has(player, amount)) {
            player.sendMessage("所持金が足りません。");
            return;
        }

        vault.withdraw(player, amount);
        addToJackpot(amount / 10); // Example: add 10% of the bet amount to the jackpot
        player.sendMessage("賭け金 " + amount + " を受け取りました。");
    }

    public ChipManager getChipManager() {
        return new ChipManager();
    }

    public List<Material> getSlotItems() {
        if (slotItems.isEmpty()) {
            // デフォルトのスロットアイテムを返す
            return Arrays.asList(
                    Material.COAL,            // 石炭
                    Material.IRON_INGOT,      // 鉄インゴット
                    Material.GOLD_INGOT,      // 金インゴット
                    Material.DIAMOND,         // ダイヤモンド
                    Material.NETHERITE_SCRAP  // ネザライトのかけら
            );
        }
        // カスタム設定済みのスロットアイテムを返す
        return new ArrayList<>(slotItems);
    }

}
