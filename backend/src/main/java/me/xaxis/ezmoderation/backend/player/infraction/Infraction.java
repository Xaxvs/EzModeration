package me.xaxis.ezmoderation.backend.player.infraction;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Infraction {

  private final UUID playerId;
  private final Type type;
  private final String reason;
  private final Instant timestamp;
  private final Instant expiresAt;

  Infraction(UUID playerId, Type type, String reason, Instant expiresAt) {
    this(playerId, type, reason, Instant.now(), expiresAt);
  }

  Infraction(UUID playerId, Type type, String reason, Instant timestamp, Instant expiresAt) {
    this.playerId = Objects.requireNonNull(playerId, "playerId");
    this.type = Objects.requireNonNull(type, "type");
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason cannot be blank");
    }
    this.reason = reason;
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    if (expiresAt != null && expiresAt.isBefore(timestamp)) {
      throw new IllegalArgumentException("expiresAt cannot be before timestamp");
    }
    this.expiresAt = expiresAt;
  }

  public static Infraction temporary(UUID playerId, Type type, String reason, Instant expiresAt) {
    Objects.requireNonNull(expiresAt, "expiresAt");
    return new Infraction(playerId, type, reason, expiresAt) {};
  }

  public static Infraction permanent(UUID playerId, Type type, String reason) {
    return new Infraction(playerId, type, reason, null) {};
  }

  public static Infraction load(
      UUID playerId, Type type, String reason, Instant timestamp, Instant expiresAt) {
    return new Infraction(playerId, type, reason, timestamp, expiresAt) {};
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public String getReason() {
    return reason;
  }

  public Type getType() {
    return type;
  }

  public boolean isPermanent() {
    return expiresAt().isEmpty();
  }

  public boolean isExpired() {
    return !isPermanent() && !Instant.now().isBefore(expiresAt);
  }

  public Optional<Instant> expiresAt() {
    return Optional.ofNullable(expiresAt);
  }

  public enum Type {
    BAN,
    MUTE,
    WARN,
    KICK,
    FREEZE,
  }
}
