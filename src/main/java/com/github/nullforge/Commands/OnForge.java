package com.github.nullforge.Commands;

import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.GUI.SwitchDrawGUI;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnForge implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean PlayerStatus = sender instanceof Player;
        if (!PlayerStatus) {
            NullForge.INSTANCE.getLogger().info("必须是玩家执行!");
            return true;
        }
        Player p = (Player)sender;
        PlayerData pd = PlayerData.pMap.get(p.getName());
        if (pd.getLearn().isEmpty()) {
            p.sendMessage(MessageLoader.getMessage("draw-no-learn"));
            return true;
        }
        p.openInventory(SwitchDrawGUI.getGUI(p, 0));
        return true;
    }
}

