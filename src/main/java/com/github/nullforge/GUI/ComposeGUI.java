package com.github.nullforge.GUI;

import com.github.nullbridge.Inventory.InventoryContext;
import com.github.nullbridge.Inventory.NullInventory;
import com.github.nullbridge.annotate.RegisterInventory;
import com.github.nullforge.Config.Settings;
import com.github.nullforge.MessageLoader;
import com.github.nullforge.NullForge;
import com.github.nullforge.Utils.GemUtil;
import com.github.nullforge.Utils.ItemMaker;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

@RegisterInventory
public class ComposeGUI extends NullInventory {
    private static ComposeGUI instance;

    public Inventory initInventory() {
        Inventory inventory = createInventory(null, 27, "§b§l宝石合成");
        ItemStack gem1 = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)15, MessageLoader.getMessage("gui-compose-gem1"), "");
        ItemStack gem2 = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)6, MessageLoader.getMessage("gui-compose-gem2"), "");
        ItemStack over = ItemMaker.create(Material.STAINED_GLASS_PANE, (short)5, MessageLoader.getMessage("gui-compose-click"), "");
        for (int i = 0; i < 27; i++) {
            if (i == 0 || i == 1 || i == 2 || i == 9 || i == 11 || i == 18 || i == 19 || i == 20) {
                inventory.setItem(i, gem1);
            } else if (i == 3 || i == 4 || i == 5 || i == 21 || i == 22 || i == 23) {
                inventory.setItem(i, over);
            } else if (i == 6 || i == 7 || i == 8 || i == 15 || i == 17 || i == 24 || i == 25 || i == 26) {
                inventory.setItem(i, gem2);
            }
        }
        return inventory;
    }

    @Override
    public void initInventory(InventoryContext inventoryContext) {

    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot < 0) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        Inventory inventory = e.getInventory();
        if (e.getClickedInventory().getTitle().equals("§b§l宝石合成")) {
            ItemStack gemFail = null;
            ItemStack over = null;
            int success = 0;
            int fail = 0;
            // int slot = e.getRawSlot();
            if ((slot < 12 || slot > 14) && slot != 10 && slot != 16) {
                e.setCancelled(true);
            }
            if (slot >= 21 && slot <= 23 || slot >= 3 && slot <= 6) {
                if (inventory.getItem(10) != null && inventory.getItem(16) != null) {
                    ItemStack gem1 = inventory.getItem(10).clone();
                    ItemStack gem2 = inventory.getItem(16).clone();
                    if (GemUtil.getGemLevel(gem1) != 0 && GemUtil.getGemLevel(gem2) != 0) {
                        if (!GemUtil.isSameGem(gem1, gem2)) {
                            p.sendMessage(MessageLoader.getMessage("gem-different")); //宝石种类不同
                            return;
                        }
                        if (GemUtil.getGemLevel(gem1) != GemUtil.getGemLevel(gem2)) {
                            p.sendMessage(MessageLoader.getMessage("gem-same-level")); //宝石必须同级
                            return;
                        }
                        if (gem1.getAmount() != gem2.getAmount()) {
                            p.sendMessage(MessageLoader.getMessage("gem-same-amount")); //数量必须同等
                            return;
                        }
                        if (inventory.getItem(12) == null && inventory.getItem(14) == null) {
                            int gemLevel = GemUtil.getGemLevel(gem1);
                            for (int i = gem1.getAmount(); i > 0; --i) {
                                if (NullForge.random.nextInt(1000) <= Settings.I.Gem_Level_Up_Chance) {
                                    ++success;
                                    over = GemUtil.changeGemLevel(gem1, gemLevel + 1);
                                } else {
                                    gemFail = gem1;
                                    ++fail;
                                }
                                gem1.setAmount(i);
                                gem2.setAmount(i);
                                inventory.setItem(10, gem1);
                                inventory.setItem(16, gem2);
                            }
                            if (success != 0) {
                                over.setAmount(success);
                                inventory.setItem(12, over);
                            }
                            if (fail != 0) {
                                gemFail.setAmount(fail);
                                inventory.setItem(14, gem1);
                            }
                            inventory.setItem(10, null);
                            inventory.setItem(16, null);
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                            String successMsg = MessageLoader.getMessage("gem-success").replace("%success%", String.valueOf(success)); //成功数量
                            String failureMsg = MessageLoader.getMessage("gem-failure").replace("%fail%", String.valueOf(fail)); //失败数量

                            p.sendMessage(successMsg);
                            if (fail > 0) {
                                p.sendMessage(failureMsg);
                            }
                            return;
                        }
                        p.sendMessage(MessageLoader.getMessage("gem-not-placement")); //禁止放置
                        return;
                    }
                    p.sendMessage(MessageLoader.getMessage("gem-must")); //必须是宝石
                    return;
                }
                p.sendMessage(MessageLoader.getMessage("gem-place")); //宝石放置
            }
        }
    }

    @Override
    public void close(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        ArrayList<ItemStack> give = new ArrayList<>();
        if (inv.getItem(10) != null) {
            give.add(inv.getItem(10));
        }
        if (inv.getItem(16) != null) {
            give.add(inv.getItem(16));
        }
        for (int i = 12; i < 15; ++i) {
            if (inv.getItem(i) == null) continue;
            give.add(inv.getItem(i));
        }
        for (ItemStack item3 : give) {
            p.getInventory().addItem(item3);
        }
    }

    public static ComposeGUI getInstance() {
        if (instance == null) {
            instance = new ComposeGUI();
        }
        return instance;
    }
}

