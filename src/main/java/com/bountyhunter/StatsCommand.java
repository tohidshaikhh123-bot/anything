package com.bountyhunter;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final BountyPlugin plugin;

    public StatsCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        BountyPlugin.PlayerData data = plugin.getData(p);

        if (args.length == 0) {
            p.sendMessage(ChatColor.GOLD + "===== Powers =====");
            p.sendMessage(ChatColor.AQUA + "Stats Points: " + data.statsPoints);
            for (String power : data.powers.keySet()) {
                int level = data.powers.get(power);
                int cost = plugin.config.getInt("power-costs." + power, 1000) * (level + 1);
                p.sendMessage(ChatColor.YELLOW + power + " Lv." + level + ChatColor.GRAY + " - Next: " + cost + " points");
            }
            p.sendMessage(ChatColor.GRAY + "Upgrade: /stats upgrade <power>");
            return true;
        }

        if (args[0].equalsIgnoreCase("upgrade") && args.length >= 2) {
            String power = args[1].toLowerCase();
            if (!data.powers.containsKey(power)) {
                p.sendMessage(ChatColor.RED + "Unknown power!");
                return true;
            }
            int level = data.powers.get(power);
            int cost = plugin.config.getInt("power-costs." + power, 1000) * (level + 1);
            if (data.statsPoints < cost) {
                p.sendMessage(ChatColor.RED + "Not enough stats points!");
                return true;
            }
            data.statsPoints -= cost;
            data.powers.put(power, level + 1);
            applyPower(p, power, level + 1);
            p.sendMessage(ChatColor.GREEN + "Upgraded " + power + " to level " + (level + 1) + "!");
            plugin.updateScoreboard(p);
            return true;
        }

        return false;
    }

    private void applyPower(Player p, String power, int level) {
        switch (power) {
            case "strength" -> p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1 + level * 0.5);
            case "speed" -> p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1 + level * 0.02);
            case "health" -> {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20 + level * 2);
                p.setHealth(p.getMaxHealth());
            }
            case "resistance" -> p.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(level * 2);
            case "haste" -> p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4 + level * 0.3);
        }
    }
}
