package net.hotamachisubaru.casino.Blackjack;

public class Card {
    private final String name; // カード名（例： "2", "K", "A"）
    private final int value;   // カードの値（例： 2, 10, 11）

    public Card(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
