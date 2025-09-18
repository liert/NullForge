package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullbridge.annotate.SlotClick;
import com.github.nullbridge.item.NullItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@RegisterInventory
public class ForgeMenuGUI extends NullInventory {
    public static ForgeMenuGUI instance;

    public Inventory initInventory() {
        Inventory inventory = createInventory(null, inventorySize, title);
        for (int i = 0; i < layout.size(); i++) {
            String line = layout.get(i);
            for (int j = 0; j < line.length(); j++) {
                NullItemStack item = getItem(line.charAt(j));
                inventory.setItem(i * 9 + j, item.getItemStack());
                if (initialized || item.getFlag().isEmpty()) continue;
                registerFlag(i * 9 + j, item.getFlag());
            }
        }
        initialized = true;
        return inventory;
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @SlotClick(flag = "锻造")
    public void onForgeClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        player.openInventory(SwitchDrawGUI.getInstance().initInventory(player, 0));
    }

    @SlotClick(flag = "重铸")
    public void onReforgeClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        player.openInventory(ReforgeGUI.getInstance().initInventory());
    }

    @Override
    public void close(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        // player.sendMessage("锻造菜单窗口销毁");
    }

    public static ForgeMenuGUI getInstance() {
        if (instance == null) {
            instance = new ForgeMenuGUI();
        }
        return instance;
    }
}
