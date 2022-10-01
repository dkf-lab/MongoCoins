package me.dkflab.coins;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&',s);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void error(CommandSender sender, String error) {
        sendMessage(sender,"&c&lError! &7" + error);
    }

    public static void info(CommandSender sender, String info) {
        sendMessage(sender, "&b&l[!] &7" + info);
    }

    public static void notPlayer(CommandSender s) {
        error(s, "You need to be a player to execute that command!");
    }

    public static boolean parseInt(CommandSender sender, String parse) {
        try {
            Integer.parseInt(parse);
        } catch (NumberFormatException e) {
            error(sender, parse + " is not an integer!");
            return false;
        }
        return true;
    }

    public static boolean parseLong(CommandSender sender, String parse) {
        try {
            Long.parseLong(parse);
        } catch (NumberFormatException e) {
            error(sender, parse + " is not an integer!");
            return false;
        }
        return true;
    }

    private static ItemStack pane;
    public static ItemStack blankPane() {
        if (pane == null) {
            pane = createItem(Material.GRAY_STAINED_GLASS_PANE,1,"&r",null,null,null,false);
        }
        return pane;
    }

    public static ItemStack createItem(Material material, int amount, String name, List<String> lore, HashMap<Enchantment, Integer> enchants, List<ItemFlag> flags, Boolean unbreakable) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(color(name));
        if (lore != null) {
            List<String> l = new ArrayList<>();
            for (String s:lore) {
                l.add(color(s));
            }
            meta.setLore(l);
        }
        if (enchants != null) {
            for (Enchantment e:enchants.keySet()) {
                meta.addEnchant(e, enchants.get(e), true);
            }
        }
        if (flags != null) {
            for (ItemFlag f : flags) {
                meta.addItemFlags(f);
            }
        }
        meta.setUnbreakable(unbreakable);

        item.setItemMeta(meta);
        return item;
    }

    public static void success(CommandSender sender, String message) {
        sendMessage(sender, "&a&lSuccess! &7" + message);
    }

    public static void noPerms(CommandSender s) {
        error(s, "Insufficient permissions.");
    }
}
