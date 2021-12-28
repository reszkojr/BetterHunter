package me.santres.betterhunter;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.ArrayList;

public class BetterHunter extends JavaPlugin implements CommandExecutor, Listener {

    static List<Player> hunters = new ArrayList<>();
    Location portalLocation;

    @Override
    public void onEnable() {
        getServer().getPluginCommand("hunter").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        sendConsoleMessage(ChatColor.GREEN + "BetterHunter Enabled");
        removePlayersCompass();

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (Bukkit.getOnlinePlayers().size() > 1 && Bukkit.getOnlinePlayers().size() - 1 == hunters.size()) {
                updateHunterCompass();
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        sendConsoleMessage(ChatColor.RED + "BetterHunter Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (cmd.getName().equalsIgnoreCase("hunter")) {
            if (args.length != 2) {
                return true;
            }

            if (Bukkit.getPlayer(args[1]) == null) {
                sender.sendMessage(ChatColor.RED + "Player not found");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);

            switch (args[0]) {
                case "add":
                    hunters.add(player);
                    sender.sendMessage(ChatColor.GREEN + player.getName() + " is now a hunter.");
                    if (!player.getInventory().contains(hunterCompass())) {
                        player.getInventory().addItem(hunterCompass());
                    }
                    break;
                case "remove":
                    if (hunters.contains(player)) {
                        hunters.remove(player);
                        player.getInventory().remove(hunterCompass());
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player is not a hunter!");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Wrong usage!");
                    sender.sendMessage(ChatColor.RED + "Use: /hunter add|remove <player>");

            }
        }

        return true;
    }

    private ItemStack hunterCompass() {
        ItemStack hunterCompass = new ItemStack(Material.COMPASS);
        ItemMeta hunterCompassMeta = hunterCompass.getItemMeta();

        hunterCompassMeta.setDisplayName("§fHunter Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Tracks the speedrunner.");
        hunterCompassMeta.setLore(lore);
        hunterCompass.setItemMeta(hunterCompassMeta);
        return hunterCompass;
    }

    private void updateHunterCompass() {
        // Get speedrunner location
        Player speedrunner = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hunters.contains(player)) {
                speedrunner = player;
            }
        }

        // Update hunters compass
        for (Player hunter : hunters) {
            if (speedrunner.getWorld().getEnvironment() != World.Environment.NORMAL) {
                hunter.setCompassTarget(getPortalLocation());
                return;
            }
            hunter.setCompassTarget(speedrunner.getLocation());
        }
    }

    @EventHandler
    public void removeCompassOnJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getInventory().contains(hunterCompass())) {
            event.getPlayer().getInventory().remove(hunterCompass());
        }
    }

    @EventHandler
    public void removeHunterIfQuit(PlayerQuitEvent event) {
        if (hunters.contains(event.getPlayer())) {
            hunters.remove(event.getPlayer());
        }
    }

    public void removePlayersCompass() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getInventory().contains(hunterCompass())) {
                player.getInventory().remove(hunterCompass());
            }
        }
    }

    @EventHandler
    public void cancelCompassDrop(PlayerDropItemEvent event) {
        // If the player that drops the item is not a hunter, return.
        if (!hunters.contains(event.getPlayer())) return;

        // If the item dropped is not a compass, return.
        if (event.getItemDrop().getItemStack().getType() != Material.COMPASS) return;

        event.setCancelled(true);
    }


    @EventHandler
    public void updateCompassOnPlayerNetherJoin(PlayerPortalEvent event) {
        Player speedrunner = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hunters.contains(player)) {
                speedrunner = player;
            }
        }
        if (event.getPlayer() == speedrunner) {
            setPortalLocation(event.getPlayer().getLocation());
        }
        updateHunterCompass();
    }

    @EventHandler
    public void giveCompassToRespawnedPlayer(PlayerRespawnEvent event) {
        if (hunters.contains(event.getPlayer())) {
            event.getPlayer().getInventory().addItem(hunterCompass());
        }
    }

    @EventHandler
    public void removeCompassIfHunterDies(PlayerDeathEvent event) {
        if (event.getDrops().contains(hunterCompass())) {
            event.getDrops().remove(hunterCompass());
        }
    }

    private void sendConsoleMessage(String msg) {
        getServer().getConsoleSender().sendMessage(msg);
    }

    public Location getPortalLocation() {
        return portalLocation;
    }

    public void setPortalLocation(Location portalLocation) {
        this.portalLocation = portalLocation;
    }
}
