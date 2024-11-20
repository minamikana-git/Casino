package net.hotamachisubaru.casino.Blackjack;

import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game implements Listener {
    private boolean doubleUpInProgress = false;
    private final Player player;
    private final Deck deck;
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();
    private final Economy economy;
    private double bet;
    private int playerScore = 0;
    private int dealerScore = 0;
    private GameState gameState = GameState.WAITING_FOR_START;

    public Game(Player player, Economy economy, double bet) {
        this.player = player;
        this.deck = new Deck();
        this.deck.shuffle();
        this.economy = economy;
        this.bet = bet;
        Bukkit.getPluginManager().registerEvents(this, Casino.getInstance());
        logDebug("Game constructor: プレイヤー=" + player.getName() + "、賭け金=" + bet);
    }

    private void logDebug(String message) {
        System.out.println("[Casino Debug] " + message);
    }


    public void startGame(double betAmount) {
        logDebug("ゲーム開始処理中...");

        if (betAmount <= 0) {
            player.sendMessage("賭け金は正の値を入力してください。");
            logDebug("賭け金は正の値を入力してください。");
            return;
        }

        double playerBalance = economy.getBalance(player);
        logDebug("現在のプレイヤー残高: " + playerBalance);

        if (betAmount > playerBalance) {
            player.sendMessage("所持金より多い金額は賭けられません。");
            logDebug("所持金より多い金額は賭けられません。");
            return;
        }

        if (gameState == GameState.IN_PROGRESS) {
            player.sendMessage("ゲームは既に進行中です。");
            logDebug("ゲームは既に進行中です。");
            return;
        }

        this.bet = betAmount;
        EconomyResponse response = economy.withdrawPlayer(player, bet);
        if (!response.transactionSuccess()) {
            String errorMessage = response.errorMessage != null ? response.errorMessage : "不明なエラー";
            player.sendMessage("所持金が不足しています：" + errorMessage);
            logDebug("トランザクション失敗: " + errorMessage);
            return;
        }

        logDebug("トランザクション成功: " + betAmount + " 引き落としました。残高: " + economy.getBalance(player));

        playerHand.clear();
        dealerHand.clear();

        if (!dealInitialCards()) {
            player.sendMessage("デッキに十分なカードがありません。ゲームを中止します。");
            logDebug("デッキに十分なカードがありません。");
            return;
        }

        updateScores();
        displayHands();
        player.sendMessage("あなたは " + betAmount + " を賭けました。ブラックジャックを開始します！");

        // ゲーム状態を IN_PROGRESS に遷移
        gameState = GameState.IN_PROGRESS;
        logDebug("ゲーム状態: IN_PROGRESS");

        // ヒットまたはスタンドを待つ
        player.sendMessage("ヒットまたはスタンドを選んでください。");
        openBlackjackGUI();
    }

    public boolean dealInitialCards() {
        if (deck.getCards().size() < 4) { // デッキのカードが4枚より少ない場合
            return false;
        }
        playerHand.add(deck.remove(0)); // プレイヤー1枚目
        dealerHand.add(deck.remove(0)); // ディーラー1枚目
        playerHand.add(deck.remove(0)); // プレイヤー2枚目
        dealerHand.add(deck.remove(0)); // ディーラー2枚目
        return true;
    }

    private void updateScores() {
        playerScore = calculateHandValue(playerHand);
        dealerScore = calculateHandValue(dealerHand);
        if (playerScore > 21) {
            endGame(false);  // バーストしている場合即終了
        }
    }
    private int calculateHandValue(List<Card> hand) {
        int total = 0;
        int aceCount = 0;
        for (Card card : hand) {
            int value = card.getValue();
            if (value == 11) {
                aceCount++;
                total += 11; // エースは11として計算
            } else if (value >= 10) {
                total += 10; // 10, J, Q, K
            } else {
                total += value;
            }
        }
        while (total > 21 && aceCount > 0) {
            total -= 10; // 11を1に変更
            aceCount--;
        }
        return total;
    }




    private void displayHands() {
        player.sendMessage("あなたの手札: " + handToString(playerHand) + " （合計: " + playerScore + "）");
        player.sendMessage("ディーラーの手札: " + dealerHand.get(0) + " と隠されたカード");
        logDebug("プレイヤー手札: " + handToString(playerHand));
        logDebug("ディーラー手札: " + dealerHand.get(0));
    }


    private String handToString(List<Card> hand) {
        return hand.stream()
                .map(Card::toString)
                .collect(Collectors.joining(", "));
    }

    private void openBlackjackGUI() {
        openGui("ブラックジャック: アクションを選択", "ヒット", "スタンド");
    }

    public void openBlackjackDoubleUpGUI() {
        if (player == null || !player.isOnline()) {
            System.out.println("[Casino Debug] プレイヤーが無効です。ダブルアップGUIを開けません。");
            return;
        }

        System.out.println("[Casino Debug] ダブルアップGUIを作成しています...");
        Inventory doubleUpGUI = Bukkit.createInventory(null, 9, "ダブルアップ！");

        ItemStack doubleUpItem = new ItemStack(Material.GOLD_INGOT);  // サンプルアイテム
        ItemMeta meta = doubleUpItem.getItemMeta();
        meta.setDisplayName("ダブルアップ");
        doubleUpItem.setItemMeta(meta);

        doubleUpGUI.setItem(4, doubleUpItem); // 中央に配置
        System.out.println("[Casino Debug] ダブルアップGUIを開きます。");

        player.openInventory(doubleUpGUI);
    }




    private void openGui(String title, String... buttonNames) {
        Inventory gui = Bukkit.createInventory(null, 9, title);

        for (int i = 0; i < buttonNames.length; i++) {
            ItemStack button = createButton(buttonNames[i]);
            gui.setItem(i * 2 + 3, button);  // ボタンを設定
        }

        player.openInventory(gui);
    }

    private ItemStack createButton(String name) {
        ItemStack button = new ItemStack(Material.GREEN_WOOL);  // デフォルトはGREEN_WOOL
        if (name.equals("スタンド")) {
            button.setType(Material.RED_WOOL);  // スタンドはRED_WOOL
        } else if (name.equals("ドロップアウト")) {
            button.setType(Material.GRAY_WOOL);  // ドロップアウトはGRAY_WOOL
        }

        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player clickedPlayer = (Player) event.getWhoClicked();
        if (!clickedPlayer.equals(player)) return;  // 他のプレイヤーがクリックした場合は無視

        event.setCancelled(true);  // イベントのキャンセル

        String title = event.getView().getTitle();

        if (title.equals("ブラックジャック: アクションを選択")) {
            handleBlackjackAction(event);
        } else if (title.equals("ダブルアップ: 黒か赤を選択")) {
            handleDoubleUpAction(event);
        }
    }

    private void handleBlackjackAction(InventoryClickEvent event) {
        String clickedItemName = event.getCurrentItem().getItemMeta().getDisplayName();

        if (clickedItemName.equals("ヒット")) {
            hit();
        } else if (clickedItemName.equals("スタンド")) {
            stand();
        }
    }

    private void handleDoubleUpAction(InventoryClickEvent event) {
        String clickedItemName = event.getCurrentItem().getItemMeta().getDisplayName();

        // ゲームが既に終了している場合は何もしない
        if (gameState == GameState.FINISHED) {
            return;
        }

        if (clickedItemName.equals("黒") || clickedItemName.equals("赤")) {
            processDoubleUp(clickedItemName);
        } else if (clickedItemName.equals("ドロップアウト")) {
            dropOut();
        }

        // ダブルアップ処理後、ゲーム状態を終了にしてGUIを閉じる
        gameState = GameState.FINISHED;
        closeBlackjackGUI();  // ダブルアップ後にGUIを閉じる
    }

    private void closeBlackjackGUI() {
        // プレイヤーがGUIを開いていたインベントリを閉じる処理
        if (player.getOpenInventory() != null) {
            player.closeInventory();
        }
    }

    public void hit() {
        playerHand.add(deck.drawCard());
        updateScores();
        displayHands();

        if (playerScore > 21) {
            endGame(false);  // バーストしたら即終了
        }
    }


    public void stand() {
        // プレイヤーがバーストしている場合はディーラーのターンを開始しない
        if (playerScore > 21) {
            endGame(false);  // プレイヤーがバーストしているので即終了
            return;
        }

        // ディーラーのターンを開始
        while (dealerScore < 17) {
            dealerHand.add(deck.drawCard());
            updateScores();
        }

        // 勝敗を判定
        endGame(dealerScore > 21 || playerScore > dealerScore);
    }


    private void processDoubleUp(String color) {
        // ダブルアップ処理
        double doubleUpBet = bet * 2;

        EconomyResponse response = economy.withdrawPlayer(player, doubleUpBet);
        if (!response.transactionSuccess()) {
            player.sendMessage("所持金が不足しています。");
            return;
        }

        player.sendMessage("ダブルアップを選択しました。");
        bet = doubleUpBet;
        openBlackjackDoubleUpGUI();  // 次のステップに進む
    }

    private void dropOut() {
        player.sendMessage("あなたはゲームをドロップアウトしました。");
        endGame(false);
    }

    private void endGame(boolean playerWins) {
        if (playerWins) {
            economy.depositPlayer(player, bet * 2);  // 勝った場合、倍額を支給
            player.sendMessage("おめでとうございます！あなたの勝ちです！");

            // ダブルアップがまだ進行していない場合のみ開く
            if (!doubleUpInProgress) {
                doubleUpInProgress = true;
                System.out.println("[Casino Debug] ダブルアップ画面を開きます。");

                Bukkit.getScheduler().runTask(Casino.getInstance(), () -> {
                    openBlackjackDoubleUpGUI();
                    doubleUpInProgress = false; // GUIを開いた後に解除
                });
            }
        } else {
            player.sendMessage(playerScore > 21 ? "あなたはバーストしました。ディーラーの勝ちです！" : "残念、あなたは負けました！");
        }
        gameState = GameState.FINISHED;

        // ゲーム終了時にGUIを閉じる
        Bukkit.getScheduler().runTask(Casino.getInstance(), this::closeBlackjackGUI);
    }





    private enum GameState {
        WAITING_FOR_START, IN_PROGRESS, FINISHED
    }
}
