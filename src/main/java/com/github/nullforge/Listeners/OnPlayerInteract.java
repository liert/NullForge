package com.github.nullforge.Listeners;

import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import java.util.List;

import com.github.nullforge.MessageLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class OnPlayerInteract
        implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action ac = e.getAction();
        if (p.getItemInHand() == null) {
            return;
        }
        if (ac != Action.RIGHT_CLICK_AIR && ac != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = p.getItemInHand();
        if (OnPlayerClickInv.getDraw(item) == null) {
            return;
        }
        DrawData dd = OnPlayerClickInv.getDraw(item);
        PlayerData pd = PlayerData.pMap.get(p.getName());
        if (pd.getLevel() < dd.getNeedPlayerLevel()) {
            p.sendMessage(MessageLoader.getMessage("draw-not-level"));
            return;
        }
        String dName = DrawData.getDrawName(dd);
        if (dName == null) {
            p.sendMessage(MessageLoader.getMessage("draw-disused"));
            return;
        }
        if (item.getAmount() > 1) {
            p.sendMessage(MessageLoader.getMessage("draw-must-1"));
            return;
        }
        List<String> learn = pd.getLearn();
        if (learn.contains(dName)) {
            p.sendMessage(MessageLoader.getMessage("draw-learned"));
            return;
        }
        learn.add(dName);
        pd.setLearn(learn);
        p.getInventory().removeItem(new ItemStack[]{item});
        String message = MessageLoader.getMessage("draw-learn-succeed").replace("%draw_name%", dName);
        p.sendMessage(message);
    }
}

