package com.github.nullforge.Listeners;

import com.github.nullbridge.util.ItemStackUtils;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.Event.PlayerForgeItemEvent;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ExpUtil;
import java.text.DecimalFormat;
import java.util.Map;

import net.md_5.bungee.api.chat.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OnPlayerForgeItem implements Listener {
    @EventHandler
    public void forgeItem(PlayerForgeItemEvent e) {
        Player player = e.getPlayer();
        PlayerData pd = PlayerData.pMap.get(player.getName());

        if (pd.getForgeCount(e.getDraw().getFileName()) >= Settings.I.Large_Guarantee_Threshold - 1) {
            pd.resetForgeCount(e.getDraw().getFileName());
        } else {
            pd.addForgeCount(e.getDraw().getFileName());
        }

        if (!e.isFinalForge()) {
            return;
        }

        if (pd.getLevel() < Settings.I.Max_Player_Forge_Level) {
            // 直接使用事件中的总经验
            double totalExp = e.getTotalExp();

            // 累加经验
            double PlayerExp = pd.getExp();
            double newExp = PlayerExp + totalExp;

            StringBuilder messageBuilder = new StringBuilder();

            // 检查是否升级
            double needExp = ExpUtil.getNeedExp(player);
            if (newExp >= needExp) {
                int levelsGained = 0;
                while (newExp >= needExp) {
                    levelsGained++;
                    newExp -= needExp;
                    pd.setLevel(pd.getLevel() + 1);
                    needExp = ExpUtil.getNeedExp(player);
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

            // 构建经验获得消息
            messageBuilder.append("\n").append(MessageLoader.getMessage("forge-exp-gain")
                    .replace("%player%", player.getName())
                    .replace("%exp%", new DecimalFormat("###.00").format(totalExp)));

            // 构建经验进度消息
            messageBuilder.append("\n").append(MessageLoader.getMessage("forge-exp-progress")
                    .replace("%currentexp%", new DecimalFormat("###.00").format(pd.getExp()))
                    .replace("%needexp%", new DecimalFormat("###.00").format(needExp)));

            // 发送消息
            player.sendMessage(messageBuilder.toString());
        } else {
            player.sendMessage(MessageLoader.getMessage("forge-max-level"));
        }

        // 发送全服公告
        String plainText;
        if (e.getItems().size() == 1) {
            plainText = MessageLoader.getMessage("forge-broadcast");
        } else {
            plainText = MessageLoader.getMessage("forge-broadcast-batch");
        }

        plainText = plainText.replace("%player%", player.getName())
                .replace("%totel%", String.valueOf(e.getItems().size()))
                .replace("%quality%", e.getQualityInfo().keySet().iterator().next());
        String[] parts = plainText.split("%itemname%");

        ComponentBuilder builder = new ComponentBuilder(parts[0]);
        builder.append(e.getDraw().getDisplayName());

        // 创建悬浮提示内容
        if (e.getItems().size() == 1) {
            ItemStack itemStack = e.getItems().get(0);
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(ItemStackUtils.toJson(itemStack)).create()));
        } else {
            StringBuilder hoverText = new StringBuilder("品质详情:\n");
            for (Map.Entry<String, Integer> entry : e.getQualityInfo().entrySet()) {
                hoverText.append(" - ").append(entry.getKey()).append(" ").append(entry.getValue()).append("件\n");
            }
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText.toString()).create()));
        }
        builder.append(parts[1]).event((HoverEvent) null);

        // 广播消息
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(builder.create());
        }

        // 保存玩家数据
        try {
            pd.savePlayer();
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§c玩家数据保存是失败！");
        }
    }
}