package me.xaxis.ezmoderation.player;

import java.time.Instant;

public final class PlayerActivity {
  private boolean online;
  private Instant lastLogin;
  private Instant lastLogout;
  private String lastKnownName;

  public PlayerActivity(boolean online, Instant lastLogin, Instant lastLogout, String lastKnownName) {
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
    online = true;
    lastLogin = now;
    lastKnownName = playerName;
  }

  public void logout(Instant now) {
    online = false;
    lastLogout = now;
  }
}
