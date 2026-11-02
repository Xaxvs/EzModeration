---
name: spigotmc
description: 'Guidance for building, debugging, and maintaining SpigotMC plugin code and server-side Minecraft Java workflows.'
---

# SpigotMC development skill

Use this skill when working on Java plugins for Spigot, Paper, or Bukkit-based Minecraft servers. It helps with plugin architecture, event handling, command registration, configuration, performance debugging, and compatibility issues across server versions.

## Core workflow

1. Start by confirming the target server platform and API version.
   - Prefer Paper or Spigot compatibility unless the project explicitly targets a different implementation.
   - Check the server version, Bukkit API version, and any dependencies before editing code.

2. Inspect the project layout before changing behavior.
   - Standard plugin structure includes `src/main/java`, `src/main/resources`, `plugin.yml`, and optional schema files.
   - Identify the main command class, listener classes, and service/model classes before patching logic.

3. Follow Spigot plugin conventions.
   - Register commands in the plugin bootstrap class or a dedicated command manager.
   - Use `JavaPlugin`, `Listener`, and event handlers for gameplay logic.
   - Keep plugin state thread-safe when interacting with asynchronous tasks or scheduler calls.
   - Store user-facing defaults and tunables in `schema.yml` or companion schema files.

4. Handle compatibility carefully.
   - Avoid using APIs that are unavailable in the target Minecraft version.
   - If a method or enum changed across versions, add version checks or use reflection only when necessary.
   - Prefer stable, documented Bukkit/Spigot APIs over implementation-specific hacks.

5. Validate behavior with the smallest reliable test loop.
   - Run `mvn test` or the project’s Gradle equivalent when available.
   - If possible, verify plugin startup and basic commands on a local Paper/Spigot server instance.
   - Check logs for `NoClassDefFoundError`, `ClassNotFoundException`, and plugin enable/disable issues.

## Common patterns

- Plugin startup:
  - Initialize schema files, registries, and managers in `onEnable()`.
  - Clean up resources in `onDisable()`.

- Commands:
  - Validate arguments before performing expensive operations.
  - Return clear player-facing feedback using `sendMessage()`.
  - Use permissions checks when appropriate.

- Events:
  - Keep event listeners lightweight and avoid blocking operations inside event handlers.
  - Use Bukkit scheduler or async tasks for long-running work.

- Persistence:
  - Save schema changes within the plugin lifecycle and on explicit user actions.
  - Use YAML or other supported file formats consistently.

## Debugging checklist

When the plugin fails to load or behaves incorrectly:

- Review the server console for startup errors and stack traces.
- Confirm the plugin jar is built for the correct Minecraft version.
- Check `plugin.yml` for commands, permissions, and main class names.
- Verify dependencies are present and not shadowed by incompatible versions.
- Test with a clean server schema before assuming the issue is in gameplay logic.
- Reproduce the issue with logging around the suspected event or command path.

## Recommended project practices

- Keep each system isolated: commands, listeners, services, persistence, and scheduling.
- Prefer clearly named classes and packages over broad utility classes.
- Document assumptions about the server version and supported features.
- Add validation for schema values that must be numeric, non-null, or within ranges.
- Avoid heavy operations in the main thread; offload work to async tasks when appropriate.

## Output expectations

When helping with SpigotMC work, explain:

- the target server/API version,
- the plugin lifecycle step involved,
- the exact class or event where the issue occurs,
- the safest fix that preserves compatibility,
- how to validate the fix in a local test server.

Focus on practical, server-safe fixes rather than speculative API use.
