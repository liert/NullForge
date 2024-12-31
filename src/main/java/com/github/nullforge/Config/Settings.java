package com.github.nullforge.Config;

import java.util.List;
import java.util.Map;

public class Settings {
    public static Settings I;
    public Map<Integer, List<Integer>> Ore_Chance;
    public List<String> Ore_Worlds;
    public Map<Integer, List<String>> Gem_Lore;
    public int Draw_Item_ID;
    public String Gem_Level_Text;
    public String Gem_Level_Color;
    public int Gem_Level_Up_Chance;
    public Map<Integer, Integer> Gem_Level_Chance;
    public Map<String, String> Forge_Attrib;
    public Map<String, Float> Forge_Chance;
    public Map<String, String> Attrib_Level_Text;
    public String Attrib_Perce_Text;
    public Map<Integer, Integer> Forge_Exp;
    public int Forge_Exp_Float;
    public int Max_Player_Forge_Level;
    public String Exp_Text;
    public String ForgeOwner;
    public String ForgeDate;
    public String Attrib_Up_Item_Lore;

    public Settings() {
        I = this;
    }
}

