package com.bountyhunter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {

    private final BountyPlugin plugin;

    public ClassCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (args.length == 0) {
            p.sendMessage(ChatColor.GOLD + "Choose your class:");
            p.sendMessage(ChatColor.YELLOW + "/class bountyhunter " + ChatColor.GRAY + "- More bounty & stats");
            p.sendMessage(ChatColor.RED + "/class raider " + ChatColor.GRAY + "- More money from kills");
            return true;
        }

        BountyPlugin.PlayerData data = plugin.getData(p);
        if (!data.playerClass.equals("none")) {
            p.sendMessage(ChatColor.RED + "You already chose a class!");
            return true;
        }

        if (args[0].equalsIgnoreCase("bountyhunter")) {
            data.playerClass = "Bounty Hunter";
            p.sendMessage(ChatColor.GREEN + "You are now a Bounty Hunter!");
        } else if (args[0].equalsIgnoreCase("raider")) {
            data.playerClass = "Raider";
            p.sendMessage(ChatColor.GREEN + "You are now a Raider!");
        } else {
            p.sendMessage(ChatColor.RED + "Unknown class. Use bountyhunter or raider.");
            return true;
        }

        plugin.updateScoreboard(p);
        return true;
    }
}
