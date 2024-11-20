package net.hotamachisubaru.casino.Commands

import net.hotamachisubaru.casino.Blackjack.Game
import net.hotamachisubaru.casino.Casino
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BlackjackCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<String?>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤー専用です。")
            return true
        }

        val player = sender

        // Economyの初期化（Vaultが正しくセットアップされている必要があります）
        val economy = Casino.getEconomy()

        // 引数が1つの場合（賭け金を指定）
        var betAmount = 100.0 // デフォルト値
        if (args.size == 1) {
            try {
                betAmount = args[0]!!.toDouble()
            } catch (e: NumberFormatException) {
                player.sendMessage("無効な賭け金です。数値を入力してください。")
                return false
            }
        }

        // プレイヤーの所持金を確認
        val balance = economy.getBalance(player)
        player.sendMessage("現在の所持金: " + balance)

        // 所持金が賭け金に足りているか確認
        if (balance < betAmount) {
            player.sendMessage("所持金が不足しています。ブラックジャックを開始できません。")
            return true
        }

        // ゲームを開始
        val blackjackGame = Game(player, economy, betAmount)
        blackjackGame.startGame(betAmount)

        return true
    }
}
