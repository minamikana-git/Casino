package net.hotamachisubaru.casino.Commands;



import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BuyChip implements CommandExecutor {
    Casino plugin = Casino.getInstance();
    Vault vault = plugin.getVault();

    public BuyChip(Casino plugin) {
        this.plugin = plugin;
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

        // チップ1枚あたりの価格
        double pricePerChip = 10.0; // 例: 1チップ = 10通貨
        double totalCost = chipAmount * pricePerChip;

        // プレイヤーが十分な金額を持っているか確認
        if (!vault.withdraw(player, totalCost)) {
            player.sendMessage("チップを購入するために必要な " + totalCost + " 通貨を持っていません。");
            return false;
        }

        // チップを作成してインベントリに追加
        ItemStack chipItem = createChipItem(chipAmount);
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
    private ItemStack createChipItem(int amount) {
        ItemStack chip = new ItemStack(Material.GOLD_INGOT, amount);
        ItemMeta meta = chip.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6チップ"); // チップの名前を設定 (色付き)
            chip.setItemMeta(meta);
        }
        return chip;
    }
}
