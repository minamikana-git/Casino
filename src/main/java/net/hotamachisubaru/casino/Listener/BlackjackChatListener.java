package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Blackjack.Game;
import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.economy.EconomyHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlackjackChatListener implements Listener {
    private final Casino plugin;
    private final Map<Player, Game> activeGames = new ConcurrentHashMap<>();
    private final Map<Player, PreGameState> preGameStates = new ConcurrentHashMap<>();

    private static class PreGameState {
        private final double balance;

        private PreGameState(double balance) {
            this.balance = balance;
        }
    }

    public BlackjackChatListener(Casino plugin) {
        this.plugin = plugin;
    }

    public void startGame(Player player) {
        if (activeGames.containsKey(player) || preGameStates.containsKey(player)) {
            player.sendMessage("現在、ゲームが進行中です。新しいゲームを開始するには終了してください。");
            return;
        }
        double balance = EconomyHelper.getBalance(player); // EconomyHelperで所持金を取得
        if (balance <= 0) {
            player.sendMessage("所持金が足りません！ゲームを開始できません。");
            return;
        }
        // 賭け金を設定する
        player.sendMessage("かけ金を入力してください。所持金: " + balance);
        preGameStates.put(player, new PreGameState(balance)); // ゲーム開始準備
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (preGameStates.containsKey(player) || activeGames.containsKey(player)) {
            event.setCancelled(true);
            if (preGameStates.containsKey(player)) {
                // かけ金の入力処理
                try {
                    double betAmount = Double.parseDouble(message);
                    PreGameState preGameState = preGameStates.get(player);
                    double balance = preGameState.balance;
                    if (betAmount <= 0) {
                        player.sendMessage("賭け金は正の数でなければなりません。");
                        return;
                    }
                    if (betAmount > balance) {
                        player.sendMessage("所持金を超える金額は賭けられません。");
                        return;
                    }
                    // ゲーム開始
                    Game game = new Game(player, EconomyHelper.getEconomy(), betAmount); // Economyオブジェクトと賭け金を渡す
                    activeGames.put(player, game);
                    preGameStates.remove(player);
                    player.sendMessage("ブラックジャックゲームが開始されました！「ヒット」または「スタンド」と入力してください。");
                } catch (NumberFormatException e) {
                    player.sendMessage("無効な金額です。整数で入力してください。");
                }
            } else if (activeGames.containsKey(player)) {
                // ゲーム進行
                Game game = activeGames.get(player);
                switch (message) {
                    case "ヒット":
                        Bukkit.getScheduler().runTask(plugin, game::hit);
                        break;
                    case "スタンド":
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            game.stand();
                            activeGames.remove(player); // 終了後は削除
                        });
                        break;
                    default:
                        player.sendMessage("無効なコマンドです。「ヒット」または「スタンド」と入力してください。");
                }
            }
        }
    }
}