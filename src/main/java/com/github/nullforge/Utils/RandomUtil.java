package com.github.nullforge.Utils;

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

    public static String probabString(Map<String, Float> map) {
        float total = 0.0f;
        TreeMap<Float, String> cumulativeMap = new TreeMap<>();
        for (Map.Entry<String, Float> entry : normalizeWeights(map).entrySet()) {
            total += entry.getValue();
            if (entry.getValue() > 0) {
                cumulativeMap.put(total, entry.getKey());
            }
        }
        float randomValue = ThreadLocalRandom.current().nextFloat() * total;
        return cumulativeMap.ceilingEntry(randomValue).getValue();
    }

    private static Map<String, Float> normalizeWeights(Map<String, Float> map) {
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

