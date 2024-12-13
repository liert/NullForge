package com.github.nullforge.Data;

import com.github.nullforge.Config.Settings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawManager {
    private static int drawCount = 0;
    private static final HashMap<String, DrawData> drawManager = new HashMap<>();

    public static List<String> getDrawNames() {
        return new ArrayList<>(drawManager.keySet());
    }

    public static List<DrawData> getDrawData() {
        return new ArrayList<>(drawManager.values());
    }

    public static DrawData getDraw(ItemStack itemStack) {
            if (itemStack.getTypeId() != Settings.I.Draw_Item_ID) {
                return null;
            }
            if (!itemStack.hasItemMeta()) {
                return null;
            }
            ItemMeta meta = itemStack.getItemMeta();
            if (!meta.hasLore()) {
                return null;
            }
            List<String> lore = meta.getLore();
            String flag = lore.get(0);
            return getDraw(flag);
    }

    public static DrawData getDraw(String name) {
        return drawManager.getOrDefault(name, null);
    }

    public static DrawData getDrawDataOfFileName(String fileName) {
        for (DrawData drawData: drawManager.values()) {
            if (drawData.getFileName().equals(fileName)) {
                return drawData;
            }
        }
        return null;
    }

    public static void addDraw(DrawData drawData) {
        drawManager.put(drawData.getDisplayName(), drawData);
        drawCount++;
    }

    public static int getDrawCount() {
        return drawCount;
    }

    public static void reset() {
        drawManager.clear();
        drawCount = 0;
    }

    public static boolean hasDrawData(String fileName) {
        for (DrawData drawData: drawManager.values()) {
            if (drawData.getFileName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }
}
