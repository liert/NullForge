package com.github.nullforge.db;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;

public class SQLite extends DBHelper {
    private final String url;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "SQLite Driver not found");
        }
    }

    public SQLite(String url) {
        this.url = url;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url);
    }
}
