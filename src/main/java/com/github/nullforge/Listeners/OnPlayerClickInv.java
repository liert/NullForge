package com.github.nullforge.Listeners;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Data.TempItemStack;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.Forge;
import com.github.nullforge.GUI.ForgeGUI;
import com.github.nullforge.GUI.SwitchDrawGUI;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.GemUtil;
import com.github.nullforge.Utils.RandomUtil;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class OnPlayerClickInv implements Listener {
    public static Map<String, List<ItemStack>> tempItemMap = new HashMap<>();
    public static Map<String, DrawData> drawPlayerMap = new HashMap<>();
    public static List<String> nextList = new ArrayList<>();
    public static List<String> unClickList = new ArrayList<>();
    public static Map<String, Integer> indexMap = new HashMap<>();
    public static Map<String, DrawData> previewDrawMap = new HashMap<>();
    @EventHandler
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot < 0) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        String invTitle = e.getInventory().getTitle();
        if (invTitle.equals("§c§l请选择你需要锻造的图纸")) {
            e.setCancelled(true);
            if (slot > 35) {
                // 处理分页逻辑
                int index = indexMap.get(p.getName());
                if (slot == 36) {
                    if (index <= 0) {
                        p.sendMessage(MessageLoader.getMessage("gui-first-page")); // 已经是第一页
                    } else {
                        p.openInventory(SwitchDrawGUI.getGUI(p, index - 1));
                    }
                } else if (slot == 44) {
                    PlayerData pd = PlayerData.pMap.get(p.getName());
                    if ((index + 1) * 36 > pd.getLearn().size()) {
                        p.sendMessage(MessageLoader.getMessage("gui-last-page")); // 已经是最后一页
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

            // 判断左键或右键点击
            if (e.isLeftClick()) {
                // 左键点击：执行锻造操作
                nextList.remove(p.getName());
                unClickList.add(p.getName());
                if (dd.getDrawItem() == null) {
                    p.sendMessage(MessageLoader.getMessage("gui-not-draw")); // 不存在图纸
                    return;
                }
                p.openInventory(ForgeGUI.getGUI(dd.getDrawItem()));
            } else if (e.isRightClick()) {
                // 右键点击：预览图纸所需材料
                // 右键点击：预览图纸所需材料
                Inventory previewInv = Bukkit.createInventory(null, 54, "§c§l图纸材料预览：" + dd.getDisplayName());
                List<ItemStack> materials = dd.getFormula();
                for (int i = 0; i < materials.size(); i++) {
                    previewInv.setItem(i, materials.get(i));
                }

                // 添加管理员按钮（第53号槽位）
                if (p.hasPermission("nullforge.admin")) {
                    ItemStack adminButton = new ItemStack(Material.NETHER_STAR);
                    ItemMeta meta = adminButton.getItemMeta();
                    meta.setDisplayName("§6§l[仅管理员可见] 一键获取材料");
                    List<String> lore = new ArrayList<>();
                    lore.add("§7点击获取本页展示的所有锻造材料");
                    meta.setLore(lore);
                    adminButton.setItemMeta(meta);
                    previewInv.setItem(53, adminButton);

                    // 存储当前预览的图纸数据
                    previewDrawMap.put(p.getName(), dd);
                }
                p.openInventory(previewInv);
            }
        }
        if (invTitle != null && invTitle.matches("§c§l图纸材料预览：.*")) {
            e.setCancelled(true);

            // 处理管理员按钮点击
            if (slot == 53 && p.hasPermission("nullforge.admin")) {
                DrawData dd = previewDrawMap.get(p.getName());
                if (dd != null) {
                    // 精确计算所需空间
                    int requiredSlots = 0;
                    for (ItemStack material : dd.getFormula()) {
                        if (material == null || material.getType() == Material.AIR) continue;
                        int maxStack = material.getType().getMaxStackSize();
                        requiredSlots += (int) Math.ceil((double) material.getAmount() / maxStack);
                    }

                    // 获取实际空闲格子
                    int emptySlots = (int) Arrays.stream(p.getInventory().getStorageContents())
                            .filter(Objects::isNull)
                            .count();

                    // 空间不足直接返回
                    if (emptySlots < requiredSlots) {
                        p.sendMessage(MessageLoader.getMessage("forge-inventory-full"));
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
                        return;
                    }

                    // 尝试添加所有材料
                    boolean success = true;
                    List<ItemStack> clonedMaterials = new ArrayList<>();
                    for (ItemStack material : dd.getFormula()) {
                        ItemStack clone = material.clone();
                        clonedMaterials.add(clone);
                        HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(clone);
                        if (!leftover.isEmpty()) {
                            success = false;
                            break;
                        }
                    }
                    // 处理宝石材料
                    ItemStack gem = dd.getGem(); // 获取图纸所需的宝石
                    if (gem != null) {
                        ItemStack gemClone = gem.clone();
                        HashMap<Integer, ItemStack> gemLeftover = p.getInventory().addItem(gemClone);
                        if (!gemLeftover.isEmpty()) {
                            success = false;
                        }
                    }
                    // 处理添加结果
                    if (success) {
                        p.sendMessage(MessageLoader.getMessage("admin-get-materials-success"));
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    } else {
                        // 回滚已添加物品
                        clonedMaterials.forEach(m -> p.getInventory().removeItem(m));
                        p.sendMessage(MessageLoader.getMessage("forge-inventory-full"));
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
                    }
                }
                return;
            }
        }
        if (invTitle.equals("§c锻造系统")) {
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
            ItemStack draw = inv.getItem(10).clone();
            ItemStack gemstone = inv.getItem(13).clone();
            DrawData drawData = DrawManager.getDraw(draw);
            if (drawData == null) {
                p.sendMessage(MessageLoader.getMessage("gui-invalid-draw")); //图纸不合法
                return;
            }
            int playerLevel = PlayerData.pMap.get(p.getName()).getLevel();
            if (playerLevel < drawData.getNeedPlayerLevel()) {
                p.sendMessage(MessageLoader.getMessage("gui-not-level") + drawData.getNeedPlayerLevel()); //锻造等级不足
                return;
            }
            ItemStack item = drawData.getGem();
            int gemstoneLevel = this.getGemLevel(item, gemstone);
            if (gemstoneLevel <= 0) {
                p.sendMessage(MessageLoader.getMessage("gui-not-gem")); //放置的不是有效的锻造宝石
                return;
            }
            if (gemstoneLevel < drawData.getNeedGemLevel()) {
                String message = MessageLoader.getMessage("gui-not-gemlevel").replace("%gemlevel%", String.valueOf(drawData.getNeedGemLevel())); //需要的宝石等级不足
                p.sendMessage(message);
                return;
            }
            unClickList.remove(p.getName());
            List<ItemStack> tempList = new ArrayList<>();
            tempList.add(gemstone);
            tempItemMap.put(p.getName(), tempList);
            // addMiddleItem(p.getPlayer(), gemstone);
            TempItemStack.addTempItem(p, gemstone);
            drawPlayerMap.put(p.getName(), drawData);
            Inventory fInv = Bukkit.createInventory(null, 54, "§c请放入锻造材料后关闭背包开始锻造");
            nextList.add(p.getName());
            p.openInventory(fInv);
        }
        if (invTitle.equals("§b§l宝石合成")) {
            Inventory inventory = e.getInventory();
            if (e.getClickedInventory().getTitle().equals("§b§l宝石合成")) {
                ItemStack gemFail = null;
                ItemStack over = null;
                int success = 0;
                int fail = 0;
                // int slot = e.getRawSlot();
                if ((slot < 12 || slot > 14) && slot != 10 && slot != 16) {
                    e.setCancelled(true);
                }
                if (slot >= 21 && slot <= 23 || slot >= 3 && slot <= 6) {
                    if (inventory.getItem(10) != null && inventory.getItem(16) != null) {
                        ItemStack gem1 = inventory.getItem(10).clone();
                        ItemStack gem2 = inventory.getItem(16).clone();
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
                                    if (Forge.rd.nextInt(1000) <= Settings.I.Gem_Level_Up_Chance) {
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
        Player p = (Player) e.getPlayer();
        String title = e.getInventory().getTitle();

        // 判断关闭的是“图纸材料预览”界面
        if (title != null && title.matches("§c§l图纸材料预览：.*")) {
            // 清理预览数据
            previewDrawMap.remove(p.getName());
            // 重新打开“选择需要锻造的图纸”界面
            Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> {
                int pageIndex = indexMap.getOrDefault(p.getName(), 0); // 获取当前页码，如果没有则默认为第一页
                p.openInventory(SwitchDrawGUI.getGUI(p, pageIndex));
            }, 3L); // 延迟1个tick
        }

        // 判断关闭的是“锻造系统”界面
        if (title != null && title.equals("§c锻造系统")) {
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
            Inventory inv = e.getInventory();
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
            int rd;
            if (!drawPlayerMap.containsKey(p.getName()) || !tempItemMap.containsKey(p.getName())) {
                return;
            }
            TempItemStack tempItemStack = TempItemStack.getTempItemStack(p);
            Inventory inv = e.getInventory();
            DrawData dd = drawPlayerMap.get(p.getName());
            List<ItemStack> formulaList = dd.getFormula();
            // 获取玩家放入的材料
            for (int j = 0; j < inv.getSize(); ++j) {
                if (inv.getItem(j) == null) continue;
                tempItemStack.addTempItem(inv.getItem(j).clone());
            }

            // 没有放入锻造材料，返回宝石
            if (tempItemStack.getSize() <= 1) {
                tempItemStack.toPlayerInv();
                p.sendMessage(MessageLoader.getMessage("forge-null"));
                return;
            }

            // 放入的材料不足以锻造一个物品，返回材料和宝石
            formulaList.add(dd.getGem());
            int finalCount = getFinalCount(tempItemStack, formulaList);
            if (finalCount <= 0) {
                tempItemStack.toPlayerInv();
                p.sendMessage(MessageLoader.getMessage("forge-null"));
                return;
            }
            p.sendMessage(MessageLoader.getMessage("forge-ing")); //锻造中...

            // 对临时物品进行分类，目前只分类了强化石
            tempItemStack.analysisItem();

            List<ItemStack> finalResult = new ArrayList<>();
            for (int i = 0; i < finalCount; ++i) {
                int addChance = tempItemStack.getLuckStoneChance();
                if (addChance > 0) {
                    // 获取原始消息字符串
                    String message = MessageLoader.getMessage("forge-hoist"); //配置增加
                    // 使用 replace 方法替换占位符 %addchance%
                    String formattedMessage = message.replace("%addchance%", String.valueOf(addChance));
                    // 发送格式化后的消息给玩家
                    p.sendMessage(formattedMessage);
                }
                Map<String, Float> map = Settings.I.Forge_Chance;
                String level = RandomUtil.probabString(map);
                String quality = Settings.I.Attrib_Level_Text.get(level);
                String attributeRange = Settings.I.Forge_Attrib.get(level); // 属性波动范围
                String[] raw = attributeRange.split(" => ");
                int min = Integer.parseInt(raw[0]);
                int max = Integer.parseInt(raw[1]);
                int r = Forge.rd.nextInt(max - min);
                double add = (double) r + (double) (max - min) * ((double) addChance / 100.0);
                r = add + add * ((double) PlayerData.pMap.get(p.getName()).getLevel() / 100.0) > (double) (max - min) ? max - min : (int) (add + add * ((double) PlayerData.pMap.get(p.getName()).getLevel() / 100.0));
                float pref = (float) r / (float) (max - min);
                int pre = (int) (pref * 25.0f);
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
                ItemStack resultItem = dd.getResult().clone();
                ItemMeta resultMeta = resultItem.getItemMeta();
                List<String> lore = resultMeta.hasLore() ? resultMeta.getLore() : new ArrayList<>();
                Pattern pa = Pattern.compile("\\([^(]+\\)");
                List<String> attrib = dd.getAttrib();
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
                lore.add(Settings.I.ForgeOwner.replaceAll("<player>", p.getName()));
                DateFormat df = DateFormat.getDateInstance(2, Locale.CHINA);
                DateFormat df2 = DateFormat.getTimeInstance(2, Locale.CHINA);
                String date = df.format(new Date()) + " " + df2.format(new Date());
                lore.add(Settings.I.ForgeDate.replaceAll("<date>", date));
                resultMeta.setLore(lore);
                resultItem.setItemMeta(resultMeta);
                finalResult.add(resultItem);
                // 锻造完成，发送全服广播
                p.sendMessage(MessageLoader.getMessage("forge-finish")); // 锻造成功
                String playerName = p.getName();
                String itemName = resultItem.getItemMeta().getDisplayName();
                String message = MessageLoader.getMessage("forge-broadcast") // 全服广播
                        .replace("%player%", playerName)
                        .replace("%quality%", quality)
                        .replace("%itemname%", itemName);
                Bukkit.broadcastMessage(message);
                // 执行自定义命令
                List<String> customCommands = dd.getCustomCommands();
                for (String command : customCommands) {
                    command = command.replace("%player%", p.getName()); // 替换占位符
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command); // 执行命令
                }
            }
            int invSize = finalResult.size() + (9 - finalResult.size() % 9);
            Inventory rInv = Bukkit.createInventory(null, invSize, "§c§l锻造结果");
            for (int i = 0; i < finalResult.size(); ++i) {
                ItemStack item = finalResult.get(i);
                rInv.setItem(i, item);
                PlayerForgeItemEvent event = new PlayerForgeItemEvent(p, item.clone(), dd);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
            // 返还无效和多余的材料
            // playerAddItem(p);
            tempItemStack.toPlayerInv();
            Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> p.openInventory(rInv), 20L);
        }
        if (e.getInventory().getTitle().equals("§c§l锻造结果")) {
            PlayerInventory playerInventory = p.getInventory();
            for (int i = 0; i < e.getInventory().getSize(); ++i) {
                if (e.getInventory().getItem(i) == null) continue;
                playerInventory.addItem(e.getInventory().getItem(i));
            }
        }
    }

    // 获取最终锻造数量，并更改total中的物品数量
    private int getFinalCount(TempItemStack tempItemStack, List<ItemStack> flist) {
        if (tempItemStack.getSize() < 0 || flist == null) {
            return 0; // 如果 total 或 flist 为 null，直接返回 0
        }

        List<Integer> counts = new ArrayList<>();
        for (ItemStack item : flist) {
            if (item == null) {
                continue; // 如果某个材料为 null，跳过
            }
            int count = tempItemStack.getItemStackAmount(item);
            counts.add(count / item.getAmount());
        }

        if (counts.isEmpty()) {
            return 0; // 如果没有有效的材料，返回 0
        }

        int finalCount = Collections.min(counts);

        for (ItemStack item : flist) {
            if (item == null) {
                continue; // 如果某个材料为 null，跳过
            }
            tempItemStack.removeTempItem(item, finalCount * item.getAmount());
        }
        return finalCount;
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
                if (gemLore.get(i).equals(itemLore.get(i))) {
                    continue;
                }
                return -1;
            }
            String levelLore = itemLore.get(i);
            if (!levelLore.startsWith(Settings.I.Gem_Level_Color)) {
                return -1;
            }
            if (!levelLore.endsWith(Settings.I.Gem_Level_Text)) {
                return -1;
            }
            level = levelLore.split(Settings.I.Gem_Level_Color)[1].length();
        }
        return level;
    }
}

