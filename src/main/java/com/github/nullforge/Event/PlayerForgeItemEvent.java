package com.github.nullforge.Event;

import com.github.nullforge.Data.DrawData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerForgeItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player p;
    private ItemStack item;
    private DrawData draw;
    private boolean isFinalForge; // 标记是否是最后一次锻造
    private double totalExp; // 总经验

    public PlayerForgeItemEvent(Player p, ItemStack item, DrawData draw, boolean isFinalForge, double totalExp) {
        this.p = p;
        this.item = item;
        this.draw = draw;
        this.isFinalForge = isFinalForge;
        this.totalExp = totalExp;
    }

    public Player getPlayer() {
        return this.p;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public DrawData getDraw() {
        return this.draw;
    }

    public boolean isFinalForge() {
        return isFinalForge;
    }

    public double getTotalExp() {
        return totalExp;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}