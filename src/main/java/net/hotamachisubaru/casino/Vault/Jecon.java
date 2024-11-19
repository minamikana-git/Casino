package net.hotamachisubaru.casino.Vault;

import org.bukkit.entity.Player;

public class Jecon {
    // Jeconのインスタンスを管理
    private final JeconAPI jeconAPI; // ここでJeconAPIインスタンスを使う

    public Jecon(JeconAPI jeconAPI) {
        this.jeconAPI = jeconAPI; // Jeconの初期化
    }

    public boolean hasBalance(Player player, double amount) {
        // JeconのAPIを用いて残高を確認
        double balance = jeconAPI.getBalance(player);
        return balance >= amount;
    }

    public boolean withdraw(Player player, double amount) {
        if (hasBalance(player, amount)) {
            // JeconのAPIを用いて残高を引く
            jeconAPI.subtract(player, amount);
            return true;
        }
        return false;
    }

    public void deposit(Player player, double amount) {
        // JeconのAPIを用いて残高を追加
        jeconAPI.add(player, amount);
    }
}
