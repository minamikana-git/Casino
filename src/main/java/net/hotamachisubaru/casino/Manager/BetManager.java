package net.hotamachisubaru.casino.Manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BetManager {
    private final Map<Player, Integer> playerBetAmounts = new HashMap<>();
    private static final int DEFAULT_BET = 0;

    public void setPlayerBet(Player player, int amount) {
        playerBetAmounts.put(player, amount);
    }

    public int getPlayerBet(Player player) {
        return playerBetAmounts.getOrDefault(player, DEFAULT_BET);
    }

    public void clearPlayerBet(Player player) {
        playerBetAmounts.remove(player);
    }

    public void clearBet(Player player) {
        playerBetAmounts.remove(player);
    }

    public Integer getBetAmount(Player player) {
        return playerBetAmounts.get(player);
    }

    public void setBet(Player player, int betAmount) {
        playerBetAmounts.put(player, betAmount);
    }
}