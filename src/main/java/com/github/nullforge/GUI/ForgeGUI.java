package com.github.nullforge.GUI;

import com.github.nullforge.Utils.ItemMaker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ForgeGUI {
    public static Inventory getGUI(ItemStack draw) {
        Inventory inv = Bukkit.createInventory(null, 27, "§c锻造系统");
        ItemStack hong = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)14, "§c§l请放入锻造图纸", "");
        ItemStack huang = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)4, "§e§l请放入锻造宝石", "");
        ItemStack lv = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, "§a§l点击开中间图标始锻造...", "");
        ItemStack botton = ItemMaker.create(145, 0, "", "");
        inv.setItem(0, hong);
        inv.setItem(1, hong);
        inv.setItem(2, hong);
        inv.setItem(9, hong);
        inv.setItem(10, draw);
        inv.setItem(11, hong);
        inv.setItem(18, hong);
        inv.setItem(19, hong);
        inv.setItem(20, hong);
        inv.setItem(3, huang);
        inv.setItem(4, huang);
        inv.setItem(5, huang);
        inv.setItem(12, huang);
        inv.setItem(14, huang);
        inv.setItem(21, huang);
        inv.setItem(22, huang);
        inv.setItem(23, huang);
        inv.setItem(6, lv);
        inv.setItem(7, lv);
        inv.setItem(8, lv);
        inv.setItem(15, lv);
        inv.setItem(17, lv);
        inv.setItem(24, lv);
        inv.setItem(25, lv);
        inv.setItem(26, lv);
        inv.setItem(16, botton);
        return inv;
    }
}

