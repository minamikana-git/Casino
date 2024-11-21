package net.hotamachisubaru.casino.Manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BetManager {
    private final Map<Player, Integer> playerBets = new HashMap<>();

    public void setBet(Player player, int amount) {
        playerBets.put(player, amount);
    }

    public int getBet(Player player) {
        return playerBets.getOrDefault(player, 0);
    }

    public void clearBet(Player player) {
        playerBets.remove(player);
    }
}
