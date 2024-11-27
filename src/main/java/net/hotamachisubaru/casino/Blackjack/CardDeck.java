package net.hotamachisubaru.casino.Blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    private List<String> deck;

    // コンストラクタでデッキを初期化
    public CardDeck() {
        this.deck = new ArrayList<>();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

        // 52枚のカードを生成
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + " of " + suit);
            }
        }

        shuffleDeck();
    }

    // デッキをシャッフル
    public void shuffleDeck() {
        Collections.shuffle(this.deck);
    }

    // カードを1枚引く
    public String drawCard() {
        if (deck.isEmpty()) {
            throw new IllegalStateException("Deck is empty!");
        }
        return deck.remove(0); // 山札の一番上を取り出す
    }

    // 残りのカード枚数を取得
    public int getRemainingCards() {
        return deck.size();
    }
}

