package com.github.nullforge.Event;

import com.github.nullforge.Data.DrawData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PlayerForgeItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private List<ItemStack> items;
    private Map<String, Integer> qualityInfo;
    private DrawData draw;
    private boolean isFinalForge; // 标记是否是最后一次锻造
    private double totalExp; // 总经验

    public PlayerForgeItemEvent(Player player, DrawData draw, List<ItemStack> items, Map<String, Integer> qualityInfo, double totalExp, boolean isFinalForge) {
        this.player = player;
        this.draw = draw;
        this.items = items;
        this.qualityInfo = qualityInfo;
        this.totalExp = totalExp;
        this.isFinalForge = isFinalForge;
    }

    public Player getPlayer() {
        return this.player;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public Map<String, Integer> getQualityInfo() {
        return this.qualityInfo;
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