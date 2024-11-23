package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Manager.ChipManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;

public class BuyChip implements CommandExecutor {
    private static final double PRICE_PER_CHIP = 10.0; // 例: 1チップ = 10通貨
    private final Casino plugin;
    private final Economy vault;
    private final ChipManager chipManager;
    private File file;
    private FileConfiguration config;

    public BuyChip(Casino plugin) {
        this.plugin = plugin;
        this.vault = plugin.getEconomy();
        this.chipManager = plugin.getChipManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
            return false;
        }
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("使用方法: /buychip <チップ枚数>");
            return false;
        }

        int chipAmount;
        try {
            chipAmount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("有効な数値を入力してください。");
            return false;
        }

        if (chipAmount <= 0) {
            player.sendMessage("購入するチップの数は1以上にしてください。");
            return false;
        }

        double totalCost = chipAmount * PRICE_PER_CHIP;

        if (!vault.withdrawPlayer(player, totalCost).transactionSuccess()) {
            player.sendMessage("チップを購入するために必要な " + totalCost + " 通貨を持っていません。");
            return false;
        }

        // 現在のチップを取得
        int currentChips = chipManager.getChips(player);
        // 新しいチップ数を計算
        int newTotalChips = currentChips + chipAmount;
        // 更新を反映
        chipManager.setChips(player, newTotalChips);

        player.sendMessage("チップを " + chipAmount + " 枚購入しました (合計金額: " + totalCost + ")。");
        player.sendMessage("現在のチップ数: " + newTotalChips + " 枚");
        ItemStack chipItem = createChipStack(chipAmount);
        player.getInventory().addItem(chipItem);
        player.sendMessage("チップを " + chipAmount + " 枚購入しました (合計金額: " + totalCost + ")。");
        return true;
    }

    public void loadChipConfig() {
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

    // チップを設定（直接変更）
    public void setChips(Player player, int amount) {
        config.set(player.getUniqueId().toString(), amount);
        saveConfig();
    }

    // チップを保存
    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("チップの保存に失敗しました: " + e.getMessage());
        }
    }

    /**
     * チップアイテムを作成します。
     *
     * @param amount 作成するチップの数
     * @return 作成されたチップアイテム
     */
    private ItemStack createChipStack(int amount) {
        ItemStack chip = new ItemStack(Material.GOLD_INGOT, amount);
        ItemMeta meta = chip.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6チップ"); // チップの名前を設定 (色付き)
            chip.setItemMeta(meta);
        }
        return chip;
    }
}