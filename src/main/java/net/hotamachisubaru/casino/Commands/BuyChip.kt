package net.hotamachisubaru.casino.Commands

import net.hotamachisubaru.casino.Casino
import net.hotamachisubaru.casino.Vault.Vault
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BuyChip(plugin: Casino) : CommandExecutor {
    private val plugin: Casino?
    private val vault: Vault

    init {
        this.plugin = plugin
        this.vault = plugin.getVault()
    }

    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<String?>): Boolean {
        // デバッグ: コマンド実行ログ
        Bukkit.getLogger().info("BuyChipコマンドが実行されました: " + label + " args=" + String.join(" ", *args))

        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。")
            Bukkit.getLogger().warning("BuyChipコマンドはプレイヤー以外から実行されました。")
            return false
        }

        val player = sender

        if (args.size != 1) {
            player.sendMessage("使用方法: /buychip <チップ枚数>")
            Bukkit.getLogger().info(player.getName() + " が無効な引数でBuyChipコマンドを実行しました。")
            return false
        }

        val chipAmount: Int
        try {
            chipAmount = args[0]!!.toInt()
        } catch (e: NumberFormatException) {
            player.sendMessage("有効な数値を入力してください。")
            Bukkit.getLogger().info(player.getName() + " が数値以外の入力を使用しました: " + args[0])
            return false
        }

        if (chipAmount <= 0) {
            player.sendMessage("購入するチップの数は1以上にしてください。")
            Bukkit.getLogger().info(player.getName() + " が無効なチップ枚数 (" + chipAmount + ") を指定しました。")
            return false
        }

        // チップ1枚あたりの価格
        val pricePerChip = 10.0 // 例: 1チップ = 10通貨
        val totalCost = chipAmount * pricePerChip

        // デバッグ: 購入処理の詳細ログ
        Bukkit.getLogger()
            .info(player.getName() + " が " + chipAmount + " 枚のチップを購入しようとしています (合計コスト: " + totalCost + ")。")

        // プレイヤーが十分な金額を持っているか確認
        if (!vault.withdraw(player, totalCost)) {
            player.sendMessage("チップを購入するために必要な " + totalCost + " 通貨を持っていません。")
            Bukkit.getLogger().info(player.getName() + " の所持金が不足しています。")
            return false
        }

        // チップを作成してインベントリに追加
        val chipItem = createChipItem(chipAmount)
        player.getInventory().addItem(chipItem)

        player.sendMessage("チップを " + chipAmount + " 枚購入しました (合計金額: " + totalCost + ")。")
        Bukkit.getLogger().info(player.getName() + " がチップを購入しました: " + chipAmount + " 枚。")

        return true
    }

    /**
     * チップアイテムを作成します。
     *
     * @param amount 作成するチップの数
     * @return 作成されたチップアイテム
     */
    private fun createChipItem(amount: Int): ItemStack {
        val chip = ItemStack(Material.GOLD_INGOT, amount)
        val meta = chip.getItemMeta()
        if (meta != null) {
            meta.setDisplayName("§6チップ") // チップの名前を設定 (色付き)
            chip.setItemMeta(meta)
        }
        Bukkit.getLogger().info("チップアイテムが作成されました: " + amount + " 個。")
        return chip
    }
}
