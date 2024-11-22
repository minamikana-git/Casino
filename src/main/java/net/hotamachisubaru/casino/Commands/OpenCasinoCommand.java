package net.hotamachisubaru.casino.Commands;


import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.GUI.CasinoGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCasinoCommand implements CommandExecutor {
    private CasinoGUI casinoGui;

    public OpenCasinoCommand() {
        this.casinoGui = new CasinoGUI(Casino.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayer(sender)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
            return false;
        }
        casinoGui.openCasinoGUI((Player) sender);
        return true;
    }

    private boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
}
