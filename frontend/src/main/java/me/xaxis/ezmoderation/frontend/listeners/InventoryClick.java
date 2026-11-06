package me.xaxis.ezmoderation.frontend.listeners;

import me.xaxis.ezmoderation.frontend.inventory.PlayerInfractionsGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClick implements Listener {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() instanceof PlayerInfractionsGUI gui) {
      event.setCancelled(true);
      gui.onClick(event);
    }
  }
}
