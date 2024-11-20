package net.hotamachisubaru.casino.Blackjack;



public class Card {
    private final Suit suit;
    private final int value;

    public Card(Suit suit, int value) {
        this.suit = suit;
        this.value = value;
    }

    public int getValue() {
        return value > 10 ? 10 : value; // J, Q, K は 10 として扱う
    }

    @Override
    public String toString() {
        return suit + " " + value;
    }

    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }
}
