package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.TempItemStack;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ExpUtil;
import com.github.nullforge.Utils.ForgeUtils;
import com.github.nullforge.Utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@RegisterInventory
public class ForgeInputGUI extends NullInventory {
    private static ForgeInputGUI instance;

    public Inventory initInventory() {
        return createInventory(null, 54, "§c请放入锻造材料后关闭背包开始锻造");
    }

    @Override
    public void click(InventoryClickEvent e) {

    }

    @Override
    public void close(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (!OnPlayerClickInv.drawPlayerMap.containsKey(player.getName()) || !OnPlayerClickInv.tempItemMap.containsKey(player.getName())) {
            return;
        }
        TempItemStack tempItemStack = TempItemStack.getTempItemStack(player);
        Inventory inv = e.getInventory();
        DrawData dd = OnPlayerClickInv.drawPlayerMap.get(player.getName());
        List<ItemStack> formulas = dd.getFormula();
        // 获取玩家放入的材料
        for (int j = 0; j < inv.getSize(); ++j) {
            if (inv.getItem(j) == null) continue;
            tempItemStack.addTempItem(inv.getItem(j).clone());
        }

        // 没有放入锻造材料，返回宝石
        if (tempItemStack.getSize() <= 1) {
            tempItemStack.toPlayerInv();
            player.sendMessage(MessageLoader.getMessage("forge-null"));
            return;
        }

        // 放入的材料不足以锻造一个物品，返回材料和宝石
        formulas.add(dd.getGem());
        int finalCount = getFinalCount(tempItemStack, formulas);
        if (finalCount <= 0) {
            tempItemStack.toPlayerInv();
            player.sendMessage(MessageLoader.getMessage("forge-null"));
            return;
        }
        player.sendMessage(MessageLoader.getMessage("forge-ing")); //锻造中...

        // 对临时物品进行分类，目前只分类了强化石
        tempItemStack.analysisItem();

        List<ItemStack> finalResult = new ArrayList<>();
        Map<String, Integer> qualityInfo = new HashMap<>();
        double totalExp = 0.0;
        for (int i = 0; i < finalCount; ++i) {
            int addChance = tempItemStack.getLuckStoneChance();
            if (addChance > 0) {
                // 获取原始消息字符串
                String message = MessageLoader.getMessage("forge-hoist"); //配置增加
                // 使用 replace 方法替换占位符 %addchance%
                String formattedMessage = message.replace("%addchance%", String.valueOf(addChance));
                // 发送格式化后的消息给玩家
                player.sendMessage(formattedMessage);
            }
            String level = RandomUtil.probabString(player, dd, Settings.I.Forge_Chance);
            String quality = Settings.I.Attrib_Level_Text.get(level);
            ItemStack forgedItem = ForgeUtils.generateForgedItem(player, dd, level, addChance);
            finalResult.add(forgedItem);

            totalExp += ExpUtil.getNeedExp(dd.getNeedGemLevel());

            // 统计品质
            qualityInfo.put(quality, qualityInfo.getOrDefault(quality, 0) + 1);

            // 执行自定义命令
            List<String> customCommands = dd.getCustomCommands();
            for (String command : customCommands) {
                command = command.replace("%player%", player.getName()); // 替换占位符
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command); // 执行命令
            }

            PlayerForgeItemEvent event = new PlayerForgeItemEvent(player, dd, finalResult, qualityInfo, totalExp, i == finalCount - 1);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        // 返还无效和多余的材料
        tempItemStack.toPlayerInv();

        // 打开锻造结果界面
        Bukkit.getScheduler().runTaskLater(NullForge.INSTANCE, () -> player.openInventory(ForgeResultGUI.getInstance().initInventory(finalResult)), 20L);
    }

    // 获取最终锻造数量，并更改total中的物品数量
    private int getFinalCount(TempItemStack tempItemStack, List<ItemStack> flist) {
        if (tempItemStack.getSize() < 0 || flist == null || flist.isEmpty()) {
            return 0; // 如果 total 或 flist 为 null，直接返回 0
        }

        Map<ItemStack, Integer> formulas = new HashMap<>();
        for (ItemStack itemStack: flist) {
            ItemStack item = itemStack.clone();
            item.setAmount(1);
            formulas.compute(item, (k, v) -> v == null ? itemStack.getAmount() : v + itemStack.getAmount());
        }

        List<Integer> counts = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : formulas.entrySet()) {
            ItemStack itemStack = entry.getKey();
            Integer count = entry.getValue();
            if (itemStack == null || count == null) {
                continue; // 如果某个材料为 null，跳过
            }
            counts.add(tempItemStack.getItemStackAmount(itemStack) / count);
        }
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

    public static ForgeInputGUI getInstance() {
        if (instance == null) {
            instance = new ForgeInputGUI();
        }
        return instance;
    }
}
