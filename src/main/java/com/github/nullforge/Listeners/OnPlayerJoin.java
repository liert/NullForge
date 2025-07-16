package com.github.nullforge.Listeners;

import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.NullForge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        NullForge.dataManger.loadPlayerData(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        NullForge.dataManger.savePlayerData(e.getPlayer());
        PlayerData.pMap.remove(e.getPlayer().getName());
    }
}

