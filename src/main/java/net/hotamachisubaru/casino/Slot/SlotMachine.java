package net.hotamachisubaru.casino.Slot;

import net.hotamachisubaru.casino.Casino;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

import static net.hotamachisubaru.casino.Slot.SlotDoubleUp.askForDoubleUp;

public class SlotMachine {
    public static final Casino plugin = Casino.getPlugin(Casino.class);
    private static final Random RANDOM = new Random();

    private static final int[][] PAYLINES = {
            {0, 1, 2}, // Horizontal
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7}, // Vertical
            {2, 5, 8},
            {0, 4, 8}, // Diagonal
            {2, 4, 6}
    };
    private static final String INVALID_INPUT_MESSAGE = "無効な入力です。賭け金は数字で入力してください。";
    private static final String JACKPOT_MESSAGE = "ジャックポット獲得！ %d チップを獲得！";

    public static void startBetting(Player player) {
        int minBet = plugin.getConfig().getInt("minimum_bet");
        int maxBet = plugin.getConfig().getInt("maximum_bet");
        int playerChips = plugin.getChips(player);
        player.sendMessage("現在のチップ数: " + playerChips);
        player.sendMessage("スロットマシンに挑戦します！賭け金を入力してください (最低: " + minBet + "、最大: " + maxBet + ")");
        plugin.getChatListener().waitForInput(player, input -> handleBetInput(player, input, minBet, maxBet, playerChips));
    }

    private static void handleBetInput(Player player, String input, int minBet, int maxBet, int playerChips) {
        if (input == null || input.trim().isEmpty()) {
            player.sendMessage(INVALID_INPUT_MESSAGE);
            return;
        }
        try {
            int betAmount = Integer.parseInt(input);
            if (!isValidBetAmount(betAmount, minBet, maxBet, playerChips, player)) return;
            synchronized (plugin) {
                if (!plugin.getEconomy().has(player, betAmount)) {
                    player.sendMessage("残高が不足しています。必要額: " + betAmount);
                    return;
                }
                plugin.getEconomy().withdrawPlayer(player, betAmount);
            }
            player.sendMessage("賭け金 " + betAmount + " を設定しました！スロットを回します...");
            addToJackpotFromBet(betAmount);
            openSlotGUI(player, betAmount);
        } catch (NumberFormatException e) {
            player.sendMessage(INVALID_INPUT_MESSAGE);
        }
    }

    private static void addToJackpotFromBet(int betAmount) {
        int currentJackpot = plugin.getJackpotAmount();
        int newJackpotAmount = currentJackpot + (int) (betAmount * 0.1); // ベット額の10%をジャックポットに追加
        plugin.setJackpotAmount(newJackpotAmount);
    }

    private static boolean isValidBetAmount(int betAmount, int minBet, int maxBet, int playerChips, Player player) {
        if (betAmount < minBet || betAmount > maxBet) {
            player.sendMessage("賭け金は " + minBet + " から " + maxBet + " の間で入力してください。");
            return false;
        }
        if (betAmount > playerChips) {
            player.sendMessage("チップが不足しています。現在のチップ数: " + playerChips);
            return false;
        }
        return true;
    }

    public static void openSlotGUI(Player player, int betAmount) {
        Inventory slotGUI = Bukkit.createInventory(null, 9, "スロットマシン");
        fillSlotItems(slotGUI);
        player.openInventory(slotGUI);
        startSlotAnimation(player, slotGUI, betAmount);
    }

    private static void fillSlotItems(Inventory slotGUI) {
        List<Material> slotItems = plugin.getSlotItems();
        slotItems = isSlotItemEmpty() ? Collections.singletonList(Material.STONE) : slotItems;
        for (int i = 0; i < 9; i++) {
            slotGUI.setItem(i, new ItemStack(slotItems.get(RANDOM.nextInt(slotItems.size()))));
        }
    }

    private static boolean isSlotItemEmpty() {
        return plugin.getSlotItems().isEmpty();
    }

    private static void startSlotAnimation(Player player, Inventory slotGUI, int betAmount) {
        new BukkitRunnable() {
            int column = 0;

            @Override
            public void run() {
                if (column < 3) {
                    stopColumn(slotGUI, column);
                    column++;
                } else {
                    handleSlotResult(player, slotGUI, betAmount);
                    closeGuiAfterDelay(player, slotGUI);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    private static void stopColumn(Inventory slotGUI, int column) {
        for (int row = 0; row < 3; row++) {
            int index = row * 3 + column;
            slotGUI.setItem(index, getRandomSlotItem());
        }
    }

    private static ItemStack getRandomSlotItem() {
        List<Material> slotItems = plugin.getSlotItems();
        slotItems = isSlotItemEmpty() ? Collections.singletonList(Material.STONE) : slotItems;
        return new ItemStack(slotItems.get(RANDOM.nextInt(slotItems.size())));
    }

    private static void handleSlotResult(Player player, Inventory slotGUI, int betAmount) {
        SlotResult result = calculateSlotResult(slotGUI);
        giveRewards(player, result, betAmount);
        checkJackpot(player);
    }

    private static void closeGuiAfterDelay(Player player, Inventory slotGUI) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() == slotGUI) {
                    player.closeInventory();
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    private static SlotResult calculateSlotResult(Inventory slotGUI) {
        Map<Material, Integer> payout = new HashMap<>();
        for (int[] line : PAYLINES) {
            Material first = slotGUI.getItem(line[0]).getType();
            if (isWinningLine(slotGUI, line, first)) {
                payout.put(first, payout.getOrDefault(first, 0) + 1);
            }
        }
        return new SlotResult(!payout.isEmpty(), payout);
    }

    private static boolean isWinningLine(Inventory slotGUI, int[] line, Material first) {
        return Arrays.stream(line)
                .mapToObj(slotGUI::getItem)
                .allMatch(item -> item != null && item.getType() == first);
    }

    private static void giveRewards(Player player, SlotResult result, int betAmount) {
        if (result.isWinning()) {
            player.sendMessage("おめでとう！当たりラインがありました！");
            Map<Material, Integer> rewards = result.getRewardItems();
            if (rewards == null) {
                player.sendMessage("予期せぬエラーが発生しました。");
                return;
            }
            rewards.forEach((material, count) -> {
                try {
                    long reward = Math.multiplyExact(betAmount, (long) count);
                    plugin.addChips(player, (int) reward);
                    player.sendMessage(material.name() + "に" + reward + "チップを獲得しました！");
                } catch (ArithmeticException e) {
                    player.sendMessage("リワードの計算中にエラーが発生しました。");
                }
            });
            askForDoubleUp(player, betAmount, 1);
        } else {
            player.sendMessage("残念、今回はハズレです。");
        }
    }

    private static void checkJackpot(Player player) {
        double winChance = plugin.getConfig().getDouble("jackpot_win_chance", 0.05);
        if (Math.random() < winChance) {
            int jackpotAmount = plugin.getJackpotAmount();
            player.sendMessage(String.format(JACKPOT_MESSAGE, jackpotAmount));
            plugin.addChips(player, jackpotAmount);
            plugin.resetJackpot();
        } else {
            player.sendMessage("JP抽選はハズレ！また挑戦してね！");
        }
    }

    public static void waitForBetAmount(Player player, Consumer<Integer> betAmountConsumer) {
        player.sendMessage("賭け金を入力してください:");
        plugin.getChatListener().waitForInput(player, input -> {
            try {
                int betAmount = Integer.parseInt(input);
                if (betAmount > 0) {
                    betAmountConsumer.accept(betAmount);
                } else {
                    player.sendMessage(INVALID_INPUT_MESSAGE);
                    waitForBetAmount(player, betAmountConsumer);  // 再入力を促す
                }
            } catch (NumberFormatException e) {
                player.sendMessage(INVALID_INPUT_MESSAGE);
                waitForBetAmount(player, betAmountConsumer);  // 再入力を促す
            }
        });
    }
}