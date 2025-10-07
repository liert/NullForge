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
import com.github.nullforge.Data.TempItemStack;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.MessageLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@RegisterInventory
public class ForgeGUI extends NullInventory {
    private static ForgeGUI instance;

    @Override
    public void initInventory(InventoryContext context) {
        List<Integer> drawSlots = getFlagSlots("图纸槽");
        if (drawSlots.isEmpty()) {
            return;
        }
        Inventory inventory = context.getInventory();
        Bukkit.getLogger().info(drawSlots.toString());
        inventory.setItem(drawSlots.get(0), context.get("draw", ItemStack.class));
    }

    @SlotClick(flag = "输入槽")
    public void onInputClick(InventoryClickEvent e) {
        e.setCancelled(false);
    }

    @SlotClick(flag = "锻造")
    public void onForgeClick(InventoryClickEvent e) {
        e.setCancelled(true);
        NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        InventoryContext context = holder.getContext();
        Player player = (Player) e.getWhoClicked();
        ItemStack gem = this.getInventoryItem(context, "输入槽");

        if (gem == null) {
            player.sendMessage(MessageLoader.getMessage("gui-gem-empty")); //宝石不能为空
            return;
        }
        ItemStack draw = getInventoryItem(context, "图纸槽");
        DrawData drawData = DrawManager.getDraw(draw);
        if (drawData == null) {
            player.sendMessage(MessageLoader.getMessage("gui-invalid-draw")); //图纸不合法
            return;
        }
        int playerLevel = PlayerData.pMap.get(player.getName()).getLevel();
        if (playerLevel < drawData.getNeedPlayerLevel()) {
            player.sendMessage(MessageLoader.getMessage("gui-not-level") + drawData.getNeedPlayerLevel()); //锻造等级不足
            return;
        }
        ItemStack needGem = drawData.getGem();
        int gemLevel = this.getGemLevel(needGem, gem);
        if (gemLevel <= 0) {
            player.sendMessage(MessageLoader.getMessage("gui-not-gem")); //放置的不是有效的锻造宝石
            return;
        }
        if (gemLevel < drawData.getNeedGemLevel()) {
            String message = MessageLoader.getMessage("gui-not-gemlevel").replace("%gemlevel%", String.valueOf(drawData.getNeedGemLevel())); //需要的宝石等级不足
            player.sendMessage(message);
            return;
        }

        TempItemStack.addTempItem(player, gem);
        OnPlayerClickInv.nextList.add(player.getName());
        InventoryContext newContext = new InventoryContext(player);
        newContext.put("drawData", drawData);
        InventoryManager.open(ForgeInputGUI.class, newContext);
    }

    @Override
    public void click(InventoryClickEvent e) {
        if (e.getRawSlot() < inventorySize) {
            e.setCancelled(true);
        }
        // NullInventoryHolder holder = (NullInventoryHolder) e.getInventory().getHolder();
        // InventoryContext context = holder.getContext();
        // Player player = (Player) e.getWhoClicked();
        // ItemStack gem = getInventoryItem(context, "输入槽");

        // if (gem == null) {
        //     player.sendMessage(MessageLoader.getMessage("gui-gem-empty")); //宝石不能为空
        //     return;
        // }
        // ItemStack draw = getInventoryItem(context, "图纸槽");
        // DrawData drawData = DrawManager.getDraw(draw);
        // if (drawData == null) {
        //     player.sendMessage(MessageLoader.getMessage("gui-invalid-draw")); //图纸不合法
        //     return;
        // }
        // int playerLevel = PlayerData.pMap.get(player.getName()).getLevel();
        // if (playerLevel < drawData.getNeedPlayerLevel()) {
        //     player.sendMessage(MessageLoader.getMessage("gui-not-level") + drawData.getNeedPlayerLevel()); //锻造等级不足
        //     return;
        // }
        // ItemStack needGem = drawData.getGem();
        // int gemLevel = this.getGemLevel(needGem, gem);
        // if (gemLevel <= 0) {
        //     player.sendMessage(MessageLoader.getMessage("gui-not-gem")); //放置的不是有效的锻造宝石
        //     return;
        // }
        // if (gemLevel < drawData.getNeedGemLevel()) {
        //     String message = MessageLoader.getMessage("gui-not-gemlevel").replace("%gemlevel%", String.valueOf(drawData.getNeedGemLevel())); //需要的宝石等级不足
        //     player.sendMessage(message);
        //     return;
        // }
        // OnPlayerClickInv.unClickList.remove(player.getName());
        // List<ItemStack> tempList = new ArrayList<>();
        // tempList.add(gem);
        // OnPlayerClickInv.tempItemMap.put(player.getName(), tempList);
        // TempItemStack.addTempItem(player, gem);
        // // OnPlayerClickInv.drawPlayerMap.put(player.getName(), drawData);
        // OnPlayerClickInv.nextList.add(player.getName());
        // // player.openInventory(ForgeInputGUI.getInstance().initInventory());
        // InventoryContext newContext = new InventoryContext(player);
        // newContext.put("drawData", drawData);
        // InventoryManager.open(ForgeInputGUI.class, newContext);
    }

    @Override
    public void close(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (OnPlayerClickInv.unClickList.contains(p.getName())) {
            Inventory inv2 = e.getInventory();
            if (inv2.getItem(13) != null) {
                p.getInventory().addItem(inv2.getItem(13));
            }
            OnPlayerClickInv.unClickList.remove(p.getName());
            return;
        }
        if (OnPlayerClickInv.nextList.contains(p.getName())) {
            return;
        }
        if (!OnPlayerClickInv.tempItemMap.containsKey(p.getName())) {
            return;
        }
        List<ItemStack> list = OnPlayerClickInv.tempItemMap.get(p.getName());
        for (ItemStack item : list) {
            p.getInventory().addItem(item);
        }
        p.sendMessage(MessageLoader.getMessage("forge-fail")); //锻造失败
    }

    public static ForgeGUI getInstance() {
        if (instance == null) {
            instance = new ForgeGUI();
        }
        return instance;
    }

    public int getGemLevel(ItemStack needGem, ItemStack gem) {
        if (gem.getType() != needGem.getType()) {
            return -1;
        }
        if (!gem.hasItemMeta()) {
            return -1;
        }
        ItemMeta needGemItemMeta = needGem.getItemMeta();
        ItemMeta gemItemMeta = gem.getItemMeta();
        if (!gemItemMeta.hasDisplayName()) {
            return -1;
        }
        if (!gemItemMeta.getDisplayName().equals(needGemItemMeta.getDisplayName())) {
            return -1;
        }
        if (!gemItemMeta.hasLore()) {
            return -1;
        }
        List<String> needGemLore = needGemItemMeta.getLore();
        List<String> gemLore = gemItemMeta.getLore();
        if (needGemLore.size() != gemLore.size()) {
            return -1;
        }
        int level = 0;
        for (int i = 0; i < needGemLore.size(); ++i) {
            if (i != 1) {
                if (needGemLore.get(i).equals(gemLore.get(i))) {
                    continue;
                }
                return -1;
            }
            String levelLore = gemLore.get(i);
            if (!levelLore.startsWith(Settings.I.Gem_Level_Color)) {
                return -1;
            }
            if (!levelLore.endsWith(Settings.I.Gem_Level_Text)) {
                return -1;
            }
            level = levelLore.split(Settings.I.Gem_Level_Color)[1].length();
        }
        return level;
    }
}

