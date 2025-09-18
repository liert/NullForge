package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Data.TempItemStack;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ItemMaker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@RegisterInventory
public class ForgeGUI extends NullInventory {
    private static ForgeGUI instance;

    public Inventory initInventory(ItemStack draw) {
        Inventory inv = createInventory(null, 27, "§c锻造系统");
        ItemStack red = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)14, MessageLoader.getMessage("gui-forge-draw"), "");
        ItemStack yellow = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)4, MessageLoader.getMessage("gui-forge-gem"), "");
        ItemStack green = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, MessageLoader.getMessage("gui-forge-click"), "");
        ItemStack anvil = ItemMaker.create(145, 0, "", "");
        for (int i = 0; i < 27; i++) {
            if (i == 0 || i == 1 || i == 2 || i == 9 || i == 11 || i == 18 || i == 19 || i == 20) {
                inv.setItem(i, red);
            } else if (i == 3 || i == 4 || i == 5 || i == 12 || i == 14 || i == 21 || i == 22 || i == 23) {
                inv.setItem(i, yellow);
            } else if (i == 6 || i == 7 || i == 8 || i == 15 || i == 17 || i == 24 || i == 25 || i == 26) {
                inv.setItem(i, green);
            } else if (i == 10) {
                inv.setItem(i, draw);
            } else if (i == 16) {
                inv.setItem(i, anvil);
            }
        }
        return inv;
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot < 0) {
            return;
        }
        if (slot > 26) {
            if (e.getAction() != InventoryAction.PICKUP_ALL && e.getAction() != InventoryAction.PICKUP_HALF && e.getAction() != InventoryAction.PICKUP_ONE && e.getAction() != InventoryAction.PICKUP_SOME && e.getAction() != InventoryAction.PLACE_ALL && e.getAction() != InventoryAction.PLACE_ONE && e.getAction() != InventoryAction.PLACE_SOME) {
                e.setCancelled(true);
            }
            return;
        }
        if (slot == 13) {
            return;
        }
        e.setCancelled(true);
        if (slot != 16) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        Inventory inv = e.getInventory();
        if (inv.getItem(13) == null) {
            p.sendMessage(MessageLoader.getMessage("gui-gem-empty")); //宝石不能为空
            return;
        }
        ItemStack draw = inv.getItem(10).clone();
        ItemStack gemstone = inv.getItem(13).clone();
        DrawData drawData = DrawManager.getDraw(draw);
        if (drawData == null) {
            p.sendMessage(MessageLoader.getMessage("gui-invalid-draw")); //图纸不合法
            return;
        }
        int playerLevel = PlayerData.pMap.get(p.getName()).getLevel();
        if (playerLevel < drawData.getNeedPlayerLevel()) {
            p.sendMessage(MessageLoader.getMessage("gui-not-level") + drawData.getNeedPlayerLevel()); //锻造等级不足
            return;
        }
        ItemStack item = drawData.getGem();
        int gemstoneLevel = this.getGemLevel(item, gemstone);
        if (gemstoneLevel <= 0) {
            p.sendMessage(MessageLoader.getMessage("gui-not-gem")); //放置的不是有效的锻造宝石
            return;
        }
        if (gemstoneLevel < drawData.getNeedGemLevel()) {
            String message = MessageLoader.getMessage("gui-not-gemlevel").replace("%gemlevel%", String.valueOf(drawData.getNeedGemLevel())); //需要的宝石等级不足
            p.sendMessage(message);
            return;
        }
        OnPlayerClickInv.unClickList.remove(p.getName());
        List<ItemStack> tempList = new ArrayList<>();
        tempList.add(gemstone);
        OnPlayerClickInv.tempItemMap.put(p.getName(), tempList);
        TempItemStack.addTempItem(p, gemstone);
        OnPlayerClickInv.drawPlayerMap.put(p.getName(), drawData);
        OnPlayerClickInv.nextList.add(p.getName());
        p.openInventory(ForgeInputGUI.getInstance().initInventory());
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

    public int getGemLevel(ItemStack gem, ItemStack item) {
        if (item.getType() != gem.getType()) {
            return -1;
        }
        if (!item.hasItemMeta()) {
            return -1;
        }
        ItemMeta gemMeta = gem.getItemMeta();
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasDisplayName()) {
            return -1;
        }
        if (!itemMeta.getDisplayName().equals(gemMeta.getDisplayName())) {
            return -1;
        }
        if (!itemMeta.hasLore()) {
            return -1;
        }
        List<String> gemLore = gemMeta.getLore();
        List<String> itemLore = itemMeta.getLore();
        if (gemLore.size() != itemLore.size()) {
            return -1;
        }
        int level = 0;
        for (int i = 0; i < gemLore.size(); ++i) {
            if (i != 1) {
                if (gemLore.get(i).equals(itemLore.get(i))) {
                    continue;
                }
                return -1;
            }
            String levelLore = itemLore.get(i);
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

