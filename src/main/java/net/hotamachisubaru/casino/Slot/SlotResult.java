package net.hotamachisubaru.casino.Slot;

import org.bukkit.Material;

import java.util.Map;

public class SlotResult {
    private final boolean win;
    private final Map<Material, Integer> rewards;

    public SlotResult(boolean win, Map<Material, Integer> rewards) {
        this.win = win;
        this.rewards = rewards;
    }

    public boolean isWin() {
        return win;
    }

    public Map<Material, Integer> getRewards() {
        return rewards;
    }
}