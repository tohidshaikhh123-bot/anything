package com.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BountyGUI implements Listener {

    private final BountyPlugin plugin;

    public BountyGUI(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Bounty Menu");

        BountyPlugin.PlayerData data = plugin.getData(p);

        inv.setItem(11, createItem(Material.DIAMOND_SWORD, ChatColor.YELLOW + "Your Info",
                ChatColor.GRAY + "Bounty: " + plugin.format(data.bounty),
                ChatColor.GRAY + "Stats Points: " + data.statsPoints,
                ChatColor.GRAY + "Class: " + data.playerClass));

        inv.setItem(13, createItem(Material.BOOK, ChatColor.AQUA + "Choose Class",
                ChatColor.GRAY + "Click to pick Bounty Hunter or Raider"));

        inv.setItem(15, createItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Abilities",
                ChatColor.GRAY + "Buy special abilities with stats"));

        inv.setItem(22, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Powers",
                ChatColor.GRAY + "Upgrade your powers"));

        p.openInventory(inv);
    }

    public void openClassMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Choose Class");

        inv.setItem(11, createItem(Material.IRON_SWORD, ChatColor.YELLOW + "Bounty Hunter",
                ChatColor.GRAY + "More bounty from kills",
                ChatColor.GRAY + "Better for hunting players",
                ChatColor.GREEN + "Click to select"));

        inv.setItem(15, createItem(Material.GOLDEN_SWORD, ChatColor.RED + "Raider",
                ChatColor.GRAY + "More stats points from kills",
                ChatColor.GRAY + "Better for grinding",
                ChatColor.GREEN + "Click to select"));

        // No back button on first join, force them to choose
        BountyPlugin.PlayerData data = plugin.getData(p);
        if (!"none".equals(data.playerClass)) {
            inv.setItem(22, createItem(Material.BARRIER, ChatColor.RED + "Back"));
        }

        p.openInventory(inv);
    }

    public void openAbilitiesMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 36, ChatColor.LIGHT_PURPLE + "Abilities");
        BountyPlugin.PlayerData data = plugin.getData(p);

        int slot = 10;
        for (String ability : plugin.abilityCosts.keySet()) {
            boolean owned = data.abilities.contains(ability);
            Material mat = owned ? Material.LIME_DYE : Material.GRAY_DYE;
            String status = owned ? ChatColor.GREEN + "OWNED" : ChatColor.RED + "Cost: " + plugin.abilityCosts.get(ability) + " stats";

            inv.setItem(slot, createItem(mat, ChatColor.AQUA + ability, status, ChatColor.GRAY + "Click to buy"));
            slot++;
            if (slot == 17) slot = 19;
        }

        inv.setItem(31, createItem(Material.BARRIER, ChatColor.RED + "Back"));
        p.openInventory(inv);
    }

    public void openPowersMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Powers");
        BountyPlugin.PlayerData data = plugin.getData(p);

        int slot = 10;
        for (String power : data.powers.keySet()) {
            int level = data.powers.get(power);
            int cost = plugin.config.getInt("power-costs." + power, 1000) * (level + 1);
            inv.setItem(slot, createItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW + power + " Lv." + level,
                    ChatColor.GRAY + "Next upgrade: " + cost + " stats",
                    ChatColor.GRAY + "Click to upgrade"));
            slot += 2;
        }

        inv.setItem(22, createItem(Material.BARRIER, ChatColor.RED + "Back"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        for (String line : lore) loreList.add(line);
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.loadPlayer(p);

        // Force class choice on first join
        new BukkitRunnable() {
            @Override
            public void run() {
                BountyPlugin.PlayerData data = plugin.getData(p);
                if ("none".equals(data.playerClass)) {
                    p.sendMessage(ChatColor.GOLD + "Welcome! Choose your class to start:");
                    openClassMenu(p);
                }
            }
        }.runTaskLater(plugin, 20L); // 1 second delay so they fully join
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        if (!title.contains("Bounty") && !title.contains("Class") && !title.contains("Abilities") && !title.contains("Powers")) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        ItemStack item = e.getCurrentItem();
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();

        if (title.contains("Bounty Menu")) {
            if (name.contains("class")) openClassMenu(p);
            else if (name.contains("abilities")) openAbilitiesMenu(p);
            else if (name.contains("powers")) openPowersMenu(p);
        } else if (title.contains("Choose Class")) {
            if (name.contains("back")) openMainMenu(p);
            else {
                BountyPlugin.PlayerData data = plugin.getData(p);
                if (!data.playerClass.equals("none")) {
                    p.sendMessage(ChatColor.RED + "You already chose a class!");
                    return;
                }
                if (name.contains("bounty hunter")) {
                    data.playerClass = "Bounty Hunter";
                    p.sendMessage(ChatColor.GREEN + "You are now a Bounty Hunter!");
                } else if (name.contains("raider")) {
                    data.playerClass = "Raider";
                    p.sendMessage(ChatColor.GREEN + "You are now a Raider!");
                }
                plugin.updateScoreboard(p);
                p.closeInventory();
            }
        } else if (title.contains("Abilities")) {
            if (name.contains("back")) {
                openMainMenu(p);
                return;
            }
            String ability = name.replace(" ", "-");
            if (plugin.abilityCosts.containsKey(ability)) {
                buyAbility(p, ability);
                openAbilitiesMenu(p);
            }
        } else if (title.contains("Powers")) {
            if (name.contains("back")) {
                openMainMenu(p);
                return;
            }
            String power = name.split(" ")[0];
            upgradePower(p, power);
            openPowersMenu(p);
        }
    }

    private void buyAbility(Player p, String ability) {
        BountyPlugin.PlayerData data = plugin.getData(p);
        if (data.abilities.contains(ability)) {
            p.sendMessage(ChatColor.RED + "Already owned!");
            return;
        }
        int cost = plugin.abilityCosts.get(ability);
        if (data.statsPoints < cost) {
            p.sendMessage(ChatColor.RED + "Not enough stats points!");
            return;
        }
        data.statsPoints -= cost;
        data.abilities.add(ability);
        p.sendMessage(ChatColor.GREEN + "Bought " + ability + "!");
        plugin.updateScoreboard(p);
    }

    private void upgradePower(Player p, String power) {
        BountyPlugin.PlayerData data = plugin.getData(p);
        if (!data.powers.containsKey(power)) return;

        int level = data.powers.get(power);
        int cost = plugin.config.getInt("power-costs." + power, 1000) * (level + 1);
        if (data.statsPoints < cost) {
            p.sendMessage(ChatColor.RED + "Not enough stats points!");
            return;
        }
        data.statsPoints -= cost;
        data.powers.put(power, level + 1);
        p.sendMessage(ChatColor.GREEN + "Upgraded " + power + " to level " + (level + 1));
        plugin.updateScoreboard(p);
    }
}
