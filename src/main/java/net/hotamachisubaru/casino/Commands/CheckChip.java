package net.hotamachisubaru.casino.Commands;

import net.hotamachisubaru.casino.Manager.ChipManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckChip implements CommandExecutor {
    private final ChipManager chipManager;

    public CheckChip() {
        this.chipManager = new ChipManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
            return false;
        }
        Player player = (Player) sender;

        int totalChips = chipManager.getChips(player);
        player.sendMessage("現在のチップ数: " + totalChips + " 枚");
        return true;
    }
}
