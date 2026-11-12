package com.bountyhunter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbilityCommand implements CommandExecutor {

    private final BountyPlugin plugin;

    public AbilityCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        BountyPlugin.PlayerData data = plugin.getData(p);

        if (args.length == 0) {
            p.sendMessage(ChatColor.GOLD + "===== Abilities =====");
            p.sendMessage(ChatColor.YELLOW + "Your Money: " + plugin.format(data.money));
            for (String ability : plugin.abilityCosts.keySet()) {
                String status = data.abilities.contains(ability) ? ChatColor.GREEN + "OWNED" : ChatColor.RED + "Not owned";
                int cost = plugin.abilityCosts.get(ability);
                p.sendMessage(ChatColor.AQUA + ability + ChatColor.GRAY + " - " + cost + " money " + status);
            }
            p.sendMessage(ChatColor.GRAY + "Buy with: /abilities buy <name>");
            return true;
        }

        if (args[0].equalsIgnoreCase("buy") && args.length >= 2) {
            String ability = args[1].toLowerCase();
            if (!plugin.abilityCosts.containsKey(ability)) {
                p.sendMessage(ChatColor.RED + "Unknown ability!");
                return true;
            }
            if (data.abilities.contains(ability)) {
                p.sendMessage(ChatColor.RED + "You already own this!");
                return true;
            }
            int cost = plugin.abilityCosts.get(ability);
            if (data.money < cost) {
                p.sendMessage(ChatColor.RED + "Not enough money! Need " + cost);
                return true;
            }
            data.money -= cost;
            data.abilities.add(ability);
            p.sendMessage(ChatColor.GREEN + "You bought " + ability + "!");
            plugin.updateScoreboard(p);
            return true;
        }

        return false;
    }
}
