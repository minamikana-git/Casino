package net.hotamachisubaru.casino.Blackjack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlackjackGUI {

    public static void openBlackjackGUI(Player player, String title, String... buttonNames) {
        Inventory gui = Bukkit.createInventory(null, 9, title);

        for (int i = 0; i < buttonNames.length; i++) {
            ItemStack button = createButton(buttonNames[i]);
            gui.setItem(i * 2 + 3, button);
        }

        player.openInventory(gui);
    }

    private static ItemStack createButton(String name) {
        ItemStack button = new ItemStack(Material.GREEN_WOOL);  // デフォルトはGREEN_WOOL
        if (name.equals("スタンド")) {
            button.setType(Material.RED_WOOL);
        } else if (name.equals("ドロップアウト")) {
            button.setType(Material.GRAY_WOOL);
        }

        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }
}
