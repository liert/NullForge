package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ExpUtil;
import com.github.nullforge.Utils.ForgeUtils;
import com.github.nullforge.Utils.ItemMaker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.nullforge.Utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import qfeng.qfsoulbag.api.SoulBagApi;

@RegisterInventory
public class SwitchDrawGUI extends NullInventory {
    public static SwitchDrawGUI instance;
    public static Map<String, List<DrawData>> switchMap = new HashMap<>();
    private final AtomicBoolean quickForgeLock = new AtomicBoolean(false);

    public Inventory initInventory(Player p, int index) {
        Inventory inv = createInventory(null, 45, "§c§l请选择你需要锻造的图纸");
        ItemStack up = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)14, "§c§l上一页", "");
        ItemStack down = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, "§a§l下一页", "");
        inv.setItem(36, up);
        inv.setItem(44, down);
        PlayerData pd = PlayerData.pMap.get(p.getName());
        List<String> draws = pd.getLearn();
        List<DrawData> switchTemp = new ArrayList<>();
        int count = 0;
        int startIndex = index * 36;
        while (startIndex < draws.size()) {
            String draw = draws.get(startIndex++);
            DrawData drawData = DrawManager.getDraw(draw);
            if (drawData != null) {
                if (count >= 36) break;
                ItemStack item = drawData.getDrawItem();
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.add(MessageLoader.getMessage("gui-draw-preview-left-click"));
                    lore.add(MessageLoader.getMessage("gui-draw-preview-right-click"));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(count, item);
                    switchTemp.add(drawData);
                    ++count;
                }
            }
        }
        switchMap.put(p.getName(), switchTemp);
        OnPlayerClickInv.indexMap.put(p.getName(), index);
        return inv;
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot < 0) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);
        if (slot > 35) {
            // 处理分页逻辑
            int index = OnPlayerClickInv.indexMap.get(p.getName());
            if (slot == 36) {
                if (index <= 0) {
                    p.sendMessage(MessageLoader.getMessage("gui-first-page")); // 已经是第一页
                } else {
                    p.openInventory(initInventory(p, index - 1));
                }
            } else if (slot == 44) {
                PlayerData pd = PlayerData.pMap.get(p.getName());
                if ((index + 1) * 36 > pd.getLearn().size()) {
                    p.sendMessage(MessageLoader.getMessage("gui-last-page")); // 已经是最后一页
                } else {
                    p.openInventory(initInventory(p, index + 1));
                }
            }
            return;
        }

        if (!SwitchDrawGUI.switchMap.containsKey(p.getName())) {
            return;
        }
        List<DrawData> list = SwitchDrawGUI.switchMap.get(p.getName());
        if (slot > list.size() - 1) {
            return;
        }
        DrawData dd = list.get(slot);
        if (dd.getDrawItem() == null) {
            p.sendMessage(MessageLoader.getMessage("gui-not-draw")); // 不存在图纸
            return;
        }

        if (e.isShiftClick() && e.isLeftClick()) {
            if (!Settings.I.Quick_Forge) {
                return;
            }
            // 一键锻造
            if (Bukkit.getPluginManager().getPlugin("QFSoulBag") == null) {
                p.sendMessage(MessageLoader.getMessage("soul-bag-not-installed"));
                return;
            }

            if (quickForgeLock.get()) {
                p.sendMessage(MessageLoader.getMessage("quick-forge-cooling"));
                return;
            }

            // 检查背包空间
            boolean flag = false;
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getItem(i) == null) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                p.sendMessage(MessageLoader.getMessage("player-not-enough-space"));
                return;
            }

            qfeng.qfsoulbag.PlayerData qfPlayerData = SoulBagApi.INSTANCE.getPlayerData(p.getUniqueId());
            List<ItemStack> materials = new ArrayList<>(dd.getFormula());
            materials.add(dd.getGem());
            List<ItemStack> deletedItems = new ArrayList<>();
            for (ItemStack material : materials) {
                if (SoulBagApi.INSTANCE.removeItemInSoulBag(qfPlayerData, material, material.getAmount())) {
                    deletedItems.add(material);
                } else {
                    for (ItemStack deletedItem : deletedItems) {
                        SoulBagApi.INSTANCE.addItemIntoSoulBag(qfPlayerData, deletedItem, deletedItem.getAmount());
                    }
                    p.sendMessage(MessageLoader.getMessage("soul-bag-not-enough-materials"));
                    return;
                }
            }

            // 不支持使用幸运石
            String level = RandomUtil.probabString(p, dd, Settings.I.Forge_Chance);
            String quality = Settings.I.Attrib_Level_Text.get(level);
            ItemStack forgedItem = ForgeUtils.generateForgedItem(p, dd, level, 0);
            Map<Integer, ItemStack> itemStackMap = p.getInventory().addItem(forgedItem);
            if (!itemStackMap.isEmpty()) {
                for (ItemStack deletedItem : deletedItems) {
                    SoulBagApi.INSTANCE.addItemIntoSoulBag(qfPlayerData, deletedItem, deletedItem.getAmount());
                }

                p.sendMessage(MessageLoader.getMessage("player-not-enough-space"));
                return;
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            itemStacks.add(forgedItem);
            Map<String, Integer> qualityInfo = new HashMap<>();
            qualityInfo.put(quality, 1);

            quickForgeLock.set(true);
            Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> {
                quickForgeLock.set(false);
            }, (long) Settings.I.Quick_Forge_Cooldown * 20);

            PlayerForgeItemEvent event = new PlayerForgeItemEvent(p, dd, itemStacks, qualityInfo, ExpUtil.getRandomExp(dd.getNeedGemLevel()), true);
            Bukkit.getServer().getPluginManager().callEvent(event);
        } else if (e.isLeftClick()) {
            // 左键点击：执行锻造操作
            OnPlayerClickInv.nextList.remove(p.getName());
            OnPlayerClickInv.unClickList.add(p.getName());
            p.openInventory(ForgeGUI.getInstance().initInventory(dd.getDrawItem()));
        } else if (e.isRightClick()) {
            // 右键点击：预览图纸所需材料
            p.openInventory(MaterialPreviewGUI.getInstance().initInventory(p, dd));
        }
    }

    @Override
    public void close(InventoryCloseEvent e) {

    }

    public static SwitchDrawGUI getInstance() {
        if (instance == null) {
            instance = new SwitchDrawGUI();
        }
        return instance;
    }
}
