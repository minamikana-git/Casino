package net.hotamachisubaru.casino.gui;

import org.bukkit.inventory.InventoryHolder;

import java.util.Map;

public interface GUI extends InventoryHolder {

    String getTitle();
    Map<Integer, GUIIcon> guiIcons();
    Map<Integer, ClickAction> clickActions();
    Map<Integer, GUIIcon> fillIcons();
    boolean lock();
    int[] exceptionUnlock();
}
