package com.github.nullforge.Config;

import com.github.nullforge.NullForge;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DBConfig {
    public static String URL;
    public static String USER;
    public static String PASSWORD;
    public static String TABLE_PREFIX;
    public static String DRAW_TABLE;
    public static String PLAYER_TABLE;

    public static void loadConfig() {
        File dataFolder = NullForge.INSTANCE.getDataFolder();
        File configFile = new File(dataFolder, "database.yml");
        if (!configFile.exists()) {
            NullForge.INSTANCE.saveResource("database.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String host = config.getString("mysql_host");
        String port = config.getString("mysql_port");
        String database = config.getString("mysql_database");
        URL = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai", host, port, database);
        USER = config.getString("mysql_user");
        PASSWORD = config.getString("mysql_password");
        TABLE_PREFIX = config.getString("mysql_table_prefix");
        String drawTable = config.getString("mysql_draw_table");
        DRAW_TABLE = TABLE_PREFIX + drawTable;
        String playerTable = config.getString("mysql_player_table");
        PLAYER_TABLE = TABLE_PREFIX + playerTable;
    }
}
