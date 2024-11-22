package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.block.Vault;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static net.hotamachisubaru.casino.Casino.economy;

public class BuyChip implements CommandExecutor {
    private static final double PRICE_PER_CHIP = 10.0; // 例: 1チップ = 10通貨
    private final Casino plugin;
    private final Economy vault;

    public BuyChip(Casino plugin) {
        this.plugin = plugin;
        this.vault = plugin.getEconomy();
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

        if (vault.withdrawPlayer(player, totalCost).transactionSuccess()) {
            player.sendMessage("チップを購入するために必要な " + totalCost + " 通貨を持っていません。");
            return false;
        }

        ItemStack chipItem = createChipStack(chipAmount);
        player.getInventory().addItem(chipItem);
        player.sendMessage("チップを " + chipAmount + " 枚購入しました (合計金額: " + totalCost + ")。");
        return true;
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