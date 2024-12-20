package net.hotamachisubaru.casino.listener;

import net.hotamachisubaru.casino.gui.GUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof GUI gui)) return;
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;
        boolean cancelled = false;
        for (int slot : gui.exceptionUnlock()) {
            if (e.getSlot() == slot) {
                cancelled = true;
                break;
            }
        }
        if (!cancelled && gui.lock()) {
            e.setCancelled(true);
        } else {
            e.setCancelled(cancelled);
        }
        if (!gui.clickActions().containsKey(e.getSlot())) return;
        gui.clickActions().get(e.getSlot()).onClick(e);
    }
}
