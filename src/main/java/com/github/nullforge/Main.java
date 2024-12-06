package com.github.nullforge;

import com.github.nullforge.Commands.OnAdminCommands;
import com.github.nullforge.Commands.OnCompose;
import com.github.nullforge.Commands.OnForge;
import com.github.nullforge.Config.ConfigurationLoader;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.DataManagerImpl;
import com.github.nullforge.Data.YamlManager;
import com.github.nullforge.Listeners.OnPlayerBreakBlock;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.Listeners.OnPlayerForgeItem;
import com.github.nullforge.Listeners.OnPlayerInteract;
import com.github.nullforge.Listeners.OnPlayerJoin;
import com.github.nullforge.PlaceHolder.NameHolder;
import com.github.nullforge.Utils.MMItemManager;
import io.lumine.xikage.mythicmobs.MythicMobs;

import java.io.File;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main {
    public static JavaPlugin instance;
    public static DataManagerImpl dataManger;
    public static Economy vault;
    public static Random rd;
    private static MythicMobs mythicMobs;
    private static MMItemManager itemManager;

    static {
        rd = new Random();
    }

    public Main(JavaPlugin plugin) {
        instance = plugin;
    }

    public void start() {
        MessageLoader.initialize(instance);
        EnableListener();
        mythicMobs = MythicMobs.inst();
        itemManager = new MMItemManager();
        this.setupEconomy();
        ConfigurationLoader.loadYamlConfiguration(instance, Settings.class, true);
        this.initFolder();
        dataManger = new YamlManager();
        dataManger.getDrawData();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NameHolder(instance, "forge").hook();
        }
        // 再次打印一条确认信息
        Bukkit.getConsoleSender().sendMessage("§8| §a插件加载成功!");
        Bukkit.getConsoleSender().sendMessage("§8=============================================");
    }

    public void stop() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            dataManger.savePlayerData(p);
        }
        dataManger.saveDrawData();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            vault = economyProvider.getProvider();
        }
    }


    private void EnableListener() {
        instance.getCommand("dz").setExecutor(new OnForge());
        instance.getCommand("hc").setExecutor(new OnCompose());
        instance.getCommand("fadmin").setExecutor(new OnAdminCommands());
        Bukkit.getPluginManager().registerEvents(new OnPlayerBreakBlock(), instance);
        Bukkit.getPluginManager().registerEvents(new OnPlayerClickInv(), instance);
        Bukkit.getPluginManager().registerEvents(new OnPlayerForgeItem(), instance);
        Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(), instance);
        Bukkit.getPluginManager().registerEvents(new OnPlayerInteract(), instance);
    }

    private void initFolder() {
        try {
            File draw;
            File players;
            File dataFolder = instance.getDataFolder();
            if (!dataFolder.exists()) {
                boolean ignore = dataFolder.mkdir();
            }
            if (!(players = new File(dataFolder, "players")).exists()) {
                boolean ignore = players.mkdir();
            }
            if (!(draw = new File(dataFolder, "draw")).exists()) {
                boolean ignore = draw.mkdir();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MMItemManager getMMItemManager() {
        return itemManager;
    }

    public static MythicMobs getMythicMobs() {
        return mythicMobs;
    }

}

