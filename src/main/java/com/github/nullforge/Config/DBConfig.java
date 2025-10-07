package com.github.nullforge.Config;

import com.github.nullforge.NullForge;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DBConfig {
    public static String DATABASE_TYPE;
    public static String MYSQL_URL;
    public static String SQLITE_URL;
    public static String USER;
    public static String PASSWORD;
    public static String MYSQL_TABLE_PREFIX;
    public static String SQLITE_TABLE_PREFIX;
    public static String DRAW_TABLE;
    public static String PLAYER_TABLE;
    public static String DATABASE_TABLE;

    public static void loadConfig() {
        File dataFolder = NullForge.INSTANCE.getDataFolder();
        File configFile = new File(dataFolder, "database.yml");
        if (!configFile.exists()) {
            NullForge.INSTANCE.saveResource("database.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        DATABASE_TYPE = config.getString("database_type", "SQLite");
        String host = config.getString("mysql_host");
        String port = config.getString("mysql_port");
        String database = config.getString("mysql_database");
        // MySQL 用于兼容图纸转换功能
        MYSQL_URL = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai", host, port, database);
        USER = config.getString("mysql_user");
        PASSWORD = config.getString("mysql_password");
        MYSQL_TABLE_PREFIX = config.getString("mysql_table_prefix", "forge_");
        DRAW_TABLE = MYSQL_TABLE_PREFIX + config.getString("mysql_draw_table", "draw");
        PLAYER_TABLE = MYSQL_TABLE_PREFIX + config.getString("mysql_player_table", "player");
        if ("MySQL".equalsIgnoreCase(DATABASE_TYPE)) {
            DATABASE_TABLE = MYSQL_TABLE_PREFIX + config.getString("mysql_database_table", "database");
        } else if ("SQLite".equalsIgnoreCase(DATABASE_TYPE)) {
            String sqliteFile = config.getString("sqlite_file", "database.db");
            SQLITE_URL = "jdbc:sqlite:" + new File(dataFolder, sqliteFile).getAbsolutePath();
            SQLITE_TABLE_PREFIX = config.getString("sqlite_table_prefix", "forge_");
            DATABASE_TABLE = SQLITE_TABLE_PREFIX + config.getString("sqlite_database_table", "database");
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + DATABASE_TYPE);
        }
    }
}
