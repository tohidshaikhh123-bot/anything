package com.bountyhunter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BountyCommand implements CommandExecutor {

    private final BountyPlugin plugin;

    public BountyCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        if (args.length == 0) {
            new BountyGUI(plugin).openMainMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("top")) {
            List<Player> online = new ArrayList<>(plugin.getServer().getOnlinePlayers());
            online.sort((a, b) -> Long.compare(plugin.getData(b).bounty, plugin.getData(a).bounty));

            p.sendMessage(ChatColor.GOLD + "===== Top Bounties =====");
            for (int i = 0; i < Math.min(10, online.size()); i++) {
                Player target = online.get(i);
                p.sendMessage(ChatColor.YELLOW + "" + (i+1) + ". " + target.getName() + " - " + plugin.format(plugin.getData(target).bounty));
            }
            return true;
        }

        return false;
    }
}
