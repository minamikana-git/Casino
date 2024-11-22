package net.hotamachisubaru.casino.Slot;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SlotMachine {

    private static final Casino plugin = Casino.getPlugin(Casino.class);

    // 賭け金の設定
    private static final int MIN_BET = 1000; // 最低賭け金
    private static final int MAX_BET = 50000; // 最大賭け金

    public static void startBetting(Player player) {
        player.sendMessage("スロットマシンに挑戦します！賭け金を入力してください (最低: " + MIN_BET + "、最大: " + MAX_BET + ")");

        // チャットで賭け金を入力させる例
        plugin.getChatListener().wait(player, input -> {
            try {
                int betAmount = Integer.parseInt(input);

                if (betAmount < MIN_BET || betAmount > MAX_BET) {
                    player.sendMessage("賭け金は " + MIN_BET + " から " + MAX_BET + " の間で入力してください。");
                    return;
                }

                // 賭け金チェック
                if (!plugin.getEconomy().has(player, betAmount)) {
                    player.sendMessage("残高が不足しています。必要額: " + betAmount);
                    return;
                }

                // 賭け金を差し引く
                plugin.getEconomy().withdrawPlayer(player, betAmount);
                player.sendMessage("賭け金 " + betAmount + " を設定しました！スロットを回します...");

                // ジャックポットに賭け金の一部を加算
                addToJackpotFromBet(betAmount);

                // スロットGUIを開く
                openSlotGUI(player, betAmount);

            } catch (NumberFormatException e) {
                player.sendMessage("賭け金は整数で入力してください。");
            }
        });
    }

    public static void openSlotGUI(Player player, int betAmount) {
        Inventory slotGUI = Bukkit.createInventory(null, 27, "スロットマシン");

        for (int i = 0; i < 9; i++) {
            slotGUI.setItem(i, getRandomSlotItem());
        }

        player.openInventory(slotGUI);

        // スロット回転アニメーション
        new BukkitRunnable() {
            int column = 0;

            @Override
            public void run() {
                if (column < 3) {
                    stopColumn(slotGUI, column);
                    column++;
                } else {
                    SlotResult result = calculateSlotResult(slotGUI);

                    // 勝利時の報酬
                    giveRewards(player, result, betAmount);

                    // ジャックポットチェック
                    checkJackpot(player);

                    // 数秒後にGUIを閉じる
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.getOpenInventory() != null &&
                                    player.getOpenInventory().getTopInventory() == slotGUI) {
                                player.closeInventory();
                            }
                        }
                    }.runTaskLater(plugin, 60L);

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    private static void stopColumn(Inventory slotGUI, int column) {
        Random random = new Random();
        for (int row = 0; row < 3; row++) {
            int index = row * 3 + column;
            slotGUI.setItem(index, getRandomSlotItem());
        }
    }

    private static ItemStack getRandomSlotItem() {
        List<Material> slotItems = plugin.getSlotItems();
        if (slotItems.isEmpty()) return new ItemStack(Material.STONE);
        Material randomItem = slotItems.get(new Random().nextInt(slotItems.size()));
        return new ItemStack(randomItem);
    }

    private static SlotResult calculateSlotResult(Inventory slotGUI) {
        Map<Material, Integer> payout = new HashMap<>();
        for (int[] line : paylines) {
            Material first = slotGUI.getItem(line[0]).getType();
            boolean isWinningLine = Arrays.stream(line)
                    .mapToObj(slotGUI::getItem)
                    .allMatch(item -> item != null && item.getType() == first);

            if (isWinningLine) {
                payout.put(first, payout.getOrDefault(first, 0) + 1);
            }
        }
        return new SlotResult(!payout.isEmpty(), payout);
    }

    private static void giveRewards(Player player, SlotResult result, int betAmount) {
        if (result.isWin()) {
            player.sendMessage("おめでとう！当たりラインがありました！");

            // 報酬額計算
            result.getRewards().forEach((type, count) -> {
                int rewardAmount = (betAmount * count) / paylines; // 配当倍率
                player.getInventory().addItem(new ItemStack(type, rewardAmount));
                player.sendMessage(type + " のラインで " + rewardAmount + " 獲得！");
            });
        } else {
            player.sendMessage("残念、今回はハズレです。");
        }
    }

    private static void checkJackpot(Player player) {
        double winChance = plugin.getConfig().getDouble("jackpot_win_chance", 0.01);
        if (Math.random() < winChance) {
            int jackpotAmount = plugin.getJackpotAmount();
            player.sendMessage("ジャックポット獲得！ " + jackpotAmount + " チップを獲得！");
            plugin.addChips(player, jackpotAmount);
            plugin.resetJackpot();
        } else {
            player.sendMessage("JP抽選はハズレ！また挑戦してね！");
        }
    }

    private static void addToJackpotFromBet(int betAmount) {
        double increaseRate = plugin.getConfig().getDouble("jackpot_increase_rate", 0.05);
        int amountToAdd = (int) (betAmount * increaseRate);
        plugin.addToJackpot(amountToAdd);
    }

    public static void openSlotGUI(Player player) {
        int betAmount = MIN_BET; // 仮に最低賭け金を設定
        openSlotGUI(player, betAmount);
    }
}
