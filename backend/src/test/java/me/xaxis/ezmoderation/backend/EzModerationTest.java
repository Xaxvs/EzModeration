package me.xaxis.ezmoderation.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

import me.xaxis.ezmoderation.backend.config.*;
import me.xaxis.ezmoderation.backend.player.PlayerActivity;
import me.xaxis.ezmoderation.backend.player.PlayerInfractionHistory;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
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
        PlayerProfileSchema.load(values).orElseThrow();

    assertEquals(PLAYER_ID, profile.getPlayerId());
    assertEquals("Alex", profile.getActivity().getLastKnownName());
    assertTrue(profile.getActivity().isOnline());
    assertTrue(profile.getInfractionHistory().isBanned());
    assertEquals(1, profile.getInfractionHistory().getInfractions().size());
    assertEquals(Infraction.Type.WARN, profile.getInfractionHistory().getInfractions().getFirst().getType());
  }

  @Test
  void profileSchemaSerializesDomainObjectsToConfigValues() {
    PlayerProfile profile =
        PlayerProfileSchema.load(profileValues()).orElseThrow();

    Map<String, Object> serialized = PlayerProfileSchema.serialize(profile);
    Object rawInfractions = serialized.get("infraction-history.infractions");

    assertInstanceOf(List.class, rawInfractions);
    Object rawInfraction = ((List<?>) rawInfractions).getFirst();
    assertInstanceOf(Map.class, rawInfraction);
    assertEquals("WARN", ((Map<?, ?>) rawInfraction).get("type"));
    assertEquals(1_700_000_000_000L, ((Map<?, ?>) rawInfraction).get("timestamp"));
    assertFalse(((Map<?, ?>) rawInfraction).containsKey("expires-at"));
  }

  @Test
  void profileLoaderRejectsMalformedValues() {
    Map<String, Object> values = profileValues();
    values.put("player-id", "not-a-uuid");
    assertTrue(PlayerProfileSchema.load(values).isEmpty());

    values = profileValues();
    values.put("infraction-history.infractions", List.of("not-a-map"));
    assertTrue(PlayerProfileSchema.load(values).isEmpty());

    values = profileValues();
    values.put("activity.last-login-time", 1.5d);
    assertTrue(PlayerProfileSchema.load(values).isEmpty());

    assertTrue(PlayerProfileSchema.load(null).isEmpty());
  }

  @Test
  void profileLoaderHandlesRepeatedLoads() {
    for (int i = 0; i < 10_000; i++) {
      Map<String, Object> values = profileValues();
      values.put("activity.last-known-name", "Player" + i);
      assertTrue(PlayerProfileSchema.load(values).isPresent());
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
    assertThrows(
        IllegalArgumentException.class,
        () -> new PlayerActivity(false, null, initial, "Alex"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new PlayerActivity(false, initial, initial, " "));
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
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Infraction.load(
                PLAYER_ID,
                Infraction.Type.MUTE,
                "reason",
                now,
                now.minusSeconds(1)));
  }

  @Test
  void profileRequiresPlayerId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new PlayerProfile(null, "Alex"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new PlayerProfile(
                PLAYER_ID,
                List.of(),
                false,
                false,
                false,
                null,
                Instant.EPOCH,
                "Alex"));
  }

  @Test
  void validatorsAndFileWritesRejectNullInputs() throws Exception {
    MapConfigSource source = new MapConfigSource(profileValues());
    assertThrows(
        NullPointerException.class,
        () -> new ConfigValidator(null, Logger.getAnonymousLogger()));
    assertThrows(
        NullPointerException.class,
        () -> new ConfigValidator(source, null));
    assertThrows(
        NullPointerException.class,
        () -> me.xaxis.ezmoderation.backend.file.SafeFileWrite.save(null, "data"));
    assertThrows(
        NullPointerException.class,
        () ->
            me.xaxis.ezmoderation.backend.file.SafeFileWrite.save(
                Files.createTempDirectory("ezmoderation-null-write"), null));
  }

  @Test
  void configFileRequiresValidConstructionArgumentsAndInitializesOnce() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> new ConfigFile(null, "profile.yml"));
    assertThrows(IllegalArgumentException.class, () -> new ConfigFile(Path.of("."), null));

    Path folder = Files.createTempDirectory("ezmoderation-config-test");
    ConfigFile file = new ConfigFile(folder, "profile.yml");
    assertTrue(file.initialize());
    assertFalse(file.initialize());
    assertTrue(Files.exists(folder.resolve("profile.yml")));
    file.delete();
    assertFalse(Files.exists(folder.resolve("profile.yml")));
  }

  @Test
  void profileManagerCanCreateAndCacheProfile() throws Exception {
    Path folder = Files.createTempDirectory("ezmoderation-manager-test");
    PlayerProfile expected = new PlayerProfile(PLAYER_ID, "Alex");
    MemoryProfileStore store = new MemoryProfileStore(expected);
    me.xaxis.ezmoderation.backend.player.PlayerProfileManager manager =
        new me.xaxis.ezmoderation.backend.player.PlayerProfileManager(store);

    assertSame(expected, manager.getPlayerProfile(PLAYER_ID.toString()));
    assertSame(expected, manager.getPlayerProfile(PLAYER_ID.toString()));
    assertEquals(1, manager.getPlayerProfiles().spliterator().getExactSizeIfKnown());
  }

  @Test
  void profileManagerRejectsNullDependencies() throws Exception {
    Path folder = Files.createTempDirectory("ezmoderation-manager-null-test");
    MemoryProfileStore store = new MemoryProfileStore(null);

    assertThrows(
        NullPointerException.class,
        () -> new me.xaxis.ezmoderation.backend.player.PlayerProfileManager(null));
  }

  @Test
  void profileManagerRejectsStoreReturningWrongProfile() throws Exception {
    UUID otherId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    MemoryProfileStore store = new MemoryProfileStore(new PlayerProfile(otherId, "Other"));
    me.xaxis.ezmoderation.backend.player.PlayerProfileManager manager =
        new me.xaxis.ezmoderation.backend.player.PlayerProfileManager(store);

    assertThrows(
        IllegalStateException.class,
        () -> manager.getPlayerProfile(PLAYER_ID));
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

  private static final class MemoryProfileStore
      implements ConfigStore<PlayerProfile> {
    private PlayerProfile profile;
    private int saves;

    private MemoryProfileStore(PlayerProfile profile) {
      this.profile = profile;
    }

    @Override
    public ConfigStore.Result<PlayerProfile> load(UUID playerId) {
      return new ConfigStore.Result<>(false, profile);
    }

    @Override
    public void save(ConfigStore.Result<PlayerProfile> result) {
      this.profile = result.profile();
      saves++;
    }
  }
}
