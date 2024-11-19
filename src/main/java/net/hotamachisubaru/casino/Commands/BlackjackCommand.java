package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Blackjack.Game;
import net.hotamachisubaru.casino.Listener.BlackjackListener;
import net.hotamachisubaru.casino.economy.EconomyHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.hotamachisubaru.casino.Slot.SlotMachine.plugin;

public class BlackjackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // プレイヤーの所持金を取得
            double balance = EconomyHelper.getBalance(player);

            if (balance <= 0) {
                player.sendMessage("所持金が不足しています。");
                return false;
            }

            // 賭け金をチャットで入力させる（イベントで処理する）
            player.sendMessage("賭け金を入力してください。");

            // 賭け金を別途取得するロジックが必要です。仮に賭け金を100とします。
            double betAmount = 100.0;  // ここで適切な賭け金の取得ロジックを実装するべきです

            if (balance < betAmount) {
                player.sendMessage("所持金が不足しています。");
                return false;
            }

            // ゲームインスタンスを作成し、リスナーを登録
            Game game = new Game(player, EconomyHelper.getEconomy(), EconomyHelper.getBalance(player));
            BlackjackListener blackjackListener = new BlackjackListener(game);
            Bukkit.getPluginManager().registerEvents(blackjackListener, plugin);

            return true;
        }
        sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
        return false;
    }
}
