package com.github.nullforge.Data;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.Utils.ItemMaker;
import com.github.nullforge.Utils.ItemString;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DrawData {
    public static Map<String, DrawData> DrawMap = new HashMap<>();
    private String gem;
    private List<String> formula;
    private String result;
    private int needGemLevel;
    private int needPlayerLevel;
    private List<String> detail;
    private List<String> attrib;

    public ItemStack getGem() {
        return ItemString.getItem(this.gem);
    }

    public void setGem(ItemStack item) {
        this.gem = ItemString.getString(item);
    }

    public List<ItemStack> getFormula() {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (String s : this.formula) {
            itemStacks.add(ItemString.getItem(s));
        }
        return itemStacks;
    }

    public void setFormula(List<ItemStack> formula) {
        ArrayList<String> list = new ArrayList<>();
        for (ItemStack item : formula) {
            list.add(ItemString.getString(item));
        }
        this.formula = list;
    }

    public ItemStack getResult() {
        return ItemString.getItem(this.result);
    }

    public void setResult(ItemStack item) {
        this.result = ItemString.getString(item);
    }

    public ItemStack getDrawItem() {
        String dName = DrawData.getDrawName(this);
        if (dName == null) {
            return null;
        }
        ItemStack item = ItemMaker.create(Settings.I.Draw_Item_ID, 0, "§b§l锻造图纸", dName, "§c该图纸需要锻造等级:§b§l" + this.needPlayerLevel);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.addAll(this.detail);
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static String getDrawName(DrawData dd) {
        String dName = null;
        for (String name : DrawMap.keySet()) {
            if (!dd.equals(DrawMap.get(name))) continue;
            dName = name;
        }
        return dName;
    }

    @ConstructorProperties(value={"gem", "formula", "result", "needGemLevel", "needPlayerLevel", "detail", "attrib"})
    public DrawData(String gem, List<String> formula, String result, int needGemLevel, int needPlayerLevel, List<String> detail, List<String> attrib) {
        this.gem = gem;
        this.formula = formula;
        this.result = result;
        this.needGemLevel = needGemLevel;
        this.needPlayerLevel = needPlayerLevel;
        this.detail = detail;
        this.attrib = attrib;
    }

    public int getNeedGemLevel() {
        return this.needGemLevel;
    }

    public int getNeedPlayerLevel() {
        return this.needPlayerLevel;
    }

    public List<String> getDetail() {
        return this.detail;
    }

    public List<String> getAttrib() {
        return this.attrib;
    }

    public void setNeedGemLevel(int needGemLevel) {
        this.needGemLevel = needGemLevel;
    }

    public void setNeedPlayerLevel(int needPlayerLevel) {
        this.needPlayerLevel = needPlayerLevel;
    }

    public void setDetail(List<String> detail) {
        this.detail = detail;
    }

    public void setAttrib(List<String> attrib) {
        this.attrib = attrib;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DrawData)) {
            return false;
        }
        DrawData other = (DrawData)o;
        if (!other.canEqual(this)) {
            return false;
        }
        ItemStack this_gem = this.getGem();
        if (!Objects.equals(this_gem, other.getGem())) {
            return false;
        }
        List<ItemStack> this_formula = this.getFormula();
        if (!Objects.equals(this_formula, other.getFormula())) {
            return false;
        }
        ItemStack this_result = this.getResult();
        if (!Objects.equals(this_result, other.getResult())) {
            return false;
        }
        if (this.getNeedGemLevel() != other.getNeedGemLevel()) {
            return false;
        }
        if (this.getNeedPlayerLevel() != other.getNeedPlayerLevel()) {
            return false;
        }
        List<String> this_detail = this.getDetail();
        if (!Objects.equals(this_detail, other.getDetail())) {
            return false;
        }
        List<String> this_attrib = this.getAttrib();
        List<String> other_attrib = other.getAttrib();
        return Objects.equals(this_attrib, other_attrib);
    }

    protected boolean canEqual(Object other) {
        return other instanceof DrawData;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        ItemStack $gem = this.getGem();
        result = result * PRIME + ($gem == null ? 43 : $gem.hashCode());
        List<ItemStack> $formula = this.getFormula();
        result = result * PRIME + ($formula == null ? 43 : ((Object)$formula).hashCode());
        ItemStack $result = this.getResult();
        result = result * PRIME + ($result == null ? 43 : $result.hashCode());
        result = result * PRIME + this.getNeedGemLevel();
        result = result * PRIME + this.getNeedPlayerLevel();
        List<String> $detail = this.getDetail();
        result = result * PRIME + ($detail == null ? 43 : ((Object)$detail).hashCode());
        List<String> $attrib = this.getAttrib();
        result = result * PRIME + ($attrib == null ? 43 : ((Object)$attrib).hashCode());
        return result;
    }

    public String toString() {
        return "DrawData(gem=" + this.getGem() + ", formula=" + this.getFormula() + ", result=" + this.getResult() + ", needGemLevel=" + this.getNeedGemLevel() + ", needPlayerLevel=" + this.getNeedPlayerLevel() + ", detail=" + this.getDetail() + ", attrib=" + this.getAttrib() + ")";
    }
}

