package com.github.nullforge.Commands;

import com.github.nullforge.GUI.BaoshiGUI;
import com.github.nullforge.NullForge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemComposeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查是否是玩家执行命令
        if (!(sender instanceof Player)) {
            // 如果不是玩家，记录日志并返回
            NullForge.INSTANCE.getLogger().info("必须是玩家执行!");
            return true;
        }

        // 将CommandSender强制转换为Player
        Player player = (Player) sender;

        // 打开宝石合成界面
        player.openInventory(BaoshiGUI.getGUI());

        return true;
    }
}