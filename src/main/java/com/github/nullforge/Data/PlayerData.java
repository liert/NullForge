package com.github.nullforge.Data;

import com.github.nullforge.Config.GlobalConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerData {
    public static Map<String, PlayerData> pMap = new HashMap<>();
    private final String playerName;
    private int Level;
    private double Exp;
    private List<String> learn = new ArrayList<>();
    private Map<String, Object> ForgeRecord = new HashMap<>();

    public PlayerData(String playerName) {
        this.playerName = playerName;
        this.Level = 0;
        this.Exp = 0;
    }

    public PlayerData(String playerName, int level, double exp, List<String> learn, Map<String, Object> forgeRecord) {
        this.playerName = playerName;
        this.Level = level;
        this.Exp = exp;
        this.learn = learn;
        this.ForgeRecord = forgeRecord;
    }

    public static PlayerData getPlayerData(String playerName) {
        return pMap.get(playerName);
    }

    public File savePlayer() throws IOException {
        YamlConfiguration playerData = new YamlConfiguration();
        playerData.set("level", this.Level);
        playerData.set("exp", this.Exp);
        playerData.set("learn", this.learn);
        playerData.set("ForgeRecord", this.ForgeRecord);
        File playerFile = new File(GlobalConfig.getPlayerFolder(), playerName + ".yml");
        playerData.save(playerFile);
        return playerFile;
    }

    public int getLevel() {
        return this.Level;
    }

    public double getExp() {
        return this.Exp;
    }

    public List<String> getLearn() {
        return this.learn;
    }

    public void setLevel(int Level2) {
        this.Level = Level2;
    }

    public void setExp(double Exp) {
        this.Exp = Exp;
    }

    public void setLearn(List<String> learn) {
        this.learn = learn;
    }

    public int getForgeCount(String name) {
        return (int) this.ForgeRecord.getOrDefault(name, 0);
    }

    public void addForgeCount(String name) {
        this.ForgeRecord.put(name, (int) this.ForgeRecord.getOrDefault(name, 0) + 1);
    }

    public void resetForgeCount(String name) {
        this.ForgeRecord.put(name, 0);
    }

    protected boolean canEqual(Object other) {
        return other instanceof PlayerData;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlayerData)) {
            return false;
        }
        PlayerData other = (PlayerData)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getLevel() != other.getLevel()) {
            return false;
        }
        if (Double.compare(this.getExp(), other.getExp()) != 0) {
            return false;
        }
        List<String> this$learn = this.getLearn();
        List<String> other$learn = other.getLearn();
        return Objects.equals(this$learn, other$learn);
    }

    @Override
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getLevel();
        result = result * PRIME + Double.hashCode(this.getExp());
        List<String> learn = this.getLearn();
        result = result * PRIME + (learn == null ? 43 : learn.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "PlayerData(Level=" + this.getLevel() + ", Exp=" + this.getExp() + ", learn=" + this.getLearn() + ")";
    }
}

