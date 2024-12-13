package com.github.nullforge.Listeners;

import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import java.util.List;
import com.github.nullforge.MessageLoader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class OnPlayerInteract implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();
        PlayerInventory playerInventory = p.getInventory();
        ItemStack itemStack = playerInventory.getItemInMainHand();
        if (itemStack == null) {
            return;
        }
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        DrawData drawData = DrawManager.getDraw(itemStack);
        if (drawData == null) {
            return;
        }
        PlayerData playerData = PlayerData.pMap.get(p.getName());
        if (playerData.getLevel() < drawData.getNeedPlayerLevel()) {
            p.sendMessage(MessageLoader.getMessage("draw-not-level"));
            return;
        }
        String displayName = drawData.getDisplayName();
        if (itemStack.getAmount() > 1) {
            p.sendMessage(MessageLoader.getMessage("draw-must-1"));
            return;
        }
        List<String> learn = playerData.getLearn();
        if (learn.contains(displayName)) {
            p.sendMessage(MessageLoader.getMessage("draw-learned"));
            return;
        }
        learn.add(displayName);
        playerData.setLearn(learn);
        playerInventory.setItemInMainHand(new ItemStack(Material.AIR));
        String message = MessageLoader.getMessage("draw-learn-succeed").replace("%draw_name%", displayName);
        p.sendMessage(message);
    }
}

