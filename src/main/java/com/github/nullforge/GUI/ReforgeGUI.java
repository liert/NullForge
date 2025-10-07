package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.InventoryContext;
import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullbridge.annotate.SlotClick;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ForgeUtils;
import com.github.nullforge.Utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@RegisterInventory
public class ReforgeGUI extends NullInventory {
    public static ReforgeGUI instance;
    private final List<Integer> lockAttributeSlots = new ArrayList<>();
    private final List<Integer> unlockAttributeSlots = new ArrayList<>();

    @Override
    public void initInventory(InventoryContext context) {}

    @Override
    public void click(InventoryClickEvent e) {
        if (e.getRawSlot() < inventorySize) {
            e.setCancelled(true);
        }
    }

    @SlotClick(flag = "输入")
    public void onInputClick(InventoryClickEvent e) {
        e.setCancelled(false);
        Player player = (Player) e.getWhoClicked();
        player.sendMessage("点击了输入");

        // getCurrentItem获取的是界面里面的物品
        ItemStack clickedItem = e.getCurrentItem();
        // getCursor获取的是当前鼠标的物品
        ItemStack cursorItem = e.getCursor();
        boolean clickedItemisForgedItem = ForgeUtils.isForgedItem(clickedItem);
        boolean cursorItemisForgedItem = ForgeUtils.isForgedItem(cursorItem);

        // player.sendMessage("getCurrentItem => " + clickedItem.toString());
        // player.sendMessage("getCursor => " + cursorItem.toString());

        if (clickedItem.getType() == Material.AIR && cursorItem.getType() != Material.AIR) {
            if (!cursorItemisForgedItem) {
                player.sendMessage("请放入正确的物品");
                return;
            }
            // 放入锻造物品
            // player.sendMessage("放入锻造物品");
            initAttribute(e.getInventory(), cursorItem);
            return;
        }

        if (cursorItem.getType() == Material.AIR && clickedItemisForgedItem) {
            // 取出原物品
            // player.sendMessage("取出原物品");
            clearAttribute(e.getInventory(), true);
            setOutputItem(e.getInventory(), null);
            toggleStatus(e.getInventory(), false);
            return;
        }


        if (ForgeUtils.isForgedItem(clickedItem) && cursorItemisForgedItem) {
            // 切换物品
            // player.sendMessage("切换物品");
            resetAttribute(e.getInventory(), cursorItem, true);
        }
    }

    @SlotClick(flag = "输出")
    public void onOutputClick(InventoryClickEvent e) {}

    @SlotClick(flag = "词条展示框")
    public void onAttributeClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        ItemStack itemStack = e.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        // 添加一个假附魔，让物品发光
        if (lockAttributeSlots.contains(slot)) {
            meta.removeEnchant(Enchantment.DURABILITY);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            lockAttributeSlots.remove((Object) slot);
            unlockAttributeSlots.add(e.getRawSlot());
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            // 隐藏附魔显示，只保留发光
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            lockAttributeSlots.add(e.getRawSlot());
            unlockAttributeSlots.remove((Object) e.getRawSlot());
        }

        itemStack.setItemMeta(meta);

        // 更新物品到槽位
        Bukkit.getScheduler().runTask(NullForge.INSTANCE, () -> {
            e.getInventory().setItem(e.getRawSlot(), itemStack);
        });
    }

    @SlotClick(flag = "确认")
    public void onConfirmClick(InventoryClickEvent e) {
        toggleStatus(e.getInventory(), false);
        ItemStack itemStack = getInventoryItem(e.getInventory(), "输出");
        if (itemStack == null) {
            return;
        }
        setInventoryItem(e.getInventory(), "输入", itemStack);
    }

    @SlotClick(flag = "取消")
    public void onCancelClick(InventoryClickEvent e) {
        toggleStatus(e.getInventory(), false);
    }

    @SlotClick(flag = "重铸石")
    public void onReforgeStone(InventoryClickEvent e) {
        e.setCancelled(false);
    }

    @SlotClick(flag = "幸运石")
    public void onLuckyStone(InventoryClickEvent e) {
        e.setCancelled(false);
    }


    @SlotClick(flag = "重铸")
    public void onReforgeClick(InventoryClickEvent e) {
        ItemStack itemStack = getInventoryItem(e.getInventory(), "输入");
        if (itemStack == null) {
            return;
        }
        String drawName = ForgeUtils.getDrawName(itemStack);
        if (drawName == null) {
            return;
        }
        if (unlockAttributeSlots.isEmpty()) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        DrawData drawData = DrawManager.getDrawDataOfFileName(drawName);
        if (drawData == null) {
            player.sendMessage("图纸数据异常，请联系管理员");
            return;
        }
        // 检查是否放入了正确的重铸石
        ItemStack reforgeStone = getInventoryItem(e.getInventory(), "重铸石");
        if (!ForgeUtils.isReforgeStone(reforgeStone)) {
            player.sendMessage("请放入正确的重铸石");
            return;
        }

        ItemStack previewItemStack = itemStack.clone();
        ItemMeta itemMeta = previewItemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        String level = ForgeUtils.extractLevel(itemStack);
        Settings.Level levelObj = Settings.I.Levels.get(level);

        ItemStack luckyStone = getInventoryItem(e.getInventory(), "幸运石");
        int addChance = ForgeUtils.getLuckyStonePercentage(luckyStone);

        int playerLevel = PlayerData.pMap.get(player.getName()).getLevel();
        Map<String, Number> wave = ForgeUtils.calculateWave(levelObj.AttributeRange, addChance, playerLevel, NullForge.random);
        int percent = wave.get("percent").intValue();
        int randomCount = RandomUtil.getRandomAttributeCount(level);
        NullForge.debug("随机词条数量: " + randomCount);
        // 如果随机词条数量大于锁定的词条数量
        if (randomCount > lockAttributeSlots.size()) {
            // return;
            List<String> newAttributes = RandomUtil.getRandomAttributes(randomCount - lockAttributeSlots.size(), drawData.getType(), drawData.getRandomAttributes());
            NullForge.debug("新的随机词条: " + newAttributes);
            for (String attribute : newAttributes) {
                String newAttribute = ForgeUtils.apply(attribute, percent);
                lore = ForgeUtils.replaceAttribute(lore, newAttribute);
            }
            itemMeta.setLore(lore);
            previewItemStack.setItemMeta(itemMeta);
            // 重新初始化词条展示框
            resetAttribute(e.getInventory(), previewItemStack, false);
        }
        // String ratingText = ForgeUtils.getRatingText(wave.get("rating").intValue());
        // for (int i = 0; i < lore.size(); i++) {
        //     if (lore.get(i).startsWith(Settings.I.Attrib_Rating_Text)) {
        //         lore.set(i, ratingText);
        //     }
        // }
        // itemMeta.setLore(lore);
        // previewItemStack.setItemMeta(itemMeta);
        setOutputItem(e.getInventory(), previewItemStack);
        // 重新初始化词条展示框
        // resetAttribute(e.getInventory(), previewItemStack);

        // 确认和取消增加附魔效果
        toggleStatus(e.getInventory(), true);

        // 减少重铸石和幸运石数量
        if (reforgeStone.getAmount() > 1) {
            reforgeStone.setAmount(reforgeStone.getAmount() - 1);
        } else {
            reforgeStone = null;
        }
        setInventoryItem(e.getInventory(), "重铸石", reforgeStone);

        if (luckyStone != null) {
            if (luckyStone.getAmount() > 1) {
                luckyStone.setAmount(luckyStone.getAmount() - 1);
            } else {
                luckyStone = null;
            }
            setInventoryItem(e.getInventory(), "幸运石", luckyStone);
        }
    }

    @Override
    public void close(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        // player.sendMessage("重铸窗口销毁");
        ItemStack itemStack = getInventoryItem(e.getInventory(), "输入");
        if (itemStack != null) {
            player.getInventory().addItem(itemStack);
        }
        itemStack = getInventoryItem(e.getInventory(), "重铸石");
        if (itemStack != null) {
            player.getInventory().addItem(itemStack);
        }
        itemStack = getInventoryItem(e.getInventory(), "幸运石");
        if (itemStack != null) {
            player.getInventory().addItem(itemStack);
        }
    }

    // 初始化词条展示框
    private void initAttribute(Inventory inventory, ItemStack itemStack) {
        String drawName = NullForge.getItemManager().getStringNBT(itemStack, "DrawName");

        if (drawName == null || !DrawManager.hasDrawData(drawName)) {
            return;
        }

        DrawData drawData = DrawManager.getDrawDataOfFileName(drawName);
        if (drawData == null) {
            Bukkit.getLogger().warning("获取图纸[" + drawName + "]数据失败");
            return;
        }

        String level = ForgeUtils.extractLevel(itemStack);
        Settings.Level levelObj = Settings.I.Levels.get(level);
        int max = levelObj.AttributeRange.get(1);

        List<String> randomAttributes = ForgeUtils.getRandomAttributes(itemStack);
        List<Integer> slots = this.flagSlotsMap.get("词条展示框");
        NullForge.debug(slots.toString());
        Iterator<String> iterator = randomAttributes.iterator();
        Map<String, Object> nbt = new HashMap<>();
        // List<String> sameAttributes = ForgeUtils.getRandomAttributes(itemStack);

        Bukkit.getScheduler().runTask(NullForge.INSTANCE, () -> {
            for (int slot : slots) {
                if (!iterator.hasNext()) {
                    break;
                }
                ItemStack originalItem = inventory.getItem(slot);
                if (originalItem != null && originalItem.getType() != Material.AIR) {
                    continue;
                }
                String next = iterator.next();
                nbt.put("Flag", next);
                ItemStack item = NullForge.getItemManager().addItemNBT(new ItemStack(Material.PAPER), nbt);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ForgeUtils.apply(next, max));
                item.setItemMeta(itemMeta);
                inventory.setItem(slot, item);
                unlockAttributeSlots.add(slot);
            }
        });
    }

    // 清空词条展示框
    private void clearAttribute(Inventory inventory, boolean all) {
        List<Integer> slots = all ? this.flagSlotsMap.get("词条展示框") : this.unlockAttributeSlots;
        Bukkit.getScheduler().runTask(NullForge.INSTANCE, () -> {
            for (int slot : slots) {
                inventory.setItem(slot, null);
            }
        });
    }

    // 重置词条展示框
    private void resetAttribute(Inventory inventory, ItemStack itemStack, boolean all) {
        clearAttribute(inventory, all);
        initAttribute(inventory, itemStack);
    }

    public ItemStack getInventoryItem(Inventory inventory, String flag) {
        List<Integer> slots = this.flagSlotsMap.get(flag);
        for (int slot : slots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                return item;
            }
        }
        return null;
    }

    public void setInventoryItem(Inventory inventory, String flag, ItemStack itemStack) {
        List<Integer> slots = this.flagSlotsMap.get(flag);
        Bukkit.getScheduler().runTask(NullForge.INSTANCE, () -> {
            inventory.setItem(slots.get(0), itemStack);
        });
    }

    public void setOutputItem(Inventory inventory, ItemStack itemStack) {
        List<Integer> slots = this.flagSlotsMap.get("输出");
        Bukkit.getScheduler().runTask(NullForge.INSTANCE, () -> {
            for (int slot : slots) {
                inventory.setItem(slot, itemStack);
            }
        });
    }

    private void toggleStatus(Inventory inventory, boolean status) {
        String[] flags = {"确认", "取消"};
        for (String flag : flags) {
            ItemStack item = getInventoryItem(inventory, flag);
            ItemMeta itemMeta = item.getItemMeta();
            if (status) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                itemMeta.removeEnchant(Enchantment.DURABILITY);
                itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);
            setInventoryItem(inventory, flag, item);
        }
    }

    public static ReforgeGUI getInstance() {
        if (instance == null) {
            instance = new ReforgeGUI();
        }
        return instance;
    }
}
