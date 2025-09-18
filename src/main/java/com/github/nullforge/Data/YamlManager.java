package com.github.nullforge.Data;

import com.github.nullforge.Config.GlobalConfig;
import com.github.nullforge.NullForge;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class YamlManager {
    public void loadPlayerData(Player p) {
        File playerDataFolder = GlobalConfig.getPlayerFolder();
        File playerConfigFile = new File(playerDataFolder, p.getName() + ".yml");
        PlayerData playerData;
        if (playerConfigFile.exists()) {
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
            int level = playerConfig.getInt("level");
            double exp = playerConfig.getDouble("exp");
            List<String> learn = playerConfig.getStringList("learn");
            Map<String, Object> forgeRecord = new HashMap<>();
            if (playerConfig.contains("ForgeRecord")) {
                forgeRecord = playerConfig.getConfigurationSection("ForgeRecord").getValues(false);
            }
            playerData = new PlayerData(p.getName(), level, exp, learn, forgeRecord);
        } else {
            try {
                playerData = new PlayerData(p.getName());
                playerData.savePlayer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        PlayerData.pMap.put(p.getName(), playerData);
    }

    public void savePlayerData(Player p) {
        if (!PlayerData.pMap.containsKey(p.getName())) {
            return;
        }
        try {
            PlayerData pd = PlayerData.pMap.get(p.getName());
            pd.savePlayer();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadPlayerData() {
        PlayerData.pMap.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player);
            loadPlayerData(player);
        }
    }

    public void loadDrawData() {
        File drawDataFolder = GlobalConfig.getDrawFolder();
        File defaultDrawFile = new File(drawDataFolder, "example.yml");
        if (!defaultDrawFile.exists()) {
            NullForge.INSTANCE.saveResource("draw/example.yml", true);
        }
        // 列出并加载现有的 .yml 文件
        File[] files = drawDataFolder.listFiles(pathname -> {
            String name = pathname.getName();
            return name.toLowerCase().endsWith(".yml");
        });
        if (files == null || files.length == 0) {
            Bukkit.getConsoleSender().sendMessage("§c[系统]§a没有找到任何图纸配置文件!");
            return;
        }

        Date first = new Date();
        for (File file : files) {
            try {
                DrawData.CreateDrawData(file);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§c[错误]§a加载文件 " + file.getName() + " 时发生异常: " + e.getMessage());
                e.printStackTrace();
            }
        }

        long diff = new Date().getTime() - first.getTime();
        if (DrawManager.getDrawCount() > 0) {
            // 打印顶部边框
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
            Bukkit.getConsoleSender().sendMessage("§8| §a§lNullForge §8- §aVersion: §b" + NullForge.INSTANCE.getDescription().getVersion());

            // 打印底部边框和总结信息
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage(String.format("§8| §a共加载了 %d 个图纸, 耗时 %d 毫秒", DrawManager.getDrawCount(), diff));
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
        } else {
            // 如果没有加载任何文件，则打印提示信息
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
            Bukkit.getConsoleSender().sendMessage("§c| §a没有任何有效的图纸被加载.");
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
        }
    }

    public void saveDrawData() {
        for (DrawData drawData : DrawManager.getDrawData()) {
            try {
                drawData.saveDraw();
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage(drawData.getDisplayName() + " §c保存失败");
            }
        }
    }

    public void reloadDrawData() {
        DrawManager.reset();
        loadDrawData();
    }

    public void delDraw(String name) {
        File drawDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "draw");
        File drawConfigFile = new File(drawDataFolder, name + ".yml");
        if (drawConfigFile.exists()) {
            boolean ignore = drawConfigFile.delete();
        }
    }

    public String getDrawName(String name) {
        File drawDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "draw");
        File drawConfigFile = new File(drawDataFolder, name + ".yml");
        YamlConfiguration drawConfig = YamlConfiguration.loadConfiguration(drawConfigFile);
        return drawConfig.getString("name");
    }

    public void reload() {
        reloadDrawData();
        reloadPlayerData();
    }
}

