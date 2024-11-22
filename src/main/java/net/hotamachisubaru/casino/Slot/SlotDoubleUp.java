package net.hotamachisubaru.casino.Slot;


import java.util.concurrent.ThreadLocalRandom;

import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.Listener.ChatListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlotDoubleUp {

    public static void attemptDoubleUp(Player player, int winnings) {
        player.sendMessage("ダブルアップに挑戦しますか？(はい/いいえ)");
        ChatListener chatListener = Casino.getInstance().getChatListener();
        if (chatListener != null) {
            chatListener.waitForInput(player, (input) -> {
                handleDoubleUpResponse(player, winnings, input);
            });
        } else {
            player.sendMessage("チャットリスナーの取得に失敗しました。");
        }
    }

    private static void handleDoubleUpResponse(Player player, int winnings, String input) {
        if ("はい".equalsIgnoreCase(input)) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                winnings *= 2;
                player.sendMessage("ダブルアップ成功！獲得額: " + winnings);
            } else {
                winnings = 0;
                player.sendMessage("ダブルアップ失敗... 獲得額はゼロになりました。");
            }
        } else {
            player.sendMessage("ダブルアップをキャンセルしました。 獲得額: " + winnings);
        }

        try {
            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, winnings));
        } catch (Exception e) {
            player.sendMessage("チップの追加に失敗しました。管理者に連絡してください。");
            e.printStackTrace();
        }
    }

}