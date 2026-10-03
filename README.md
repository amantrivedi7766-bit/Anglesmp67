# AngelSMP

An **Angel / Demon elemental SMP** plugin for **Paper, Spigot and Purpur**
(Minecraft **1.21+**). It implements all seven modules of the specification:
an asynchronous database lifecycle, a cross-dimensional death & banishment loop,
six elemental ability trees, timing/progression frameworks, three GUI menus,
vanilla keybind ability triggers and a full level-up animation engine.

> Download the compiled `AngelSMP-<version>.jar` from the **Actions → Build
> AngelSMP → Artifacts** panel (or the **Releases** page on a version tag).

---

## Build

Compiled against the **lowest** supported API (Paper 1.21.1) so the bytecode
runs on every newer 1.21+ server. Version-sensitive symbols are resolved at
runtime (`util/Compat.java`); no NMS, no version-specific code.

```bash
gradle build        # fat JAR (bundles the SQLite + MySQL JDBC drivers)
gradle spigotCheck  # compiles the same sources against the Spigot API
```

Output: `build/libs/AngelSMP-2.0.0.jar`.

---

## Modules

### Module 1 - Data Architecture & Memory Management
Profiles are keyed strictly by **UUID** and track seven primitives:
`race` (ANGEL/DEMON), `element` (FIRE/ICE/LIGHTNING/WIND/EARTH/LIGHT/NONE),
`level` (0–5), `kills`, `deaths`, `activeAbilityTimestamp`, `ultimateAbilityTimestamp`.
The lifecycle is async-buffered: `AsyncPlayerPreLoginEvent` fetches (and inserts
a baseline row if missing), `PlayerJoinEvent` injects into the live RAM map, and
`PlayerQuitEvent` copies out and saves on a background thread. Back-end is
**SQLite** (default, bundled) or **MySQL** (`database.type` in config).

### Module 2 - Death Loop & Banishment
On death an Angel is demoted to a **Demon** (race = DEMON, element = NONE), loses
one level via `max(0, level - 1)`, the killer's kill counter is audited, a
`ENTITY_WITHER_SPAWN` packet (pitch 0.4) plays dimension-wide and a global
announcement broadcasts. `PlayerRespawnEvent` (HIGHEST) ignores beds/anchors and
forces Demons to the configured Nether spawn after an anti-suffocation scan, then
applies Blindness (4s) + Darkness (6s). The **Rebirth Shard** (a Nether Star with
a hidden namespaced tag, crafted from 4 Diamond Blocks + 4 Netherite Ingots + 1
Nether Star) purifies a Demon back to the Overworld with a 3-second spiral ritual.

### Module 3 - Absolute Elemental Ability Matrix

| Element | Level 1 (Active) | Level 2 |
| --- | --- | --- |
| 🔥 Fire | Blaze Fireball (10s) | Hellfire Dome (30s) |
| ❄️ Ice | Frost Nova (15s) | Glacial Path (passive) |
| ⚡ Lightning | Storm Caller (12s) | Angelic Fury (passive) |
| 💨 Wind | Wind Leap (8s) | Aerodynamic Descent (passive) |
| ⛰️ Earth | Earthen Fortify (20s) | Seismic Pitfall (25s) |
| ☀️ Light | Divine Intervention (18s) | Purifying Beacon (passive) |

Plus Fire's Level 0 passive **Pyro Immunity**. Every cast is gated behind the
assigned element, the required level and an expired cooldown.

### Module 4 - Timing & Progression
A single master task (every 2 ticks) renders the action-bar cooldown bar
(`§cAbility Cooldown: 4.5s [§a████§c██████§7]`) and fires a `● ABILITY READY ●`
alert + chime the moment a timer expires. Two upgrade paths: **combat kills**
(5 / 15 / 30 → levels 1 / 2 / 3) and the **`/upgrade` Sacrifice GUI**.

### Module 5 - Interfaces
`/sparkgui` (or `/angel power`) opens the 27-slot Element Selection Screen;
`/smp menu` opens the 54-slot Admin Overlord Menu (dynamic player heads, live
lore, Shift/Right/Left click matrix) with the 9-slot Level Adjuster Sub-GUI.

### Module 6 - Keybinds
`F` → Level 1 ability; `Shift + F` → Level 2 ultimate; the tagged **Elemental
Wand** (Blaze Rod) right-click / Shift + right-click does the same.

### Module 7 - Level-Up Animation
20-tick movement freeze, three layered sound bursts to nearby players, the
double-helix `sin/cos` particle swirl, and the `★ LEVEL UP ★` title card.

---

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/sparkgui`, `/angel power` | `angel.use` | Element Selection Screen |
| `/smp menu` | `angel.admin` | Admin Overlord Menu |
| `/upgrade` | `angel.use` | Sacrifice Upgrade GUI |

---

## Compatibility

Minecraft **1.21 → latest stable**, on **Paper, Spigot and Purpur**, Java 21.
A CI step compiles the identical sources against the Spigot API to prove no
Paper-only symbol is used, and every renamed symbol is resolved by name at
runtime.
