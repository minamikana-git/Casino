package net.hotamachisubaru.casino.Listener

import net.hotamachisubaru.casino.Blackjack.Game
import net.hotamachisubaru.casino.economy.EconomyHelper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class BlackjackListener(private val game: Game) : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.getPlayer()
        val message = event.getMessage()

        // 賭け金として数値を受け取る
        try {
            val betAmount = message.toDouble()
            if (betAmount > 0 && betAmount <= EconomyHelper.getBalance(player)) {
                // 賭け金がプレイヤーの所持金以内であればゲームを開始
                game.startGame(betAmount)
                player.sendMessage("ブラックジャックの賭け金は " + betAmount + " に設定されました。ゲーム開始！")
            } else {
                player.sendMessage("所持金が足りないか、無効な賭け金です。")
            }
        } catch (e: NumberFormatException) {
            player.sendMessage("無効な賭け金の形式です。数字を入力してください。")
        }

        event.setCancelled(true) // チャットイベントをキャンセル（メッセージがそのままチャットに送信されないようにする）
    }
}
