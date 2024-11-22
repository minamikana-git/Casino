package net.hotamachisubaru.casino.Listener;

import net.hotamachisubaru.casino.Blackjack.Game;
import net.hotamachisubaru.casino.economy.EconomyHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class BlackjackListener implements Listener {
    private final Game game;

    public BlackjackListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        handleBetMessage(player, message);
        event.setCancelled(true); // チャットイベントをキャンセル
    }

    private void handleBetMessage(Player player, String message) {
        try {
            double betAmount = Double.parseDouble(message);
            if (isValidBet(player, betAmount)) {
                game.startGame(betAmount);
                sendMessage(player, "ブラックジャックの賭け金は " + betAmount + " に設定されました。ゲーム開始！");
            } else {
                sendMessage(player, "所持金が足りないか、無効な賭け金です。");
            }
        } catch (NumberFormatException e) {
            sendMessage(player, "無効な賭け金の形式です。数字を入力してください。");
        }
    }

    private boolean isValidBet(Player player, double betAmount) {
        return betAmount > 0 && betAmount <= EconomyHelper.getBalance(player);
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }
}