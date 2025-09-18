package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

@RegisterInventory
public class ForgeResultGUI extends NullInventory {
    public static ForgeResultGUI instance;

    public Inventory initInventory(List<ItemStack> itemStacks) {
        int invSize = itemStacks.size() + (9 - itemStacks.size() % 9);
        Inventory inventory = createInventory(null, invSize, "§c§l锻造结果");
        for (int i = 0; i < itemStacks.size(); ++i) {
            ItemStack item = itemStacks.get(i);
            inventory.setItem(i, item);
        }
        return inventory;
    }

    @Override
    public void click(InventoryClickEvent inventoryClickEvent) {}

    @Override
    public void close(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        PlayerInventory playerInventory = p.getInventory();
        for (int i = 0; i < e.getInventory().getSize(); ++i) {
            if (e.getInventory().getItem(i) == null) continue;
            playerInventory.addItem(e.getInventory().getItem(i));
        }
    }

    public static ForgeResultGUI getInstance() {
        if (instance == null) {
            instance = new ForgeResultGUI();
        }
        return instance;
    }
}
