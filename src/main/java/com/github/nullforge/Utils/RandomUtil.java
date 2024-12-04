
package com.github.nullforge.Utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

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
        Float total = 0.0f;
        LinkedHashMap<Float, String> tempMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            total = total + entry.getValue();
            tempMap.put(total, entry.getKey());
        }
        float index = new Random().nextFloat() * total;
        for (Map.Entry<Float, String> next : tempMap.entrySet()) {
            if (!(index < next.getKey())) continue;
            return next.getValue();
        }
        return null;
    }
}

