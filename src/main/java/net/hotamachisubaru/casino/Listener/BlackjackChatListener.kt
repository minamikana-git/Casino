package net.hotamachisubaru.casino.Listener

import net.hotamachisubaru.casino.Blackjack.Game
import net.hotamachisubaru.casino.Casino
import net.hotamachisubaru.casino.economy.EconomyHelper
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.concurrent.ConcurrentHashMap

class BlackjackChatListener(private val plugin: Casino) : Listener {
    private val activeGames: MutableMap<Player?, Game> = ConcurrentHashMap<Player?, Game>()
    private val preGameStates: MutableMap<Player?, PreGameState> = ConcurrentHashMap<Player?, PreGameState>()

    private class PreGameState(val balance: Double)

    fun startGame(player: Player) {
        if (activeGames.containsKey(player) || preGameStates.containsKey(player)) {
            player.sendMessage("現在、ゲームが進行中です。新しいゲームを開始するには終了してください。")
            return
        }
        val balance = EconomyHelper.getBalance(player) // EconomyHelperで所持金を取得
        if (balance <= 0) {
            player.sendMessage("所持金が足りません！ゲームを開始できません。")
            return
        }
        // 賭け金を設定する
        player.sendMessage("かけ金を入力してください。所持金: " + balance)
        preGameStates.put(player, PreGameState(balance)) // ゲーム開始準備
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.getPlayer()
        val message = event.getMessage()
        if (preGameStates.containsKey(player) || activeGames.containsKey(player)) {
            event.setCancelled(true)
            if (preGameStates.containsKey(player)) {
                // かけ金の入力処理
                try {
                    val betAmount = message.toDouble()
                    val preGameState: PreGameState = preGameStates.get(player)!!
                    val balance = preGameState.balance
                    if (betAmount <= 0) {
                        player.sendMessage("賭け金は正の数でなければなりません。")
                        return
                    }
                    if (betAmount > balance) {
                        player.sendMessage("所持金を超える金額は賭けられません。")
                        return
                    }
                    // ゲーム開始
                    val game = Game(player, EconomyHelper.getEconomy(), betAmount) // Economyオブジェクトと賭け金を渡す
                    activeGames.put(player, game)
                    preGameStates.remove(player)
                    player.sendMessage("ブラックジャックゲームが開始されました！「ヒット」または「スタンド」と入力してください。")
                } catch (e: NumberFormatException) {
                    player.sendMessage("無効な金額です。整数で入力してください。")
                }
            } else if (activeGames.containsKey(player)) {
                // ゲーム進行
                val game: Game = activeGames.get(player)!!
                when (message) {
                    "ヒット" -> Bukkit.getScheduler().runTask(plugin, Runnable { game.hit() })
                    "スタンド" -> Bukkit.getScheduler().runTask(plugin, Runnable {
                        game.stand()
                        activeGames.remove(player) // 終了後は削除
                    })

                    else -> player.sendMessage("無効なコマンドです。「ヒット」または「スタンド」と入力してください。")
                }
            }
        }
    }
}