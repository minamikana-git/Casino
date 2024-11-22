package net.hotamachisubaru.casino.Slot;

import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Listener.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

import static net.hotamachisubaru.casino.Slot.SlotMachine.plugin;

public class SlotDoubleUp {

    private static final String YES = "はい";
    private static final String NO = "いいえ";
    private static final double DEFAULT_SUCCESS_RATE = 0.4;
    private static final String DOUBLE_UP_PROMPT = "ダブルアップに挑戦しますか？(はい/いいえ)";
    private static final String CHAT_LISTENER_ERROR = "チャットリスナーの取得に失敗しました。";

    public static void askForDoubleUp(Player player, int betAmount, int attempt) {
        if (attempt > 5) {
            player.sendMessage("ダブルアップの上限に達しました！");
            return;
        }

        player.sendMessage("ダブルアップに挑戦しますか？ (はい/いいえ)");
        plugin.getChatListener().waitForInput(player, input -> {
            if ("はい".equals(input)) {
                SlotDoubleUp.attemptDoubleUp(player, betAmount * 2);
            } else {
                player.sendMessage("ゲーム終了、お疲れ様でした！");
            }
        });
    }


    public static void attemptDoubleUp(Player player, int winnings) {
        player.sendMessage(DOUBLE_UP_PROMPT);
        ChatListener chatListener = Casino.getInstance().getChatListener();
        if (chatListener != null) {
            chatListener.waitForInput(player, input -> handleDoubleUpResponse(player, winnings, input));
        } else {
            player.sendMessage(CHAT_LISTENER_ERROR);
        }
    }


    private static void handleDoubleUpResponse(Player player, int winnings, String input) {
        if (YES.equalsIgnoreCase(input)) {
            attemptDoubleUp(player, winnings);
        } else if (NO.equalsIgnoreCase(input)) {
            finalizeWinnings(player, winnings, winnings);
        } else {
            player.sendMessage("無効な入力です。「はい」か「いいえ」で答えてください。");
            attemptDoubleUp(player, winnings);
        }
    }


    private static void finalizeWinnings(Player player, int originalWinnings, int finalWinnings) {
        player.sendMessage("最終獲得額 " + finalWinnings + " がアカウントに追加されました！");
        Bukkit.getScheduler().runTask(Casino.getInstance(), () -> {
            Casino.getInstance().getEconomy().depositPlayer(player, finalWinnings);
        });
    }
}