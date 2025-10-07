package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.InventoryContext;
import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.Inventory.NullInventoryHolder;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullbridge.annotate.SlotClick;
import com.github.nullbridge.item.SlotItem;
import com.github.nullbridge.manager.InventoryManager;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@RegisterInventory
public class MaterialPreviewGUI extends NullInventory {
    public static MaterialPreviewGUI instance;

    @Override
    public void initInventory(InventoryContext context) {
        DrawData drawData = context.get("drawData", DrawData.class);
        List<ItemStack> materials = drawData.getFormula();
        List<Integer> slots = this.flagSlotsMap.get("材料槽");
        for (int i = 0; i < Math.min(materials.size(), slots.size()); i++) {
            inventory.setItem(slots.get(i), materials.get(i));
        }
    }

    @SlotClick(flag = "一键获取")
    public void onAdminGetMaterialsClick(InventoryClickEvent e) {
        NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        InventoryContext context = holder.getContext();
        SlotItem slotItem = context.getItem(e.getRawSlot());
        Player player = (Player) e.getWhoClicked();
        if (player.hasPermission(slotItem.getPermission())) {
            DrawData drawData = context.get("drawData", DrawData.class);
            if (drawData == null) {
                return;
            }
            // 精确计算所需空间
            List<ItemStack> materials = drawData.getFormula();
            int requiredSlots = materials.size() + 1; // +1 是为了宝石位置

            // 获取实际空闲格子
            int emptySlots = (int) Arrays.stream(player.getInventory().getStorageContents())
                    .filter(Objects::isNull)
                    .count();

            // 空间不足直接返回
            if (emptySlots < requiredSlots) {
                player.sendMessage(MessageLoader.getMessage("forge-inventory-full"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
                return;
            }

            // 尝试添加所有材料
            boolean success = true;
            List<ItemStack> clonedMaterials = new ArrayList<>();
            for (ItemStack material : drawData.getFormula()) {
                ItemStack clone = material.clone();
                clonedMaterials.add(clone);
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(clone);
                if (!leftover.isEmpty()) {
                    success = false;
                    break;
                }
            }
            // 处理宝石材料
            ItemStack gem = drawData.getGem(); // 获取图纸所需的宝石
            if (gem != null) {
                ItemStack gemClone = gem.clone();
                HashMap<Integer, ItemStack> gemLeftover = player.getInventory().addItem(gemClone);
                if (!gemLeftover.isEmpty()) {
                    success = false;
                }
            }
            // 处理添加结果
            if (success) {
                player.sendMessage(MessageLoader.getMessage("admin-get-materials-success"));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            } else {
                // 回滚已添加物品
                clonedMaterials.forEach(m -> player.getInventory().removeItem(m));
                player.sendMessage(MessageLoader.getMessage("forge-inventory-full"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
            }
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void close(InventoryCloseEvent e) {
        NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        InventoryContext context = holder.getContext();
        Player player = (Player) e.getPlayer();
        // 清理预览数据
        OnPlayerClickInv.previewDrawMap.remove(player.getName());
        // 重新打开“选择需要锻造的图纸”界面
        Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> {
            int page = context.get("page", Integer.class); // 获取当前页码，如果没有则默认为第一页
            InventoryContext newContext = new InventoryContext(player);
            newContext.put("page", page);
            InventoryManager.open(PlayerDrawGUI.class, newContext);
        }, 3L); // 延迟1个tick
    }

    public static MaterialPreviewGUI getInstance() {
        if (instance == null) {
            instance = new MaterialPreviewGUI();
        }
        return instance;
    }
}
