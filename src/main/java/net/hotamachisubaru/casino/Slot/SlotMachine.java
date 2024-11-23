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
import java.util.stream.Collectors;

import static net.hotamachisubaru.casino.Slot.SlotDoubleUp.askForDoubleUp;

public class SlotMachine {
    public static final Casino plugin = Casino.getPlugin(Casino.class);
    private static final Random RANDOM = new Random();
    private static final Map<Material, String> ITEM_NAMES = new HashMap<>() {{
        put(Material.COAL, "石炭");
        put(Material.IRON_INGOT, "鉄インゴット");
        put(Material.GOLD_INGOT, "金インゴット");
        put(Material.DIAMOND, "ダイヤモンド");
        put(Material.NETHERITE_SCRAP, "ネザライトのかけら");
    }};


    private static String getItemName(Material material) {
        return ITEM_NAMES.getOrDefault(material, material.name()); // マップにない場合、英語名を使用
    }

    private static final int[][] PAYLINES = {
            {0, 1, 2}, // 横ライン1
            {3, 4, 5}, // 横ライン2
            {6, 7, 8}, // 横ライン3
            {0, 3, 6}, // 縦ライン1
            {1, 4, 7}, // 縦ライン2
            {2, 5, 8}, // 縦ライン3
            {0, 4, 8}, // 斜めライン1
            {2, 4, 6}  // 斜めライン2
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
        Inventory slotGUI = Bukkit.createInventory(null, 27, "スロットマシン");
        fillSlotItems(slotGUI); // スロットにアイテムを配置
        player.openInventory(slotGUI);
        startSlotAnimation(player, slotGUI, betAmount);
    }



    private static final int[] SLOT_INDEXES = {3, 4, 5, 12, 13, 14, 21, 22, 23};

    private static void fillSlotItems(Inventory slotGUI) {
        List<Material> slotItems = plugin.getSlotItems();
        slotItems = isSlotItemEmpty() ? Collections.singletonList(Material.STONE) : slotItems;

        // 特定のスロット番号にランダムなアイテムを配置
        for (int slot : SLOT_INDEXES) {
            Material randomItem = slotItems.get(RANDOM.nextInt(slotItems.size()));
            slotGUI.setItem(slot, new ItemStack(randomItem));
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
                    stopColumn(slotGUI, column); // 現在の列のスロットを更新
                    column++;
                } else {
                    handleSlotResult(player, slotGUI, betAmount); // 結果判定
                    closeGuiAfterDelay(player, slotGUI);          // GUIを閉じる
                    this.cancel();                                // タスクを終了
                }
            }
        }.runTaskTimer(plugin, 20L, 10L); // アニメーション間隔：20L初期遅延, 10Lループ間隔
    }

    private static void stopRow(Inventory slotGUI, int row) {
        for (int column = 0; column < 3; column++) {
            int index = row * 3 + column;
            slotGUI.setItem(index, getRandomSlotItem());  // 行ごとにアイテムを設定
        }
    }

    private static void stopColumn(Inventory slotGUI, int column) {
        // 各列のスロット番号を計算
        int[] columnSlots = {
                SLOT_INDEXES[column],        // 上段
                SLOT_INDEXES[column + 3],    // 中段
                SLOT_INDEXES[column + 6]     // 下段
        };

        // 列に対応するスロットをランダムなアイテムで上書き
        for (int slot : columnSlots) {
            slotGUI.setItem(slot, getRandomSlotItem());
        }
    }


    private static ItemStack getRandomSlotItem() {
        // スロットアイテムリストを取得
        List<Material> slotItems = plugin.getSlotItems();
        if (slotItems.isEmpty()) { // カスタム設定が空の場合、デフォルトアイテムを使用
            slotItems = plugin.getDefaultSlotItems();
        }
        Material randomItem = slotItems.get(RANDOM.nextInt(slotItems.size()));
        return new ItemStack(randomItem);
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
                .allMatch(item -> item != null && item.getType() == first); // 全アイテムが一致しているか確認
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
                    player.sendMessage(getItemName(material) + "で " + reward + " チップを獲得しました！");
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