package me.xaxis.ezmoderation.backend.player;

import java.util.ArrayList;
import java.util.List;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;

public class PlayerInfractionHistory {

  private final List<Infraction> infractions;
  private boolean isBanned;
  private boolean isMuted;

  public PlayerInfractionHistory(List<Infraction> infractions, boolean isBanned, boolean isMuted) {
    if (infractions == null || infractions.stream().anyMatch(java.util.Objects::isNull)) {
      throw new IllegalArgumentException("Infractions cannot be null");
    }
    this.infractions = new ArrayList<>(infractions);
    this.isBanned = isBanned;
    this.isMuted = isMuted;
  }

  public boolean isBanned() {
    return isBanned;
  }

  public void unban() {
    isBanned = false;
  }

  public void ban() {
    isBanned = true;
  }

  public boolean isMuted() {
    return isMuted;
  }

  public void unmute() {
    isMuted = false;
  }

  public void mute() {
    isMuted = true;
  }

  public List<Infraction> getInfractions() {
    return infractions.stream().toList();
  }

  public void addInfraction(Infraction infraction) {
    if (infraction == null) {
      throw new IllegalArgumentException("Infraction cannot be null");
    }
    infractions.add(infraction);
  }

  public void deleteInfraction(Infraction infraction) {
    infractions.remove(infraction);
  }
}
