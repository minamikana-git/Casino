package net.hotamachisubaru.casino.Blackjack;

import org.bukkit.Bukkit;
import java.util.Scanner;

public class Game {
    public static void main(String[] args) {
        CardDeck cardDeck = new CardDeck();
        PlayerHand playerHand = new PlayerHand();
        PlayerHand dealerHand = new PlayerHand();
        Scanner scanner = new Scanner(System.in);

        // 初期配布
        for (int i = 0; i < 2; i++) {
            playerHand.addCard(cardDeck.drawCard());
            dealerHand.addCard(cardDeck.drawCard());
        }

        // プレイヤーターン
        Bukkit.getLogger().info("プレイヤーの番です:");
        boolean playerBust = playerTurn(playerHand, cardDeck, scanner);

        if (!playerBust) {
            // ディーラーターン
            Bukkit.getLogger().info("\nディーラーの番です:");
            dealerTurn(dealerHand, cardDeck);
        }

        // 勝敗判定
        determineWinner(playerHand, dealerHand);

        scanner.close();
    }

    // プレイヤーターン処理
    private static boolean playerTurn(PlayerHand playerHand, CardDeck cardDeck, Scanner scanner) {
        while (true) {
            Bukkit.getLogger().info("あなたのハンド: " + playerHand.getHand());
            int score = HandEvaluator.calculateScore(playerHand.getHand());
            Bukkit.getLogger().info("現在のスコア: " + score);

            if (score > 21) {
                Bukkit.getLogger().info("あなたはバーストしました!");
                return true;
            }

            Bukkit.getLogger().info("あなたはヒットしますか？スタンドしますか? (h/s) ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("h")) {
                playerHand.addCard(cardDeck.drawCard());
            } else if (choice.equalsIgnoreCase("s")) {
                break;
            } else {
                Bukkit.getLogger().info("無効な入力です。 'h' もしくは 's'で入力してください。");
            }
        }
        return false;
    }

    // ディーラーターン処理
    private static void dealerTurn(PlayerHand dealerHand, CardDeck cardDeck) {
        while (true) {
            int score = HandEvaluator.calculateScore(dealerHand.getHand());
            Bukkit.getLogger().info("ディーラーのハンド: " + dealerHand.getHand());
            Bukkit.getLogger().info("ディーラーのスコア: " + score);

            if (score >= 17) {
                Bukkit.getLogger().info("ディーラーはスタンドしました。");
                break;
            }

            Bukkit.getLogger().info("ディーラーがヒットしました。");
            dealerHand.addCard(cardDeck.drawCard());
        }
    }

    // 勝敗判定
    private static void determineWinner(PlayerHand playerHand, PlayerHand dealerHand) {
        int playerScore = HandEvaluator.calculateScore(playerHand.getHand());
        int dealerScore = HandEvaluator.calculateScore(dealerHand.getHand());

        Bukkit.getLogger().info("\n最終結果");
        Bukkit.getLogger().info("あなたのハンド: " + playerHand.getHand() + " (Score: " + playerScore + ")");
        Bukkit.getLogger().info("ディーラーのハンド: " + dealerHand.getHand() + " (Score: " + dealerScore + ")");

        if (playerScore > 21) {
            Bukkit.getLogger().info("バーストしました。あなたの負けです。");
        } else if (dealerScore > 21 || playerScore > dealerScore) {
            Bukkit.getLogger().info("あなたの勝ちです!");
        } else if (playerScore < dealerScore) {
            Bukkit.getLogger().info("負けてしまいました。");
        } else {
            Bukkit.getLogger().info("引き分け!");
        }
    }
}