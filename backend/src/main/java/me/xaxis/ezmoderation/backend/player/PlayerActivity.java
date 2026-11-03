package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;

public final class PlayerActivity {
  private boolean online;
  private Instant lastLogin;
  private Instant lastLogout;
  private String lastKnownName;

  public PlayerActivity(boolean online, Instant lastLogin, Instant lastLogout, String lastKnownName) {
    if (lastLogin == null || lastLogout == null) {
      throw new IllegalArgumentException("Activity timestamps are required");
    }
    if (lastKnownName == null || lastKnownName.isBlank()) {
      throw new IllegalArgumentException("Last known name is required");
    }
    this.online = online;
    this.lastLogin = lastLogin;
    this.lastLogout = lastLogout;
    this.lastKnownName = lastKnownName;
  }

  public boolean isOnline() {
    return online;
  }

  public Instant getLastLogin() {
    return lastLogin;
  }

  public Instant getLastLogout() {
    return lastLogout;
  }

  public String getLastKnownName() {
    return lastKnownName;
  }

  public void login(String playerName, Instant now) {
    if (playerName == null || playerName.isBlank() || now == null) {
      throw new IllegalArgumentException("Player name and login time are required");
    }
    online = true;
    lastLogin = now;
    lastKnownName = playerName;
  }

  public void logout(Instant now) {
    if (now == null) {
      throw new IllegalArgumentException("Logout time is required");
    }
    online = false;
    lastLogout = now;
  }
}
