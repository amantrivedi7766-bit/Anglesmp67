# AngelSMP

An **Angel / Devil elemental SMP** plugin for **Paper, Spigot and Purpur**
(Minecraft **1.20.x - 1.21+**). It implements all seven phases of the
specification: installation requirements, first-join alignment selection, the
floating head-sign system, the full elemental ability breakdown, the interface
system, the death &amp; lockout system and the configuration architecture.

> Download the compiled `AngelSMP-<version>.jar` from the **Actions → Build
> AngelSMP → Artifacts** panel (or the **Releases** page on a version tag).

---

## Build

```bash
gradle build        # fat JAR (bundles the SQLite JDBC driver)
gradle spigotCheck  # compiles the same sources against the Spigot API
```

Output: `build/libs/AngelSMP-3.0.0.jar`.

---

## Phases

**Phase 1 - Installation.** Runs on Spigot / Paper / Purpur. On first boot it
creates `plugins/AngelSMP/` with `config.yml`, `messages.yml` and `database.db`
(SQLite). ProtocolLib and LuckPerms are supported as optional (`softdepend`)
companions - all packet-style animations use the standard Bukkit API, so the
plugin works out of the box without them.

**Phase 2 - First-Time Joining.** A new player is placed in **Stasis** (cannot
walk, break blocks or take damage), shown the `SELECT YOUR DESTINY` title, and
handed the 9×3 Selection GUI to pick their Guardian Spirit.

**Phase 3 - Head Sign System.** A floating Angel Halo (or Devil Horns) is
rendered exactly 0.5 blocks above the head as a client-side particle array,
coloured per element, spinning at the configured speed, crouching with the
player and vanishing while invisible.

**Phase 4 - Abilities.** Six element trees, each with an Active Ability and a
permanent Passive Buff (unlocked at Tier II):

| Element | Active | Passive |
| --- | --- | --- |
| 🔥 Fire | Inferno Blast | Fire Resistance I |
| ❄️ Ice | Glacial Freeze | Water Breathing + fast on ice |
| ⚡ Lightning | Thunder Bolt | Lightning immunity |
| 💨 Wind | Zephyr Leap | Safe Fall (30 blocks) |
| ⛰️ Earth | Giga Shield | Knockback Resistance I |
| ☀️ Light | Holy Restoration | Night Vision I |

Triggers: press **F** (offhand swap), right-click the **Elemental Focus**, or
run `/angel cast`.

**Phase 5 - Interface.** The top-of-screen boss bar (drains 100% → 0% with the
remaining cooldown) and the action-bar tracker (`[■■■■■■□□□□] Cooldown: 4.5
seconds`, turning green with a chime when ready). Three menus: the Player Status
GUI (`/angel power`), the Upgrade GUI (`/upgrade`) and the Admin Management GUI
(`/angel admin`).

**Phase 6 - Death System.** On death: the **Aura Shatter** animation, a server
broadcast, and a dropped **Soul Essence**. On respawn the player enters the
**Fractured Soul** state (Weakness I + Slowness I, halo/horns gone) with a
**15-minute hard lockout**. Recover by waiting (a `SOUL RESTORED` title + chime),
at a **Sacred Altar** (Crying Obsidian + Gold Blocks + Quartz, offering 3 Diamond
Blocks or 1 Nether Star), or take the **Dark Turn** at a **Nether Altar** to
become a Tier 1 Devil.

**Phase 7 - Configuration.** `config.yml` follows the documented blueprint
(server-settings, alignment-settings, ability-settings, death-system,
gui-settings) with `messages.yml` for chat layouts.

---

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/angel power` | `angel.use` | Player Status GUI |
| `/angel upgrade`, `/upgrade` | `angel.use` | Upgrade GUI |
| `/angel cast` | `angel.use` | Cast your Active Ability |
| `/angel admin` | `angel.admin` | Admin Management GUI |
| `/angel token give <player> <n>` | `angel.admin` | Grant Angel Tokens |
| `/angel reload` | `angel.admin` | Reload config |

---

## Compatibility

Minecraft **1.20.x - 1.21+**, on **Paper, Spigot and Purpur**, Java 21. A CI step
compiles the identical sources against the Spigot API to prove no Paper-only
symbol is used, and every renamed symbol is resolved by name at runtime.
