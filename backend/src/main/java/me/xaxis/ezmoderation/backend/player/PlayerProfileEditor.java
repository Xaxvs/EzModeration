package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;

import me.xaxis.ezmoderation.backend.player.infraction.Infraction;

public final class PlayerProfileEditor {

  private final PlayerProfile profile;
  private final Runnable markDirty;

  PlayerProfileEditor(PlayerProfile profile, Runnable markDirty) {
    if (profile == null) {
      throw new IllegalArgumentException("profile cannot be null");
    }
    if (markDirty == null) {
      throw new IllegalArgumentException("markDirty cannot be null");
    }
    this.profile = profile;
    this.markDirty = markDirty;
  }

  public PlayerProfileEditor login(Instant lastLoginTime, String playerName) {
    if (lastLoginTime == null) {
      throw new IllegalArgumentException("lastLoginTime cannot be null");
    }
    if (playerName == null || playerName.isBlank()) {
      throw new IllegalArgumentException("playerName cannot be null or blank");
    }
    profile.getActivity().login(playerName, lastLoginTime);
    markDirty.run();
    return this;
  }

  public PlayerProfileEditor logout(Instant lastLogoutTime) {
    if (lastLogoutTime == null) {
      throw new IllegalArgumentException("lastLogoutTime cannot be null");
    }
    profile.getActivity().logout(lastLogoutTime);
    markDirty.run();
    return this;
  }

  public PlayerProfileEditor addInfraction(Infraction infraction) {
    if (infraction == null) {
      throw new IllegalArgumentException("infraction cannot be null");
    }
    profile.getInfractionHistory().addInfraction(infraction);
    markDirty.run();
    return this;
  }

  public PlayerProfileEditor deleteInfraction(Infraction infraction) {
    if (infraction == null) {
      throw new IllegalArgumentException("infraction cannot be null");
    }
    if (!profile.getInfractionHistory().getInfractions().contains(infraction)) return this;
    profile.getInfractionHistory().deleteInfraction(infraction);
    markDirty.run();
    return this;
  }
}
