package com.github.nullforge.Utils;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.PlayerData;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    public static Integer probabInt(Map<Integer, Float> map) {
        Float total = 0.0f;
        LinkedHashMap<Float, Integer> tempMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Float> entry : map.entrySet()) {
            total = total + entry.getValue();
            tempMap.put(total, entry.getKey());
        }
        float index = new Random().nextFloat() * total;
        for (Map.Entry<Float, Integer> next : tempMap.entrySet()) {
            if (!(index < next.getKey())) continue;
            return next.getValue();
        }
        return null;
    }

    public static String probabString(Player player, DrawData drawData, Map<String, Float> map) {
        if (player != null & drawData != null) {
            PlayerData playerData = PlayerData.getPlayerData(player.getName());
            int forgeCount = playerData.getForgeCount(drawData.getFileName());
            if ((forgeCount % Settings.I.Large_Guarantee_Threshold) >= Settings.I.Large_Guarantee_Threshold - 1) {
                return Settings.I.Large_Guaranteed_Quality;
            }
            if ((forgeCount % Settings.I.Small_Guarantee_Threshold) >= Settings.I.Small_Guarantee_Threshold - 1) {
                return Settings.I.Small_Guaranteed_Quality;
            }
        }
        float total = 0.0f;
        TreeMap<Float, String> cumulativeMap = new TreeMap<>();
        for (Map.Entry<String, Float> entry : normalizeWeights(map).entrySet()) {
            total += entry.getValue();
            if (entry.getValue() > 0) {
                cumulativeMap.put(total, entry.getKey());
            }
        }
        float randomValue = ThreadLocalRandom.current().nextFloat() * total;
        String quality = cumulativeMap.ceilingEntry(randomValue).getValue();
        if (player != null & drawData != null && quality.equals(Settings.I.Large_Guaranteed_Quality)) {
            PlayerData playerData = PlayerData.getPlayerData(player.getName());
            playerData.resetForgeCount(drawData.getFileName());
        }
        return cumulativeMap.ceilingEntry(randomValue).getValue();
    }

    public static Map<String, Float> normalizeWeights(Map<String, Float> map) {
        Map<String, Float> normalizedWeights = new LinkedHashMap<>();
        float totalWeight = 0f;
        // 计算总权重
        for (Float weight : map.values()) {
            totalWeight += weight;
        }
        // 归一化
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            normalizedWeights.put(entry.getKey(), entry.getValue() / totalWeight);
        }
        return normalizedWeights;
    }
}

