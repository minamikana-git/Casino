package net.hotamachisubaru.casino.Slot;

import org.bukkit.Material;

public class SlotResult {
    private final boolean win;
    private final Material rewardType;

    public SlotResult(boolean win, Material rewardType) {
        this.win = win;
        this.rewardType = rewardType;
    }

    public boolean isWin() {
        return win;
    }

    public Material getRewardType() {
        return rewardType;
    }
}

