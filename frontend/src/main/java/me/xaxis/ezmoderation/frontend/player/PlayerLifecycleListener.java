package me.xaxis.ezmoderation.frontend.player;

import java.time.Instant;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.player.data.PlayerProfileManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLifecycleListener implements Listener {

  private final PlayerProfileManager playerProfileManager;

  public PlayerLifecycleListener(PlayerProfileManager playerProfileManager) {
    if (playerProfileManager == null) {
      throw new NullPointerException("playerProfileManager cannot be null");
    }
    this.playerProfileManager = playerProfileManager;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    playerProfileManager.loadOrCreateProfile(playerId, event.getPlayer().getName());
    playerProfileManager.editProfile(playerId).login(Instant.now(), event.getPlayer().getName());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    if (playerProfileManager.findProfile(playerId).isEmpty()) {
      return;
    }
    playerProfileManager.editProfile(playerId).logout(Instant.now());
    playerProfileManager.savePlayerProfile(playerId);
  }
}
