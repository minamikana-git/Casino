package net.hotamachisubaru.casino.Blackjack;

import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        for (Card card : hand) {
            value += card.getValue();
            if (card.getName().equals("A")) {
                aceCount++;
            }
        }
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        logDebug("計算中: 手札=" + handToString(hand) + " 合計=" + value);
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // インベントリのカスタムタイトルを取得する
        String inventoryTitle = event.getView().getTitle();
        if (!inventoryTitle.equals("ブラックジャック: アクションを選択")) return;

        event.setCancelled(true);  // アイテムを取り出せないようにする

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // 「ヒット」ボタンがクリックされた場合
        if (itemName.equals("ヒット")) {
            hit();
        }
        // 「スタンド」ボタンがクリックされた場合
        else if (itemName.equals("スタンド")) {
            stand();
        }
    }




    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) return;

        String message = event.getMessage().toLowerCase();
        event.setCancelled(true);

        logDebug("チャットメッセージ受信: " + message);

        switch (gameState) {
            case IN_PROGRESS:
                player.sendMessage("GUIで選択してください。");
                break;

            case WAITING_FOR_RESTART:
                handleRestartInput(message);
                break;

            case WAITING_FOR_BET:
                handleBetInput(message);
                break;

            case WAITING_FOR_DOUBLE_UP:
                handleDoubleUpInput(message);
                break;

            default:
                player.sendMessage("無効な状態です。ゲームを再開できません。");
                logDebug("無効な状態でメッセージを送信");
        }
    }


    public void hit() {
        logDebug("プレイヤーがヒットを選択...");
        if (deck.getCards().isEmpty()) {
            player.sendMessage("デッキが空です。");
            logDebug("デッキが空です。ゲーム終了");
            determineWinner();
            return;
        }
        playerHand.add(deck.drawCard());
        updateScores();
        displayHands();

        if (playerScore > 21) {
            player.sendMessage("あなたはバーストしました！負けです。");
            logDebug("プレイヤーがバーストしました。");
            endGame(false);
        } else {
            // ヒットまたはスタンドの選択肢をGUIで表示
            openBlackjackGUI();  // GUIを再度表示
            logDebug("ヒット後、GUIでヒットまたはスタンドの選択肢を表示しました。");
        }
    }

    private void showHitOrStandGUI() {
        // GUIインベントリを作成
        Inventory gui = Bukkit.createInventory(null, 9, "ブラックジャック: アクションを選択");

        // ヒットボタンを作成
        ItemStack hitButton = new ItemStack(Material.GREEN_WOOL); // 好きなアイテムを指定
        ItemMeta hitMeta = hitButton.getItemMeta();
        if (hitMeta != null) {
            hitMeta.setDisplayName("ヒット");
            hitButton.setItemMeta(hitMeta);
        }

        // スタンドボタンを作成
        ItemStack standButton = new ItemStack(Material.RED_WOOL);
        ItemMeta standMeta = standButton.getItemMeta();
        if (standMeta != null) {
            standMeta.setDisplayName("スタンド");
            standButton.setItemMeta(standMeta);
        }

        // ボタンをGUIに追加
        gui.setItem(3, hitButton);
        gui.setItem(5, standButton);

        // プレイヤーにGUIを開かせる
        player.openInventory(gui);
    }


    public void stand() {
        logDebug("プレイヤーがスタンドを選択...");
        player.sendMessage("あなたはスタンドしました。ディーラーのターンです。");
        player.sendMessage("ディーラーの手札: " + handToString(dealerHand) + " （合計: " + dealerScore + "）");

        while (dealerScore < 17) {
            if (deck.getCards().isEmpty()) {
                player.sendMessage("デッキが空です。");
                logDebug("デッキが空です。ディーラーのターン終了");
                break;
            }
            dealerHand.add(deck.drawCard());
            updateScores();
            logDebug("ディーラーがカードを引きました: " + handToString(dealerHand) + " （合計: " + dealerScore + "）");
        }
        determineWinner();
    }

    private void determineWinner() {
        logDebug("勝者判定中...");
        if (playerScore > 21) {
            player.sendMessage("あなたはバーストしました！ディーラーの勝ちです。");
            economy.depositPlayer(player, bet); // 返金
        } else if (dealerScore > 21 || playerScore > dealerScore) {
            player.sendMessage("あなたの勝ちです！おめでとう！");
            economy.depositPlayer(player, bet * 2); // 賭け金の倍を返却
        } else if (playerScore == dealerScore) {
            player.sendMessage("引き分けです！");
            economy.depositPlayer(player, bet); // 賭け金をそのまま返す
        } else {
            player.sendMessage("ディーラーの勝ちです。");
            // 賭け金は戻らない
        }
        endGame(true);
    }

    private void endGame(boolean won) {
        gameState = GameState.WAITING_FOR_RESTART;
        HandlerList.unregisterAll(this);
        player.closeInventory();
        logDebug(won ? "ゲーム終了、プレイヤーの勝利" : "ゲーム終了、ディーラーの勝利");
    }

    public void openBlackjackGUI() {
        // 新しいGUIインベントリを作成
        Inventory gui = Bukkit.createInventory(null, 9, "ブラックジャック: アクションを選択");

        // ヒットボタンを作成
        ItemStack hitButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta hitMeta = hitButton.getItemMeta();
        if (hitMeta != null) {
            hitMeta.setDisplayName("ヒット");
            hitButton.setItemMeta(hitMeta);
        }

        // スタンドボタンを作成
        ItemStack standButton = new ItemStack(Material.RED_WOOL);
        ItemMeta standMeta = standButton.getItemMeta();
        if (standMeta != null) {
            standMeta.setDisplayName("スタンド");
            standButton.setItemMeta(standMeta);
        }

        // インベントリにボタンをセット
        gui.setItem(3, hitButton);  // ヒットボタン（インベントリの3番目）
        gui.setItem(5, standButton); // スタンドボタン（インベントリの5番目）


        // プレイヤーにインベントリを表示
        player.openInventory(gui);
    }


    private void handleRestartInput(String message) {
        if (message.equals("yes")) {
            player.sendMessage("新しい賭け金を入力してください。");
            gameState = GameState.WAITING_FOR_BET;  // 新しい賭け金を待つ状態に変更
        } else {
            player.sendMessage("ゲームを終了しました。");
            endGame(false);
        }
    }


    private void handleBetInput(String message) {
        try {
            double newBet = Double.parseDouble(message);
            if (newBet > 0) {
                startGame(newBet);
            } else {
                player.sendMessage("無効な金額です。");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("無効な金額です。");
        }
    }

    private void handleDoubleUpInput(String message) {
        // ダブルアップ処理
    }

    private void logDebug(String message) {
        // デバッグ用のロギング
        Bukkit.getLogger().info(message);
    }

    private enum GameState {
        WAITING_FOR_START,
        IN_PROGRESS,
        WAITING_FOR_RESTART,
        WAITING_FOR_BET,
        WAITING_FOR_DOUBLE_UP
    }
}
