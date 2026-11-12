# Bounty Hunter Plugin

## How to build
1. Install Java 21 and Maven
2. Open terminal in this folder
3. Run: `mvn clean package`
4. The plugin jar will be in `target/BountyHunter-1.0.jar`
5. Put it in your Paper server's `plugins` folder
6. Restart the server

## Commands
- `/bounty` - See your info
- `/bounty top` - Leaderboard
- `/class bountyhunter` or `/class raider` - Choose class (once)
- `/abilities` - See abilities
- `/abilities buy <name>` - Buy ability
- `/stats` - See powers
- `/stats upgrade <power>` - Upgrade a power

## Features
- Start with 5,000 bounty
- Kill players to steal bounty + get stats points + money
- Kill mobs for small bounty (45% chance)
- Two classes: Bounty Hunter (more bounty/stats) and Raider (more money)
- Buyable abilities
- Upgradable powers (Strength, Speed, Health, Resistance, Haste)
- Live scoreboard

You can edit values in `config.yml` after first run.
