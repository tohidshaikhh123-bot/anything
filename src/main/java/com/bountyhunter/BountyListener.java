package com.bountyhunter;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class BountyListener implements Listener {

    private final BountyPlugin plugin;
    private final Random random = new Random();

    public BountyListener(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return;

        BountyPlugin.PlayerData victimData = plugin.getData(victim);
        BountyPlugin.PlayerData killerData = plugin.getData(killer);

        long stolen = (long) (victimData.bounty * (plugin.config.getDouble("bounty-steal-percent") / 100.0));
        if (stolen < 100) stolen = 100;

        // Class multipliers
        if ("bountyhunter".equalsIgnoreCase(killerData.playerClass)) {
            stolen = (long) (stolen * plugin.config.getDouble("bountyhunter-bounty-multiplier"));
        }

        killerData.bounty += stolen;
        int statsGain = (int) (stolen * plugin.config.getDouble("stats-from-bounty-percent") / 100.0);
        if ("raider".equalsIgnoreCase(killerData.playerClass)) {
            statsGain = (int) (statsGain * plugin.config.getDouble("raider-stats-multiplier"));
        }
        killerData.statsPoints += statsGain;

        // Victim loses bounty
        long lost = (long) (victimData.bounty * 0.7);
        if (victimData.abilities.contains("bounty-shield")) {
            lost = (long) (lost * 0.5);
        }
        victimData.bounty = Math.max(0, victimData.bounty - lost);

        // Apply buffs / debuffs
        updateBountyEffects(victim);
        updateBountyEffects(killer);

        // Life steal ability
        if (killerData.abilities.contains("life-steal")) {
            double health = killer.getHealth() + 4.0;
            killer.setHealth(Math.min(killer.getMaxHealth(), health));
        }

        killer.sendMessage(ChatColor.GREEN + "You killed " + victim.getName() + "!");
        killer.sendMessage(ChatColor.GOLD + "+" + plugin.format(stolen) + " bounty");
        killer.sendMessage(ChatColor.AQUA + "+" + statsGain + " stats points");

        victim.sendMessage(ChatColor.RED + "You were killed by " + killer.getName() + " and lost " + plugin.format(lost) + " bounty!");

        plugin.updateScoreboard(killer);
        plugin.updateScoreboard(victim);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) return;
        if (e.getEntity().getKiller() == null) return;

        Player killer = e.getEntity().getKiller();
        EntityType type = e.getEntityType();

        // Only hostile mobs
        if (!isHostile(type)) return;

        if (random.nextDouble() > plugin.config.getDouble("mob-bounty-chance")) return;

        int bounty = random.nextInt(plugin.config.getInt("mob-bounty-max") - plugin.config.getInt("mob-bounty-min")) 
                     + plugin.config.getInt("mob-bounty-min");

        BountyPlugin.PlayerData data = plugin.getData(killer);
        data.bounty += bounty;

        if (data.abilities.contains("bounty-magnet") && random.nextDouble() < 0.3) {
            data.bounty += bounty;
            killer.sendMessage(ChatColor.GOLD + "Bounty Magnet activated! Double bounty!");
        }

        killer.sendMessage(ChatColor.YELLOW + "+" + bounty + " bounty from " + type.name());
        plugin.updateScoreboard(killer);
        updateBountyEffects(killer);
    }

    private void updateBountyEffects(Player player) {
        BountyPlugin.PlayerData data = plugin.getData(player);
        int low = plugin.config.getInt("low-bounty-threshold", 2500);
        int high = plugin.config.getInt("high-bounty-threshold", 50000);
        int veryHigh = plugin.config.getInt("very-high-bounty-threshold", 200000);

        // Clear old effects first
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.RESISTANCE);

        if (data.bounty < low) {
            // Low bounty = strong debuffs
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 400, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 400, 0, true, false));

            if (data.bounty < low / 2) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0, true, false));
                player.sendMessage(ChatColor.DARK_RED + "Your bounty is critically low! You are weak, slow, and hungry!");
            } else {
                player.sendMessage(ChatColor.RED + "Your bounty is low! You feel weak and can't sprint well.");
            }
        } else if (data.bounty >= veryHigh) {
            // Very high bounty = strong buffs
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 400, 0, true, false));
            player.sendMessage(ChatColor.GOLD + "Your bounty is extremely high! You feel powerful!");
        } else if (data.bounty >= high) {
            // High bounty = medium buffs
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0, true, false));
            player.sendMessage(ChatColor.YELLOW + "Your bounty is high! You feel stronger.");
        }
    }

    private boolean isHostile(EntityType type) {
        return switch (type) {
            case ZOMBIE, SKELETON, CREEPER, SPIDER, CAVE_SPIDER, ENDERMAN, WITCH, 
                 PILLAGER, VINDICATOR, EVOKER, RAVAGER, BLAZE, GHAST, MAGMA_CUBE, 
                 SLIME, PHANTOM, DROWNED, HUSK, STRAY, WITHER_SKELETON, PIGLIN, 
                 PIGLIN_BRUTE, HOGLIN, ZOGLIN -> true;
            default -> false;
        };
    }
}
