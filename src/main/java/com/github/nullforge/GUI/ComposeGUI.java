package com.github.nullforge.GUI;

import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ItemMaker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ComposeGUI {
    public static Inventory getGUI() {
        Inventory inventory = Bukkit.createInventory(null, 27, "§b§l宝石合成");
        ItemStack gem1 = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)15, MessageLoader.getMessage("gui-compose-gem1"), "");
        ItemStack gem2 = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)6, MessageLoader.getMessage("gui-compose-gem2"), "");
        ItemStack over = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, MessageLoader.getMessage("gui-compose-click"), "");
        for (int i = 0; i < 27; i++) {
            if (i == 0 || i == 1 || i == 2 || i == 9 || i == 11 || i == 18 || i == 19 || i == 20) {
                inventory.setItem(i, gem1);
            } else if (i == 3 || i == 4 || i == 5 || i == 21 || i == 22 || i == 23) {
                inventory.setItem(i, over);
            } else if (i == 6 || i == 7 || i == 8 || i == 15 || i == 17 || i == 24 || i == 25 || i == 26) {
                inventory.setItem(i, gem2);
            }
        }
        return inventory;
    }
}

