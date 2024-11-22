package net.hotamachisubaru.casino.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Consumer<String>> chatListeners = new HashMap<>();

    public ChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    // プレイヤーのUUIDをキーにしてメッセージ処理を登録
    public void waitForInput(Player player, Consumer<String> onMessageReceived) {
        chatListeners.put(player.getUniqueId(), onMessageReceived);
        player.sendMessage("チャットに入力してください。");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        // リスナーが登録されているプレイヤーのみ処理する
        if (chatListeners.containsKey(playerId)) {
            // イベントをキャンセルして他のリスナーに影響しないようにする
            event.setCancelled(true);

            // 入力メッセージを取得して登録された処理を実行
            Consumer<String> action = chatListeners.get(playerId);
            action.accept(event.getMessage());

            // 処理が完了したらリスナーを解除
            chatListeners.remove(playerId);
        }
    }
}
