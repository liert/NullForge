package com.github.nullforge.Utils;

import com.github.nullforge.Config.Settings;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GemUtil {
    public static int getGemLevel(ItemStack item) {
        int id = item.getTypeId();
        if (!Settings.I.Gem_Lore.containsKey(id)) {
            return 0;
        }
        if (!item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> list = Settings.I.Gem_Lore.get(id);
        if (!meta.hasDisplayName()) {
            return 0;
        }
        if (!meta.getDisplayName().equalsIgnoreCase(list.get(0))) {
            return 0;
        }
        if (!meta.hasLore()) {
            return 0;
        }
        if (meta.getLore().size() < 2) {
            return 0;
        }
        if (!meta.getLore().get(0).equalsIgnoreCase(list.get(1))) {
            return 0;
        }
        if (!meta.getLore().get(1).contains(Settings.I.Gem_Level_Color)) {
            return 0;
        }
        String[] raw = meta.getLore().get(1).split(Settings.I.Gem_Level_Color);
        return raw[1].length();
    }

    public static boolean isSameGem(ItemStack a, ItemStack b) {
        ItemMeta am = a.getItemMeta();
        ItemMeta bm = b.getItemMeta();
        return a.getType().equals(b.getType()) && am.hasDisplayName() && bm.hasDisplayName() && am.getDisplayName().equalsIgnoreCase(bm.getDisplayName()) && am.getLore().size() == bm.getLore().size() && am.getLore().get(0).equalsIgnoreCase(bm.getLore().get(0));
    }

    public static ItemStack changeGemLevel(ItemStack gem, int level) {
        ItemStack item = gem.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        StringBuilder newText = new StringBuilder(Settings.I.Gem_Level_Color);
        int i = 0;
        while (i < level) {
            newText.append(Settings.I.Gem_Level_Text);
            ++i;
        }
        lore.set(1, newText.toString());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeGem(int id, int level) {
        if (!Settings.I.Gem_Lore.containsKey(id)) {
            return null;
        }
        List<String> lore = Settings.I.Gem_Lore.get(id);
        StringBuilder raw = new StringBuilder(Settings.I.Gem_Level_Color);
        int i = 0;
        while (i < level) {
            raw.append(Settings.I.Gem_Level_Text);
            ++i;
        }
        return ItemMaker.create(id, 0, lore.get(0), lore.get(1), raw.toString());
    }
}

