package com.github.nullforge.GUI;

import com.github.nullforge.Utils.ItemMaker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ComposeGUI {
    public static Inventory getGUI() {
        Inventory createInventory = Bukkit.createInventory(null, 27, "§b§l宝石合成");
        ItemStack gem1 = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)15, "§c§l第一个宝石", "");
        ItemStack gem2 = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)6, "§c§l第二个宝石", "");
        ItemStack over = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, "§a§l点击合成!", "");
        createInventory.setItem(0, gem1);
        createInventory.setItem(1, gem1);
        createInventory.setItem(2, gem1);
        createInventory.setItem(9, gem1);
        createInventory.setItem(11, gem1);
        createInventory.setItem(18, gem1);
        createInventory.setItem(19, gem1);
        createInventory.setItem(20, gem1);
        createInventory.setItem(6, gem2);
        createInventory.setItem(7, gem2);
        createInventory.setItem(8, gem2);
        createInventory.setItem(15, gem2);
        createInventory.setItem(17, gem2);
        createInventory.setItem(24, gem2);
        createInventory.setItem(25, gem2);
        createInventory.setItem(26, gem2);
        createInventory.setItem(3, over);
        createInventory.setItem(4, over);
        createInventory.setItem(5, over);
        createInventory.setItem(21, over);
        createInventory.setItem(22, over);
        createInventory.setItem(23, over);
        return createInventory;
    }
}

