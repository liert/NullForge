package com.github.nullforge.PlaceHolder;

import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Utils.ExpUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class NameHolder extends PlaceholderExpansion {
    private final Plugin plugin; //

    public NameHolder(Plugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "forge";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "liert";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (!PlayerData.pMap.containsKey(player.getName())) {
            return "无";
        }
        switch (params) {
            case "get_level":
                return String.valueOf(PlayerData.pMap.get(player.getName()).getLevel());
            case "get_exp":
                int exp = (int) PlayerData.pMap.get(player.getName()).getExp();
                return String.valueOf(exp);
            case "get_maxexp":
                int needExp = (int) ExpUtil.getNeedExp(player);
                return String.valueOf(needExp);
        }
        return null;
    }
}

