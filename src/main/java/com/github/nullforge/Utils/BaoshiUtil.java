package com.github.nullforge.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class BaoshiUtil {
    // 模拟一个方法来触发指令
    public static void giveGem(Player player, String gemName, int level, int amount) {
        // 构造指令
        String command = "lr give " + player.getName() + " " + gemName + " " + level + " " + amount;
        // 执行指令
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    // 检测物品名称并触发指令
    public static boolean checkAndGiveGem(Player player, List<ItemStack> gems, int[] result) {
        // 检查宝石数量是否足够
        if (gems.size() < 2) {
            return false; // 不提示，直接返回 false
        }

        // 提取第一个宝石的名称和等级
        ItemMeta meta1 = gems.get(0).getItemMeta();
        if (meta1 == null || !meta1.hasDisplayName()) {
            return false; // 不提示，直接返回 false
        }
        String displayName1 = meta1.getDisplayName();
        String pattern = "§.[^§]*§8\\[§6Lv\\.(\\d+)§8\\]";
        if (!displayName1.matches(".*" + pattern + ".*")) {
            return false; // 不提示，直接返回 false
        }
        int level1 = Integer.parseInt(displayName1.replaceAll(".*§8\\[§6Lv\\.(\\d+)§8\\].*", "$1"));
        String gemName1 = getPureGemName(displayName1);

        // 检查其他宝石是否与第一个宝石匹配
        int totalAmount = 0; // 用于记录总数量
        for (ItemStack gem : gems) {
            ItemMeta meta = gem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                continue; // 跳过无法识别的物品
            }
            String displayName = meta.getDisplayName();
            if (!displayName.matches(".*" + pattern + ".*")) {
                continue; // 跳过无法识别的物品
            }
            int level = Integer.parseInt(displayName.replaceAll(".*§8\\[§6Lv\\.(\\d+)§8\\].*", "$1"));
            String gemName = getPureGemName(displayName);

            // 检查宝石名称和等级是否相同
            if (gemName.equals(gemName1) && level == level1) {
                totalAmount += gem.getAmount(); // 累加数量
            }
        }

        // 合成逻辑
        Random random = new Random();
        int newLevel = level1 + 1; // 合成结果的等级等于当前等级的下一级
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < totalAmount / 2; i++) { // 每次合成需要2个宝石
            if (random.nextDouble() < 0.5) { // 40%成功率
                successCount++;
            } else {
                failCount++;
            }
        }

        if (successCount > 0) {
            giveGem(player, gemName1, newLevel, successCount); // 给予合成后的宝石
        }

        // 返回合成结果
        result[0] = successCount; // 成功数量
        result[1] = failCount; // 失败数量
        return successCount > 0; // 如果有成功的，返回 true
    }

    // 提取纯净的宝石名称
    public static String getPureGemName(String displayName) {
        if (displayName == null) {
            return null; // 如果输入为空，直接返回 null
        }
        // 去除颜色代码和等级信息
        String pureName = displayName.replaceAll("§.", "").replaceAll(" \\[Lv\\.\\d+\\]", "").trim();
        return pureName;
    }
}