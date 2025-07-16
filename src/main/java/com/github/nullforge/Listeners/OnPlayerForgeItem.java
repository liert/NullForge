package com.github.nullforge.Listeners;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ExpUtil;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPlayerForgeItem implements Listener {
    @EventHandler
    public void forgeItem(PlayerForgeItemEvent e) {
        Player p = e.getPlayer();
        PlayerData pd = PlayerData.pMap.get(p.getName());
        if (pd.getLevel() >= Settings.I.Max_Player_Forge_Level) {
            p.sendMessage(MessageLoader.getMessage("forge-max-level"));
            return;
        }

        // 直接使用事件中的总经验
        double totalExp = e.getTotalExp();

        // 累加经验
        double PlayerExp = pd.getExp();
        double newExp = PlayerExp + totalExp;

        StringBuilder messageBuilder = new StringBuilder();

        // 检查是否升级
        double needExp = ExpUtil.getNeedExp(p);
        if (newExp >= needExp) {
            int levelsGained = 0;
            while (newExp >= needExp) {
                levelsGained++;
                newExp -= needExp;
                pd.setLevel(pd.getLevel() + 1);
                needExp = ExpUtil.getNeedExp(p);
            }
            pd.setExp(newExp);

            // 构建升级消息
            if (levelsGained == 1) {
                messageBuilder.append(MessageLoader.getMessage("forge-level-up")
                        .replace("%level%", String.valueOf(pd.getLevel())));
            } else {
                messageBuilder.append(MessageLoader.getMessage("forge-level-up-multi")
                        .replace("%levels%", String.valueOf(levelsGained))
                        .replace("%newlevel%", String.valueOf(pd.getLevel())));
            }
        } else {
            pd.setExp(newExp);
        }
        // 保存玩家数据
        try {
            pd.savePlayer();
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§c玩家数据保存是失败！");
        }


        // 构建经验获得消息
        messageBuilder.append("\n").append(MessageLoader.getMessage("forge-exp-gain")
                .replace("%player%", p.getName())
                .replace("%exp%", new DecimalFormat("###.00").format(totalExp)));

        // 构建经验进度消息
        messageBuilder.append("\n").append(MessageLoader.getMessage("forge-exp-progress")
                .replace("%currentexp%", new DecimalFormat("###.00").format(pd.getExp()))
                .replace("%needexp%", new DecimalFormat("###.00").format(needExp)));

        // 发送消息
        p.sendMessage(messageBuilder.toString());
    }
}