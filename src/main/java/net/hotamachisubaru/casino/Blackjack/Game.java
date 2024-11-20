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

        double playerBalance = economy.getBalance(player);
        logDebug("現在のプレイヤー残高: " + playerBalance);

        if (betAmount > playerBalance) {
            player.sendMessage("所持金より多い金額は賭けられません。");
            logDebug("所持金より多い金額は賭けられません。");
            return;
        }

        this.bet = betAmount;
        EconomyResponse response = economy.withdrawPlayer(player, bet);
        if (!response.transactionSuccess()) {
            player.sendMessage("所持金が不足しています：" + response.errorMessage);
            logDebug("トランザクション失敗: " + response.errorMessage);
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

    private boolean dealInitialCards() {
        logDebug("初期カードを配布中...");
        if (deck.getCards().size() < 4) {
            logDebug("デッキに十分なカードがありません。");
            return false;
        }
        playerHand.add(deck.drawCard());
        playerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        logDebug("初期カード配布完了: プレイヤー手札=" + handToString(playerHand) + ", ディーラー手札=" + handToString(dealerHand));
        return true;
    }

    private void updateScores() {
        playerScore = calculateHandValue(playerHand);
        dealerScore = calculateHandValue(dealerHand);
        logDebug("スコア更新: プレイヤー=" + playerScore + ", ディーラー=" + dealerScore);
    }

    private int calculateHandValue(List<Card> hand) {
        int value = 0;
        int aceCount = 0;

        // カードの値を計算
        for (Card card : hand) {
            if (card.getName().equals("J") || card.getName().equals("Q") || card.getName().equals("K")) {
                value += 10;
            } else if (card.getName().equals("A")) {
                aceCount++;  // エースは後で調整するため、カウントする
                value += 11; // エースを最初は11として加算
            } else {
                value += card.getValue();  // 数字カード
            }
        }

        // エースが11のままだと21を超える可能性があるので、その場合は11を1に調整
        while (value > 21 && aceCount > 0) {
            value -= 10;  // エースを1に変換
            aceCount--;
        }

        logDebug("手札の合計計算: 手札=" + handToString(hand) + " 合計=" + value);
        return value;
    }


    private void displayHands() {
        player.sendMessage("あなたの手札: " + handToString(playerHand) + " （合計: " + playerScore + "）");
        player.sendMessage("ディーラーの手札: " + dealerHand.get(0).getName() + " と隠されたカード");
        logDebug("プレイヤー手札表示: " + handToString(playerHand) + " 合計: " + playerScore);
        logDebug("ディーラー手札表示: " + dealerHand.get(0).getName() + " と隠されたカード");
    }

    private String handToString(List<Card> hand) {
        return hand.stream().map(Card::getName).collect(Collectors.joining(", "));
    }

    private void openBlackjackGUI() {
        openGui("ブラックジャック: アクションを選択", "ヒット", "スタンド");
    }

    public void openBlackjackDoubleUpGUI() {
        System.out.println("[Casino Debug] ダブルアップGUIを作成しています...");

        Inventory doubleUpGUI = Bukkit.createInventory(null, 9, "ダブルアップ！");

        ItemStack doubleUpItem = new ItemStack(Material.GOLD_INGOT);  // アイテム例
        ItemMeta meta = doubleUpItem.getItemMeta();
        meta.setDisplayName("ダブルアップ");
        doubleUpItem.setItemMeta(meta);

        doubleUpGUI.setItem(4, doubleUpItem);  // 中央に配置

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
            player.sendMessage("あなたの手札が21を超えました。あなたの負けです！");
            endGame(false);
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

            // デバッグログを追加
            System.out.println("[Casino Debug] ダブルアップ画面を開きます。");

            // Bukkitのスケジューラを使用して非同期表示
            Bukkit.getScheduler().runTask(Casino.getInstance(), new Runnable() {
                @Override
                public void run() {
                    openBlackjackDoubleUpGUI();
                }
            });
        } else {
            if (playerScore > 21) {
                player.sendMessage("あなたはバーストしました。ディーラーの勝ちです！");
            } else {
                player.sendMessage("残念、あなたは負けました！");
            }
        }
        gameState = GameState.FINISHED;

        // 終了時のGUI閉じ処理
        Bukkit.getScheduler().runTask(Casino.getInstance(), () -> closeBlackjackGUI());
    }






    private enum GameState {
        WAITING_FOR_START, IN_PROGRESS, FINISHED
    }
}
