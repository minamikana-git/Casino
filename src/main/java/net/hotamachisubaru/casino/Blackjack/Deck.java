package net.hotamachisubaru.casino.Blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    public final List<Card> cards = new ArrayList<>();

    public Deck() {
        // カードの生成（2から10、J, Q, K, A）
        String[] names = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        for (int i = 0; i < names.length; i++) {
            cards.add(new Card(names[i], values[i]));
        }
    }

    // カードをランダムにシャッフルする
    public void shuffle() {
        Collections.shuffle(cards);
    }

    // カードを1枚引く
    public Card drawCard() {
        if (cards.isEmpty()) {
            return null; // デッキが空の場合
        }
        return cards.remove(0);
    }
    public List<Card> getCards() {
        return cards;
    }
}

