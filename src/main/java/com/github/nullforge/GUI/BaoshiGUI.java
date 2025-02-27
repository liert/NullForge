package com.github.nullforge.GUI;

import com.github.nullforge.Utils.BaoshiUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BaoshiGUI implements Listener {
    private static final int[] GEM_SLOTS = {11, 15}; // 宝石放置的槽位
    private static final int SYNTHESIS_SLOT = 13; // 合成按钮槽位

    public static Inventory getGUI() {
        Inventory inventory = Bukkit.createInventory(null, 27, "§c§l✠§f§l合成系统§c§l✠");

        // 创建占位物品
        ItemStack placeholder = createPlaceholder();

        // 填充占位符
        for (int i = 0; i < 27; i++) {
            if (!isGemSlot(i) && i != SYNTHESIS_SLOT) {
                inventory.setItem(i, placeholder);
            }
        }

        // 创建合成按钮
        ItemStack synthesisButton = new ItemStack(Material.WOOL, 1, (byte) 13); // 绿色羊毛的数据值为 13
        ItemMeta synthesisMeta = synthesisButton.getItemMeta();
        synthesisMeta.setDisplayName("§a点击合成");
        synthesisButton.setItemMeta(synthesisMeta);
        inventory.setItem(SYNTHESIS_SLOT, synthesisButton);

        return inventory;
    }

    private static boolean isGemSlot(int slot) {
        for (int gemSlot : GEM_SLOTS) {
            if (gemSlot == slot) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 确保事件发生在合成界面
        if (!event.getView().getTitle().equals("§c§l✠§f§l合成系统§c§l✠")) {
            return;
        }

        // 确保点击的库存是合成界面
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        // 如果点击的是占位符，取消事件
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == getPlaceholderMaterial()) {
            event.setCancelled(true); // 阻止占位符被移动
            return;
        }

        // 如果点击的是合成按钮，取消事件
        if (event.getSlot() == SYNTHESIS_SLOT) {
            event.setCancelled(true); // 防止物品被移动
            Player player = (Player) event.getWhoClicked();
            performSynthesis(player, event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 确保事件发生在合成界面
        if (!event.getView().getTitle().equals("§c§l✠§f§l合成系统§c§l✠")) {
            return;
        }

        // 当玩家关闭GUI时，将剩余物品放回玩家背包
        Inventory inventory = event.getInventory();
        for (int slot : GEM_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Player player = (Player) event.getPlayer();
                player.getInventory().addItem(item);
            }
        }
    }

    private void performSynthesis(Player player, Inventory inventory) {
        // 获取所有宝石槽位中的宝石
        List<ItemStack> gems = new ArrayList<>();
        for (int slot : GEM_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                gems.add(item);
            }
        }

        // 检查宝石数量是否足够
        if (gems.size() < 2) {
            player.sendMessage("§7[§3神铸§7]§7需要2个及以上的物品");
            return; // 不清空槽位，直接返回
        }

        // 检查是否有无法识别的物品
        boolean allGemsRecognized = true;
        String gemName1 = null;
        for (ItemStack gem : gems) {
            ItemMeta meta = gem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                player.sendMessage("§7[§3神铸§7]§7请确认放置的是正确的物品类型");
                allGemsRecognized = false;
                break;
            }
            String displayName = meta.getDisplayName();
            String pureName = BaoshiUtil.getPureGemName(displayName);
            if (gemName1 == null) {
                gemName1 = pureName;
            } else if (!pureName.equals(gemName1)) {
                player.sendMessage("§7[§3神铸§7]§7物品的类型必须相同");
                allGemsRecognized = false;
                break;
            }
        }

        if (!allGemsRecognized) {
            return; // 不清空槽位，直接返回
        }

        // 检查宝石等级是否相同
        int level1 = getGemLevel(gems.get(0));
        for (int i = 1; i < gems.size(); i++) {
            int level = getGemLevel(gems.get(i));
            if (level != level1) {
                player.sendMessage("§7[§3神铸§7]§7物品的等级必须相同");
                return; // 不清空槽位，直接返回
            }
        }

        // 检查两边的宝石数量是否相同
        int amount1 = gems.get(0).getAmount();
        int amount2 = gems.get(1).getAmount();
        if (amount1 != amount2) {
            player.sendMessage("§7[§3神铸§7]§7物品的数量必须相同");
            return; // 不清空槽位，直接返回
        }

        // 合成逻辑
        int[] result = new int[2]; // 用于存储成功和失败的数量
        boolean synthesisResult = BaoshiUtil.checkAndGiveGem(player, gems, result);

        if (synthesisResult) {
            player.sendMessage("§7[§3神铸§7]§7合成成功§a " + result[0] + " §7个物品，合成失败§a " + result[1] + " §7个物品");
            // 合成成功时清空宝石槽位
            for (int slot : GEM_SLOTS) {
                inventory.setItem(slot, null);
            }
        } else {
            player.sendMessage("§7[§3神铸§7]§4合成失败");
            // 合成失败时清空宝石槽位
            for (int slot : GEM_SLOTS) {
                inventory.setItem(slot, null);
            }
        }
    }

    private static int getGemLevel(ItemStack gem) {
        ItemMeta meta = gem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return -1; // 无法识别的物品
        }
        String displayName = meta.getDisplayName();
        String pattern = "§.[^§]*§8\\[§6Lv\\.(\\d+)§8\\]";
        if (!displayName.matches(".*" + pattern + ".*")) {
            return -1; // 无法识别的物品
        }
        return Integer.parseInt(displayName.replaceAll(".*§8\\[§6Lv\\.(\\d+)§8\\].*", "$1"));
    }

    private static ItemStack createPlaceholder() {
        Material material = getPlaceholderMaterial();
        ItemStack placeholder = new ItemStack(material, 1);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        placeholderMeta.setDisplayName("§7占位符");
        placeholder.setItemMeta(placeholderMeta);
        return placeholder;
    }

    private static Material getPlaceholderMaterial() {
        try {
            // 尝试使用较新的材料
            return Material.valueOf("GRAY_STAINED_GLASS_PANE");
        } catch (IllegalArgumentException e) {
            // 如果不支持，使用兼容的材料
            return Material.STAINED_GLASS_PANE;
        }
    }
}