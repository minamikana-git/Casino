package net.hotamachisubaru.casino.Commands;


import net.hotamachisubaru.casino.Casino;
import net.hotamachisubaru.casino.GUI.CasinoGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCasinoCommand implements CommandExecutor {

    private CasinoGUI casinoGUI;

    public OpenCasinoCommand() {
     this.casinoGUI = new CasinoGUI(Casino.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
            return false;
        }

        Player player = (Player) sender;
        casinoGUI.openCasinoGUI(player); // CasinoGUI インスタンスを使用してメソッドを呼び出し
        return true;
    }
}
