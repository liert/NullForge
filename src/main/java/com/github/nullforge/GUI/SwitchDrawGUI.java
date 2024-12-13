package com.github.nullforge.GUI;

import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.Utils.ItemMaker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SwitchDrawGUI {
    public static Map<String, List<DrawData>> switchMap = new HashMap<>();

    public static Inventory getGUI(Player p, int index) {
        Inventory inv = Bukkit.createInventory(null, 45, "§c§l请选择你需要锻造的图纸");
        ItemStack up = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)14, "§c§l上一页", "");
        ItemStack down = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, "§a§l下一页", "");
        inv.setItem(36, up);
        inv.setItem(44, down);
        PlayerData pd = PlayerData.pMap.get(p.getName());
        List<String> draws = pd.getLearn();
        List<DrawData> switchTemp = new ArrayList<>();
        int count = 0;
        int startIndex = index * 36;
        while (startIndex < draws.size()) {
            String draw = draws.get(startIndex++);
            DrawData drawData = DrawManager.getDraw(draw);
            if (drawData != null) {
                if (count >= 36) break;
                ItemStack item = drawData.getDrawItem();
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.add("§c§l点击此图纸开始锻造...");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(count, item);
                    switchTemp.add(drawData);
                    ++count;
                }
            }
        }
        switchMap.put(p.getName(), switchTemp);
        OnPlayerClickInv.indexMap.put(p.getName(), index);
        return inv;
    }
}

