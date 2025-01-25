package com.github.nullforge.Listeners;

import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.MessageLoader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class OnPlayerInteract implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();
        PlayerInventory playerInventory = p.getInventory();
        ItemStack itemStack = playerInventory.getItemInMainHand();

        // 确保玩家的主手物品不为空
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        // 检查是否是右键操作
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // 获取图纸数据
        DrawData drawData = DrawManager.getDraw(itemStack);
        if (drawData == null) {
            return;
        }

        // 检查玩家等级是否符合要求
        PlayerData playerData = PlayerData.pMap.get(p.getName());
        if (playerData.getLevel() < drawData.getNeedPlayerLevel()) {
            p.sendMessage(MessageLoader.getMessage("draw-not-level"));
            return;
        }

        // 检查图纸数量是否为1
        if (itemStack.getAmount() > 1) {
            p.sendMessage(MessageLoader.getMessage("draw-must-1"));
            return;
        }

        // 检查是否已经学习过该图纸
        String displayName = drawData.getDisplayName();
        List<String> learn = playerData.getLearn();
        if (learn.contains(displayName)) {
            p.sendMessage(MessageLoader.getMessage("draw-learned"));
            return;
        }

        // 学习图纸
        learn.add(displayName);
        playerData.setLearn(learn);
        playerInventory.setItemInMainHand(new ItemStack(Material.AIR)); // 移除图纸
        String message = MessageLoader.getMessage("draw-learn-succeed").replace("%draw_name%", displayName);
        p.sendMessage(message);
    }
}