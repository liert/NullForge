package com.github.nullforge.API;

import com.github.nullforge.Data.PlayerData;
import org.bukkit.entity.Player;

public class ForgeLevelAPI {
    public static PlayerData getPlayerLevelData(Player p) {
        if (!PlayerData.pMap.containsKey(p.getName())) {
            return null;
        }
        return PlayerData.pMap.get(p.getName());
    }

    public static boolean setPlayerLevelData(Player p, PlayerData pd) {
        if (!PlayerData.pMap.containsKey(p.getName())) {
            return false;
        }
        PlayerData.pMap.put(p.getName(), pd);
        return true;
    }
}

