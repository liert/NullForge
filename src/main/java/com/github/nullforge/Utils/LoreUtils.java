package com.github.nullforge.Utils;

import com.github.nullforge.NullForge;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LoreUtils {
    public static List<String> similarMatch(List<String> all, List<String> target) {
        List<String> result = new ArrayList<>();
        List<String> targetPrefix = target.stream().map(LoreUtils::extractPrefix).collect(Collectors.toList());
        NullForge.debug(targetPrefix.toString());
        for (String string : all) {
            for (int j = 0; j < target.size(); j++) {
                if (string.startsWith(targetPrefix.get(j))) {
                    result.add(target.get(j));
                }
            }
        }
        return result;
    }

    /**
     * 提取属性前缀，用作匹配依据
     * 例如 "§a攻击力： §d25-35§A§0" -> "§a攻击力： "
     */
    public static String extractPrefix(String line) {
        Pattern pattern = Pattern.compile("(§.)?[+\\-]?(§.)?\\(?\\d+");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return line.substring(0, matcher.start());
        }
        return null;
    }
}
