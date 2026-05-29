<div align="center">
  <h1>☁️ CloudFall Core</h1>
  <p><i>The ultimate Living World Engine and Core Utility plugin for Spigot & Paper.</i></p>

  <img src="https://img.shields.io/badge/Paper-1.21-green.svg" alt="Paper 1.21" />
  <img src="https://img.shields.io/badge/Java-21+-blue.svg" alt="Java 21+" />
  <img src="https://img.shields.io/badge/Events-40-orange.svg" alt="40 Random Events" />
  <img src="https://img.shields.io/badge/Version-1.1.0-red.svg" alt="v1.1.0" />
</div>

---

## 📖 Overview

**CloudFall Core** is a custom-built Spigot/Paper plugin that replaces traditional, boring survival gameplay with a dynamic **Living World Engine**. It triggers over 40 unique events at random intervals to keep players constantly engaged, rewarded, or terrified.

In addition to the world engine, it handles server essential commands (Store, Discord, Support) and an automated broadcasting system, all of which are highly customizable or toggleable via configuration.

---

## ✨ Features

- **Living World Engine**: Randomly fires events every 3-8 minutes (configurable).
- **Weighted Event Selection**: Events are categorized and selected based on configurable probability weights (Weather, Horror, Reward, Combat, Social, Mystery).
- **AFK Skipping**: Intelligent AFK tracker prevents personal events from targeting players who are away.
- **Smart Scaling**: Combat events automatically scale the number of spawned mobs based on the active player count to ensure fairness.
- **Crash Prevention**: Robust error handling ensures that if a single event fails, the engine safely logs the error and continues running.
- **Auto-Broadcaster**: Broadcasts important server links at regular intervals.
- **No Block Damage**: Events like the Meteor Shower create dramatic visual explosions without damaging player builds or farms.
- **Customizable Commands**: All command messages/links can be easily configured, and commands can be enabled or disabled entirely.

---

## 🌩️ Event Categories (40 Total Events)

1. **Weather & World** (e.g., *Acid Rain, Meteor Shower, Solar Eclipse, The Fog*)
2. **Horror & Psychological** (e.g., *Phantom Footsteps, Cave Breathing, The Screamer, Inventory Shuffle*)
3. **Reward & Loot** (e.g., *Supply Drop, Golden Hour, XP Rain, Full Heal*)
4. **Combat & Challenge** (e.g., *Zombie Siege, Boss Spawn, Creeper Swarm*)
5. **Social & Fun** (e.g., *Server Trivia, Dare System, Photo Bomb*)
6. **Mystery & Exploration** (e.g., *Dimensional Rift, Buried Treasure, The Merchant*)

---

## 💻 Commands

All commands can be toggled on/off in the config.

| Command | Toggle Path | Description |
|---|---|---|
| `/support` | `commands.support.enabled` | Displays clickable links to the Store, Discord, and Website. |
| `/buyrank` | `commands.buyrank.enabled` | Displays available ranks and their perks. |
| `/buykits` | `commands.buykits.enabled` | Directs players to the store to retrieve kits. |

---

## ⚙️ Configuration

The `config.yml` file allows full customization. Below is a sample snippet showing how to configure command toggles and weights:

```yaml
# Command Configurations
commands:
  support:
    enabled: true
    disabled-message: "<red>This command is currently disabled.</red>"
    messages:
      - "<color:#00ffff><bold>YOURSERVER STORE</bold></color>"
      # ...

# Living World Engine Settings
engine:
  enabled: true
  min-interval-minutes: 3
  max-interval-minutes: 8
  category-weights:
    weather: 18
    horror: 20
    reward: 30
    combat: 7
    social: 15
    mystery: 10
```

---

## 📥 Installation

The easiest way to install CloudFall Core is to download the pre-compiled JAR:
1. Go to the **[Releases](https://github.com/laalaalaee/cloudfall-core-plugin/releases)** tab on the right side of this repository.
2. Download the latest `CloudFallCore-1.1.0.jar`.
3. Drop it into your server's `plugins/` folder and restart your server.
*(Note: This plugin is compiled for Java 21. It is fully forward-compatible and works perfectly on servers running Java 21, 22, 23, 24, and 25).*

---

## 🛠️ Building from Source

If you want to compile the code yourself, this project requires **Java 21** and **Maven**.

1. Clone the repository:
   ```bash
   git clone https://github.com/laalaalaee/cloudfall-core-plugin.git
   ```
2. Navigate to the project directory:
   ```bash
   cd cloudfall-core-plugin
   ```
3. Compile using Maven:
   ```bash
   mvn clean package
   ```
4. The compiled JAR will be located in the `target/` directory as `CloudFallCore-1.1.0.jar`. Drop this into your server's `plugins/` folder and restart!

---
*Built with ❤️ for the Minecraft Community.*
