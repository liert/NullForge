package com.github.nullforge.Data;

import com.github.nullforge.Config.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempItemStack {
    private static final Map<Player, TempItemStack> tempItemStackMap = new HashMap<>();
    private final Player player;
    public Map<ItemStack, Integer> itemStacks = new HashMap<>();
    private final Map<ItemStack, Integer> luckStones = new HashMap<>();

    private TempItemStack(Player player) {
        this.player = player;
    }

    public void addTempItem(ItemStack itemStack) {
        ItemStack key = itemStack.clone();
        key.setAmount(1);
        itemStacks.compute(key, (k, v) -> v == null ? itemStack.getAmount() : v + itemStack.getAmount());
    }

    public void removeTempItem(ItemStack itemStack, int count) {
        ItemStack key = itemStack.clone();
        key.setAmount(1);
        itemStacks.compute(key, (k, v) -> v == null ? 0 : (v <= count ? 0 : v - count));
    }

    public void removeTempItem(ItemStack itemStack) {
        ItemStack key = itemStack.clone();
        key.setAmount(1);
        itemStacks.compute(key, (k, v) -> v == null ? 0 : (v <= itemStack.getAmount() ? 0 : v - itemStack.getAmount()));
    }

    // 从物品堆中获取幸运石
    public void analysisItem() {
        for (Map.Entry<ItemStack, Integer> entry : itemStacks.entrySet()) {
            if (entry.getValue() == 0) continue;
            ItemStack itemStack = entry.getKey();
            String luckFlag = Settings.I.Attrib_Up_Item_Lore.split("<chance>")[0];
            List<String> lore = getLore(itemStack);
            if (lore.get(0).startsWith(luckFlag)) {
                luckStones.clear();
                luckStones.put(itemStack, entry.getValue());
            }
        }
    }

    public void toPlayerInv() {
        if (itemStacks.isEmpty()) return;
        PlayerInventory playerInventory = player.getInventory();
        for (Map.Entry<ItemStack, Integer> entry : itemStacks.entrySet()) {
            if (entry.getValue() == 0) continue;
            ItemStack itemStack = entry.getKey();
            itemStack.setAmount(entry.getValue());
            playerInventory.addItem(itemStack);
        }
        clear();
    }

    // 获取物品的数量
    public int getItemStackAmount(ItemStack itemStack) {
        ItemStack key = itemStack.clone();
        key.setAmount(1);
        return itemStacks.getOrDefault(key, 0);
    }

    // 获取增加的成功率，注意：使用之前一定要执行 analysisItem()
    public int getLuckStoneChance() {
        int chance = 0;
        int used = 0;
        String luckFlag = Settings.I.Attrib_Up_Item_Lore.split("<chance>")[0];
        for (Map.Entry<ItemStack, Integer> entry : luckStones.entrySet()) {
            int amount = getItemStackAmount(entry.getKey());
            if (amount == 0) continue;
            List<String> lore = getLore(entry.getKey());
            int oneChance = Integer.parseInt(lore.get(0).replace(luckFlag, ""));
            if (amount < Settings.I.Draw_Chance_Up_Count - used) {
                chance += oneChance * amount;
                used += amount;
                removeTempItem(entry.getKey(), amount);
                if (used >= Settings.I.Draw_Chance_Up_Count) break;
            } else {
                chance += oneChance * Settings.I.Draw_Chance_Up_Count - used;
                removeTempItem(entry.getKey(), Settings.I.Draw_Chance_Up_Count - used);
                break;
            }
        }
        return chance;
    }

    public int getSize() {
        return itemStacks.size();
    }

    public Map<ItemStack, Integer> getItemStacks() {
        return itemStacks;
    }

    private List<String> getLore(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return new ArrayList<>();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasLore()) return new ArrayList<>();
        return itemMeta.getLore();
    }

    public void clear() {
        itemStacks.clear();
        luckStones.clear();
    }

    public static void addTempItem(Player player, ItemStack itemStack) {
        TempItemStack tempItemStack = getTempItemStack(player);
        tempItemStack.addTempItem(itemStack);
    }

    public static TempItemStack getTempItemStack(Player player) {
        if (!tempItemStackMap.containsKey(player)) {
            tempItemStackMap.put(player, new TempItemStack(player));
        }
        return tempItemStackMap.get(player);
    }
}
