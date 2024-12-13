package com.github.nullforge.GUI;

import com.github.nullforge.Utils.ItemMaker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ForgeGUI {
    public static Inventory getGUI(ItemStack draw) {
        Inventory inv = Bukkit.createInventory(null, 27, "§c锻造系统");
        ItemStack red = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)14, "§c§l请放入锻造图纸", "");
        ItemStack yellow = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)4, "§e§l请放入锻造宝石", "");
        ItemStack green = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, "§a§l点击开中间图标始锻造...", "");
        ItemStack anvil = ItemMaker.create(145, 0, "", "");
        for (int i = 0; i < 27; i++) {
            if (i == 0 || i == 1 || i == 2 || i == 9 || i == 11 || i == 18 || i == 19 || i == 20) {
                inv.setItem(i, red);
            } else if (i == 3 || i == 4 || i == 5 || i == 12 || i == 14 || i == 21 || i == 22 || i == 23) {
                inv.setItem(i, yellow);
            } else if (i == 6 || i == 7 || i == 8 || i == 15 || i == 17 || i == 24 || i == 25 || i == 26) {
                inv.setItem(i, green);
            } else if (i == 10) {
                inv.setItem(i, draw);
            } else if (i == 16) {
                inv.setItem(i, anvil);
            }
        }
        return inv;
    }
}

