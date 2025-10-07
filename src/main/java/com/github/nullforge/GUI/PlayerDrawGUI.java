package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.InventoryContext;
import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.Inventory.NullInventoryHolder;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullbridge.annotate.SlotClick;
import com.github.nullbridge.manager.InventoryManager;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ExpUtil;
import com.github.nullforge.Utils.ForgeUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.nullforge.Utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import qfeng.qfsoulbag.api.SoulBagApi;

@RegisterInventory
public class PlayerDrawGUI extends NullInventory {
    public static PlayerDrawGUI instance;
    private final AtomicBoolean quickForgeLock = new AtomicBoolean(false);

    @Override
    public void initInventory(InventoryContext context) {
        Player player = context.getPlayer();
        Inventory inventory = context.getInventory();
        Integer page = context.get("page", Integer.class);
        if (page == null) {
            page = 0;
            context.put("page", 0);
        }
        PlayerData playerData = PlayerData.pMap.get(player.getName());
        List<Integer> drawSlots = this.flagSlotsMap.get("图纸槽");
        if (drawSlots == null || drawSlots.isEmpty()) {
            return;
        }
        int size = drawSlots.size();
        Map<Integer, DrawData> drawMap = new HashMap<>();
        List<String> learn = playerData.getLearn();
        List<String> draws = learn.subList(page * size, Math.min((page + 1) * size, learn.size()));
        Iterator<String> iterator = draws.iterator();
        for (int i: this.flagSlotsMap.get("图纸槽")) {
            if (!iterator.hasNext()) break;
            String draw = iterator.next();
            DrawData drawData = DrawManager.getDraw(draw);
            if (drawData == null) {
                continue;
            }
            ItemStack item = drawData.getDrawItem();
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(MessageLoader.getMessage("gui-draw-preview-left-click"));
            lore.add(MessageLoader.getMessage("gui-draw-preview-right-click"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
            drawMap.put(i, drawData);
        }
        context.put("drawMap", drawMap);
    }

    @SlotClick(flag = "图纸槽")
    public void onDrawClick(InventoryClickEvent e) {
        NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        InventoryContext context = holder.getContext();
        Player player = (Player) e.getWhoClicked();
        Map<Integer, DrawData> drawMap = (Map<Integer, DrawData>) context.get("drawMap", Map.class);
        if (!drawMap.containsKey(e.getRawSlot())) {
            return;
        }
        DrawData drawData = drawMap.get(e.getRawSlot());
        if (drawData.getDrawItem() == null) {
            player.sendMessage(MessageLoader.getMessage("gui-not-draw")); // 不存在图纸
            return;
        }

        if (e.isShiftClick() && e.isLeftClick()) {
            if (!Settings.I.Quick_Forge) {
                return;
            }
            // 一键锻造
            if (Bukkit.getPluginManager().getPlugin("QFSoulBag") == null) {
                player.sendMessage(MessageLoader.getMessage("soul-bag-not-installed"));
                return;
            }

            if (quickForgeLock.get()) {
                player.sendMessage(MessageLoader.getMessage("quick-forge-cooling"));
                return;
            }

            // 检查背包空间
            boolean flag = false;
            for (int i = 0; i < 36; i++) {
                if (player.getInventory().getItem(i) == null) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                player.sendMessage(MessageLoader.getMessage("player-not-enough-space"));
                return;
            }

            qfeng.qfsoulbag.PlayerData qfPlayerData = SoulBagApi.INSTANCE.getPlayerData(player.getUniqueId());
            List<ItemStack> materials = new ArrayList<>(drawData.getFormula());
            materials.add(drawData.getGem());
            List<ItemStack> deletedItems = new ArrayList<>();
            for (ItemStack material : materials) {
                if (SoulBagApi.INSTANCE.removeItemInSoulBag(qfPlayerData, material, material.getAmount())) {
                    deletedItems.add(material);
                } else {
                    for (ItemStack deletedItem : deletedItems) {
                        SoulBagApi.INSTANCE.addItemIntoSoulBag(qfPlayerData, deletedItem, deletedItem.getAmount());
                    }
                    player.sendMessage(MessageLoader.getMessage("soul-bag-not-enough-materials"));
                    return;
                }
            }

            // 不支持使用幸运石
            String level = RandomUtil.probabString(player, drawData, Settings.Level.getChance());
            Settings.Level levelObj = Settings.I.Levels.get(level);
            // String quality = Settings.I.Attrib_Level_Text.get(level);
            ItemStack forgedItem = ForgeUtils.generateForgedItem(player, drawData, level, 0);
            Map<Integer, ItemStack> itemStackMap = player.getInventory().addItem(forgedItem);
            if (!itemStackMap.isEmpty()) {
                for (ItemStack deletedItem : deletedItems) {
                    SoulBagApi.INSTANCE.addItemIntoSoulBag(qfPlayerData, deletedItem, deletedItem.getAmount());
                }

                player.sendMessage(MessageLoader.getMessage("player-not-enough-space"));
                return;
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            itemStacks.add(forgedItem);
            Map<String, Integer> qualityInfo = new HashMap<>();
            qualityInfo.put(levelObj.Lore, 1);

            quickForgeLock.set(true);
            Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> {
                quickForgeLock.set(false);
            }, (long) Settings.I.Quick_Forge_Cooldown * 20);

            PlayerForgeItemEvent event = new PlayerForgeItemEvent(player, drawData, itemStacks, qualityInfo, ExpUtil.getRandomExp(drawData.getNeedGemLevel()), true);
            Bukkit.getServer().getPluginManager().callEvent(event);
        } else if (e.isLeftClick()) {
            // 左键点击：执行锻造操作
            InventoryContext newContext = new InventoryContext(player);
            newContext.put("draw", drawData.getDrawItem());
            InventoryManager.open(ForgeGUI.class, newContext);
        } else if (e.isRightClick()) {
            // 右键点击：预览图纸所需材料
            InventoryContext newContext = new InventoryContext(player);
            newContext.put("page", context.get("page", Integer.class));
            newContext.put("drawData", drawData);
            InventoryManager.open(MaterialPreviewGUI.class, newContext);
        }
    }

    @SlotClick(flag = "上一页")
    public void onPreviousPageClick(InventoryClickEvent e) {
        NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        InventoryContext context = holder.getContext();
        int page = context.get("page", Integer.class);
        if (page <= 0) {
            Player p = (Player) e.getWhoClicked();
            p.sendMessage(MessageLoader.getMessage("gui-first-page")); // 已经是第一页
            return;
        }
        context.put("page", page - 1);
        this.refresh(context);
    }

    @SlotClick(flag = "下一页")
    public void onNextPageClick(InventoryClickEvent e) {
        NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        InventoryContext context = holder.getContext();
        int page = context.get("page", Integer.class);
        Player p = (Player) e.getWhoClicked();
        PlayerData playerData = PlayerData.pMap.get(p.getName());
        if ((page + 1) * this.getDrawGridSize() >= playerData.getLearn().size()) {
            p.sendMessage(MessageLoader.getMessage("gui-last-page")); // 已经是最后一页
            return;
        }
        context.put("page", page + 1);
        this.refresh(context);
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void close(InventoryCloseEvent e) {}

    private void refresh(InventoryContext context) {
        Inventory inventory = context.getInventory();
        for (int i: this.flagSlotsMap.get("图纸槽")) {
            inventory.setItem(i, null);
        }
        this.initInventory(context);
    }

    private int getDrawGridSize() {
        return this.flagSlotsMap.get("图纸槽").size();
    }

    public static PlayerDrawGUI getInstance() {
        if (instance == null) {
            instance = new PlayerDrawGUI();
        }
        return instance;
    }
}
