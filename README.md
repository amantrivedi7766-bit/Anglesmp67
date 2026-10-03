# AngelSMP

**AngelSMP** is a Paper/Spigot elemental-abilities plugin. Every player is
assigned one of **six elemental archetypes**, each with a **three-tier**
progression path, cooldown HUD, and three native chest GUIs. Abilities fire
real-time particle and sound animations exactly as laid out in the technical
specification.

> Download the compiled `AngelSMP-<version>.jar` from the **Actions → Build
> AngelSMP → Artifacts** panel of this repository (or from the **Releases**
> page when a version tag is pushed).

---

## Build

The plugin is compiled against the **lowest** supported API (Paper 1.21.1) so
that the resulting bytecode keeps working on every newer 1.21+ server. All
version- and platform-sensitive symbols are resolved at runtime (see
`util/Compat.java`) - no NMS and no version-specific code anywhere.

```bash
gradle build        # produces the JAR
gradle spigotCheck  # compiles the same sources against the Spigot API
```

The JAR lands in `build/libs/AngelSMP-1.0.0.jar`.

A GitHub Actions workflow (`.github/workflows/build.yml`) builds the JAR on
every push, **verifies Spigot API compatibility** and uploads the artifact.

---

## Compatibility

| | Support |
| --- | --- |
| **Minecraft** | 1.21 and every subsequent stable release |
| **Server software** | Paper, Spigot, Purpur |
| **Java** | 21 |
| **NMS / version-specific code** | None - Bukkit/Paper API only |

How this is achieved:

* Compiled against **Paper API 1.21.1** (the lowest supported version), so newer
  servers load it unchanged.
* A CI step (`gradle spigotCheck`) compiles the identical sources against the
  plain **Spigot API** to prove no Paper-only symbol is used.
* `util/Compat.java` resolves every symbol that has been renamed or moved across
  versions **by name at runtime** (potion effects such as
  `RESISTANCE`/`DAMAGE_RESISTANCE`, `MAX_HEALTH`/`GENERIC_MAX_HEALTH`, particles,
  and the action-bar API), caching the result and degrading gracefully instead
  of throwing `NoSuchFieldError` / `NoSuchMethodError`.
* Particles are looked up by their `minecraft:` key with a registry fallback;
  sounds are played by namespaced key - both survive renames.
* No Adventure or Paper-only API is referenced at compile time, so the same JAR
  runs on Spigot and Purpur too.

---

## Installation

1. Drop `AngelSMP-1.0.0.jar` into your server's `plugins/` folder.
2. Start the server once to generate `plugins/AngelSMP/config.yml`.
3. Tune the config, then `/angel reload` (or restart).

Supported on **Paper, Spigot and Purpur, Minecraft 1.21 and newer**, with **Java 21**.

---

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/angel power` | `angel.use` | Activate your elemental ability. |
| `/angel menu` (`/angel gui`) | `angel.use` | Open the Player Menu GUI. |
| `/angel admin` | `angel.admin` | Open the Operator Admin Dashboard. |
| `/angel set <player> <element>` | `angel.admin` | Override a player's element. |
| `/angel reload` | `angel.admin` | Reload `config.yml` on the fly. |
| `/angel help` | - | List commands. |

Aliases: `/angelsmp`, `/ang`, `/angels`.

---

## The six archetypes

| Element | Active ability | Base cooldown |
| --- | --- | --- |
| 🔥 Fire (Pyromancer) | Directional fireball → non-destructive explosion + burn DoT | 10s |
| ❄️ Ice (Cryomancer) | Eye raycast → stun/freeze + water → frosted_ice | 15s |
| ⚡ Lightning (Stormbringer) | Raycast → LightningBolt strike + hard stun | 12s |
| 🌀 Wind (Zephyr) | Forward leap (1.8×) + AoE knockback | 8s |
| ⛰️ Earth (Titan) | Resistance + Absorption buff shell | 20s |
| ☀️ Light (Seraph) | Instant heal + Regeneration + Glowing | 18s |

Each element scales through **Tier 1 → Tier 2 → Tier 3** with the exact
cooldowns, damage/healing, durations and milestone unlocks from the metrics
table (see `AngelElement.java`).

### Tier milestones

* **Fire** — small fireball → explosive ghast fireball → triple spread cone.
* **Ice** — single freeze → frosty trail → Glacial Tomb (5-block freeze).
* **Lightning** — 10-block ray → 18-block ray → Storm Call (3 strikes).
* **Wind** — basic leap → Speed II on landing → Sonic Boom (no fall damage).
* **Earth** — Res II/Abs I → Res III/Abs II → Unmovable Titan (knockback immune).
* **Light** — heal 8 → heal 12 + Strength → Holy Aura (heal allies, 6-block).

---

## GUIs

### 👥 Player Menu (`/angel menu`) — 9×3
Gray-pane frame with your skull profile (slot 10), the **Evolutionary Upgrade**
trigger (slot 13) and the **Stats & Codex Ledger** (slot 16).

### 👑 Upgrade Window — 9×5
Golden-pane pipeline with a diagonal tier path:
`Tier 1 (10) → » (11) → Tier 2 (21) → » (22) → Tier 3 (32)`, plus the
**Return** barrier (slot 39). Locked tiers show redstone, unlockable tiers
show a **flashing gold ingot** with the exact cost lore, unlocked tiers show a
glowing emerald block.

### 🛡️ Operator Admin Dashboard (`/angel admin`) — 9×6
Black-pane matrix with six element injectors (slots 10–15), a live feed of
online-player skulls (slots 28–34, 37–43) and the **RELOAD MASTER CACHE** TNT
(slot 48). Click an injector to lock a cursor brush, then click a player skull
to override that player's element via the console backend.

---

## Cooldown HUD

* **Action bar** — `⚡ ANGEL POWER READY` / `⏳ ANGEL POWER COOLDOWN: %time%s`,
  with the █ progress bar draining one block per second.
* **Boss bar** — per-element colour, `SEGMENTED_10` style, progress draining
  smoothly from 1.0 → 0.0.

Both are toggled in `config.yml`.

---

## Configuration

`config.yml` controls the HUD toggles, action-bar templates, upgrade costs and
first-join element assignment. Player element + tier are persisted in
`plugins/AngelSMP/players.yml`.

---

## Project layout

```
src/main/java/com/angelsmp/angel/
├── AngelPlugin.java          Core engine / wiring
├── element/                  AngelElement, TierData
├── player/                   PlayerData, PlayerManager
├── ability/                  Ability, AbilityManager, ProjectileManager, impl/*
├── status/                   StatusManager (stun, knockback/fall immunity)
├── cooldown/                 CooldownManager
├── tier/                     UpgradeService
├── hud/                      HudManager (action bar + boss bar)
├── gui/                      MenuGui, UpgradeGui, AdminGui, CodexGui
├── listener/                 GuiListener, CombatListener, ConnectionListener
├── command/                  AngelCommand
└── util/                     Text, Particles, Sounds, ItemBuilder, Raycast
```

---

## License

See [LICENSE](LICENSE).
