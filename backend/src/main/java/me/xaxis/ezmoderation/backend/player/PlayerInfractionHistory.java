package me.xaxis.ezmoderation.backend.player;

import java.util.ArrayList;
import java.util.List;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;

public class PlayerInfractionHistory {

  private final List<Infraction> infractions;

  public PlayerInfractionHistory(List<Infraction> infractions) {
    if (infractions == null || infractions.stream().anyMatch(java.util.Objects::isNull)) {
      throw new IllegalArgumentException("Infractions cannot be null");
    }
    this.infractions = new ArrayList<>(infractions);
  }

  public boolean isBanned() {
    return infractions.stream()
        .anyMatch(
            infraction -> infraction.getType() == Infraction.Type.BAN && !infraction.isExpired());
  }

  public boolean isMuted() {
    return infractions.stream()
        .anyMatch(
            infraction -> infraction.getType() == Infraction.Type.MUTE && !infraction.isExpired());
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
