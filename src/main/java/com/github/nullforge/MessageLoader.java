package com.github.nullforge;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.logging.Level;

public class MessageLoader {
    private static FileConfiguration messagesConfig = null;
    private static File messagesFile = null;

    public static void initialize(JavaPlugin plugin) {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        // 如果文件不存在，则尝试从JAR中复制一份
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        // 加载配置文件
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String key) {
        // 从配置文件中获取消息，如果找不到则返回key本身
        String message = messagesConfig.getString(key, key);
        if (message.equals(key)) {
            // 如果找不到对应的键，记录一个警告
            JavaPlugin.getProvidingPlugin(MessageLoader.class).getLogger().log(Level.WARNING, "Message key not found: " + key);
        }
        return message;
    }

    // 重新加载配置文件
    public static void reloadMessages() {
        if (messagesFile != null) {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }
    }
}