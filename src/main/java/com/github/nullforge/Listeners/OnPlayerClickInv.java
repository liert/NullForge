package com.github.nullforge.Listeners;

import com.github.nullforge.Data.DrawData;
import java.util.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OnPlayerClickInv implements Listener {
    public static Map<String, List<ItemStack>> tempItemMap = new HashMap<>();
    public static Map<String, DrawData> drawPlayerMap = new HashMap<>();
    public static List<String> nextList = new ArrayList<>();
    public static List<String> unClickList = new ArrayList<>();
    public static Map<String, Integer> indexMap = new HashMap<>();
    public static Map<String, DrawData> previewDrawMap = new HashMap<>();
}

