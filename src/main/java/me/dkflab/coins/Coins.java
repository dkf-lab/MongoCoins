package me.dkflab.coins;

import com.mongodb.*;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import me.dkflab.coins.listeners.PlayerListener;
import me.dkflab.coins.objects.MongoPlayer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static me.dkflab.coins.Utils.*;

public final class Coins extends JavaPlugin implements CommandExecutor {

    private Map<UUID, MongoPlayer> map = new HashMap<>();
    private Map<UUID, Integer> coins = new HashMap<>();

    private MongoClient mongoClient;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this),this);
        // Database Setup
        try {
            mongoClient = MongoClients.create(getConfig().getString("connection"));
        } catch (Exception e) {
            Bukkit.getLogger().severe("Database not connected!");
            Bukkit.getLogger().severe("Disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Cache database
        readDatabase();
    }

    public void readDatabase() {
        MongoDatabase database = mongoClient.getDatabase("coins");
        MongoCollection<Document> col = database.getCollection("data");
        col.find().forEach((Consumer<Document>) document -> {
            map.put(UUID.fromString(document.getString("uuid")), new MongoPlayer(document.getString("name"), document.getInteger("coins")));
            coins.put(UUID.fromString(document.getString("uuid")),document.getInteger("coins"));
        });
        getLogger().info(map.toString());
    }

    public void updatePlayer(Player player) {
        MongoDatabase database = mongoClient.getDatabase("coins");
        MongoCollection<Document> col = database.getCollection("data");
        // Check if player is not part of database
        if (!map.containsKey(player.getUniqueId())) {
            // Player is not in cached DB
            // Therefore, we need to write them
            writePlayer(player, database, col);
            return;
        }
        // We can now update the document as the player is in the DB
        col.updateOne(
                eq("uuid", player.getUniqueId().toString()),
                combine(set("name", player.getName()),
                        set("coins", coins.get(player.getUniqueId()))));
    }

    public int getPlayerCoins(Player player) {
        return coins.get(player.getUniqueId());
    }

    public void setPlayerCoins(Player player, int coins) {
        this.coins.put(player.getUniqueId(),coins);
        updatePlayer(player);
    }

    public void addPlayerCoins(Player player, int amount) {
        this.coins.put(player.getUniqueId(), getPlayerCoins(player) + amount);
        updatePlayer(player);
    }

    public void subtractPlayerCoins(Player player, int amount) {
        int f = getPlayerCoins(player) - amount;
        if (f < 0) {
            f = 0;
        }
        this.coins.put(player.getUniqueId(), f);
        updatePlayer(player);
    }

    public void writePlayer(Player player, MongoDatabase db, MongoCollection<Document> col) {
        // This function is for writing a new player document
        getLogger().info("Writing player " + player.getName());
        Document data = new Document("uuid", player.getUniqueId().toString())
                .append("name",player.getName())
                .append("coins",0);
        col.insertOne(data);
        coins.put(player.getUniqueId(),0);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("coins")) {
            // coins <set> <player> <amount>
            if (args.length == 3) {
                if (!sender.hasPermission("coins.admin")) {
                    noPerms(sender);
                    return true;
                }
                if (!args[0].equalsIgnoreCase("set")) {
                    help(sender);
                    return true;
                }
                String playerName = args[1];
                Player p = null;
                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (all.getName().equalsIgnoreCase(playerName)) {
                        p = all;
                    }
                }
                if (p == null) {
                    error(sender, "Can't find player " + playerName + ". Are they online?");
                } else {
                    // We found our player
                    if (parseInt(sender,args[2])) {
                        setPlayerCoins(p, Integer.parseInt(args[2]));
                        success(sender, "Set player " + p.getName() + " coins to " + args[2] + "!");
                    }
                }
                return true;
            }
            balance(sender);
        }
        return true;
    }

    private void balance(CommandSender sender) {
        if (!(sender instanceof Player)) {
            notPlayer(sender);
        } else {
            sendMessage(sender,"&7You have &a" + getPlayerCoins((Player)sender) + "&7 coins!");
        }
    }

    private void help(CommandSender s) {
        info(s,"&bCoins Help");
        info(s,"/coins set <player> <amount>");
    }
}
