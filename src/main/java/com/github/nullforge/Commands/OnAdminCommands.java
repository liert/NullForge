package com.github.nullforge.Commands;

import com.github.nullcore.Config.ConfigurationLoader;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.DrawManager;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Main;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ExpUtil;
import com.github.nullforge.Utils.GemUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OnAdminCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fadmin")) {
            // 检查是否具有管理员权限
            if (!sender.hasPermission("nullforge.admin")) {
                sender.sendMessage("§c[系统]§a你没有足够的权限使用此命令!");
                return true;
            }

            if (args.length == 0 || (!args[0].equals("list") && !args[0].equals("give") && !args[0].equals("gem") && !args[0].equals("level") && !args[0].equals("loaddraw") && !args[0].equals("reload") && !args[0].equals("testexp"))) {
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
                case "gem":
                    handleGem(sender, args);
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
        sender.sendMessage("§7 - §fgem §7§o#§A§o给于指定玩家一个指定等级的锻造宝石");
        sender.sendMessage("§7 - §flevel §7§o#§A§o设置玩家的锻造等级");
        sender.sendMessage("§7 - §ftestexp §7§o#§A§o获取每级所需多少经验");
        sender.sendMessage("§7 - §floaddraw §7§o#§A§o重新载入图纸信息");
        sender.sendMessage("§7 - §freload §7§o#§A§o重新载入配置");
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

    private void handleGem(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§7完整参数:");
            sender.sendMessage("§7 - §ffadmin gem <§c§oID§f> <§c§o等级§f> <§c§o玩家名§f>");
            return;
        }

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

        String rawId = args[1];
        String rawLevel = args[2];

        if (!isNum(rawId) || !isNum(rawLevel)) {
            sender.sendMessage("§c[系统]§a默认ID:388");
            return;
        }

        int id = Integer.parseInt(rawId);
        int level = Integer.parseInt(rawLevel);

        if (!Settings.I.Gem_Lore.containsKey(id)) {
            sender.sendMessage("§c[系统]§a不存在该宝石的配置!");
            return;
        }

        ItemStack item = GemUtil.makeGem(id, level);
        targetPlayer.getInventory().addItem(item);
        sender.sendMessage("§c[系统]§a宝石已经给予到" + targetPlayer.getName() + "的背包中");
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
        Main.dataManger.getDrawData();
        sender.sendMessage("§c[系统]§a载入图纸信息成功!");
    }

    private void handleReload(CommandSender sender) {
        ConfigurationLoader.loadYamlConfiguration(NullForge.INSTANCE, Settings.class, true);
        MessageLoader.reloadMessages();
        sender.sendMessage("§c[系统]§a载入配置文件成功!");
    }

    public boolean isNum(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}