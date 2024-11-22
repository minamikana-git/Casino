package net.hotamachisubaru.casino.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyHelper {
    private static Economy economy;

    // Vault APIの初期化
    public static void initialize() {
        EconomySetupHelper setupHelper = new EconomySetupHelper();
        if (setupHelper.setupEconomy()) {
            economy = setupHelper.getEconomy();
        }
        if (economy == null) {
            System.out.println("エラー: Vaultの初期化に失敗しました。");
        }
    }

    // プレイヤーの所持金を取得
    public static double getBalance(Player player) {
        if (player == null) {
            System.out.println("エラー: プレイヤーがnullです。");
            return 0.0;
        }
        if (economy != null) {
            return economy.getBalance(player);
        } else {
            System.out.println("エラー: Vaultの経済システムが初期化されていません。");
            return 0.0;
        }
    }

    // プレイヤーの所持金を追加
    public static synchronized void addBalance(Player player, double amount) {
        if (player == null) {
            System.out.println("エラー: プレイヤーがnullです。");
            return;
        }
        if (economy != null) {
            economy.depositPlayer(player, amount);
        } else {
            System.out.println("エラー: Vaultの経済システムが初期化されていません。");
        }
    }

    // プレイヤーの所持金を減らす
    public static synchronized boolean withdrawBalance(Player player, double amount) {
        if (player == null) {
            System.out.println("エラー: プレイヤーがnullです。");
            return false;
        }
        if (economy != null && economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            return true;
        } else {
            System.out.println("エラー: 所持金が不足しているか、Vaultが初期化されていません。");
            return false;
        }
    }

    public static Economy getEconomy() {
        return economy;
    }
}
