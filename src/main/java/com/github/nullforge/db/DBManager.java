package com.github.nullforge.db;

import com.github.nullforge.Config.DBConfig;
import com.github.nullforge.Utils.TransformUtils;

import java.util.List;
import java.util.Map;

public class DBManager {
    private static DBHelper helper;

    public static void initialize() {
        DBConfig.loadConfig();
        TransformUtils.initialize();
        if ("MySQL".equalsIgnoreCase(DBConfig.DATABASE_TYPE)) {
            helper = new MySQL(DBConfig.MYSQL_URL, DBConfig.USER, DBConfig.PASSWORD);
        } else if ("SQLite".equalsIgnoreCase(DBConfig.DATABASE_TYPE)) {
            helper = new SQLite(DBConfig.SQLITE_URL);
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + DBConfig.DATABASE_TYPE);
        }
    }

    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        return helper.executeQuery(sql, params);
    }

    public static int executeUpdate(String sql, Object... params) {
        return helper.executeUpdate(sql, params);
    }
}
