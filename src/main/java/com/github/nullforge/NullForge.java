package com.github.nullforge;

import com.github.nullbridge.manager.InventoryEventManager;
import com.github.nullbridge.manager.InventoryManager;
import com.github.nullbridge.manager.ItemManager;
import com.github.nullforge.Commands.GemComposeCommand;
import com.github.nullforge.Commands.OnAdminCommands;
import com.github.nullforge.Commands.OnCompose;
import com.github.nullforge.Commands.OnForge;
import com.github.nullforge.Config.ConfigurationLoader;
import com.github.nullforge.Config.DBConfig;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.Data.YamlManager;
import com.github.nullforge.GUI.BaoshiGUI;
import com.github.nullforge.Listeners.OnPlayerBreakBlock;
import com.github.nullforge.Listeners.OnPlayerClickInv;
import com.github.nullforge.Listeners.OnPlayerForgeItem;
import com.github.nullforge.Listeners.OnPlayerInteract;
import com.github.nullforge.Listeners.OnPlayerJoin;
import com.github.nullforge.PlaceHolder.NameHolder;
import java.io.File;
import java.util.Random;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NullForge extends JavaPlugin {
    public static NullForge INSTANCE;
    public static YamlManager dataManger;
    public static Economy vault;
    public static Random rd;
    private static ItemManager itemManager;

    static {
        rd = new Random();
    }

    public NullForge() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MessageLoader.initialize(NullForge.INSTANCE);
        DBConfig.loadConfig();
        EnableListener();
        itemManager = new ItemManager();
        InventoryManager.init(this);
        this.setupEconomy();
        ConfigurationLoader.loadYamlConfiguration(NullForge.INSTANCE, Settings.class, true);
        this.initFolder();
        dataManger = new YamlManager();
        dataManger.loadDrawData();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NameHolder(NullForge.INSTANCE).register();
        }
        // 再次打印一条确认信息
        Bukkit.getConsoleSender().sendMessage("§8| §a插件加载成功!");
        Bukkit.getConsoleSender().sendMessage("§8=============================================");
    }

    @Override
    public void onDisable() {
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
        InventoryEventManager eventManager = new InventoryEventManager(this);
        NullForge.INSTANCE.getCommand("dz").setExecutor(new OnForge());
        NullForge.INSTANCE.getCommand("hc").setExecutor(new OnCompose());
        NullForge.INSTANCE.getCommand("fadmin").setExecutor(new OnAdminCommands());
        NullForge.INSTANCE.getCommand("baoshi").setExecutor(new GemComposeCommand());
        Bukkit.getPluginManager().registerEvents(new OnPlayerBreakBlock(), NullForge.INSTANCE);
        Bukkit.getPluginManager().registerEvents(new OnPlayerClickInv(), NullForge.INSTANCE);
        Bukkit.getPluginManager().registerEvents(new OnPlayerForgeItem(), NullForge.INSTANCE);
        Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(), NullForge.INSTANCE);
        Bukkit.getPluginManager().registerEvents(new OnPlayerInteract(), NullForge.INSTANCE);
        Bukkit.getPluginManager().registerEvents(new BaoshiGUI(), NullForge.INSTANCE);
    }

    private void initFolder() {
        try {
            File draw;
            File players;
            File dataFolder = NullForge.INSTANCE.getDataFolder();
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

    public static ItemManager getItemManager() {
        return itemManager;
    }
}

