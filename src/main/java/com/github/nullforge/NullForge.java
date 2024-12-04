package com.github.nullforge;

import com.github.nullcore.Log;
import com.github.nullcore.NullCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;

public class NullForge extends NullCore {
    private Main main;
    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        registerPlugin(this);
        try {
            Class<?> MainClass = Class.forName("com.github.nullforge.Main");
            Constructor<?> constructor = MainClass.getConstructor(JavaPlugin.class);
            main = (Main) constructor.newInstance(this);
            MainClass.getDeclaredMethod("start").invoke(main);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            Class<?> MainClass = Class.forName("com.github.nullforge.Main");
            MainClass.getDeclaredMethod("stop").invoke(main);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.info("NullForge is disabled!");
    }
}
