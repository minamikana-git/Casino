package net.hotamachisubaru.casino.Blackjack;
import net.hotamachisubaru.casino.*;
import net.hotamachisubaru.casino.Casino;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game implements Listener {
    private static final String GUI_TITLE_BLACKJACK = "ブラックジャック: アクションを選択";
    private static final String GUI_TITLE_DOUBLE_UP = "ダブルアップ: 黒か赤を選択";
    private static final int BLACKJACK_HIT_LIMIT = 21;
    private static final int DEALER_HIT_LIMIT = 17;

    private final Player player;
    private final Economy economy;
    private final Deck deck;
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();
    private double bet;
    private int playerScore = 0;
    private int dealerScore = 0;
    private GameState gameState = GameState.WAITING_FOR_START;

    public Game(Player player, Economy economy, double bet) {
        this.player = player;
        this.economy = economy;
        this.bet = bet;
        this.deck = new Deck();
        deck.shuffle();
        Bukkit.getPluginManager().registerEvents(this, Casino.getInstance());
        logDebug("Game initialized: Player=" + player.getName() + ", Bet=" + bet);
    }

    private void logDebug(String message) {
        System.out.println("[Casino Debug] " + message);
    }

    public void startGame(double betAmount) {
        if (!validateBet(betAmount)) return;
        prepareNewGame(betAmount);

        if (!dealInitialCards()) {
            player.sendMessage("デッキに十分なカードがありません。ゲームを中止します。");
            logDebug("デッキに十分なカードがありません。");
            return;
        }

        updateScores();
        displayHands();
        player.sendMessage("ブラックジャックを開始します！ヒットまたはスタンドを選んでください。");
        openBlackjackGUI();
    }

    private boolean validateBet(double betAmount) {
        if (betAmount <= 0) {
            sendError("賭け金は正の値を入力してください。");
            return false;
        }
        if (betAmount > economy.getBalance(player)) {
            sendError("所持金より多い金額は賭けられません。");
            return false;
        }
        if (gameState == GameState.IN_PROGRESS) {
            sendError("ゲームは既に進行中です。");
            return false;
        }
        return true;
    }

    private void prepareNewGame(double betAmount) {
        this.bet = betAmount;
        if (!withdrawBet(betAmount)) return;

        playerHand.clear();
        dealerHand.clear();
        gameState = GameState.IN_PROGRESS;
        logDebug("Game state set to IN_PROGRESS");
    }

    private boolean withdrawBet(double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            sendError("賭け金の引き落としに失敗しました: " + response.errorMessage);
            return false;
        }
        logDebug("Bet withdrawn: " + amount + ", Remaining Balance: " + economy.getBalance(player));
        return true;
    }

    private boolean dealInitialCards() {
        try {
            playerHand.add(deck.drawCard());
            dealerHand.add(deck.drawCard());
            playerHand.add(deck.drawCard());
            dealerHand.add(deck.drawCard());
            return true;
        } catch (Exception e) {
            sendError("カードの引き分けに失敗しました: " + e.getMessage());
            return false;
        }
    }

    private void updateScores() {
        playerScore = calculateHandValue(playerHand);
        dealerScore = calculateHandValue(dealerHand);

        if (playerScore > BLACKJACK_HIT_LIMIT) {
            endGame(false);  // バースト
        }
    }

    private int calculateHandValue(List<Card> hand) {
        int total = hand.stream().mapToInt(Card::getValue).sum();
        int aceCount = (int) hand.stream().filter(card -> card.getValue() == 1).count();

        while (total > BLACKJACK_HIT_LIMIT && aceCount > 0) {
            total -= 10;
            aceCount--;
        }
        return total;
    }

    private void displayHands() {
        player.sendMessage("あなたの手札: " + handToString(playerHand) + " （合計: " + playerScore + "）");
        player.sendMessage("ディーラーの手札: " + dealerHand.get(0) + " と隠されたカード");
        logDebug("Player hand: " + handToString(playerHand));
        logDebug("Dealer hand: " + dealerHand.get(0));
    }

    private String handToString(List<Card> hand) {
        return hand.stream()
                .map(Card::toString)
                .collect(Collectors.joining(", "));
    }

    private void openBlackjackGUI() {
        openGui(GUI_TITLE_BLACKJACK, "ヒット", "スタンド");
    }

    private void openGui(String title, String... buttonNames) {
        Inventory gui = Bukkit.createInventory(null, 9, title);
        // ボタンを明確に配置
        for (int i = 0; i < buttonNames.length; i++) {
            gui.setItem(i, createButton(buttonNames[i]));
        }
        player.openInventory(gui);
    }

    private ItemStack createButton(String name) {
        Material material = switch (name) {
            case "スタンド" -> Material.RED_WOOL;
            case "ドロップアウト" -> Material.GRAY_WOOL;
            default -> Material.GREEN_WOOL;
        };
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }

    private void sendError(String message) {
        player.sendMessage(message);
        logDebug(message);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clickedPlayer) || !clickedPlayer.equals(player)) return;
        event.setCancelled(true);

        String title = event.getView().getTitle();
        if (GUI_TITLE_BLACKJACK.equals(title)) {
            handleBlackjackAction(event.getCurrentItem());
        }
    }

    private void handleBlackjackAction(ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        String itemName = clickedItem.getItemMeta().getDisplayName();

        if ("ヒット".equals(itemName)) {
            hit();
        } else if ("スタンド".equals(itemName)) {
            stand();
        }
    }

    public void hit() {
        playerHand.add(deck.drawCard());
        updateScores();
        // プレイヤーの手札を表示するのはカードを引いた後だけ
        if (playerScore <= BLACKJACK_HIT_LIMIT) {
            displayHands();
        }

        if (playerScore > 21) {
            endGame(false);  // バーストしたら即終了
        }
    }

    public void stand() {
        while (dealerScore < DEALER_HIT_LIMIT) {
            dealerHand.add(deck.drawCard());
            updateScores();
        }
        endGame(playerScore > dealerScore || dealerScore > BLACKJACK_HIT_LIMIT);
    }

    private void endGame(boolean playerWins) {
        if (playerWins) {
            economy.depositPlayer(player, bet * 2);
            player.sendMessage("おめでとうございます！あなたの勝ちです！");
        } else {
            player.sendMessage(playerScore > BLACKJACK_HIT_LIMIT ? "あなたはバーストしました。" : "残念、あなたは負けました！");
        }
        gameState = GameState.FINISHED;

        // インベントリを確実に閉じる
        Bukkit.getScheduler().runTask(Casino.getInstance(), () -> {
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        });
    }

    private enum GameState {
        WAITING_FOR_START, IN_PROGRESS, FINISHED
    }
}
