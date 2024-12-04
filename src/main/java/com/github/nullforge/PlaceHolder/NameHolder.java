package com.github.nullforge.PlaceHolder;

import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Utils.ExpUtil;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NameHolder
extends EZPlaceholderHook {
    public NameHolder(Plugin plugin, String identifier) {
        super(plugin, identifier);
    }

    public String onPlaceholderRequest(Player p, String arg1) {
        if (!PlayerData.pMap.containsKey(p.getName())) {
            return "无";
        }
        switch (arg1) {
            case "get_level":
                return String.valueOf(PlayerData.pMap.get(p.getName()).getLevel());
            case "get_exp":
                int exp = (int) PlayerData.pMap.get(p.getName()).getExp();
                return String.valueOf(exp);
            case "get_maxexp":
                int needExp = (int) ExpUtil.getNeedExp(p);
                return String.valueOf(needExp);
        }
        return null;
    }
}

