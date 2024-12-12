package com.github.nullforge.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMaker {
    public static ItemStack create(Material mt, short s, String name, String ... lore) {
        ItemStack item = new ItemStack(mt);
        if (s != 0) {
            item.setDurability(s);
        }
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.setDisplayName(name);
        }
        List<String> loreList = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        int i = 0;
        while (i < lore.length) {
            loreList.add(lore[i]);
            ++i;
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack create(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.setDisplayName(name);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack create(int ID, int s, String name, String... lore) {
        ItemStack item = new ItemStack(Material.getMaterial(ID));
        if (s > 0) {
            item.setDurability((short)s);
        }
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.setDisplayName(name);
        }
        List<String> loreList = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        int i = 0;
        while (i < lore.length) {
            loreList.add(lore[i]);
            ++i;
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }
}
