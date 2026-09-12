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
- **The walk bob is measured in strides, not blocks.** 26.2 runs the camera bob and the
  first-person hand through the same `GameRenderer#bobView` transform, fed by how far the player
  has walked and how far it moved this tick — both in blocks. Scaled up that meant a violent shake
  with the hand whipping across the screen, and scaled down it was deep enough to dip the camera
  through the floor. The pace is divided by the motion scale so the bob keeps its normal cadence at
  any size, and the depth shrinks with the entity while staying put above normal size, where
  vanilla's own cap already holds it steady. Both the first person view and the third person camera
  read the same value, so they move together. The `view_bobbing` scale type is still there and still applied — it
  just defaults to 1 instead of tracking the entity's size, so set it explicitly if you want the
  whole transform to grow.
- **The camera stays above the floor when tiny.** Two things put it through the block underfoot.
  The walk bob translates the view down by up to a tenth of a block, which was more headroom than a
  player below about a thirtieth of normal size has, so every step dipped the camera into the
  ground; scaling the bob's depth with the entity, above, is what fixes that. The other is the
  world's near clipping plane, which 26.2 sets in `Camera#update` — upstream only ever scaled the
  one used for the held item, and the fixed five centimetres reaches past a shrunken player's eyes.
- **The body turns to follow the walk at any size.** Vanilla only swings an entity's body round to
  face the way it is travelling once it has covered a set distance within the tick. A shrunken
  entity's whole stride falls short of that, so below about 1/3 scale the body stayed pointing
  wherever it last faced while the head turned ahead of it. The threshold now follows the entity's
  own stride.
- **Falling is not slowed by shrinking.** Scaling what an entity covers in a tick is what gives a
  small one small steps, but applying it downwards as well made a shrunken player drift to the
  ground like a feather. Gravity now pulls at its normal rate however small the entity is. Growing
  still speeds a fall up, which is what stops a large entity appearing to sink in slow motion.
- **The inventory portrait fits its frame.** It draws through the normal entity renderer, so a
  grown player overflowed the box and only a sliver stayed visible. Growth is now capped at the
  frame, the same way vanilla already caps its own scale attribute there. Shrinking is untouched,
  so a small player still shows small.
- **A config screen on every loader.** The mod list's config button used to be greyed out
  everywhere, since upstream never had a screen to open. There is one now: it lists every key in
  `config/pehkui/config.json` by its exact name, with a search box and paging for the hundred-odd
  entries the scale clamps add, and it writes straight back to the file. It is registered through
  each loader's own hook — Mod Menu on Fabric and Quilt, `IConfigScreenFactory` on NeoForge,
  `ConfigScreenHandler` on Forge — so the button is live in all four mod lists.
- **Loader-patched sweep attack.** Forge and NeoForge each rewrite part of
  `Player#doSweepAttack`: NeoForge swaps the hard-coded 9-block radius for the entity interaction
  range attribute (which Pehkui already scales), and Forge builds the sweep box through its own
  `ItemStack#getSweepHitBox` hook. The two injections covering those lines are marked optional so
  each loader applies the ones that still exist on it. Every other injection is required on every
  loader, so a future Minecraft change breaks the build loudly instead of silently doing nothing.

### Cost at large scales

Several times a tick, vanilla walks every block an entity's hitbox covers: once to resolve
collision, once to apply the effects of the blocks it is standing in, and once more to check for
suffocation. That is nothing at the sizes the game produces on its own — its largest entity is under
sixteen blocks across — but the work grows with the cube of the scale, so a hitbox left to grow
freely does not slow the game down, it stops it.

`physicsBoxLimit` in `config/pehkui/config.json` is the side, in blocks, of the largest hitbox that
still gets all of it; it defaults to 24, which for a player is a little under thirty times normal
size. Past that the two scans that only read the world are dropped: the block-effect scan and the
suffocation check. A cactus underfoot means nothing to something the size of a hill, and it is the
scan rather than the effect that stalls the game. The trade is that blocks acting on an entity from
the inside — nether portals included — no longer act on one this large.

Collision is deliberately left exactly as vanilla resolves it. Cutting the collision scan down the
same way did make it cheap, but it left the parts of a large entity outside the scanned region
overlapping terrain that nothing then pushed them out of, and the entity got flung around instead
of walking. Whatever cost remains at very large sizes is that scan, and it is inherent to a large
hitbox rather than something the mod adds on top. If you want a hard ceiling, every scale type takes
a configurable maximum: `/scale debug config set hitbox_width maximum <n>`, and the same for
`hitbox_height`, keeps the hitbox at a fixed size while the model carries on growing.

Two smaller things Pehkui itself did are bounded now as well:

- Vanilla nudges an entity out of a wall after it grows by searching every block its new hitbox
  covers, and skips that entirely for anything over 4 blocks across. Pehkui re-enabled the search
  for players without that limit, so a growing player ran a whole-hitbox free-space search every
  tick on the client. It now honours the same limit.
- The two climb checks that let a wide entity grab a ladder its centre point misses sweep the
  blocks under the hitbox, so their cost grows with the square of the width, several times a tick.
  The sweep is abandoned past a 16x16 footprint.

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
