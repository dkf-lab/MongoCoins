package me.dkflab.coins.listeners;

import me.dkflab.coins.Coins;
import me.dkflab.coins.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private Coins main;
    public PlayerJoin(Coins main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        main.sql.createPlayer(e.getPlayer());
        if (System.currentTimeMillis()/1000 - main.sql.getFirstJoin(e.getPlayer().getUniqueId()) >= 604800) {
            // 1 week
            e.getPlayer().setWhitelisted(false);
            e.getPlayer().kickPlayer(Utils.color("&cYou have been un-whitelisted! Contact server owner for help."));
        }
    }
}
