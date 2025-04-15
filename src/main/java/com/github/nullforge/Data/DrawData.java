package com.github.nullforge.Data;

import com.github.nullforge.Config.Settings;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.Utils.ItemMaker;
import com.github.nullforge.Utils.ItemString;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DrawData {
    private final File file;
    private final String displayName;
    private String gem;
    private List<String> formula;
    private String result;
    private int needGemLevel;
    private int needPlayerLevel;
    private List<String> detail;
    private List<String> attrib;
    private List<String> customCommands; // 新增字段

    private DrawData(File file) {
        YamlConfiguration drawConfig = YamlConfiguration.loadConfiguration(file);
        this.file = file;
        this.displayName = drawConfig.getString("name");
        this.gem = drawConfig.getString("gem");
        this.formula = drawConfig.getStringList("formula");
        this.result = drawConfig.getString("result");
        this.needGemLevel = drawConfig.getInt("gemlevel");
        this.needPlayerLevel = drawConfig.getInt("playerlevel");
        this.detail = drawConfig.getStringList("detail");
        this.attrib = drawConfig.getStringList("attrib");
        this.customCommands = drawConfig.getStringList("customCommands"); // 读取自定义命令
        DrawManager.addDraw(this);
    }

    public static void CreateDrawData(File file) {
        new DrawData(file);
    }

    public File getFile() {
        return file;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return file.getName().split("\\.")[0];
    }

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
        ItemStack itemStack = ItemString.getItem(this.result);
        if (itemStack == null) {
            throw new NullPointerException(MessageLoader.getMessage("mm-null-item"));
        }
        return itemStack;
    }

    public void setResult(ItemStack item) {
        this.result = ItemString.getString(item);
    }

    public ItemStack getDrawItem() {
        ItemStack item = ItemMaker.create(Settings.I.Draw_Item_ID, 0, "§b§l锻造图纸", this.displayName, "§c该图纸需要锻造等级:§b§l" + this.needPlayerLevel);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.addAll(this.detail);
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
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

    public List<String> getCustomCommands() { // 新增方法
        return this.customCommands;
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

    public void setCustomCommands(List<String> customCommands) { // 新增方法
        this.customCommands = customCommands;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DrawData)) {
            return false;
        }
        DrawData other = (DrawData) o;
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
        if (!Objects.equals(this_attrib, other_attrib)) {
            return false;
        }
        List<String> this_customCommands = this.getCustomCommands(); // 比较自定义命令
        List<String> other_customCommands = other.getCustomCommands();
        return Objects.equals(this_customCommands, other_customCommands);
    }

    protected boolean canEqual(Object other) {
        return other instanceof DrawData;
    }

    @Override
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        ItemStack $gem = this.getGem();
        result = result * PRIME + ($gem == null ? 43 : $gem.hashCode());
        List<ItemStack> $formula = this.getFormula();
        result = result * PRIME + ($formula == null ? 43 : ((Object) $formula).hashCode());
        ItemStack $result = this.getResult();
        result = result * PRIME + ($result == null ? 43 : $result.hashCode());
        result = result * PRIME + this.getNeedGemLevel();
        result = result * PRIME + this.getNeedPlayerLevel();
        List<String> $detail = this.getDetail();
        result = result * PRIME + ($detail == null ? 43 : ((Object) $detail).hashCode());
        List<String> $attrib = this.getAttrib();
        result = result * PRIME + ($attrib == null ? 43 : ((Object) $attrib).hashCode());
        List<String> $customCommands = this.getCustomCommands(); // 添加自定义命令的哈希码
        result = result * PRIME + ($customCommands == null ? 43 : ((Object) $customCommands).hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "DrawData(gem=" + this.getGem() + ", formula=" + this.getFormula() + ", result=" + this.getResult() + ", needGemLevel=" + this.getNeedGemLevel() + ", needPlayerLevel=" + this.getNeedPlayerLevel() + ", detail=" + this.getDetail() + ", attrib=" + this.getAttrib() + ", customCommands=" + this.getCustomCommands() + ")";
    }
}