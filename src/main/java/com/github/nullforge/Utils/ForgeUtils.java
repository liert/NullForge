package com.github.nullforge.Utils;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.NullForge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgeUtils {
    public static ItemStack generateForgedItem(Player player, DrawData drawData, String level, int addChance) {
        String attributeRange = Settings.I.Forge_Attrib.get(level); // 属性波动范围（比如 "10 => 30"）
        int playerLevel = PlayerData.pMap.get(player.getName()).getLevel();
        Map<String, Number> wave = calculateWave(attributeRange, addChance, playerLevel, NullForge.rd);

        int rating = wave.get("rating").intValue();

        ItemStack forgedItem = drawData.getResult().clone();
        ItemMeta forgeMeta = forgedItem.getItemMeta();
        List<String> lore = forgeMeta.hasLore() ? forgeMeta.getLore() : new ArrayList<>();

        List<String> attributes = drawData.getAttrib();
        int percent = wave.get("percent").intValue();
        for (String attribute : attributes) {
            lore.add(applyReforge(attribute, percent));
        }
        lore.add(Settings.I.Attrib_Level_Text.get(level));
        lore.add(Settings.I.Attrib_Rating_Text + getRatingText(rating));
        lore.add(Settings.I.ForgeOwner.replaceAll("<player>", player.getName()));
        SimpleDateFormat sdf = new SimpleDateFormat(Settings.I.ForgeDateFormat);
        String date = sdf.format(System.currentTimeMillis());
        lore.add(Settings.I.ForgeDate.replaceAll("<format>", date));
        forgeMeta.setLore(lore);
        forgedItem.setItemMeta(forgeMeta);
        return forgedItem;
    }

    public static boolean isForgedItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        return NullForge.getItemManager().hasItemNBT(itemStack, "DrawName");
    }

    public static String getRatingText(int pre) {
        StringBuilder ratingText = new StringBuilder("§b[");
        String ch = "§c|";
        if (pre > 20) {
            ch = "§9|";
        } else if (pre > 15) {
            ch = "§3|";
        } else if (pre > 10) {
            ch = "§a|";
        } else if (pre > 5) {
            ch = "§e|";
        }
        for (int rd = 0; rd < 25; ++rd) {
            if (rd <= pre) {
                ratingText.append(ch);
                continue;
            }
            ratingText.append("§8|");
        }
        ratingText.append("§b]");
        return ratingText.toString();
    }

    /**
     * 根据百分比加成修改属性字符串中的数值
     * @param attribute 原始属性（例如 "§a攻击力： §d(15)-(25)§A§0"）
     * @param percent   增加百分比（例如 20 表示 +20%）
     * @return 修改后的属性字符串
     */
    public static String applyReforge(String attribute, int percent) {
        Pattern numberInParentheses = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher = numberInParentheses.matcher(attribute);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            int originalValue = Integer.parseInt(matcher.group(1));
            int updatedValue = originalValue + (int) (originalValue * percent / 100.0);

            matcher.appendReplacement(sb, String.valueOf(updatedValue));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 计算属性波动后的最终值
     *
     * @param attributeRange 格式 "min => max"
     * @param addChance      附加百分比
     * @param playerLevel    玩家等级
     * @param random         随机数生成器
     * @return 包含详细波动信息的 Map
     */
    public static Map<String, Number> calculateWave(String attributeRange, int addChance, int playerLevel, Random random) {
        String[] parts = attributeRange.split("\\s*=>\\s*");
        int minValue = Integer.parseInt(parts[0].trim());
        int maxValue = Integer.parseInt(parts[1].trim());
        int delta = Math.max(0, maxValue - minValue);

        // 基础随机
        int baseRandom = delta > 0 ? random.nextInt(delta + 1) : 0;

        // 附加概率加成
        double addedValue = baseRandom + delta * (addChance / 100.0);

        // 等级加成
        double boostedValue = addedValue * (1.0 + (playerLevel / 100.0));

        // 最终 r，封顶 delta
        int finalRandom = delta == 0 ? 0 : Math.min(delta, (int) boostedValue);

        int percent = minValue + finalRandom;
        float ratio = delta == 0 ? 1f : (float) finalRandom / delta;
        int rating = (int) (ratio * 25.0f);

        Map<String, Number> result = new HashMap<>();
        result.put("minValue", minValue);
        result.put("maxValue", maxValue);
        result.put("delta", delta);
        result.put("finalRandom", finalRandom);
        result.put("percent", percent);
        result.put("ratio", ratio);
        result.put("rating", rating);

        return result;
    }

    /**
     * 从物品中提取品质等级 key（level-*）
     *
     * @param item 物品对象
     * @return 匹配到的 level key，如果没匹配到返回 null
     */
    public static String extractLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return null;

        // 获取全局配置
        Map<String, String> levelMap = Settings.I.Attrib_Level_Text;
        if (levelMap == null || levelMap.isEmpty()) return null;

        for (String line : lore) {
            for (Map.Entry<String, String> entry : levelMap.entrySet()) {
                if (line.contains(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }

        return null; // 没匹配到
    }

    /**
     * 替换 lore 中与新属性行同类型的属性
     *
     * @param lore         原物品 lore
     * @param newAttribute 新的属性行，例如 "§a攻击力： §d25-35§A§0"
     * @return 替换后的 lore（会在原列表上修改）
     */
    public static List<String> replaceAttribute(List<String> lore, String newAttribute) {
        if (lore == null || lore.isEmpty() || newAttribute == null) return lore;

        // 获取属性前缀作为匹配依据（例如 "§a攻击力： "）
        String prefix = extractAttributePrefix(newAttribute);

        Bukkit.getLogger().info("AttributePrefix: " + prefix);

        if (prefix == null) return lore;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.startsWith(prefix)) {
                lore.set(i, newAttribute); // 替换
                break; // 只替换第一条匹配
            }
        }

        return lore;
    }

    /**
     * 提取属性前缀，用作匹配依据
     * 例如 "§a攻击力： §d25-35§A§0" -> "§a攻击力： "
     */
    private static String extractAttributePrefix(String attributeLine) {
        Pattern pattern = Pattern.compile("(§.)?[+\\-]?(§.)?\\d+");
        Matcher matcher = pattern.matcher(attributeLine);
        if (matcher.find()) {
            // 前缀就是数字区间之前的部分
            return attributeLine.substring(0, matcher.start());
        }
        return null;
    }

    public static boolean isReforgeStone(ItemStack item) {
        try {
            String name = item.getItemMeta().getDisplayName();
            return name.equals(Settings.I.ReforgeStoneFlag);
        } catch (Exception ignore) {}
        return false;
    }

    public static boolean isLuckyStone(ItemStack item) {
        try {
            String flag = Settings.I.LuckyStoneFlag.split("<percentage>")[0];
            List<String> lore = item.getItemMeta().getLore();
            for (String s : lore) {
                if (s.startsWith(flag)) {
                    return true;
                }
            }
        } catch (Exception ignore) {}
        return false;
    }

    public static int getLuckyStonePercentage(ItemStack item) {
        int luckyStonePercentage = 0;
        if (item == null) return luckyStonePercentage;
        try {
            String flag = Settings.I.LuckyStoneFlag.split("<percentage>")[1];
            List<String> lore = item.getItemMeta().getLore();
            for (String s : lore) {
                int idx = s.indexOf("%");
                if (s.startsWith(flag)) {
                    luckyStonePercentage += Integer.parseInt(s.substring(flag.length(), idx));
                }
            }
        } catch (Exception ignore) {}
        return luckyStonePercentage;
    }
}
