package com.github.nullforge.Listeners;

import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerJoin
implements Listener {
    @EventHandler
    public void join(PlayerJoinEvent e) {
        Main.dataManger.getPlayerData(e.getPlayer());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        Main.dataManger.savePlayerData(e.getPlayer());
        PlayerData.pMap.remove(e.getPlayer().getName());
    }
}

