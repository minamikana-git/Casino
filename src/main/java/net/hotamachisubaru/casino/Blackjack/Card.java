package net.hotamachisubaru.casino.Blackjack;

public class Card {
    private int value; // カードの値 (1~13)
    private int suit;  // スート (0~3)

    public Card(int value, int suit) {
        this.value = value;
        this.suit = suit;
    }

    public Card(String name, int value) {
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return "値: " + value + ", スート: " + suit;
    }
}
