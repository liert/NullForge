package com.github.nullforge.Utils;

import com.github.nullforge.Forge;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.ItemManager;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public class MMItemManager {
    private final ItemManager itemManager = Forge.getMythicMobs().getItemManager();

    public ItemStack getItemStack(String name) {
        return this.getItemStack(name, 1);
    }

    public ItemStack getItemStack(String name, int number) {
        Optional<MythicItem> mythicItem = this.itemManager.getItem(name);
        if (mythicItem.isPresent()) {
            MythicItem mi = mythicItem.get();
            return BukkitAdapter.adapt(mi.generateItemStack(number));
        }
        return null;
    }
}

