package me.xaxis.ezmoderation.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.ConfigSource;
import me.xaxis.ezmoderation.backend.config.ConfigValidator;
import me.xaxis.ezmoderation.backend.player.PlayerActivity;
import me.xaxis.ezmoderation.backend.player.PlayerInfractionHistory;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.PlayerProfileConfigLoader;
import me.xaxis.ezmoderation.backend.player.PlayerProfileSchema;
import me.xaxis.ezmoderation.backend.infraction.Infraction;
import org.junit.jupiter.api.Test;

class EzModerationTest {

  private static final UUID PLAYER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void validatorAcceptsCompleteProfileSchema() {
    Map<String, Object> values = profileValues();
    ConfigValidator validator = new ConfigValidator(new MapConfigSource(values), Logger.getAnonymousLogger());

    assertTrue(validator.validate(values, new PlayerProfileSchema()));
  }

  @Test
  void validatorRejectsMissingNestedPathAndWrongType() {
    Map<String, Object> values = profileValues();
    values.remove("activity.last-login-time");
    values.put("activity.is-online", "true");
    ConfigValidator validator = new ConfigValidator(new MapConfigSource(values), Logger.getAnonymousLogger());

    assertFalse(validator.validate(values, new PlayerProfileSchema()));
  }

  @Test
  void validatorRejectsNullArguments() {
    ConfigValidator validator =
        new ConfigValidator(new MapConfigSource(Map.of()), Logger.getAnonymousLogger());

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(null, new PlayerProfileSchema()));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(Map.of(), null));
  }

  @Test
  void profileLoaderBuildsProfileAndInfraction() {
    Map<String, Object> values = profileValues();
    PlayerProfile profile =
        new PlayerProfileConfigLoader().loadFromConfig(values).orElseThrow();

    assertEquals(PLAYER_ID, profile.getPlayerId());
    assertEquals("Alex", profile.getActivity().getLastKnownName());
    assertTrue(profile.getActivity().isOnline());
    assertTrue(profile.getInfractionHistory().isBanned());
    assertEquals(1, profile.getInfractionHistory().getInfractions().size());
    assertEquals(Infraction.Type.WARN, profile.getInfractionHistory().getInfractions().getFirst().getType());
  }

  @Test
  void profileLoaderRejectsMalformedValues() {
    Map<String, Object> values = profileValues();
    values.put("player-id", "not-a-uuid");
    assertTrue(new PlayerProfileConfigLoader().loadFromConfig(values).isEmpty());

    values = profileValues();
    values.put("infraction-history.infractions", List.of("not-a-map"));
    assertTrue(new PlayerProfileConfigLoader().loadFromConfig(values).isEmpty());

    assertTrue(new PlayerProfileConfigLoader().loadFromConfig(null).isEmpty());
  }

  @Test
  void profileLoaderHandlesRepeatedLoads() {
    PlayerProfileConfigLoader loader = new PlayerProfileConfigLoader();

    for (int i = 0; i < 10_000; i++) {
      Map<String, Object> values = profileValues();
      values.put("activity.last-known-name", "Player" + i);
      assertTrue(loader.loadFromConfig(values).isPresent());
    }
  }

  @Test
  void activityUpdatesStateAndRejectsInvalidInput() {
    Instant initial = Instant.parse("2026-01-01T00:00:00Z");
    PlayerActivity activity = new PlayerActivity(false, initial, initial, "Alex");
    Instant login = initial.plusSeconds(10);
    activity.login("Steve", login);

    assertTrue(activity.isOnline());
    assertEquals("Steve", activity.getLastKnownName());
    assertEquals(login, activity.getLastLogin());

    Instant logout = login.plusSeconds(30);
    activity.logout(logout);
    assertFalse(activity.isOnline());
    assertEquals(logout, activity.getLastLogout());

    assertThrows(IllegalArgumentException.class, () -> activity.login("", login));
    assertThrows(IllegalArgumentException.class, () -> activity.logout(null));
  }

  @Test
  void historyProtectsItsCollectionAndRejectsNullInfractions() {
    Infraction infraction = Infraction.permanent(PLAYER_ID, Infraction.Type.BAN, "griefing");
    PlayerInfractionHistory history = new PlayerInfractionHistory(List.of(infraction), false, false);

    List<Infraction> copy = history.getInfractions();
    assertThrows(UnsupportedOperationException.class, () -> copy.add(infraction));
    assertThrows(IllegalArgumentException.class, () -> history.addInfraction(null));

    history.ban();
    history.mute();
    assertTrue(history.isBanned());
    assertTrue(history.isMuted());
    history.unban();
    history.unmute();
    assertFalse(history.isBanned());
    assertFalse(history.isMuted());
  }

  @Test
  void infractionsRespectPermanentAndExpirationSemantics() {
    Instant now = Instant.now();
    Infraction permanent = Infraction.permanent(PLAYER_ID, Infraction.Type.BAN, "reason");
    Infraction expired =
        Infraction.load(
            PLAYER_ID, Infraction.Type.MUTE, "reason", now.minusSeconds(2), now.minusSeconds(1));
    Infraction active =
        Infraction.load(
            PLAYER_ID, Infraction.Type.WARN, "reason", now, now.plusSeconds(60));

    assertTrue(permanent.isPermanent());
    assertFalse(permanent.isExpired());
    assertTrue(expired.isExpired());
    assertFalse(active.isExpired());
    assertThrows(
        IllegalArgumentException.class,
        () -> Infraction.permanent(PLAYER_ID, Infraction.Type.BAN, " "));
    assertThrows(
        NullPointerException.class,
        () -> Infraction.temporary(PLAYER_ID, Infraction.Type.MUTE, "reason", null));
  }

  @Test
  void profileRequiresPlayerId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new PlayerProfile(null, "Alex"));
  }

  private static Map<String, Object> profileValues() {
    Map<String, Object> values = new HashMap<>();
    values.put("player-id", PLAYER_ID.toString());
    values.put("activity.is-online", true);
    values.put("activity.last-login-time", 1_700_000_000_000L);
    values.put("activity.last-logout-time", 1_700_000_001_000L);
    values.put("activity.last-known-name", "Alex");
    values.put(
        "infraction-history.infractions",
        List.of(
            Map.of(
                "player-id", PLAYER_ID.toString(),
                "type", "WARN",
                "reason", "testing",
                "timestamp", 1_700_000_000_000L)));
    values.put("infraction-history.is-banned", true);
    values.put("infraction-history.is-muted", false);
    return values;
  }

  private record MapConfigSource(Map<String, Object> values) implements ConfigSource {
    @Override
    public boolean isInstance(String path, ConfigEntry.Type expectedType) {
      Object value = values.get(path);
      return switch (expectedType) {
        case STRING, UUID -> value instanceof String;
        case INTEGER -> value instanceof Integer;
        case LONG -> value instanceof Long || value instanceof Integer;
        case BOOLEAN -> value instanceof Boolean;
        case DOUBLE -> value instanceof Double || value instanceof Integer;
        case LIST -> value instanceof List<?>;
      };
    }

    @Override
    public boolean isValid(String path) {
      return values.containsKey(path);
    }
  }
}
