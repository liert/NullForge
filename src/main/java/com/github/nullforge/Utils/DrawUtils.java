package com.github.nullforge.Utils;

import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrawUtils {
    public static void autoRepair(DrawData drawData) {
        if (!drawData.getLore().isEmpty() || !drawData.getAttributes().isEmpty() && !drawData.getRandomAttributes().isEmpty()) {
            return;
        }

        List<String> lore = new ArrayList<>();
        List<String> attributes = new ArrayList<>();
        List<String> attrib = drawData.getAttrib();
        boolean first = true;
        Pattern pattern = Pattern.compile("\\([+-]?\\d+\\)");

        for (String s : attrib) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                attributes.add(s);
                if (!first) continue;
                lore.add("<Attributes>");
                lore.add("§4§m－－－－－－－－－－－－§A§0");
                lore.add("§2随机副属性:§A§0");
                lore.add("<RandomAttributes>");
                first = false;
            } else {
                lore.add(s);
            }
        }

        drawData.setLore(lore);
        drawData.setAttributes(attributes);

        ItemStack itemStack = drawData.getResult();
        String material = itemStack.getType().toString();
        if (material.contains("HELMET") || material.contains("CHESTPLATE") || material.contains("LEGGINGS") || material.contains("BOOTS")) {
            drawData.setType("Armor");
        } else {
            drawData.setType("Weapon");
        }

        try {
            drawData.saveDraw();
            DrawManager.loadDraw(drawData.getFile());
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.GREEN + "[NullForge] 自动修复图纸 " + drawData.getDisplayName() + ChatColor.GREEN +  " 失败！");
            e.printStackTrace();
        }
    }
}
