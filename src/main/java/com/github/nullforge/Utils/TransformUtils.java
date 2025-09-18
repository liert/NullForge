package com.github.nullforge.Utils;

import com.comphenix.protocol.utility.StreamSerializer;
import com.github.nullforge.Config.DBConfig;
import com.github.nullforge.Data.DrawData;
import com.github.nullforge.Data.PlayerData;
import com.github.nullforge.NullForge;
import com.github.nullforge.db.DBHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TransformUtils {
    public static Map<String, Integer> transformDraw() {
        List<Map<String, Object>> list = DBHelper.executeQuery("SELECT * FROM " + DBConfig.DRAW_TABLE);
        int succeed = 0;
        int failed = 0;
        for (Map<String, Object> map : list) {
            String displayName = map.get("name").toString();
            String gem = getItemNameAndAmount(map.get("gem").toString());

            List<String> formulaList = new ArrayList<>();
            Object formulaObject = map.get("formula");
            if (formulaObject != null && !formulaObject.toString().isEmpty()) {
                String[] formulaSerialized = formulaObject.toString().split(",");
                for (String formula : formulaSerialized) {
                    formulaList.add(getItemNameAndAmount(formula));
                }
            }

            String result = getItemNameAndAmount(map.get("result").toString());
            int needGemLevel = Integer.parseInt(map.get("gemlevel").toString());
            int needPlayerLevel = Integer.parseInt(map.get("playerlevel").toString());

            List<String> detail = new ArrayList<>();
            Object detailObject = map.get("detail");
            if (detailObject != null && !detailObject.toString().isEmpty()) {
                String[] details = detailObject.toString().split("\\|");
                detail.addAll(Arrays.asList(details));
            }

            List<String> attrib = new ArrayList<>();
            Object attribObject = map.get("attrib");
            if (attribObject != null && !attribObject.toString().isEmpty()) {
                String[] attribs = attribObject.toString().split("\\|");
                attrib.addAll(Arrays.asList(attribs));
            }

            DrawData drawData = new DrawData(displayName, gem, formulaList, result, needGemLevel, needPlayerLevel, detail, attrib);
            try {
                File file = drawData.saveDraw();
                drawData.setFile(file);
                succeed++;
            } catch (IOException e) {
                e.printStackTrace();
                failed++;
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("size", list.size());
        result.put("succeed", succeed);
        result.put("failed", failed);
        return result;
    }

    public static Map<String, Integer> transformPlayer() {
        List<Map<String, Object>> list = DBHelper.executeQuery("SELECT * FROM " + DBConfig.PLAYER_TABLE);
        int succeed = 0;
        int failed = 0;
        for (Map<String, Object> map : list) {
            String playerName = map.get("player").toString();
            int level = Integer.parseInt(map.get("level").toString());
            double exp = Double.parseDouble(map.get("exp").toString());

            Object learnObject = map.get("learn");
            List<String> learn = new ArrayList<>();
            if (learnObject != null && !learnObject.toString().isEmpty()) {
                String[] learns = learnObject.toString().split(",");
                learn.addAll(Arrays.asList(learns));
            }

            try {
                PlayerData playerData = new PlayerData(playerName, level, exp, learn, new HashMap<>());
                playerData.savePlayer();
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.isOnline()) {
                    PlayerData.pMap.put(playerName, playerData);
                }
                succeed++;
            } catch (IOException e) {
                e.printStackTrace();
                failed++;
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("size", list.size());
        result.put("succeed", succeed);
        result.put("failed", failed);
        return result;
    }

    public static String getItemNameAndAmount(String serialized) {
        try {
            ItemStack item = StreamSerializer.getDefault().deserializeItemStack(serialized);
            String itemName = NullForge.getItemManager().getItemId(item.clone());
            if (itemName == null) {
                itemName = "未知物品";
            }
            int amount = item.getAmount();
            return itemName + "x" + amount;
        }
        catch (IOException ignore) {}
        return "未知物品x1";
    }
}
