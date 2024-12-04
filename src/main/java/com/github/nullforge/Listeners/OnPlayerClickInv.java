package com.github.nullforge.Listeners;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.GUI.ForgeGUI;
import com.github.nullforge.GUI.SwitchDrawGUI;
import com.github.nullforge.Main;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.GemUtil;
import com.github.nullforge.Utils.RandomUtil;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class OnPlayerClickInv
        implements Listener {
    public static Map<String, List<ItemStack>> tempItemMap = new HashMap<>();
    public static Map<String, DrawData> drawPlayerMap = new HashMap<>();
    public static List<String> nextList = new ArrayList<>();
    public static List<String> unClickList = new ArrayList<>();
    public static Map<String, Integer> indexMap = new HashMap<>();

    @EventHandler
    public void click(InventoryClickEvent e) {
        int level;
        ItemStack gemFail;
        ItemStack gem2;
        int slot;
        Player p = (Player)e.getWhoClicked();
        if (e.getInventory().getTitle().equals("§c§l请选择你需要锻造的图纸")) {
            slot = e.getRawSlot();
            if (slot < 0) {
                return;
            }
            e.setCancelled(true);
            if (slot > 35) {
                int index = indexMap.get(p.getName());
                if (slot == 36) {
                    if (index <= 0) {
                        p.sendMessage(MessageLoader.getMessage("gui-first-page")); //已经是第一页
                    } else {
                        p.openInventory(SwitchDrawGUI.getGUI(p, index - 1));
                    }
                    return;
                }
                if (slot == 44) {
                    PlayerData pd = PlayerData.pMap.get(p.getName());
                    if ((index + 1) * 36 > pd.getLearn().size()) {
                        p.sendMessage(MessageLoader.getMessage("gui-last-page")); //已经是最后一页
                    } else {
                        p.openInventory(SwitchDrawGUI.getGUI(p, index + 1));
                    }
                }
                return;
            }
            if (!SwitchDrawGUI.switchMap.containsKey(p.getName())) {
                return;
            }
            List<DrawData> list = SwitchDrawGUI.switchMap.get(p.getName());
            if (slot > list.size() - 1) {
                return;
            }
            DrawData dd = list.get(slot);
            nextList.remove(p.getName());
            unClickList.add(p.getName());
            if (dd.getDrawItem() == null) {
                p.sendMessage(MessageLoader.getMessage("gui-not-draw")); //不存在图纸
                return;
            }
            p.openInventory(ForgeGUI.getGUI(dd.getDrawItem()));
        }
        if (e.getInventory().getTitle().equals("§c锻造系统")) {
            slot = e.getRawSlot();
            if (slot < 0) {
                return;
            }
            if (slot > 26) {
                if (e.getAction() != InventoryAction.PICKUP_ALL && e.getAction() != InventoryAction.PICKUP_HALF && e.getAction() != InventoryAction.PICKUP_ONE && e.getAction() != InventoryAction.PICKUP_SOME && e.getAction() != InventoryAction.PLACE_ALL && e.getAction() != InventoryAction.PLACE_ONE && e.getAction() != InventoryAction.PLACE_SOME) {
                    e.setCancelled(true);
                }
                return;
            }
            if (slot == 13) {
                return;
            }
            e.setCancelled(true);
            if (slot != 16) {
                return;
            }
            Inventory inv = e.getInventory();
            if (inv.getItem(13) == null) {
                p.sendMessage(MessageLoader.getMessage("gui-gem-empty")); //宝石不能为空
                return;
            }
            gem2 = inv.getItem(10).clone();
            gemFail = inv.getItem(13).clone();
            if (gemFail.getAmount() != 1) {
                p.sendMessage(MessageLoader.getMessage("gui-gem-1")); //宝石必须为1
                return;
            }
            DrawData dd2 = OnPlayerClickInv.getDraw(gem2);
            if (dd2 == null) {
                p.sendMessage(MessageLoader.getMessage("gui-invalid-draw")); //图纸不合法
                return;
            }
            if (!this.getAttrib(dd2)) {
                p.sendMessage(MessageLoader.getMessage("gui-invalid-item")); //输出物品不合法
                return;
            }
            int success = PlayerData.pMap.get(p.getName()).getLevel();
            if (success < dd2.getNeedPlayerLevel()) {
                p.sendMessage(MessageLoader.getMessage("gui-not-level") + dd2.getNeedPlayerLevel()); //锻造等级不足
                return;
            }
            ItemStack item = dd2.getGem();
            level = this.getGemLevel(item, gemFail);
            if (level <= 0) {
                p.sendMessage(MessageLoader.getMessage("gui-not-gem")); //放置的不是有效的锻造宝石
                return;
            }
            if (level < dd2.getNeedGemLevel()) {
                String message = MessageLoader.getMessage("gui-not-gemlevel").replace("%gemlevel%", String.valueOf(dd2.getNeedGemLevel())); //需要的宝石等级不足
                p.sendMessage(message);
                return;
            }
            unClickList.remove(p.getName());
            ArrayList<ItemStack> tempList = new ArrayList<>();
            tempList.add(gemFail);
            tempItemMap.put(p.getName(), tempList);
            drawPlayerMap.put(p.getName(), dd2);
            Inventory fInv = Bukkit.createInventory(null, 54, "§c请放入锻造材料后关闭背包开始锻造");
            nextList.add(p.getName());
            p.openInventory(fInv);
        }
        if (e.getInventory().getTitle().equals("§b§l宝石合成")) {
            Inventory inventory = e.getInventory();
            if (e.getClickedInventory().getTitle().equals("§b§l宝石合成")) {
                gemFail = null;
                ItemStack over = null;
                int success = 0;
                int fail = 0;
                level = e.getRawSlot();
                if ((level < 12 || level > 14) && level != 10 && level != 16) {
                    e.setCancelled(true);
                }
                if (level >= 21 && level <= 23 || level >= 3 && level <= 6) {
                    if (inventory.getItem(10) != null && inventory.getItem(16) != null) {
                        ItemStack gem1 = inventory.getItem(10).clone();
                        gem2 = inventory.getItem(16).clone();
                        if (GemUtil.getGemLevel(gem1) != 0 && GemUtil.getGemLevel(gem2) != 0) {
                            if (!GemUtil.isSameGem(gem1, gem2)) {
                                p.sendMessage(MessageLoader.getMessage("gem-different")); //宝石种类不同
                                return;
                            }
                            if (GemUtil.getGemLevel(gem1) != GemUtil.getGemLevel(gem2)) {
                                p.sendMessage(MessageLoader.getMessage("gem-same-level")); //宝石必须同级
                                return;
                            }
                            if (gem1.getAmount() != gem2.getAmount()) {
                                p.sendMessage(MessageLoader.getMessage("gem-same-amount")); //数量必须同等
                                return;
                            }
                            if (inventory.getItem(12) == null && inventory.getItem(14) == null) {
                                int gemLevel = GemUtil.getGemLevel(gem1);
                                for (int i = gem1.getAmount(); i > 0; --i) {
                                    if (Main.rd.nextInt(1000) <= Settings.I.Gem_Level_Up_Chance) {
                                        ++success;
                                        over = GemUtil.changeGemLevel(gem1, gemLevel + 1);
                                    } else {
                                        gemFail = gem1;
                                        ++fail;
                                    }
                                    gem1.setAmount(i);
                                    gem2.setAmount(i);
                                    inventory.setItem(10, gem1);
                                    inventory.setItem(16, gem2);
                                }
                                if (success != 0) {
                                    over.setAmount(success);
                                    inventory.setItem(12, over);
                                }
                                if (fail != 0) {
                                    gemFail.setAmount(fail);
                                    inventory.setItem(14, gem1);
                                }
                                inventory.setItem(10, null);
                                inventory.setItem(16, null);
                                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                                String successMsg = MessageLoader.getMessage("gem-success").replace("%success%", String.valueOf(success)); //成功数量
                                String failureMsg = MessageLoader.getMessage("gem-failure").replace("%fail%", String.valueOf(fail)); //失败数量

                                p.sendMessage(successMsg);
                                if (fail > 0) {
                                    p.sendMessage(failureMsg);
                                }
                                return;
                            }
                            p.sendMessage(MessageLoader.getMessage("gem-not-placement")); //禁止放置
                            return;
                        }
                        p.sendMessage(MessageLoader.getMessage("gem-must")); //必须是宝石
                        return;
                    }
                    p.sendMessage(MessageLoader.getMessage("gem-place")); //宝石放置
                }
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        Inventory inv;
        Player p = (Player)e.getPlayer();
        if (e.getInventory().getTitle().equals("§c锻造系统")) {
            if (unClickList.contains(p.getName())) {
                Inventory inv2 = e.getInventory();
                if (inv2.getItem(13) != null) {
                    p.getInventory().addItem(inv2.getItem(13));
                }
                unClickList.remove(p.getName());
                return;
            }
            if (nextList.contains(p.getName())) {
                return;
            }
            if (!tempItemMap.containsKey(p.getName())) {
                return;
            }
            List<ItemStack> list = tempItemMap.get(p.getName());
            for (ItemStack item : list) {
                p.getInventory().addItem(item);
            }
            p.sendMessage(MessageLoader.getMessage("forge-fail")); //锻造失败
        }
        if (e.getInventory().getTitle().equals("§b§l宝石合成")) {
            inv = e.getInventory();
            ArrayList<ItemStack> give = new ArrayList<>();
            if (inv.getItem(10) != null) {
                give.add(inv.getItem(10));
            }
            if (inv.getItem(16) != null) {
                give.add(inv.getItem(16));
            }
            for (int i = 12; i < 15; ++i) {
                if (inv.getItem(i) == null) continue;
                give.add(inv.getItem(i));
            }
            for (ItemStack item3 : give) {
                p.getInventory().addItem(item3);
            }
        }
        if (e.getInventory().getTitle().equals("§c请放入锻造材料后关闭背包开始锻造")) {
            String at2;
            int rd;
            int r;
            String line1;
            p.sendMessage(MessageLoader.getMessage("forge-ing")); //锻造中...
            if (!drawPlayerMap.containsKey(p.getName())) {
                return;
            }
            if (!tempItemMap.containsKey(p.getName())) {
                return;
            }
            inv = e.getInventory();
            DrawData dd2 = drawPlayerMap.get(p.getName());
            List<ItemStack> flist = dd2.getFormula();
            int count = 0;
            ArrayList<ItemStack> ilist = new ArrayList<>();
            int addChance = 0;
            for (int j = 0; j < inv.getSize(); ++j) {
                String start;
                String[] raws;
                ItemMeta meta;
                if (inv.getItem(j) == null) continue;
                ItemStack item4 = inv.getItem(j).clone();
                if (item4.hasItemMeta() && (meta = item4.getItemMeta()).hasLore() && (line1 = meta.getLore().get(0)).startsWith((raws = (start = Settings.I.Attrib_Up_Item_Lore).split("<chance>"))[0])) {
                    r = raws.length;
                    for (int n2 = 0; n2 < r; ++n2) {
                        String s = raws[n2];
                        line1 = line1.replaceAll(s, "");
                    }
                    addChance += Integer.parseInt(line1) * item4.getAmount();
                    continue;
                }
                ilist.add(inv.getItem(j).clone());
            }
            block6: for (ItemStack fitem : flist) {
                for (ItemStack item5 : ilist) {
                    if (!item5.equals(fitem)) continue;
                    ilist.remove(item5);
                    ++count;
                    continue block6;
                }
            }
            if (count != flist.size()) {
                PlayerInventory pInv = p.getInventory();
                for (int k = 0; k < inv.getSize(); ++k) {
                    if (inv.getItem(k) == null) continue;
                    pInv.addItem(inv.getItem(k));
                }
                List<ItemStack> tList = tempItemMap.get(p.getName());
                for (ItemStack item5 : tList) {
                    pInv.addItem(item5);
                }
                p.sendMessage(MessageLoader.getMessage("forge-null")); //材料不匹配
                return;
            }
            if (addChance > 0) {
                // 获取原始消息字符串
                String message = MessageLoader.getMessage("forge-hoist"); //配置增加
                // 使用 replace 方法替换占位符 %addchance%
                String formattedMessage = message.replace("%addchance%", String.valueOf(addChance));
                // 发送格式化后的消息给玩家
                p.sendMessage(formattedMessage);
            }
            HashMap<String, Float> map = new HashMap<>();
            for (String s22 : Settings.I.Forge_Chance.keySet()) {
                map.put(s22, (float) ((double) Settings.I.Forge_Chance.get(s22) / 1000.0));
            }
            String s2;
            do {
                s2 = RandomUtil.probabString(map);
            } while (s2 == null);
            String quality = Settings.I.Attrib_Level_Text.get(s2);
            line1 = Settings.I.Forge_Attrib.get(s2);
            String[] raw = line1.split(" => ");
            int min = Integer.parseInt(raw[0]);
            int max = Integer.parseInt(raw[1]);
            r = Main.rd.nextInt(max - min);
            double add = (double)r + (double)(max - min) * ((double)addChance / 100.0);
            r = add + add * ((double)PlayerData.pMap.get(p.getName()).getLevel() / 100.0) > (double)(max - min) ? max - min : (int)(add + add * ((double)PlayerData.pMap.get(p.getName()).getLevel() / 100.0));
            float pref = (float)r / (float)(max - min);
            int pre = (int)(pref * 25.0f);
            StringBuilder preceText = new StringBuilder("§b[");
            String ch = "§c|";
            if (pre > 5) {
                ch = "§e|";
            }
            if (pre > 10) {
                ch = "§a|";
            }
            if (pre > 15) {
                ch = "§3|";
            }
            if (pre > 20) {
                ch = "§9|";
            }
            for (rd = 0; rd < 25; ++rd) {
                if (rd <= pre) {
                    preceText.append(ch);
                    continue;
                }
                preceText.append("§8|");
            }
            preceText.append("§b]");
            rd = min + r;
            ItemStack item6 = dd2.getResult().clone();
            ItemMeta meta2 = item6.getItemMeta();
            List<String> lore = meta2.hasLore() ? meta2.getLore() : new ArrayList<>();
            Pattern pa = Pattern.compile("\\([^(]+\\)");
            List<String> attrib = dd2.getAttrib();
            for (String attribute : attrib) {
                Matcher m = pa.matcher(attribute);
                ArrayList<Integer> ori = new ArrayList<>();
                ArrayList<Integer> now = new ArrayList<>();
                while (m.find()) {
                    ori.add(Integer.parseInt(m.group().replaceAll("\\(", "").replaceAll("\\)", "")));
                }
                for (int n : ori) {
                    n += (int) ((double) n * (double) rd / 100.0);
                    now.add(n);
                }
                String atn = attribute;
                for (int i2 = 0; i2 < ori.size(); ++i2) {
                    String z = "\\(" + ori.get(i2) + "\\)";
                    atn = atn.replaceAll(z, String.valueOf(now.get(i2)));
                }
                lore.add(atn);
            }
            lore.add(quality);
            lore.add(Settings.I.Attrib_Perce_Text + preceText);
            at2 = Settings.I.ForgeOwner;
            at2 = at2.replaceAll("<player>", p.getName());
            DateFormat df = DateFormat.getDateInstance(2, Locale.CHINA);
            DateFormat df2 = DateFormat.getTimeInstance(2, Locale.CHINA);
            String date = df.format(new Date()) + " " + df2.format(new Date());
            lore.add(at2);
            lore.add(Settings.I.ForgeDate.replaceAll("<date>", date));
            meta2.setLore(lore);
            item6.setItemMeta(meta2);
            Inventory rInv = Bukkit.createInventory(null, 9, "§c§l锻造结果");
            rInv.setItem(4, item6);
            ItemStack re = item6.clone();
            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                p.openInventory(rInv);
                PlayerForgeItemEvent event = new PlayerForgeItemEvent(p, re, dd2);
                Bukkit.getServer().getPluginManager().callEvent(event);
                p.sendMessage(MessageLoader.getMessage("forge-finish")); //锻造成功
                String playerName = p.getName();
                // 假设 quality 是一个字符串，表示物品的品质
                String itemName = item6.getItemMeta().getDisplayName(); // 获取物品名称，这里假设是通过ItemMeta获得
                String message = MessageLoader.getMessage("forge-broadcast") //全服广播
                        .replace("%player%", playerName)
                        .replace("%quality%", quality)
                        .replace("%itemname%", itemName);
                    p.sendMessage(message);
                }, 20L);
        }
        if (e.getInventory().getTitle().equals("§c§l锻造结果")) {
            PlayerInventory pInv2 = p.getInventory();
            for (int i = 0; i < e.getInventory().getSize(); ++i) {
                if (e.getInventory().getItem(i) == null) continue;
                pInv2.addItem(e.getInventory().getItem(i));
            }
        }
    }

    public boolean getAttrib(DrawData dd) {
        int id = dd.getResult().getTypeId();
        boolean result = id == 267 || id == 268 || id == 272 || id == 276 || id == 283 || id == 298 || id == 302 || id == 306 || id == 310 || id == 314 || id == 299 || id == 303 || id == 307 || id == 311 || id == 315 || id == 300 || id == 304 || id == 308 || id == 312 || id == 316 || id == 301 || id == 305 || id == 309 || id == 313 || id == 317 || id == 442;
        return true;
    }

    public static DrawData getDraw(ItemStack item) {
        if (item.getTypeId() != Settings.I.Draw_Item_ID) {
            return null;
        }
        if (!item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return null;
        }
        List<String> lore = meta.getLore();
        String line0 = lore.get(0);
        return DrawData.DrawMap.getOrDefault(line0, null);
    }

    public int getGemLevel(ItemStack gem, ItemStack item) {
        if (item.getType() != gem.getType()) {
            return -1;
        }
        if (!item.hasItemMeta()) {
            return -1;
        }
        ItemMeta gemMeta = gem.getItemMeta();
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasDisplayName()) {
            return -1;
        }
        if (!itemMeta.getDisplayName().equals(gemMeta.getDisplayName())) {
            return -1;
        }
        if (!itemMeta.hasLore()) {
            return -1;
        }
        List<String> gemLore = gemMeta.getLore();
        List<String> itemLore = itemMeta.getLore();
        if (gemLore.size() != itemLore.size()) {
            return -1;
        }
        int level = 0;
        for (int i = 0; i < gemLore.size(); ++i) {
            if (i != 1) {
                if (gemLore.get(i).equals(itemLore.get(i))) continue;
                return -1;
            }
            if (!itemLore.get(i).contains(Settings.I.Gem_Level_Text)) {
                return -1;
            }
            if (itemLore.get(i).length() <= 2) {
                return -1;
            }
            if (!itemLore.get(i).contains(Settings.I.Gem_Level_Color)) {
                return -1;
            }
            String raw = itemLore.get(i);
            String[] ss = raw.split(Settings.I.Gem_Level_Color);
            level = ss[1].length();
        }
        return level;
    }
}

