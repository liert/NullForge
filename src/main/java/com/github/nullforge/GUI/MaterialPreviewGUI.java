package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@RegisterInventory
public class MaterialPreviewGUI extends NullInventory {
    public static MaterialPreviewGUI instance;

    public Inventory initInventory(Player player, DrawData drawData) {
        Inventory inventory = createInventory(null, 54, "§c§l图纸材料预览：" + drawData.getDisplayName());
        List<ItemStack> materials = drawData.getFormula();
        for (int i = 0; i < materials.size(); i++) {
            inventory.setItem(i, materials.get(i));
        }

        // 添加管理员按钮（第53号槽位）
        if (player.hasPermission("nullforge.admin")) {
            ItemStack adminButton = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = adminButton.getItemMeta();
            meta.setDisplayName("§6§l[仅管理员可见] 一键获取材料");
            List<String> lore = new ArrayList<>();
            lore.add("§7点击获取本页展示的所有锻造材料");
            meta.setLore(lore);
            adminButton.setItemMeta(meta);
            inventory.setItem(53, adminButton);

            // 存储当前预览的图纸数据
            OnPlayerClickInv.previewDrawMap.put(player.getName(), drawData);
        }
        return inventory;
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot < 0) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);

        // 处理管理员按钮点击
        if (slot == 53 && p.hasPermission("nullforge.admin")) {
            DrawData dd = OnPlayerClickInv.previewDrawMap.get(p.getName());
            if (dd == null) {
                return;
            }
            // 精确计算所需空间
            int requiredSlots = 0;
            for (ItemStack material : dd.getFormula()) {
                if (material == null || material.getType() == Material.AIR) continue;
                int maxStack = material.getType().getMaxStackSize();
                requiredSlots += (int) Math.ceil((double) material.getAmount() / maxStack);
            }

            // 获取实际空闲格子
            int emptySlots = (int) Arrays.stream(p.getInventory().getStorageContents())
                    .filter(Objects::isNull)
                    .count();

            // 空间不足直接返回
            if (emptySlots < requiredSlots) {
                p.sendMessage(MessageLoader.getMessage("forge-inventory-full"));
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
                return;
            }

            // 尝试添加所有材料
            boolean success = true;
            List<ItemStack> clonedMaterials = new ArrayList<>();
            for (ItemStack material : dd.getFormula()) {
                ItemStack clone = material.clone();
                clonedMaterials.add(clone);
                HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(clone);
                if (!leftover.isEmpty()) {
                    success = false;
                    break;
                }
            }
            // 处理宝石材料
            ItemStack gem = dd.getGem(); // 获取图纸所需的宝石
            if (gem != null) {
                ItemStack gemClone = gem.clone();
                HashMap<Integer, ItemStack> gemLeftover = p.getInventory().addItem(gemClone);
                if (!gemLeftover.isEmpty()) {
                    success = false;
                }
            }
            // 处理添加结果
            if (success) {
                p.sendMessage(MessageLoader.getMessage("admin-get-materials-success"));
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            } else {
                // 回滚已添加物品
                clonedMaterials.forEach(m -> p.getInventory().removeItem(m));
                p.sendMessage(MessageLoader.getMessage("forge-inventory-full"));
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
            }
        }
    }

    @Override
    public void close(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        // 清理预览数据
        OnPlayerClickInv.previewDrawMap.remove(player.getName());
        // 重新打开“选择需要锻造的图纸”界面
        Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> {
            int pageIndex = OnPlayerClickInv.indexMap.getOrDefault(player.getName(), 0); // 获取当前页码，如果没有则默认为第一页
            player.openInventory(SwitchDrawGUI.getInstance().initInventory(player, pageIndex));
        }, 3L); // 延迟1个tick
    }

    public static MaterialPreviewGUI getInstance() {
        if (instance == null) {
            instance = new MaterialPreviewGUI();
        }
        return instance;
    }
}
