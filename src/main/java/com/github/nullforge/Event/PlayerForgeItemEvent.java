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
    private boolean isBatchForge = false; // 标记是否是批量锻造
    private double totalExp = 0; // 用于累加批量锻造的总经验

    public PlayerForgeItemEvent(Player p, ItemStack item, DrawData draw) {
        this.p = p;
        this.item = item;
        this.draw = draw;
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

    public boolean isBatchForge() {
        return isBatchForge;
    }

    public void setBatchForge(boolean batchForge) {
        this.isBatchForge = batchForge;
    }

    public void addExp(double exp) {
        this.totalExp += exp;
    }

    public double getTotalExp() {
        return this.totalExp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}