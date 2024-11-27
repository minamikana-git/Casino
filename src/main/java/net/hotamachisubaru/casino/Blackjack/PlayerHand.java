package net.hotamachisubaru.casino.Blackjack;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class PlayerHand {
    private List<String> hand;

    // コンストラクタで手札を初期化
    public PlayerHand() {
        this.hand = new ArrayList<>();
    }

    // カードを手札に追加
    public void addCard(String card) {
        hand.add(card);
    }

    // 手札を表示（デバッグ用）
    public void showHand() {
        Bukkit.getLogger().info("Hand: " + hand);
    }

    // 手札をリセット
    public void resetHand() {
        hand.clear();
    }

    // 手札のリストを取得
    public List<String> getHand() {
        return hand;
    }

    public int getTotalValue() {
        return getTotalValue();
    }
}

