# FRACTURED - ARG horror mod (Fabric, source project)

Fractured is the entity from your YouTube ARG, rebuilt as a wonderland.jar-style anomaly: a fusion of
**The Spawn**, **Lithic** and the **Umbrella Men** (Interloper). Mod id is still `argguy` (assets/data namespace);
everything the player sees is called Fractured.

> **Not compiled or tested.** No Java compiler or internet in my sandbox and I could not check the code against the
> Minecraft 26.1.2 API. Written for Fabric / Java 25 (your coolaliens setup). Expect a few small compile errors;
> see "Version-sensitive spots".

## Build
1. Copy `build.gradle`, `gradle.properties`, `settings.gradle` and the Gradle wrapper from your working **coolaliens**
   project (values marked TODO are placeholders). Keep the mod id `argguy`.
2. `./gradlew build` -> `build/libs/argguy-0.1.0.jar`. Paste compile errors to me and I will fix them.

## The fusion (sources: wonderland.jar wiki / Interloper wiki, paraphrased)
| From | Behaviour in the mod |
|---|---|
| **Lithic** (granite + fossil-bone golem; chases fast, only stops while looked at; stare too long and it shakes then moves again; can't break blocks; water pushes it back) | It **freezes while you look at it**. Stare ~8 s and it **shakes** (stone/bone rattle sound + body shudder), then **breaks free and charges for 5 s** (fast). It never breaks blocks. **Standing in water keeps it ~4 blocks away.** Granite-grinding sounds while it creeps. |
| **The Spawn** (red-black humanoid on all fours, laughs, ambushes if it hasn't found you; helpless while watched) | **Crawl pose** (on all fours) while charging. **Ambush:** if you have not looked at it for ~3 min it vanishes (dash sound), reappears 16-22 blocks **behind** you, crouches for 5 s, then charges. Wheezing laugh sounds. |
| **Umbrella Men** (Interloper: wind chime, groups, noclip float, model mutilation, drags you out of the map) | Hunts start with a **wind chime**, then 1-2 silent **watchers** stand far away, it **floats through blocks** at you, its model is **mutilated** (slow stretching/warping) while hunting, and when it touches you it **drags you out of the map into the Red Room dimension**. Console-style chat lines (`[HINT] 0 nodes created`, `CONNECTION LOST`, ...). |
| **Your ARG lore** | Invulnerable; Containment Drive (5 passes while looking at it) = the friends' ending: fake crash lines, it escapes, **5 permanent clones**. 3 demo-log tapes + Demo Recorder (types 1-7). |

## Red Room = a real dimension
- `data/argguy/dimension/red_room.json` + `dimension_type/red_room.json`: an empty void, no sky, fixed night, ceiling,
  no mob spawning. Registered automatically by the mod's data (id `argguy:red_room`).
- On capture the player is teleported there into a **suite of 3 connected rooms** (15x7x15 each): red -> white with a
  purple/black *missing-texture* floor -> red, doorways between them, dim ceiling lights. Silent copies named `CAPTURE`
  stand in the rooms and always face you. After ~60 s you are sent back to where you were (any dimension).
- Each capture builds its own suite 64 blocks apart (slot counter). Players stuck in the dimension without a session
  (server restart, `/tp`) are sent to the overworld spawn within 2 s, or run `/fractured exit`.

## Animation
- **Premade clips**: your Blockbench `Idle` (2 s), `Walk` (0.8 s) and `Run` (0.5 s) are converted 1:1 into
  `FracturedAnims` (same bones: body > head / arms, legs at root; run is used while hunting).
- **Code layers on top** (the ones from the Figura avatar): head tracking, breathing (arms + chest), nerve drift when
  idle, "arms always a bit too long", hunting mutilation, crawl, shake. All slow/small: nothing flickers.
- The model hierarchy and every cube come from `argguy.bbmodel`; the rig is converted with a proper axis mapping
  (x and y flipped), so left/right match the original.

## Spawn egg
`Fractured Spawn Egg` (creative: Spawn Eggs tab, dark red/granite grey). Also `/fractured summon`.

## Commands (open to everyone for now, TODO in ArgGuyMod)
`/fractured summon` | `hunt` | `ending` | `room` (test the Red Room dimension) | `exit`

## ARG hooks
All story text, URLs and codes: `assets/argguy/lang/en_us.json` (`tape.argguy.*`, `console.argguy.*`, `demo.argguy.*`).
Replace the `[...TODO]` channel links/codes. See `ARG_BIBLE.md`.

## Photosensitivity (important - the lore victim is epileptic)
Nothing flashes or strobes: no screen flash, no rapid flicker, the warp has multi-second periods, the shake is a small
positional motion, and the only screen effect is vanilla Darkness (slow pulse). Keep it that way and put a
photosensitivity/horror warning on the mod page and videos.

## Version-sensitive spots (likely compile errors)
1. `client/*` render + animation classes (`KeyframeAnimation.apply/applyWalk`, `AnimationDefinition.bake`,
   `extractRenderState`, `AnimationState.copyFrom`).
2. `ArgGuyMod`: `EntityType.Builder.build(key)`, `Item.Properties.setId/spawnEgg`, `SpawnEggItem(props)`, `Identifier`,
   Fabric `EntityRendererRegistry`.
3. `RedRoom`: `ServerPlayer.teleportTo(level, x, y, z, Set, yaw, pitch, boolean)` (cross-dimension teleport),
   `Level.getServer()`, `ServerTickEvents`.
4. `FracturedEntity`: `defineSynchedData(Builder)`, `noPhysics`, `level().noCollision`, `getHeightmapPos`.
5. `dimension_type/red_room.json`: if your version rejects a field, copy `minecraft:overworld`'s file from the game jar
   and only change `min_y` 0, `height` 128, `logical_height` 128, `has_ceiling` true, `fixed_time` 18000.
6. Item `use` / `interactLivingEntity` return types; command permission check (left out on purpose).
