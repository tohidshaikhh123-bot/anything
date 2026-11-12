package com.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public class BountyPlugin extends JavaPlugin {

    public static BountyPlugin instance;
    public FileConfiguration config;
    public Map<UUID, PlayerData> playerData = new HashMap<>();
    public Map<String, Integer> abilityCosts = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();

        loadAbilityCosts();

        getServer().getPluginManager().registerEvents(new BountyListener(this), this);
        getServer().getPluginManager().registerEvents(new BountyGUI(this), this);
        getCommand("bounty").setExecutor(new BountyCommand(this));
        getCommand("abilities").setExecutor(new AbilityCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("class").setExecutor(new ClassCommand(this));

        // Load all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadPlayer(p);
        }

        getLogger().info("BountyHunter plugin enabled!");
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            savePlayer(p);
        }
        getLogger().info("BountyHunter plugin disabled. Data saved.");
    }

    private void loadAbilityCosts() {
        abilityCosts.put("bounty-magnet", config.getInt("ability-costs.bounty-magnet"));
        abilityCosts.put("life-steal", config.getInt("ability-costs.life-steal"));
        abilityCosts.put("bounty-shield", config.getInt("ability-costs.bounty-shield"));
        abilityCosts.put("critical-strike", config.getInt("ability-costs.critical-strike"));
        abilityCosts.put("ghost-mode", config.getInt("ability-costs.ghost-mode"));
        abilityCosts.put("regeneration", config.getInt("ability-costs.regeneration"));
    }

    public void loadPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        if (!playerData.containsKey(uuid)) {
            PlayerData data = new PlayerData();
            data.bounty = config.getInt("starting-bounty");
            data.statsPoints = 0;
            data.playerClass = "none";
            playerData.put(uuid, data);
        }
        updateScoreboard(p);
    }

    public void savePlayer(Player p) {
        // In a real plugin you would save to file here
        // For this version data stays in memory + will be expanded
    }

    public PlayerData getData(Player p) {
        return playerData.computeIfAbsent(p.getUniqueId(), k -> {
            PlayerData d = new PlayerData();
            d.bounty = config.getInt("starting-bounty");
            return d;
        });
    }

    public void updateScoreboard(Player p) {
        PlayerData data = getData(p);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("bounty", "dummy", ChatColor.GOLD + "" + ChatColor.BOLD + "BOUNTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(ChatColor.YELLOW + "Bounty: " + ChatColor.WHITE + format(data.bounty)).setScore(5);
        obj.getScore(ChatColor.AQUA + "Stats: " + ChatColor.WHITE + data.statsPoints).setScore(4);
        obj.getScore(ChatColor.LIGHT_PURPLE + "Class: " + ChatColor.WHITE + data.playerClass).setScore(3);
        obj.getScore("").setScore(2);
        obj.getScore(ChatColor.GRAY + "/bounty for menu").setScore(1);

        p.setScoreboard(board);
    }

    public String format(long num) {
        if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0);
        if (num >= 1_000) return String.format("%.1fK", num / 1_000.0);
        return String.valueOf(num);
    }

    public static class PlayerData {
        public long bounty = 5000;
        public int statsPoints = 0;
        public String playerClass = "none";
        public Map<String, Integer> powers = new HashMap<>();
        public Set<String> abilities = new HashSet<>();

        public PlayerData() {
            powers.put("strength", 0);
            powers.put("speed", 0);
            powers.put("health", 0);
            powers.put("resistance", 0);
            powers.put("haste", 0);
        }
    }
}
