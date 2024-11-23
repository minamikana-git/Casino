package net.hotamachisubaru.casino.Listener;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {
    private static final String ALREADY_WAITING_MESSAGE = "既に入力待ちの操作があります。完了してからもう一度試してください。";
    private static final String INPUT_PROMPT_MESSAGE = "チャットに入力してください。";
    private static final String TIMEOUT_MESSAGE = "入力の時間切れです。もう一度試してください。";
    private static final long TIMEOUT_DELAY = 20L * 60; // 60秒後

    private final ConcurrentHashMap<UUID, Consumer<String>> listeners = new ConcurrentHashMap<>();

    public void waitForInput(@NotNull Player player, @NotNull Consumer<String> consumer) {
        UUID playerId = player.getUniqueId();
        if (checkIfAlreadyWaiting(player, playerId)) return;

        listeners.put(playerId, consumer);
        player.sendMessage(INPUT_PROMPT_MESSAGE);
        scheduleTimeout(player, playerId);
    }

    private boolean checkIfAlreadyWaiting(Player player, UUID playerId) {
        if (listeners.containsKey(playerId)) {
            player.sendMessage(ALREADY_WAITING_MESSAGE);
            return true;
        }
        return false;
    }

    private void scheduleTimeout(@NotNull Player player, UUID playerId) {
        Bukkit.getScheduler().runTaskLater(Casino.getInstance(), () -> {
            if (listeners.containsKey(playerId)) {
                listeners.remove(playerId);
                player.sendMessage(TIMEOUT_MESSAGE);
            }
        }, TIMEOUT_DELAY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (listeners.containsKey(uuid)) {
            Consumer<String> consumer = listeners.remove(uuid);
            consumer.accept(event.getMessage());
            event.setCancelled(true); // チャットをキャンセル
        }
    }

}