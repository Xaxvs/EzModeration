package me.xaxis.ezmoderation.frontend.inventory;

import java.util.List;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PlayerInfractionsGUI implements InventoryHolder {

  // this will likely end up being a paginated gui

  private final Inventory inventory;

  public PlayerInfractionsGUI(List<Infraction> infractions) {
    inventory = Bukkit.createInventory(this, 9 * 6, "Player Infractions");
  }

  public void onClick(InventoryClickEvent event) {
    // todo handle click events
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}
