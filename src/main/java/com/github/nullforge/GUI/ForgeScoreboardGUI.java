package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@RegisterInventory
public class ForgeScoreboardGUI extends NullInventory {
    public static ForgeScoreboardGUI instance;

    public Inventory initInventory() {
        return null;
    }

    @Override
    public void click(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void close(InventoryCloseEvent inventoryCloseEvent) {

    }

    public static ForgeScoreboardGUI getInstance() {
        if (instance == null) {
            instance = new ForgeScoreboardGUI();
        }
        return instance;
    }
}
