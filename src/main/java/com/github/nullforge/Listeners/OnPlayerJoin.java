package com.github.nullforge.Listeners;

import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Forge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Forge.dataManger.loadPlayerData(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Forge.dataManger.savePlayerData(e.getPlayer());
        PlayerData.pMap.remove(e.getPlayer().getName());
    }
}

