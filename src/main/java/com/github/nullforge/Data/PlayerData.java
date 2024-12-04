package com.github.nullforge.Data;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData {
    public static Map<String, PlayerData> pMap = new HashMap<>();
    private int Level;
    private double Exp;
    List<String> learn;

    @ConstructorProperties(value={"Level", "Exp", "learn"})
    public PlayerData(int Level2, double Exp, List<String> learn) {
        this.Level = Level2;
        this.Exp = Exp;
        this.learn = learn;
    }

    public PlayerData() {
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
        return this$learn == null ? other$learn == null : ((Object)this$learn).equals(other$learn);
    }

    protected boolean canEqual(Object other) {
        return other instanceof PlayerData;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getLevel();
        result = result * PRIME + Double.hashCode(this.getExp());
        List<String> learn = this.getLearn();
        result = result * PRIME + (learn == null ? 43 : ((Object)learn).hashCode());
        return result;
    }

    public String toString() {
        return "PlayerData(Level=" + this.getLevel() + ", Exp=" + this.getExp() + ", learn=" + this.getLearn() + ")";
    }
}

