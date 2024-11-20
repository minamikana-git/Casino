package net.hotamachisubaru.casino.Slot;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class SlotMachine {

    public static final Casino plugin = Casino.getPlugin(Casino.class);

    public static void openSlotGUI(Player player) {
        Inventory slotGUI = Bukkit.createInventory(null, 9, "スロットマシン");

        // 初期状態のスロットをセットアップ
        for (int i = 0; i < 9; i++) {
            slotGUI.setItem(i, getRandomSlotItem());
        }

        player.openInventory(slotGUI);

        // スロットの回転アニメーション
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick < 20) { // 20回アイテムを回転させる
                    for (int i = 0; i < 9; i++) {
                        slotGUI.setItem(i, getRandomSlotItem());
                    }
                    player.updateInventory();
                    tick++;
                } else {
                    // 最終結果を表示し、報酬を与える
                    SlotResult result = calculateSlotResult(slotGUI);
                    giveRewards(player, result);
                    checkJackpot(player); // ジャックポットの抽選を追加

                    // GUIを数秒後に閉じるタスクをスケジュール
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.getOpenInventory() != null &&
                                    player.getOpenInventory().getTopInventory() == slotGUI) {
                                player.closeInventory(); // GUIを閉じる
                            }
                        }
                    }.runTaskLater(plugin, 60L); // 60L = 3秒後に実行

                    this.cancel(); // アニメーション終了
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // 5Lは0.25秒ごとに実行
    }

    // スロットのアイテムをランダムに取得するメソッド
    @Nullable
    private static ItemStack getRandomSlotItem() {
        List<Material> slotItems = plugin.getSlotItems();
        if (slotItems.isEmpty()) {
            return null; // 設定ファイルにアイテムがない場合はnullを返す
        }

        Material randomItem = slotItems.get(new Random().nextInt(slotItems.size()));
        return new ItemStack(randomItem);
    }

    // スロットの結果を計算するメソッド
    private static SlotResult calculateSlotResult(Inventory slotGUI) {
        // 例として、3つ同じアイテムが揃えば勝ち
        ItemStack first = slotGUI.getItem(3);
        ItemStack second = slotGUI.getItem(4);
        ItemStack third = slotGUI.getItem(5);

        boolean win = first != null && second != null && third != null &&
                first.getType() == second.getType() && second.getType() == third.getType();

        return new SlotResult(win, first != null ? first.getType() : Material.AIR);
    }

    // プレイヤーに報酬を与えるメソッド
    private static void giveRewards(Player player, SlotResult result) {
        if (result.isWin()) {
            player.sendMessage("おめでとう！" + result.getRewardType() + "を獲得しました！");
            player.getInventory().addItem(new ItemStack(result.getRewardType(), 5));
        } else {
            player.sendMessage("残念、今回はハズレです。");
        }
    }

    // ジャックポットの抽選を行うメソッド
    private static void checkJackpot(Player player) {
        double winChance = plugin.getJackpotConfig().getDouble("jackpot_win_chance", 0.01);

        // ランダムに抽選
        if (Math.random() < winChance) {
            int jackpotAmount = plugin.getJackpotAmount();
            player.sendMessage("ジャックポット獲得！獲得額: " + jackpotAmount + " チップ！");
            plugin.addChips(player, jackpotAmount);
            plugin.resetJackpot();
        } else {
            player.sendMessage("JP抽選はハズレ！また挑戦してね！");
        }
    }

    // 賭け金の一部をジャックポットに加算するメソッド
    private static void addToJackpotFromBet(int betAmount) {
        double increaseRate = plugin.getJackpotConfig().getDouble("jackpot_increase_rate", 0.05);
        int amountToAdd = (int) (betAmount * increaseRate);
        plugin.addToJackpot(amountToAdd);
    }
}
