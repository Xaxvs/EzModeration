package me.xaxis.ezmoderation.frontend.inventory;

import java.util.ArrayList;
import java.util.List;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

public class PlayerInfractionsGUI implements InventoryHolder {

  private final Inventory inventory;
  private final int itemsPerPage = 9 * 5;
  private final int maxPages;
  private final List<Infraction> originalInfractions;
  private List<Infraction> infractions;
  private int page;

  public PlayerInfractionsGUI(List<Infraction> infractions) {
    this.inventory = Bukkit.createInventory(this, 9 * 6, "Player Infractions");
    this.originalInfractions = new ArrayList<>(infractions);
    this.infractions = new ArrayList<>(infractions);
    this.page = 0;
    this.maxPages = Math.max(1, (int) Math.ceil((double) infractions.size() / itemsPerPage));

    populateInventory();
  }

  public PlayerInfractionsGUI(
      List<Infraction> infractions,
      @Nullable String playerName,
      Infraction.@Nullable Type filterType,
      boolean isExpired) {
    this.inventory = Bukkit.createInventory(this, 9 * 6, "Player Infractions");
    this.originalInfractions = new ArrayList<>(infractions);
    this.infractions = new ArrayList<>(infractions);
    this.page = 0;
    this.infractions = filterType(filterType);
    this.infractions = filterByActiveOnly(isExpired);
    this.infractions = filterByIssuer(playerName);
    this.maxPages = Math.max(1, (int) Math.ceil((double) infractions.size() / itemsPerPage));

    populateInventory();
  }

  public void pageForward() {
    if (page < maxPages - 1) {
      page++;
      populateInventory();
    }
  }

  public void pageBack() {
    if (page > 0) {
      page--;
      populateInventory();
    }
  }

  public void populateInventory() {
    int start = page * itemsPerPage;
    int end = Math.min(start + itemsPerPage, infractions.size());
    List<Infraction> pageInfractions = infractions.subList(start, end);
    inventory.clear();
    for (int i = 0; i < pageInfractions.size(); i++) {
      Infraction infraction = pageInfractions.get(i);
      ItemStack itemStack = createInfractionItem(infraction);
      inventory.setItem(i, itemStack);
    }

    inventory.setItem(itemsPerPage, currentPageItem());
    inventory.setItem(itemsPerPage + 1, nextPageItem());
    inventory.setItem(itemsPerPage + 2, previousPageItem());
  }

  private List<Infraction> filterType(Infraction.Type type) {
    if (type == null) {
      return infractions;
    }
    return infractions.stream().filter(infraction -> infraction.getType() == type).toList();
  }

  private List<Infraction> filterByIssuer(String issuerName) {
    if (issuerName == null) {
      return infractions;
    }
    return infractions.stream()
        .filter(infraction -> infraction.getIssuerName().equalsIgnoreCase(issuerName))
        .toList();
  }

  private List<Infraction> filterByActiveOnly(boolean isExpired) {
    return infractions.stream().filter(infraction -> infraction.isExpired() == isExpired).toList();
  }

  private ItemStack createInfractionItem(Infraction infraction) {
    ItemStack itemStack = new ItemStack(Material.PAPER);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(infraction.getType().name());
    itemMeta.setLore(
        List.of(
            "Issued by: " + infraction.getIssuerName(),
            "Issued on: " + infraction.getTimestamp(),
            "Reason: " + infraction.getReason()));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  private ItemStack currentPageItem() {
    ItemStack itemStack = new ItemStack(Material.PAPER);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName("Page " + (page + 1) + " of " + maxPages);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  private ItemStack nextPageItem() {
    ItemStack itemStack = new ItemStack(Material.ARROW);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName("Next Page");
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  private ItemStack previousPageItem() {
    ItemStack itemStack = new ItemStack(Material.ARROW);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName("Previous Page");
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public void onClick(InventoryClickEvent event) {
    int slot = event.getRawSlot();
    if (slot >= 0 && slot < itemsPerPage) {
      int index = page * itemsPerPage + slot;
      if (index < infractions.size()) {
        Infraction infraction = infractions.get(index);
        // Handle the click event for the infraction item
      }
      return;
    }

    switch (slot) {
      case itemsPerPage + 1 -> nextPageItem();
      case itemsPerPage + 2 -> previousPageItem();
      default -> {}
    }
    // Handle navigation buttons
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}
