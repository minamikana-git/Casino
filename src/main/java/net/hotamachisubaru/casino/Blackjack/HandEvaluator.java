package net.hotamachisubaru.casino.Blackjack;

import java.util.List;

public class HandEvaluator {
    // 手札の合計スコアを計算
    public static int calculateScore(List<String> hand) {
        int score = 0;
        int aceCount = 0;

        for (String card : hand) {
            String rank = card.split(" ")[0];

            switch (rank) {
                case "Jack":
                case "Queen":
                case "King":
                    score += 10;
                    break;
                case "Ace":
                    score += 11; // エースは一旦11としてカウント
                    aceCount++;
                    break;
                default:
                    score += Integer.parseInt(rank);
            }
        }

        // エースを1に変換する処理（必要な場合）
        while (score > 21 && aceCount > 0) {
            score -= 10; // 11を1に変える
            aceCount--;
        }

        return score;
    }
}
