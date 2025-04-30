package com.github.nullforge.Config;

import com.github.nullforge.NullForge;
import java.io.File;

public class GlobalConfig {
    public static boolean isTransform = true;

    public static File getDrawFolder() {
        File drawFolder;
        if (Settings.I.Draw_Folder == null || Settings.I.Draw_Folder.isEmpty()) {
            drawFolder = new File(NullForge.INSTANCE.getDataFolder(), "draw");
        } else {
            drawFolder = new File(Settings.I.Draw_Folder);
        }
        if (!drawFolder.exists() && !drawFolder.mkdirs()) {
            throw new RuntimeException("Failed to create folder " + drawFolder.getAbsolutePath());
        }
        return drawFolder;
    }

    public static File getPlayerFolder() {
        File playerFolder;
        if (Settings.I.Player_Folder == null || Settings.I.Player_Folder.isEmpty()) {
            playerFolder = new File(NullForge.INSTANCE.getDataFolder(), "players");
        } else {
            playerFolder = new File(Settings.I.Player_Folder);
        }
        if (!playerFolder.exists() && !playerFolder.mkdirs()) {
            throw new RuntimeException("Failed to create folder " + playerFolder.getAbsolutePath());
        }
        return playerFolder;
    }
}
