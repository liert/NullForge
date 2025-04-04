package com.github.nullforge.Listeners;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.Forge;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ExpUtil;
import java.text.DecimalFormat;
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

        int gemLevel = e.getDraw().getNeedGemLevel();
        if (!Settings.I.Forge_Exp.containsKey(gemLevel)) {
            return;
        }

        int baseExp = Settings.I.Forge_Exp.get(gemLevel);
        int floatExp = Forge.rd.nextInt(Settings.I.Forge_Exp_Float);
        double expFloat = (double) baseExp * ((double) floatExp / 100.0);
        double exp = Forge.rd.nextBoolean() ? (double) baseExp + expFloat : (double) baseExp - expFloat;

        // 如果是批量锻造，累加经验到事件对象中
        if (e.isBatchForge()) {
            e.addExp(exp);
            return;
        }

        // 如果不是批量锻造，直接处理经验
        double totalExp = exp;

        // 使用 MessageLoader.getMessage() 加载消息并替换占位符
        String playerName = p.getName();
        String gainExpMessage = MessageLoader.getMessage("forge-exp-gain")
                .replace("%player%", playerName)
                .replace("%exp%", new DecimalFormat("###.00").format(totalExp));
        p.sendMessage(gainExpMessage);

        double PlayerExp = pd.getExp();
        double needExp = ExpUtil.getNeedExp(p);
        if (PlayerExp + totalExp >= needExp) {
            pd.setLevel(pd.getLevel() + 1);
            pd.setExp(PlayerExp + totalExp - needExp);

            // 发送升级成功的消息
            String levelUpMessage = MessageLoader.getMessage("forge-level-up")
                    .replace("%level%", String.valueOf(pd.getLevel()));
            p.sendMessage(levelUpMessage);
        } else {
            pd.setExp(PlayerExp + totalExp);
        }

        needExp = ExpUtil.getNeedExp(p);
        // 发送当前经验进度的消息
        String progressMessage = MessageLoader.getMessage("forge-exp-progress")
                .replace("%currentexp%", new DecimalFormat("###.00").format(pd.getExp()))
                .replace("%needexp%", new DecimalFormat("###.00").format(needExp));
        p.sendMessage(progressMessage);
    }
}