package com.github.nullforge.Data;

import org.bukkit.entity.Player;

public interface DataManagerImpl {
    void getPlayerData(Player var1);

    void savePlayerData(Player var1);

    void getDrawData();

    void saveDrawData();

    void delDraw(String var1);

    String getDrawName(String var1);
}

