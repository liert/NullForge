package com.github.nullforge.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public class ConfigurationLoader {
    public static Map<String, String> messages = new HashMap<>();
    public static Map<String, MessageFormat> format_cache = new HashMap<>(32);

    public static void loadMessages(FileConfiguration cfg) {
        messages.clear();
        for (String s : cfg.getConfigurationSection("Messages").getKeys(false)) {
            messages.put(s, cfg.getString("Messages." + s));
        }
    }

    public static String lang(String key) {
        return messages.get(key);
    }

    public static String format(String key, Object... args) {
        MessageFormat cache = format_cache.get(key);
        if (cache != null) {
            return cache.format(args);
        }
        cache = new MessageFormat(ConfigurationLoader.lang(key));
        format_cache.put(key, cache);
        return cache.format(args);
    }

    public static <T> T loadYamlConfiguration(Plugin plugin, Class<T> type, boolean autoGen) {
        String className = type.getSimpleName().toLowerCase();
        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, className + ".yml");
        if (!configFile.exists()) {
            if (!autoGen) {
                return null;
            }
            if (!dataFolder.exists()) {
                boolean ignore = dataFolder.mkdirs();
            }
            plugin.saveResource(className + ".yml", false);
        }
        try {
            FileInputStream fin = new FileInputStream(configFile);
            Object object = new Yaml(new CustomClassLoaderConstructor(type.getClassLoader())).loadAs(fin, type);
            fin.close();
            return (T) object;
        } catch (IOException ex) {
            return null;
        }
    }

    public static boolean saveYamlConfiguration(Plugin plugin, Object obj, boolean overWrite) {
        File configFile;
        String className = obj.getClass().getSimpleName().toLowerCase();
        File dataFolder = plugin.getDataFolder();
        configFile = new File(dataFolder, className + ".yml");

        if (configFile.exists()) {
            if (overWrite) {
                // 如果允许覆盖，则删除现有文件
                if (!configFile.delete()) {
                    return false; // 删除失败
                }
            } else {
                return false; // 不允许覆盖且文件已存在
            }
        }

        try {
            if (configFile.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(configFile);
                Yaml yaml = new Yaml(new CustomClassLoaderConstructor(obj.getClass().getClassLoader()));
                fileOutputStream.write(yaml.dump(obj).getBytes(StandardCharsets.UTF_8));
                fileOutputStream.close();
                return true; // 成功保存配置
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // 打印异常堆栈跟踪
        }
        return false;
    }
}