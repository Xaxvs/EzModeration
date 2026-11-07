package me.xaxis.ezmoderation.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.*;
import me.xaxis.ezmoderation.backend.player.PlayerActivity;
import me.xaxis.ezmoderation.backend.player.PlayerInfractionHistory;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.PlayerProfileManager;
import me.xaxis.ezmoderation.backend.player.PlayerProfileSchema;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;
import me.xaxis.ezmoderation.backend.player.response.PlayerProfileResponse;
import org.junit.jupiter.api.Test;

class EzModerationTest {

  private static final UUID PLAYER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  private static Map<String, Object> profileValues() {
    Map<String, Object> values = new HashMap<>();
    values.put("player-id", PLAYER_ID.toString());
    values.put("activity.last-login-time", 1_700_000_000_000L);
    values.put("activity.last-logout-time", 1_700_000_001_000L);
    values.put("activity.last-known-name", "Alex");
    values.put(
        "infraction-history.infractions",
        List.of(
            Map.of(
                "player-id",
                PLAYER_ID.toString(),
                "issuer-name",
                "Console",
                "type",
                "WARN",
                "reason",
                "testing",
                "timestamp",
                1_700_000_000_000L)));
    return values;
  }

  @Test
  void validatorAcceptsCompleteProfileSchema() {
    Map<String, Object> values = profileValues();
    ConfigValidator validator =
        new ConfigValidator(new MapLoader(values), Logger.getAnonymousLogger());

    assertTrue(validator.validate(values, new PlayerProfileSchema()));
  }

  @Test
  void validatorRejectsMissingNestedPathAndWrongType() {
    Map<String, Object> values = profileValues();
    values.remove("activity.last-login-time");
    values.put("activity.last-logout-time", "not-a-timestamp");
    ConfigValidator validator =
        new ConfigValidator(new MapLoader(values), Logger.getAnonymousLogger());

    assertFalse(validator.validate(values, new PlayerProfileSchema()));
  }

  @Test
  void validatorRejectsNullArguments() {
    ConfigValidator validator =
        new ConfigValidator(new MapLoader(Map.of()), Logger.getAnonymousLogger());

    assertThrows(
        IllegalArgumentException.class, () -> validator.validate(null, new PlayerProfileSchema()));
    assertThrows(IllegalArgumentException.class, () -> validator.validate(Map.of(), null));
  }

  @Test
  void profileLoaderBuildsProfileAndInfraction() {
    Map<String, Object> values = profileValues();
    PlayerProfile profile = PlayerProfileSchema.createFrom(values).orElseThrow();

    assertEquals(PLAYER_ID, profile.playerId());
    assertEquals("Alex", profile.activity().getLastKnownName());
    assertFalse(profile.activity().isOnline());
    assertFalse(profile.infractionHistory().isBanned());
    assertEquals(1, profile.infractionHistory().getInfractions().size());
    assertEquals(
        Infraction.Type.WARN, profile.infractionHistory().getInfractions().getFirst().getType());
  }

  @Test
  void profileSchemaSerializesDomainObjectsToConfigValues() {
    PlayerProfile profile = PlayerProfileSchema.createFrom(profileValues()).orElseThrow();

    Map<String, Object> serialized = PlayerProfileSchema.serialize(profile);
    assertFalse(serialized.containsKey("activity.is-online"));
    assertEquals(1_700_000_000_000L, serialized.get("activity.last-login-time"));
    assertEquals(1_700_000_001_000L, serialized.get("activity.last-logout-time"));
    Object rawInfractions = serialized.get("infraction-history.infractions");

    assertInstanceOf(List.class, rawInfractions);
    Object rawInfraction = ((List<?>) rawInfractions).getFirst();
    assertInstanceOf(Map.class, rawInfraction);
    assertEquals("WARN", ((Map<?, ?>) rawInfraction).get("type"));
    assertEquals("Console", ((Map<?, ?>) rawInfraction).get("issuer-name"));
    assertEquals(1_700_000_000_000L, ((Map<?, ?>) rawInfraction).get("timestamp"));
    assertFalse(((Map<?, ?>) rawInfraction).containsKey("expires-at"));
  }

  @Test
  void profileLoaderRejectsMalformedValues() {
    Map<String, Object> values = profileValues();
    values.put("player-id", "not-a-uuid");
    assertTrue(PlayerProfileSchema.createFrom(values).isEmpty());

    values = profileValues();
    values.put("infraction-history.infractions", List.of("not-a-map"));
    assertTrue(PlayerProfileSchema.createFrom(values).isEmpty());

    values = profileValues();
    values.put("activity.last-login-time", 1.5d);
    assertTrue(PlayerProfileSchema.createFrom(values).isEmpty());

    assertTrue(PlayerProfileSchema.createFrom(null).isEmpty());
  }

  @Test
  void profileRejectsInfractionsBelongingToAnotherPlayer() {
    UUID otherPlayerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    Infraction infraction =
        Infraction.permanent(otherPlayerId, "Console", Infraction.Type.WARN, "testing");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new PlayerProfile(
                PLAYER_ID,
                List.of(infraction),
                false,
                Instant.EPOCH,
                Instant.EPOCH,
                "Alex"));
  }

  @Test
  void profileLoaderHandlesRepeatedLoads() {
    for (int i = 0; i < 10_000; i++) {
      Map<String, Object> values = profileValues();
      values.put("activity.last-known-name", "Player" + i);
      assertTrue(PlayerProfileSchema.createFrom(values).isPresent());
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
        IllegalArgumentException.class, () -> new PlayerActivity(false, null, initial, "Alex"));
    assertThrows(
        IllegalArgumentException.class, () -> new PlayerActivity(false, initial, initial, " "));
  }

  @Test
  void historyProtectsItsCollectionAndRejectsNullInfractions() {
    Infraction infraction =
        Infraction.permanent(PLAYER_ID, "Console", Infraction.Type.BAN, "griefing");
    PlayerInfractionHistory history = new PlayerInfractionHistory(List.of(infraction));

    List<Infraction> copy = history.getInfractions();
    assertThrows(UnsupportedOperationException.class, () -> copy.add(infraction));
    assertThrows(IllegalArgumentException.class, () -> history.addInfraction(null));

    assertTrue(history.isBanned());
    assertFalse(history.isMuted());
  }

  @Test
  void infractionsRespectPermanentAndExpirationSemantics() {
    Instant now = Instant.now();
    Infraction permanent =
        Infraction.permanent(PLAYER_ID, "Console", Infraction.Type.BAN, "reason");
    Infraction expired =
        Infraction.load(
            PLAYER_ID,
            "Console",
            Infraction.Type.MUTE,
            "reason",
            now.minusSeconds(2),
            now.minusSeconds(1));
    Infraction active =
        Infraction.load(
            PLAYER_ID, "Console", Infraction.Type.WARN, "reason", now, now.plusSeconds(60));

    assertTrue(permanent.isPermanent());
    assertFalse(permanent.isExpired());
    assertTrue(expired.isExpired());
    assertFalse(active.isExpired());
    assertThrows(
        IllegalArgumentException.class,
        () -> Infraction.permanent(PLAYER_ID, "Console", Infraction.Type.BAN, " "));
    assertThrows(
        NullPointerException.class,
        () -> Infraction.temporary(PLAYER_ID, "Console", Infraction.Type.MUTE, "reason", null));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Infraction.load(
                PLAYER_ID,
                "Console",
                Infraction.Type.MUTE,
                "reason",
                now,
                now.minusSeconds(1)));
  }

  @Test
  void profileRequiresPlayerId() {
    assertThrows(IllegalArgumentException.class, () -> new PlayerProfile(null, "Alex"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
        new PlayerProfile(PLAYER_ID, List.of(), false, null, Instant.EPOCH, "Alex"));
  }

  @Test
  void validatorsAndFileWritesRejectNullInputs() throws Exception {
    MapLoader source = new MapLoader(profileValues());
    assertThrows(
        NullPointerException.class, () -> new ConfigValidator(null, Logger.getAnonymousLogger()));
    assertThrows(NullPointerException.class, () -> new ConfigValidator(source, null));
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
  void configFileAllowsEmptyConfigurationContent() throws Exception {
    Path folder = Files.createTempDirectory("ezmoderation-empty-config-test");
    ConfigFile file = new ConfigFile(folder, "config.yml");

    file.save("");

    assertEquals("", Files.readString(file.getPath()));
  }

  @Test
  void profileManagerCanCreateAndCacheProfile() throws Exception {
    Path folder = Files.createTempDirectory("ezmoderation-manager-test");
    PlayerProfile expected = new PlayerProfile(PLAYER_ID, "Alex");
    MemoryProfileStore store = new MemoryProfileStore(expected);
    PlayerProfileManager manager = new PlayerProfileManager(store);

    assertEquals("Alex", manager.loadOrCreateProfile(PLAYER_ID, "Alex").name());
    assertEquals("Alex", manager.findProfile(PLAYER_ID).orElseThrow().name());
    assertEquals(1, manager.getPlayerProfiles().spliterator().getExactSizeIfKnown());
  }

  @Test
  void profileManagerSavesNewProfileOnlyWhenRequested() {
    MemoryProfileStore store = new MemoryProfileStore(null);
    PlayerProfileManager manager = new PlayerProfileManager(store);

    manager.loadOrCreateProfile(PLAYER_ID, "Alex");
    assertEquals(0, store.saves);

    manager.saveAllDirtyProfiles();
    assertEquals(1, store.saves);
    assertTrue(store.profile != null);

    manager.saveAllDirtyProfiles();
    assertEquals(1, store.saves);
  }

  @Test
  void profileManagerDoesNotReloadCachedProfile() {
    PlayerProfile profile = new PlayerProfile(PLAYER_ID, "Alex");
    MemoryProfileStore store = new MemoryProfileStore(profile);
    PlayerProfileManager manager = new PlayerProfileManager(store);

    manager.loadOrCreateProfile(PLAYER_ID, "Alex");
    manager.loadOrCreateProfile(PLAYER_ID, "Changed");

    assertEquals(1, store.loads);
    assertEquals("Alex", manager.findProfile(PLAYER_ID).orElseThrow().name());
  }

  @Test
  void profileManagerTracksEditorChangesAndIgnoresNoOpDelete() {
    PlayerProfile profile = new PlayerProfile(PLAYER_ID, "Alex");
    MemoryProfileStore store = new MemoryProfileStore(profile);
    PlayerProfileManager manager = new PlayerProfileManager(store);
    manager.loadOrCreateProfile(PLAYER_ID, "Alex");

    Infraction infraction =
        Infraction.permanent(PLAYER_ID, "Console", Infraction.Type.BAN, "testing");
    manager.editProfile(PLAYER_ID).addInfraction(infraction);
    manager.saveAllDirtyProfiles();
    assertEquals(1, store.saves);

    manager.editProfile(PLAYER_ID).deleteInfraction(infraction);
    manager.editProfile(PLAYER_ID).deleteInfraction(infraction);
    manager.saveAllDirtyProfiles();
    assertEquals(2, store.saves);
  }

  @Test
  void profileManagerRejectsEditingUnknownProfile() {
    PlayerProfileManager manager = new PlayerProfileManager(new MemoryProfileStore(null));

    assertThrows(
        IllegalStateException.class,
        () -> manager.editProfile(PLAYER_ID));
  }

  @Test
  void profileManagerReturnsReadOnlySnapshots() {
    PlayerProfile profile = new PlayerProfile(PLAYER_ID, "Alex");
    MemoryProfileStore store = new MemoryProfileStore(profile);
    PlayerProfileManager manager = new PlayerProfileManager(store);
    manager.loadOrCreateProfile(PLAYER_ID, "Alex");

    PlayerProfileResponse response = manager.findProfile(PLAYER_ID).orElseThrow();
    assertEquals(PLAYER_ID.toString(), response.uuid());
    assertThrows(
        UnsupportedOperationException.class,
        () -> response.playerInfractionHistory().infractions().add(
            Infraction.permanent(PLAYER_ID, "Console", Infraction.Type.WARN, "testing")));
  }

  @Test
  void profileManagerRejectsMissingPlayerIdentity() {
    MemoryProfileStore store = new MemoryProfileStore(null);
    PlayerProfileManager manager = new PlayerProfileManager(store);

    assertThrows(IllegalArgumentException.class, () -> manager.findProfile(null));
  }

  @Test
  void profileManagerRejectsNullDependencies() throws Exception {
    Path folder = Files.createTempDirectory("ezmoderation-manager-null-test");
    MemoryProfileStore store = new MemoryProfileStore(null);

    assertThrows(NullPointerException.class, () -> new PlayerProfileManager(null));
  }

  @Test
  void loadResultRejectsNullOptional() {
    assertThrows(
        NullPointerException.class,
        () -> new ConfigStore.LoadResult<PlayerProfile>(null, false));
  }

  @Test
  void profileManagerRejectsStoreReturningWrongProfile() throws Exception {
    UUID otherId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    MemoryProfileStore store = new MemoryProfileStore(new PlayerProfile(otherId, "Other"));
    PlayerProfileManager manager = new PlayerProfileManager(store);

    assertThrows(
        IllegalStateException.class,
        () -> manager.loadOrCreateProfile(PLAYER_ID, "Alex"));
  }

  private record MapLoader(Map<String, Object> values) implements Loader<Map<String, Object>> {
    @Override
    public Map<String, Object> load() {
      return values;
    }

    @Override
    public void write(Map<String, Object> values) {}

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

    @Override
    public void save() {}
  }

  private static final class MemoryProfileStore implements ConfigStore<PlayerProfile> {
    private PlayerProfile profile;
    private int saves;
    private int loads;

    private MemoryProfileStore(PlayerProfile profile) {
      this.profile = profile;
    }

    @Override
    public ConfigStore.LoadResult<PlayerProfile> get(UUID playerId) {
      loads++;
      return new ConfigStore.LoadResult<>(Optional.ofNullable(profile), false);
    }

    @Override
    public void save(PlayerProfile profile) {
      this.profile = profile;
      saves++;
    }
  }
}
