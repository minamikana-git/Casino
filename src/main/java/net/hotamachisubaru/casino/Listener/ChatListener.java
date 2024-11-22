package net.hotamachisubaru.casino.Listener;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    private final ConcurrentHashMap<UUID, Consumer<String>> listeners = new ConcurrentHashMap<>();

    public void waitForInput(Player player, Consumer<String> consumer) {
        if (listeners.containsKey(player.getUniqueId())) {
            player.sendMessage("既に入力待ちの操作があります。完了してからもう一度試してください。");
            return;
        }

        listeners.put(player.getUniqueId(), consumer);
        player.sendMessage("チャットに入力してください。");

        // タイムアウト処理
        Bukkit.getScheduler().runTaskLater(Casino.getInstance(), () -> {
            if (listeners.containsKey(player.getUniqueId())) {
                listeners.remove(player.getUniqueId());
                player.sendMessage("入力の時間切れです。もう一度試してください。");
            }
        }, 20L * 60); // 60秒後
    }

    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (listeners.containsKey(uuid)) {
            Consumer<String> consumer = listeners.get(uuid);
            listeners.remove(uuid); // 処理後に削除
            consumer.accept(event.getMessage());
            event.setCancelled(true); // チャットメッセージをキャンセル
        }
    }
}
