package me.xaxis.ezmoderation.player;

import me.xaxis.ezmoderation.infraction.Infraction;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfractionHistory {

  private final List<Infraction> infractions;
  private boolean isBanned;
  private boolean isMuted;

  public PlayerInfractionHistory(List<Infraction> infractions, boolean isBanned, boolean isMuted) {
    this.infractions = new ArrayList<>(infractions);
    this.isBanned = isBanned;
    this.isMuted = isMuted;
  }

  public boolean isBanned() {
    return isBanned;
  }

  public void unban() {
    this.isBanned = false;
  }

  public void ban() {
    this.isBanned = true;
  }

  public boolean isMuted() {
    return isMuted;
  }

  public void unmute() {
    this.isMuted = false;
  }

  public void mute() {
    this.isMuted = true;
  }

  public List<Infraction> getInfractions() {
    return infractions.stream().toList();
  }

  public void addInfraction(Infraction infraction) {
    infractions.add(infraction);
  }

  public void deleteInfraction(Infraction infraction) {
    infractions.remove(infraction);
  }

}
