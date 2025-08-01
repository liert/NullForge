package com.github.nullforge.Utils;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.PlayerData;
import org.bukkit.entity.Player;

public class ExpUtil {
    public static double getNeedExp(Player p) {
        PlayerData pd = PlayerData.pMap.get(p.getName());
        int level = pd.getLevel();
        return getNeedExp(level);
    }

    public static double getNeedExp(int level) {
        String[] raw = Settings.I.Exp_Text.split(",");
        int[] args = new int[8];
        int i = 0;
        while (i < raw.length) {
            args[i] = Integer.parseInt(raw[i]);
            ++i;
        }
        if (level == 0) {
            return 50.0;
        }
        return Math.abs((Math.pow((double)level - (double)args[0], args[1]) * (double)(level / args[2]) + (double)args[3]) / (double)args[4] + (((double)level - (double)args[5]) * (double)args[6] + (double)args[7]));
    }
}

