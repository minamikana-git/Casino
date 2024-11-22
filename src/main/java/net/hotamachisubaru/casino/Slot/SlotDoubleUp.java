package net.hotamachisubaru.casino.Slot;


import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Listener.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class SlotDoubleUp {

    public static void attemptDoubleUp(Player player, int winnings) {
        player.sendMessage("ダブルアップに挑戦しますか？(はい/いいえ)");
        ChatListener chatListener = Casino.getInstance().getChatListener();
        if (chatListener != null) {
            chatListener.waitForInput(player, input -> handleDoubleUpResponse(player, winnings, input));
        } else {
            player.sendMessage("チャットリスナーの取得に失敗しました。");
        }
    }

    private static void handleDoubleUpResponse(Player player, int winnings, String input) {
        if ("はい".equalsIgnoreCase(input)) {
            double successRate = Casino.getInstance().getConfig().getDouble("double_up_success_rate", 0.5);
            if (ThreadLocalRandom.current().nextDouble() < successRate) {
                winnings *= 2;
                player.sendMessage("ダブルアップ成功！獲得額: " + winnings);
            } else {
                winnings = 0;
                player.sendMessage("ダブルアップ失敗... 獲得額はゼロになりました。");
            }

            // 結果をチップまたは経済システムに反映
            final int finalWinnings = winnings;
            Bukkit.getScheduler().runTask(Casino.getInstance(), () -> {
                Casino.getInstance().getEconomy().depositPlayer(player, finalWinnings);
                player.sendMessage("最終獲得額 " + finalWinnings + " がアカウントに追加されました！");
            });

        } else if ("いいえ".equalsIgnoreCase(input)) {
            player.sendMessage("ダブルアップをキャンセルしました。 獲得額: " + winnings);
            int finalWinnings1 = winnings;
            Bukkit.getScheduler().runTask(Casino.getInstance(), () -> {
                Casino.getInstance().getEconomy().depositPlayer(player, finalWinnings1);
                player.sendMessage("最終獲得額 " + finalWinnings1 + " がアカウントに追加されました！");
            });
        } else {
            player.sendMessage("無効な入力です。「はい」か「いいえ」で答えてください。");
            attemptDoubleUp(player, winnings);
        }
    }
}
