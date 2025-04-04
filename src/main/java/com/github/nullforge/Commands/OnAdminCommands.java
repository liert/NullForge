package com.github.nullforge.Commands;

import com.github.nullcore.Config.ConfigurationLoader;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Forge;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ExpUtil;
import com.github.nullforge.Utils.GemUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.nullforge.Utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OnAdminCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fadmin")) {
            // 检查是否具有管理员权限
            if (!sender.hasPermission("nullforge.admin")) {
                sender.sendMessage("§c[系统]§a你没有足够的权限使用此命令!");
                return true;
            }

            if (args.length == 0 || (!args[0].equals("list") && !args[0].equals("give") && !args[0].equals("level") && !args[0].equals("loaddraw") && !args[0].equals("reload") && !args[0].equals("testexp") && !args[0].equals("get") && !args[0].equals("random"))){
                sendUsage(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "list":
                    handleList(sender);
                    break;
                case "give":
                    handleGive(sender, args);
                    break;
                case "level":
                    handleLevel(sender, args);
                    break;
                case "testexp":
                    handleTestExp(sender);
                    break;
                case "loaddraw":
                    handleLoadDraw(sender);
                    break;
                case "reload":
                    handleReload(sender);
                    break;
                case "get":
                    handleForgeGetQuality(sender, args);
                    break;
                case "random":
                    handleRandomForge(sender, args);
                    break;
                default:
                    sendUsage(sender);
                    break;
            }
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§7§LNullForge");
        sender.sendMessage("§f");
        sender.sendMessage("§7主命令: /fadmin [...]");
        sender.sendMessage("§7参数:");
        sender.sendMessage("§7 - §flist §7§o#§A§o查看列表所有图纸");
        sender.sendMessage("§7 - §fgive §7§o#§A§o给于指定玩家一个图纸");
        sender.sendMessage("§7 - §flevel §7§o#§A§o设置玩家的锻造等级");
        sender.sendMessage("§7 - §ftestexp §7§o#§A§o获取每级所需多少经验");
        sender.sendMessage("§7 - §floaddraw §7§o#§A§o重新载入图纸信息");
        sender.sendMessage("§7 - §freload §7§o#§A§o重新载入配置");
        sender.sendMessage("§7 - §fget §7§o#§A§o获取指定品质的锻造装备§f> §o(无需材料)(最大浮动)");
        sender.sendMessage("§7 - §frandom §7§o#§A§o获取随机品质的锻造装备§f> §o(无需材料)(随机浮动)");
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage("§b图纸列表:");
        for (String name: DrawManager.getDrawNames()) {
            sender.sendMessage("§c§l图纸名§f: " + name);
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7完整参数:");
            sender.sendMessage("§7 - §ffadmin give <§c§o图纸名字§f> <§c§o玩家名§f>");
            return;
        }
        String fileName = args[1];
        Player targetPlayer = null;
        if (args.length >= 3) {
            targetPlayer = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        }
        if (targetPlayer == null) {
            sender.sendMessage("§c[系统]§a指定的玩家不在线或未提供玩家名!");
            return;
        }
        DrawData drawData = DrawManager.getDrawDataOfFileName(fileName);
        if (drawData == null) {
            sender.sendMessage("§c[系统]§a不存在这个图纸!");
            return;
        }
        targetPlayer.getInventory().addItem(drawData.getDrawItem());
        sender.sendMessage("§c[系统]§a图纸已经给予到" + targetPlayer.getName() + "的背包中");
    }

    private void handleLevel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§7完整参数:");
            sender.sendMessage("§7 - §ffadmin level <§c§o玩家名§f> <§c§o等级§f>");
            return;
        }
        String pname = args[1];
        String rawLevel = args[2];
        if (!isNum(rawLevel)) {
            sender.sendMessage("§c[系统]§a等级必须为数字!");
            return;
        }
        int level = Integer.parseInt(rawLevel);
        if (level < 0 || level > Settings.I.Max_Player_Forge_Level) {
            sender.sendMessage("§c[系统]§a等级不能小于0或者大于最大锻造等级!");
            return;
        }
        if (!PlayerData.pMap.containsKey(pname)) {
            sender.sendMessage("§c[系统]§a该玩家不在线,无法修改等级!");
            return;
        }
        PlayerData pd = PlayerData.pMap.get(pname);
        pd.setLevel(level);
        pd.setExp(0.0);
        PlayerData.pMap.put(pname, pd);
        sender.sendMessage("§c[系统]§a设置等级成功!");
    }

    private void handleTestExp(CommandSender sender) {
        sender.sendMessage("§b每级所需的经验列表:");
        for (int i = 1; i <= Settings.I.Max_Player_Forge_Level; ++i) {
            double needExp = ExpUtil.getNeedExp(i);
            sender.sendMessage("§c" + i + "级§8---------->§e" + needExp);
        }
    }

    private void handleLoadDraw(CommandSender sender) {
        DrawManager.reset();
        Forge.dataManger.getDrawData();
        sender.sendMessage("§c[系统]§a载入图纸信息成功!");
    }

    private void handleReload(CommandSender sender) {
        ConfigurationLoader.loadYamlConfiguration(NullForge.INSTANCE, Settings.class, true);
        MessageLoader.reloadMessages();
        DrawManager.reset();
        Forge.dataManger.getDrawData();
        sender.sendMessage("§c[系统]§a载入配置文件成功!");
    }
    private void handleRandomForge(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7完整参数:");
            sender.sendMessage("§7 - §ffadmin random <§c§o图纸文件名§f> <§c§o玩家名§f> (可选)");
            return;
        }

        String fileName = args[1]; // 获取图纸文件名
        Player targetPlayer = null;

        if (args.length >= 3) {
            targetPlayer = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        }

        if (targetPlayer == null) {
            sender.sendMessage("§c[系统]§a指定的玩家不在线或未提供玩家名!");
            return;
        }

        DrawData drawData = DrawManager.getDrawDataOfFileName(fileName);
        if (drawData == null) {
            sender.sendMessage("§c[系统]§a不存在这个图纸!");
            return;
        }

        // 随机生成品质
        Map<String, Float> forgeChance = Settings.I.Forge_Chance;
        String randomQuality = RandomUtil.probabString(forgeChance);

        // 锻造装备
        ItemStack resultItem = forgeItemWithQuality(targetPlayer, drawData, randomQuality, false); // 设置 isCommand 为 false
        if (resultItem != null) {
            targetPlayer.getInventory().addItem(resultItem);
            sender.sendMessage("§c[系统]§a锻造装备 " + fileName + " (" + randomQuality + ") 已经给予到 " + targetPlayer.getName() + " 的背包中!");
        } else {
            sender.sendMessage("§c[系统]§a锻造失败!");
        }
    }
    private void handleForgeGetQuality(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§7完整参数:");
            sender.sendMessage("§7 - §fget <§c§olevel-1§f> <§c§o图纸文件名§f> <§c§o玩家名§f> (可选)");
            return;
        }

        String quality = args[1]; // 获取指定品质
        String fileName = args[2]; // 获取文件名
        Player targetPlayer = null;

        if (args.length >= 4) {
            targetPlayer = Bukkit.getPlayer(args[3]);
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        }

        if (targetPlayer == null) {
            sender.sendMessage("§c[系统]§a指定的玩家不在线或未提供玩家名!");
            return;
        }

        boolean found = false;
        DrawData drawData = DrawManager.getDrawDataOfFileName(fileName);
        if (drawData == null) {
            sender.sendMessage("§c[系统]§a没有找到名为 " + fileName + " 的图纸!");
            return;
        }

        ItemStack resultItem = forgeItemWithQuality(targetPlayer, drawData, quality, true); // 设置 isCommand 为 true
        if (resultItem == null) {
            sender.sendMessage("§c[系统]§a不存在 " + quality + " 等级的装备!");
            return;
        }
        targetPlayer.getInventory().addItem(resultItem);
        sender.sendMessage("§c[系统]§a锻造装备 " + fileName + " 已经给予到 " + targetPlayer.getName() + " 的背包中!");
    }
    private String generateStrengthBar(int attributeValue, int min, int max) {
        float progress = (float) (attributeValue - min) / (float) (max - min);
        int progressBarValue = (int) (progress * 25);

        StringBuilder progressBar = new StringBuilder("§b[");
        String barColor = "§c"; // 默认颜色
        if (progressBarValue > 20) {
            barColor = "§9";
        } else if (progressBarValue > 15) {
            barColor = "§3";
        } else if (progressBarValue > 10) {
            barColor = "§a";
        } else if (progressBarValue > 5) {
            barColor = "§e";
        }
        for (int i = 0; i < 25; i++) {
            if (i < progressBarValue) {
                progressBar.append(barColor + "|");
            } else {
                progressBar.append("§8|");
            }
        }
        progressBar.append("§b]");
        return progressBar.toString();
    }
    // 锻造逻辑方法
    private ItemStack forgeItemWithQuality(Player player, DrawData drawData, String quality, boolean isCommand) {
        // 获取配置中的锻造品质概率、品质描述文本和品质对应的属性范围
        Map<String, Float> forgeChance = Settings.I.Forge_Chance;
        Map<String, String> attribLevelText = Settings.I.Attrib_Level_Text;
        Map<String, String> forgeAttrib = Settings.I.Forge_Attrib;

        // 检查指定的品质是否有效
        if (!forgeChance.containsKey(quality)) {
            return null; // 如果指定的品质无效，返回 null
        }

        // 获取品质描述和属性范围
        String qualityText = attribLevelText.get(quality); // 品质描述文本
        String attributeRange = forgeAttrib.get(quality); // 品质对应的属性范围

        // 解析属性范围并生成强度值
        String[] rangeParts = attributeRange.split(" => "); // 分割范围，例如 "50 => 70"
        int min = Integer.parseInt(rangeParts[0]); // 最小值
        int max = Integer.parseInt(rangeParts[1]); // 最大值

        int attributeValue;
        if (isCommand) {
            attributeValue = max; // 如果是通过指令获取的装备，强度值设置为最大值
        } else {
            Random random = new Random();
            attributeValue = random.nextInt(max - min + 1) + min; // 在最小值和最大值之间随机生成一个值
        }

        // 生成强度条
        String strengthBar = generateStrengthBar(attributeValue, min, max);

        // 获取最终装备并调整属性
        ItemStack resultItem = drawData.getResult().clone(); // 克隆图纸的结果物品
        ItemMeta resultMeta = resultItem.getItemMeta(); // 获取物品的元数据

        // 保留原始物品的 lore
        List<String> originalLore = resultMeta.hasLore() ? resultMeta.getLore() : new ArrayList<>();
        List<String> adjustedAttrib = new ArrayList<>(originalLore); // 复制原始 lore 到新的列表

        // 调整装备属性
        for (String attribute : drawData.getAttrib()) {
            // 假设属性格式为 "属性名称: (基础值)" 或 "属性名称: (最小值)-(最大值)"
            Pattern pattern = Pattern.compile("\\((\\d+)-(\\d+)\\)|\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(attribute);
            while (matcher.find()) {
                int baseValue;
                int maxValue = 0;
                if (matcher.group(1) != null && matcher.group(2) != null) {
                    // 范围值
                    baseValue = Integer.parseInt(matcher.group(1));
                    maxValue = Integer.parseInt(matcher.group(2));
                } else {
                    // 单个值
                    baseValue = Integer.parseInt(matcher.group(3));
                }
                int adjustedValue = baseValue + (int) (baseValue * (attributeValue / 100.0));
                if (maxValue > 0) {
                    int adjustedMaxValue = maxValue + (int) (maxValue * (attributeValue / 100.0));
                    attribute = attribute.replaceAll("\\(" + baseValue + "-\\d+\\)", "(" + adjustedValue + "-" + adjustedMaxValue + ")");
                } else {
                    attribute = attribute.replaceAll("\\(" + baseValue + "\\)", String.valueOf(adjustedValue));
                }
            }
            adjustedAttrib.add(attribute); // 添加调整后的属性到 lore
        }

        // 添加品质信息和锻造者信息
        adjustedAttrib.add(qualityText); // 添加品质描述
        adjustedAttrib.add(Settings.I.Attrib_Perce_Text + strengthBar); // 添加强度条
        adjustedAttrib.add(Settings.I.ForgeOwner.replace("<player>", player.getName())); // 添加锻造者信息
        java.text.DateFormat df = java.text.DateFormat.getDateInstance(2, java.util.Locale.CHINA);
        java.text.DateFormat df2 = java.text.DateFormat.getTimeInstance(2, java.util.Locale.CHINA);
        String date = df.format(new Date()) + " " + df2.format(new Date());
        adjustedAttrib.add(Settings.I.ForgeDate.replace("<date>", date)); // 添加锻造日期

        // 更新物品描述并返回
        resultMeta.setLore(adjustedAttrib); // 更新物品的描述
        resultItem.setItemMeta(resultMeta); // 应用元数据
        return resultItem; // 返回最终的装备
    }

    public boolean isNum(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}