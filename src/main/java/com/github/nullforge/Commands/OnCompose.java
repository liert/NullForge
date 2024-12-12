package com.github.nullforge.Commands;

import com.github.nullforge.GUI.ComposeGUI;
import com.github.nullforge.Main;
import com.github.nullforge.NullForge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnCompose
        implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean PlayerStatus = sender instanceof Player;
        if (!PlayerStatus) {
            NullForge.INSTANCE.getLogger().info("必须是玩家执行!");
            return true;
        }
        Player p = (Player)sender;
        p.openInventory(ComposeGUI.getGUI());
        return true;
    }
}

