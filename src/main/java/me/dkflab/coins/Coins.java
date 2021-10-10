package me.dkflab.coins;

import me.dkflab.coins.listeners.PlayerJoin;
import me.dkflab.coins.managers.DataManager;
import me.dkflab.coins.managers.SQLInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public final class Coins extends JavaPlugin implements CommandExecutor {

    public SQLInterface sql;
    public DataManager data;
    public HashMap<Player, Integer> dailyProfit = new HashMap<>();
    private long time;

    @Override
    public void onEnable() {
        time = System.currentTimeMillis();
        saveDefaultConfig();
        // Events
        getServer().getPluginManager().registerEvents(new PlayerJoin(this),this);
        this.sql = new SQLInterface(this);

        try {
            sql.connect();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Database not connected!");
            Bukkit.getLogger().severe("Disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (sql.isConnected()) {
            Bukkit.getLogger().info("Database connected");
            sql.createTable();
        }

        data = new DataManager(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("sell")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.color("&cYou need to be a player!"));
                return true;
            }
            Player p = (Player)sender;
            sellItem(p, p.getInventory().getItemInMainHand());
        }

        if (command.getName().equalsIgnoreCase("sellall")) {
            if (!(sender instanceof Player)) {
                Utils.color("&cYou need to be a player!");
                return true;
            }
            Player p = (Player)sender;
            for (ItemStack i : p.getInventory().getContents()) {
                if (i != null) {
                    if (!i.getType().equals(Material.AIR)) {
                        if (goneOverLimit(p)) {
                            p.sendMessage(Utils.color("&cYou have reached your daily sell limit!"));
                            return true;
                        }
                        sellItem(p,i);
                    }
                }
            }
        }

        if (command.getName().equalsIgnoreCase("coins")) {
            if (!(sender instanceof Player)) {
                Utils.color("&cYou need to be a player!");
                return true;
            }
            Player p = (Player)sender;
            p.sendMessage(Utils.color("&aYou have &c" + sql.getPoints(p.getUniqueId()) + "&a points!"));
        }

        if (command.getName().equalsIgnoreCase("rwt")) {
            if (args.length != 1) {
                sender.sendMessage("Usage: /rwt <player>");
                return true;
            }
            sql.removePlayerJoinTime(args[0]);
            sender.sendMessage(Utils.color("&aIf the username was written CaSe SenSitIvE and exists, it has been removed from the database table."));
        }

        return true;
    }

    private void sellItem(Player p, ItemStack item) {
        String name = item.getI18NDisplayName();
        Material m = item.getType();
        if (m.equals(Material.AIR)) {
            p.sendMessage(Utils.color("&cYou need to be holding an item!"));
            return;
        }
        int points = 0;
        points = data.getConfig().getInt(m.toString());
        if (points == 0) {
            p.sendMessage(Utils.color("&cYou cannot sell this item!"));
            return;
        }
        int finalpoints = 0;
        int amount = item.getAmount();
        for (int i = 0; i < amount; i++) {
            if (goneOverLimit(p)) {
                if (amount - item.getAmount() != 0) {
                    p.sendMessage(Utils.color("&aYou sold &e" + (amount - item.getAmount()) + "x " + name + "&a for &e" + finalpoints + "&a!"));
                }
                p.sendMessage(Utils.color("&cYou have reached your daily sell limit!"));
                return;
            }
            item.setAmount(item.getAmount()-1);
            finalpoints += points;
            sql.addPoints(p.getUniqueId(),points);
        }
        p.sendMessage(Utils.color("&aYou sold &e" + amount + "x " + name + "&a for &e" + finalpoints + "&a!"));
    }

    private boolean goneOverLimit(Player p) {
        checkForReset();
        dailyProfit.putIfAbsent(p,0);
        int profitAllowed;
        if (p.hasPermission("coins.vip")) {
            profitAllowed = getConfig().getInt("vipLimit");
        } else {
            profitAllowed = getConfig().getInt("defaultLimit");
        }
        if (dailyProfit.get(p) >= profitAllowed) {
            return true;
        }
        return false;
    }

    public void addPoints(UUID uuid, int points) {
        Player p = Bukkit.getPlayer(uuid);
        dailyProfit.putIfAbsent(p,0);
        dailyProfit.put(p, dailyProfit.get(p)+points);
    }

    private void checkForReset() {
        if (System.currentTimeMillis() - time >= 86400000) {
            dailyProfit = null;
            time = System.currentTimeMillis();
        }
    }
}
