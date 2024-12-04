package com.github.nullforge.Listeners;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Main;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ItemMaker;
import com.github.nullforge.Utils.RandomUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class OnPlayerBreakBlock
        implements Listener {
    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        boolean isInWorld = false;
        for (String s : Settings.I.Ore_Worlds) {
            if (!s.equalsIgnoreCase(b.getLocation().getWorld().getName())) continue;
            isInWorld = true;
            break;
        }
        if (!isInWorld) {
            return;
        }
        int id = b.getTypeId();
        if (!Settings.I.Ore_Chance.containsKey(id) || Settings.I.Gem_Lore.containsKey(id)) {
            return;
        }
        List<Integer> list = Settings.I.Ore_Chance.get(id);
        if (Main.rd.nextInt(1000) <= list.get(1)) {
            ItemStack item = new ItemStack(Material.getMaterial((int)list.get(0)));
            if (!Settings.I.Gem_Lore.containsKey(item.getTypeId())) {
                return;
            }
            List<String> lore = Settings.I.Gem_Lore.get(item.getTypeId());
            String name = lore.get(0);
            ArrayList<String> newLore = new ArrayList<>();
            for (int i = 0; i < lore.size(); ++i) {
                if (i == 0) continue;
                if (!lore.get(i).equals("<等级内容无需设置>")) {
                    newLore.add(lore.get(i));
                    continue;
                }
                StringBuilder levelText = new StringBuilder(Settings.I.Gem_Level_Color);
                HashMap<Integer, Float> map = new HashMap<>();
                for (Integer g : Settings.I.Gem_Level_Chance.keySet()) {
                    map.put(g, (float) ((double) Settings.I.Gem_Level_Chance.get(g) / 1000.0));
                }
                Integer level;
                do {
                    level = RandomUtil.probabInt(map);
                } while (level == null);
                for (int z = 0; z < level; ++z) {
                    levelText.append(Settings.I.Gem_Level_Text);
                }
                newLore.add(levelText.toString());
            }
            e.setCancelled(true);
            b.setType(Material.AIR);
            Location loc = p.getLocation();
            World wd = loc.getWorld();
            wd.dropItem(loc, ItemMaker.create(item, name, newLore));
            p.sendMessage(MessageLoader.getMessage("Break-Block") + name);
        }
    }
}

