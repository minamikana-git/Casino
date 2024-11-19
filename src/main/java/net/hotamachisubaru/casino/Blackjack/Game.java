package net.hotamachisubaru.casino.Blackjack;

import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game implements Listener {
    private final Player player;
    private final Deck deck;
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();
    private final Economy economy;
    private double bet;
    private int playerScore = 0;
    private int dealerScore = 0;

    public Game(Player player, Economy economy, double bet) {
        this.player = player;
        this.deck = new Deck();
        this.deck.shuffle();
        this.economy = economy;
        this.bet = bet;
        Bukkit.getPluginManager().registerEvents(this, Casino.getInstance());
    }

    public void startGame(double betAmount) {
        double playerBalance = economy.getBalance(player);
        if (betAmount > playerBalance) {
            player.sendMessage("所持金よりも多い金額は賭けられません。");
            return;
        }
        this.bet = betAmount;
        EconomyResponse response = economy.withdrawPlayer(player, bet);
        if (!response.transactionSuccess()) {
            player.sendMessage("所持金が不足しています：" + response.errorMessage);
            return;
        }

        //カードを配る処理
        playerHand.clear();
        dealerHand.clear();
        if (!dealInitialCards()) {
            player.sendMessage("デッキに十分なカードがありません。ゲームを中止します。");
            return;
        }

        updateScores();
        displayHands();
        player.sendMessage("あなたは " + betAmount + " を賭けました。ブラックジャックを開始します！");
    }

    private boolean dealInitialCards() {
        if (deck.getCards().size() < 4) {
            return false;
        }
        playerHand.add(deck.drawCard());
        playerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        return true;
    }

    private void updateScores() {
        playerScore = calculateHandValue(playerHand);
        dealerScore = calculateHandValue(dealerHand);
    }

    private int calculateHandValue(List<Card> hand) {
        int value = 0;
        int aceCount = 0;
        for (Card card : hand) {
            value += card.getValue();
            if (card.getName().equals("A")) {
                aceCount++;
            }
        }
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        return value;
    }

    private void displayHands() {
        player.sendMessage("あなたの手札: " + handToString(playerHand) + " （合計: " + playerScore + "）");
        player.sendMessage("ディーラーの手札: " + dealerHand.get(0).getName() + " と隠されたカード");
    }

    private String handToString(List<Card> hand) {
        return hand.stream().map(Card::getName).collect(Collectors.joining(", "));
    }

    public void hit() {
        if (deck.getCards().isEmpty()) {
            player.sendMessage("デッキが空です。");
            determineWinner();
            return;
        }
        playerHand.add(deck.drawCard());
        updateScores();
        displayHands();
        if (playerScore > 21) {
            player.sendMessage("あなたはバーストしました！負けです。");
            endGame(false);
        }
    }

    public void stand() {
        player.sendMessage("あなたはスタンドしました。ディーラーのターンです。");
        player.sendMessage("ディーラーの手札: " + handToString(dealerHand) + " （合計: " + dealerScore + "）");
        while (dealerScore < 17) {
            if (deck.getCards().isEmpty()) {
                player.sendMessage("デッキが空です。");
                break;
            }
            dealerHand.add(deck.drawCard());
            updateScores();
            player.sendMessage("ディーラーがカードを引きました。現在の手札: " + handToString(dealerHand) + " （合計: " + dealerScore + "）");
        }
        determineWinner();
    }

    private void determineWinner() {
        if (dealerScore > 21 || playerScore > dealerScore) {
            player.sendMessage("おめでとうございます！あなたの勝ちです！");
            endGame(true);
        } else if (playerScore < dealerScore) {
            player.sendMessage("残念、あなたの負けです。");
            endGame(false);
        } else {
            player.sendMessage("引き分けです！");
            economy.depositPlayer(player, bet);
            endGame(false);
        }
    }

    private void endGame(boolean won) {
        if (won) {
            economy.depositPlayer(player, bet * 2);
        }
        player.sendMessage("ブラックジャックゲームが終了しました。所持金: " + economy.getBalance(player));
        askForRestart();
    }

    private void askForRestart() {
        player.sendMessage("もう一度挑戦しますか？（はい/いいえ）");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        if (chatPlayer.equals(player) && (message.equals("はい") || message.equals("いいえ"))) {
            event.setCancelled(true);
            if (message.equals("はい")) {
                player.sendMessage("賭け金を変更しますか？（変更しない場合はそのまま進行します。）");
                askForBetChange();
            } else {
                player.sendMessage("ゲームを終了します。");
                HandlerList.unregisterAll(this);
            }
        }
    }

    private void askForBetChange() {
        player.sendMessage("賭け金を変更したい場合は新しい賭け金を入力してください。変更しない場合はそのままゲームを再開します。");
    }

    @EventHandler
    public void onBetInput(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        if (chatPlayer.equals(player)) {
            try {
                double newBet = Double.parseDouble(message);
                if (newBet <= economy.getBalance(player) && newBet > 0) {
                    bet = newBet;
                    player.sendMessage("賭け金が " + newBet + " に変更されました。ゲームを再開します！");
                    startGame(newBet);
                } else {
                    player.sendMessage("無効な賭け金です。所持金より多くは賭けられません。");
                    askForBetChange();
                }
            } catch (NumberFormatException e) {
                player.sendMessage("無効な入力です。正しい金額を入力してください。");
                askForBetChange();
            }
        }
    }
}