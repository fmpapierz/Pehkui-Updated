# Pehkui

Allows resizing of most entities.

This is a multi-loader port of [Pehkui](https://github.com/Virtuoel/Pehkui) to **Minecraft 26.2**,
covering **Fabric**, **Quilt**, **Forge** and **NeoForge** from one shared codebase.

## Downloads

Each loader gets its own jar. Install only the one that matches your loader.

| Loader | Jar | Also needs |
| --- | --- | --- |
| Fabric | `Pehkui-fabric-<version>.jar` | Fabric API |
| Quilt | `Pehkui-quilt-<version>.jar` | Fabric API (see below) |
| Forge | `Pehkui-forge-<version>.jar` | — |
| NeoForge | `Pehkui-neoforge-<version>.jar` | — |

## Project layout

```
common/     every line of gameplay code, the scale API, and all mixins
fabric/     Fabric entrypoints, networking and metadata
quilt/      Quilt metadata; compiles the common + Fabric sources
forge/      Forge entrypoint, networking and metadata
neoforge/   NeoForge entrypoint, networking and metadata
```

`common` is compiled straight into each loader jar as a source directory rather than being shipped
as a separate library, so there is no extra dependency to install and no jar-in-jar. The only
loader-specific plumbing lives behind `PehkuiPlatform`, a small service interface each loader
project implements: mod-loaded checks, the config directory, and sending payloads to players.

Build everything with:

```bash
./gradlew build
```

Finished jars land in `build/libs/`.

## Requirements

| | |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.160.0+26.2 |
| Quilt Loader | 0.31.0-beta.4+ |
| Forge | 26.2-65.1.3+ |
| NeoForge | 26.2.0.82+ |

## What changed in the port

Minecraft 26.2 is many versions past the 1.21 build this was ported from, so the port is a rewrite
of the Minecraft-facing half rather than a version bump.

- **Single version, Mojang mappings.** The upstream Fabric branch shipped one jar covering
  1.14.4–1.21 through a large set of `compat<version>` mixin packages and reflection-based
  version shims. This build targets 26.2 only, against official mappings, so those packages,
  `VersionUtils`, `BackwardsCompatibility` and the mixin config plugin are all gone. What is left
  is one mixin per target class.
- **Config.** The Fabric-only config library Pehkui depended on has no 26.2 release, so
  `virtuoel.pehkui.api.config` is a small in-house replacement with the same entry shape. The
  config file stays at `config/pehkui/config.json` and keeps the same flat keys, so an existing
  file carries over.
- **Events.** `ScaleType`'s change/tick events used Fabric's event system. They now use
  `ScaleEvent`, which keeps the same `register(...)` / `invoker().onEvent(...)` shape so API
  consumers do not have to change.
- **Saving.** Entity NBT moved to `ValueInput`/`ValueOutput` in 1.21.6, so scale data is now stored
  through those under the same `pehkui:scale_data_types` key as before.
- **Rendering.** 26.2 renders entities from an extracted render state rather than from the entity.
  Model scale is captured in `EntityRenderer#extractRenderState` (stored on the render state via
  `PehkuiRenderStateExtensions`) and applied to the pose stack around `submit`.
- **Knockback.** Upstream patched the knockback constant at each call site. 26.2 routes all
  knockback through `LivingEntity#knockback`, so it is scaled once there, by the attacker.
- **Loader-patched sweep attack.** Forge and NeoForge each rewrite part of
  `Player#doSweepAttack`: NeoForge swaps the hard-coded 9-block radius for the entity interaction
  range attribute (which Pehkui already scales), and Forge builds the sweep box through its own
  `ItemStack#getSweepHitBox` hook. The two injections covering those lines are marked optional so
  each loader applies the ones that still exist on it. Every other injection is required on every
  loader, so a future Minecraft change breaks the build loudly instead of silently doing nothing.

### Not carried over

- **Compatibility mixins for Identity, Magna, reach-entity-attributes and Immersive Portals.**
  None of those have a 26.2 release. The reflective compatibility classes are still present and
  stay inert until the mods they look for appear, so support can come back without a rewrite.
- **Multiconnect support**, which targeted protocol versions this build cannot connect to.
- **The `nbt=` entity-selector exclusion.** Upstream hid scale data from vanilla `@e[nbt=...]`
  matching; in 26.2 that code lives inside a lambda that cannot be targeted stably. Scale data is
  still excluded from advancement/predicate NBT comparisons, and `@e[pehkui.scale_nbt=...]`
  remains the way to match on scales.
- **`Entity#getFinalGravity` scaling**, which was a no-op upstream (the branch returned the
  unscaled value either way). Behaviour is unchanged rather than silently altered.

## Quilt

Quilt Standard Libraries stopped publishing after 1.21.1, and there is no quilt-loom or
quilt-mappings build for 26.2. Quilt Loader itself is current and reads `quilt.mod.json` natively,
so the Quilt jar is built with Fabric Loom against Fabric Loader and Fabric API — both of which
Quilt Loader runs — and ships Quilt's own metadata alongside Fabric's. Install Fabric API next to
it as usual.

## License

MIT, same as upstream. See [LICENSE](LICENSE).
