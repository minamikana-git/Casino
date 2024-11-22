package net.hotamachisubaru.casino.Slot;

import org.bukkit.Material;

import java.util.Map;

// クラス名を目的に合わせてリネーム
public class SlotResult {
    // 不変性を保つために一貫した修飾子 (final) を使用
    private final boolean isWinning;
    private final Map<Material, Integer> rewardItems;

    // 明確さのために記述的なパラメータ名を使用
    public SlotResult(boolean isWinning, Map<Material, Integer> rewardItems) {
        this.isWinning = isWinning;
        this.rewardItems = rewardItems;
    }

    // 明確さのために一貫したメソッド命名
    public boolean isWinning() {
        return isWinning;
    }

    public Map<Material, Integer> getRewardItems() {
        return rewardItems;
    }
}