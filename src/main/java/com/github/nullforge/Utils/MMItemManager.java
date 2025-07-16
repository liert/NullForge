package com.github.nullforge.Utils;

import com.github.nullforge.Config.GlobalConfig;
import com.github.nullforge.NullForge;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.ItemManager;
import io.lumine.xikage.mythicmobs.items.MythicItem;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;

public class MMItemManager {
    private final ItemManager itemManager = NullForge.getMythicMobs().getItemManager();
    private final Map<ItemStack, String> items = new ConcurrentHashMap<>();

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

    public String getItemConfigName(ItemStack itemStack) {
        if (GlobalConfig.isTransform) {
            initItems();
            GlobalConfig.isTransform = false;
        }
        itemStack.setAmount(1);
        return items.getOrDefault(itemStack, "未知物品");
    }

    private void initItems() {
        try {
            this.items.clear();
            Field itemsField = ItemManager.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            ConcurrentHashMap<String, MythicItem> items = (ConcurrentHashMap<String, MythicItem>) itemsField.get(itemManager);
            for (Map.Entry<String, MythicItem> entry: items.entrySet()) {
                ItemStack itemStack;
                try {
                    itemStack = BukkitAdapter.adapt(entry.getValue().generateItemStack(1));
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    continue;
                }
                this.items.put(itemStack, entry.getKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

