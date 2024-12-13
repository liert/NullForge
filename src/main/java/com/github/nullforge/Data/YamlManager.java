package com.github.nullforge.Data;

import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.ItemString;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class YamlManager implements DataManagerImpl {
    @Override
    public void getPlayerData(Player p) {
        PlayerData pd;
        File playerDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "players");
        File playerConfigFile = new File(playerDataFolder, p.getName() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
        if (playerConfigFile.exists()) {
            int level = playerConfig.getInt("level");
            double exp = playerConfig.getDouble("exp");
            List<String> learn = playerConfig.getStringList("learn");
            pd = new PlayerData(level, exp, learn);
        } else {
            playerConfig.set("level", 0);
            playerConfig.set("exp", 0.0);
            playerConfig.set("learn", new ArrayList<>());
            try {
                playerConfig.save(playerConfigFile);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            pd = new PlayerData(0, 0.0, new ArrayList<>());
        }
        PlayerData.pMap.put(p.getName(), pd);
    }

    @Override
    public void savePlayerData(Player p) {
        if (!PlayerData.pMap.containsKey(p.getName())) {
            return;
        }
        PlayerData pd = PlayerData.pMap.get(p.getName());
        File playerDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "players");
        File playerConfigFile = new File(playerDataFolder, p.getName() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
        playerConfig.set("level", pd.getLevel());
        playerConfig.set("exp", pd.getExp());
        playerConfig.set("learn", pd.getLearn());
        try {
            playerConfig.save(playerConfigFile);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getDrawData() {
        File drawDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "draw");
        // 确保目录存在
        if (!drawDataFolder.exists()) {
            boolean ignore = drawDataFolder.mkdirs();
            Bukkit.getConsoleSender().sendMessage("§c[系统]§a创建了 'draw' 文件夹.");
        }
        NullForge.INSTANCE.saveResource("draw/example.yml", false);
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
            Bukkit.getConsoleSender().sendMessage("§8| §a§lNullForge §8- §aVersion: §b1.0.0");
            Bukkit.getConsoleSender().sendMessage("§8=============================================");

            // 打印加载成功的文件名，并添加序号
            int index = 1;
            for (String s : DrawManager.getDrawNames()) {
                Bukkit.getConsoleSender().sendMessage(String.format("§8|§a§l%2d. §r%s §a§l[已加载]", index++, s));
            }

            // 打印底部边框和总结信息
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
            Bukkit.getConsoleSender().sendMessage(String.format("§8| §a共加载了 %d 个图纸, 耗时 %d 毫秒", DrawManager.getDrawCount(), diff));
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
        } else {
            // 如果没有加载任何文件，则打印提示信息
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
            Bukkit.getConsoleSender().sendMessage("§c| §a没有任何有效的图纸被加载.");
            Bukkit.getConsoleSender().sendMessage("§8=============================================");
        }
    }

    @Override
    public void saveDrawData() {
        for (DrawData drawData : DrawManager.getDrawData()) {
            YamlConfiguration drawConfig = YamlConfiguration.loadConfiguration(drawData.getFile());
            drawConfig.set("gem", ItemString.getString(drawData.getGem()));
            List<ItemStack> list = drawData.getFormula();
            StringBuilder sb = new StringBuilder();
            for (ItemStack item : list) {
                sb.append(ItemString.getString(item)).append(",");
            }
            drawConfig.set("formula", sb.toString());
            drawConfig.set("result", ItemString.getString(drawData.getResult()));
            drawConfig.set("gemlevel", drawData.getNeedGemLevel());
            drawConfig.set("playerlevel", drawData.getNeedPlayerLevel());
            drawConfig.set("detail", drawData.getDetail());
            drawConfig.set("attrib", drawData.getAttrib());
        }
    }

    @Override
    public void delDraw(String name) {
        File drawDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "draw");
        File drawConfigFile = new File(drawDataFolder, name + ".yml");
        if (drawConfigFile.exists()) {
            boolean ignore = drawConfigFile.delete();
        }
    }

    @Override
    public String getDrawName(String name) {
        File drawDataFolder = new File(NullForge.INSTANCE.getDataFolder(), "draw");
        File drawConfigFile = new File(drawDataFolder, name + ".yml");
        YamlConfiguration drawConfig = YamlConfiguration.loadConfiguration(drawConfigFile);
        return drawConfig.getString("name");
    }
}

