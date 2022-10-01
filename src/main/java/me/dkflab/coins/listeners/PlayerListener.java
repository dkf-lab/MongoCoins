package me.dkflab.coins.listeners;

import me.dkflab.coins.Coins;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private Coins main;
    public PlayerListener(Coins main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        main.updatePlayer(e.getPlayer());
    }
}
