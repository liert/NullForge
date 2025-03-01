package com.github.nullforge.Utils;

import com.github.nullforge.Forge;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemString {
    public static String getString(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            return String.format("%sx%d", itemStack.getItemMeta().getDisplayName(), itemStack.getAmount());
        }
        return null;
    }

    public static ItemStack getItem(String s) {
        String[] strings = s.split("x");
        String name = strings[0];
        int amount = Integer.parseInt(strings[1]);
        return Forge.getMMItemManager().getItemStack(name, amount);
    }
}
