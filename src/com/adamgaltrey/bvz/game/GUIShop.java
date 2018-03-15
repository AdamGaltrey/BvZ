package com.adamgaltrey.bvz.game;

import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.data.PlayerScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Adam on 23/08/2015.
 */
public class GUIShop implements Listener {

    enum ShopItem {

        ICE_BOW(0, 0, 25),
        BUCKSHOT_BOW(1, 0, 15),
        POLYMORPH_EGG(2, 0, 1),
        CATACLYSM_ROD(3, 0, 1),
        CONCUSSION_GRENADE(4, 0, 1),
        GRAVITY_WELL(5, 0, 1);

        private final int slot, cost, charges;

        private ShopItem(int slot, int cost, int charges) {
            this.slot = slot;
            this.cost = cost;
            this.charges = charges;
        }

        public int getCharges() {
            return charges;
        }

        public int getCost() {
            return cost;
        }

        public int getSlot() {
            return slot;
        }

        public static ShopItem getFromSlot(int slot) {
            for (ShopItem si : ShopItem.values()) {
                if (si.getSlot() == slot) {
                    return si;
                }
            }
            return null;
        }

    }

    private final UUID u;

    private static final String title = ChatColor.BLUE + "Points Shop";

    /*
        Shop Items

        - Ice Bow - Arrows do half damage but slow zombies for 3 seconds
     */

    int[] vals;

    public GUIShop(Player p) {
        u = p.getUniqueId();

        Inventory i = Bukkit.createInventory(p, 27, title);

        /*
           0    1   2   3   4   5   6   7   8

           9    10  11  12  13  14  15  16  17

           18   19  20  21  22  23  24  25  26
         */

        String r = ChatColor.RESET + "";

        /*
            Setup shop
         */

        ItemStack iceBow = new ItemStack(Material.PACKED_ICE);
        ItemMeta meta = iceBow.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Ice Bow " + ChatColor.WHITE + "(" + ChatColor.YELLOW + ShopItem.ICE_BOW.getCost() + ChatColor.WHITE + " Points)");
        meta.setLore(Arrays.asList(r + "Arrows do no damage but", r + "freeze zombies for 8 seconds"));
        iceBow.setItemMeta(meta);

        i.setItem(ShopItem.ICE_BOW.getSlot(), iceBow);

        ItemStack buckshotBow = new ItemStack(Material.SEEDS);
        meta = buckshotBow.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Buckshot Bow " + ChatColor.WHITE + "(" + ChatColor.YELLOW + ShopItem.BUCKSHOT_BOW.getCost() + ChatColor.WHITE +
                " Points)");
        meta.setLore(Arrays.asList(r + "Fire a cone of 7 arrows", r + "each dealing 100% damage", r + "Click to shoot (No drawback)"));
        buckshotBow.setItemMeta(meta);

        i.setItem(ShopItem.BUCKSHOT_BOW.getSlot(), buckshotBow);

        ItemStack polymorphEgg = new ItemStack(Material.MONSTER_EGG, 1, (short) 54);
        meta = polymorphEgg.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Polymorph Egg" + ChatColor.WHITE + " (" + ChatColor.YELLOW + ShopItem.POLYMORPH_EGG.getCost() + ChatColor
                .WHITE + " Points)");
        meta.setLore(Arrays.asList(r + "Transforms all zombies into babies"));
        polymorphEgg.setItemMeta(meta);

        i.setItem(ShopItem.POLYMORPH_EGG.getSlot(), polymorphEgg);

        ItemStack cataclysmRod = new ItemStack(Material.BLAZE_ROD, 1);
        meta = cataclysmRod.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Cataclysm Rod" + ChatColor.WHITE + " (" + ChatColor.YELLOW + ShopItem.CATACLYSM_ROD.getCost() + ChatColor
                .WHITE + " Points)");
        meta.setLore(Arrays.asList(r + "Creates a block of impassable", r + "terrain for a short time"));
        cataclysmRod.setItemMeta(meta);

        i.setItem(ShopItem.CATACLYSM_ROD.getSlot(), cataclysmRod);

        ItemStack concussionGrenade = new ItemStack(Material.SNOW_BALL, 1);
        meta = concussionGrenade.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Concussion Grenade" + ChatColor.WHITE + " (" + ChatColor.YELLOW + ShopItem.CONCUSSION_GRENADE.getCost() + ChatColor
                .WHITE + " Points)");
        meta.setLore(Arrays.asList(r + "Blasts zombies into the air"));
        concussionGrenade.setItemMeta(meta);

        i.setItem(ShopItem.CONCUSSION_GRENADE.getSlot(), concussionGrenade);


        ItemStack gravityWell = new ItemStack(Material.BUCKET, 1);
        meta = gravityWell.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Gravity Well" + ChatColor.WHITE + " (" + ChatColor.YELLOW + ShopItem.GRAVITY_WELL.getCost() +
                ChatColor.WHITE + " Points)");
        meta.setLore(Arrays.asList(r + "Sucks zombies into a gravity well"));
        gravityWell.setItemMeta(meta);

        i.setItem(ShopItem.GRAVITY_WELL.getSlot(), gravityWell);


        /*
            Bow upgrades
         */
        vals = getBowVals(p);

        ItemStack damageUpgrade = new ItemStack(Material.BOW);
        meta = damageUpgrade.getItemMeta();

        if (vals[0] == 5) {
            meta.setDisplayName(ChatColor.RED + "No Further Upgrades");
            damageUpgrade.setItemMeta(meta);
        } else {
            meta.setDisplayName(ChatColor.RED + "Damage " + toNumeral(vals[0] + 1) + ChatColor.WHITE + " (" + ChatColor.YELLOW + Config
                    .upgradeDamageCosts[vals[0]] + ChatColor.WHITE + " Points)");
            meta.setLore(Arrays.asList(r + "Upgrade your bow's damage"));
        }
        damageUpgrade.setItemMeta(meta);

        i.setItem(18, damageUpgrade);

        ItemStack knockbackUpgrade = new ItemStack(Material.BOW);
        meta = knockbackUpgrade.getItemMeta();

        if (vals[1] == 2) {
            meta.setDisplayName(ChatColor.RED + "No Further Upgrades");
            knockbackUpgrade.setItemMeta(meta);
        } else {
            meta.setDisplayName(ChatColor.RED + "Knockback " + toNumeral(vals[1] + 1) + ChatColor.WHITE + " (" + ChatColor.YELLOW + Config
                    .upgradeKnockbackCosts[vals[1]] + ChatColor.WHITE + " Points)");
            meta.setLore(Arrays.asList(r + "Upgrade your bow's knockback"));
        }
        knockbackUpgrade.setItemMeta(meta);

        i.setItem(19, knockbackUpgrade);


        p.openInventory(i);

        Bukkit.getPluginManager().registerEvents(this, BVZ.p);
    }

    private String toNumeral(int v) {
        if (v == 1) {
            return "I";
        } else if (v == 2) {
            return "II";
        } else if (v == 3) {
            return "III";
        } else {
            return "IV";
        }
    }

    private void upgradeBow(Player p, boolean dmg) {
        Enchantment ench = dmg ? Enchantment.ARROW_DAMAGE : Enchantment.ARROW_KNOCKBACK;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType().equals(Material.BOW) && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && ChatColor.stripColor(is
                    .getItemMeta().getDisplayName()).equalsIgnoreCase("Infinity Bow")) {

                if (is.getEnchantments().containsKey(ench)) {
                    is.removeEnchantment(ench);
                }

                is.addEnchantment(ench, vals[dmg ? 0 : 1] + 1);
                break;
            }
        }
    }

    private int[] getBowVals(Player p) {
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType().equals(Material.BOW) && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && ChatColor.stripColor(is
                    .getItemMeta().getDisplayName()).equalsIgnoreCase("Infinity Bow")) {
                //check it!
                int[] ret = new int[2];

                ret[0] = is.getEnchantments().containsKey(Enchantment.ARROW_DAMAGE) ? is.getEnchantments().get(Enchantment.ARROW_DAMAGE) : 0;
                ret[1] = is.getEnchantments().containsKey(Enchantment.ARROW_KNOCKBACK) ? is.getEnchantments().get(Enchantment.ARROW_KNOCKBACK) : 0;

                return ret;
            }
        }
        return new int[]{0, 0};
    }

    @EventHandler
    private void click(InventoryClickEvent evt) {
        if (evt.getInventory().getTitle().equals(title) && evt.getWhoClicked() instanceof Player && evt.getWhoClicked().getUniqueId().equals(u)) {

            evt.setCancelled(true);

            int slot = evt.getRawSlot();
            ShopItem item = ShopItem.getFromSlot(slot);

            Player p = (Player) evt.getWhoClicked();

            if (item != null) {

                boolean transactionSuccess = false;

                if (item.equals(ShopItem.ICE_BOW)) {
                    //buying ICE BOW
                    if (PlayerScoreboard.getPoints(p) >= item.getCost()) {
                        PlayerScoreboard.deductPoints(p, item.getCost());

                        //give bow
                        ItemStack bow = new ItemStack(Material.BOW);
                        bow.setDurability((short) item.getCharges());
                        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                        ItemMeta meta = bow.getItemMeta();
                        meta.setDisplayName(ChatColor.AQUA + "Ice Bow" + ChatColor.RESET + " (" + item.getCharges() + " Charges)");
                        bow.setItemMeta(meta);

                        p.getInventory().addItem(bow);

                        transactionSuccess = true;

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Ice Bow purchased successfully."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                } else if (item.equals(ShopItem.BUCKSHOT_BOW)) {
                    if (PlayerScoreboard.getPoints(p) >= item.getCost()) {
                        PlayerScoreboard.deductPoints(p, item.getCost());

                        //give bow
                        ItemStack bow = new ItemStack(Material.BOW);
                        bow.setDurability((short) item.getCharges());
                        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                        ItemMeta meta = bow.getItemMeta();
                        meta.setDisplayName(ChatColor.RED + "Buckshot Bow" + ChatColor.RESET + " (" + item.getCharges() + " Charges)");
                        bow.setItemMeta(meta);

                        p.getInventory().addItem(bow);

                        transactionSuccess = true;

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Buckshot Bow purchased successfully."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                } else if (item.equals(ShopItem.POLYMORPH_EGG)) {
                    if (PlayerScoreboard.getPoints(p) >= item.getCost()) {
                        PlayerScoreboard.deductPoints(p, item.getCost());

                        //give bow
                        ItemStack egg = new ItemStack(Material.MONSTER_EGG, 1, (short) 54);
                        ItemMeta meta = egg.getItemMeta();
                        meta.setDisplayName(ChatColor.RED + "Polymorph Egg" + ChatColor.RESET + " (" + item.getCharges() + " Charge)");
                        egg.setItemMeta(meta);

                        p.getInventory().addItem(egg);

                        transactionSuccess = true;

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Polymorph Egg purchased successfully" +
                                "."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                } else if (item.equals(ShopItem.CATACLYSM_ROD)) {
                    if (PlayerScoreboard.getPoints(p) >= item.getCost()) {
                        PlayerScoreboard.deductPoints(p, item.getCost());

                        //give bow
                        ItemStack rod = new ItemStack(Material.BLAZE_ROD, 1);
                        ItemMeta meta = rod.getItemMeta();
                        meta.setDisplayName(ChatColor.RED + "Cataclysm Rod" + ChatColor.RESET + " (" + item.getCharges() + " Charge)");
                        rod.setItemMeta(meta);

                        p.getInventory().addItem(rod);

                        transactionSuccess = true;

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Cataclysm Rod purchased successfully" +
                                "."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                }else if (item.equals(ShopItem.CONCUSSION_GRENADE)) {
                    if (PlayerScoreboard.getPoints(p) >= item.getCost()) {
                        PlayerScoreboard.deductPoints(p, item.getCost());

                        //give bow
                        ItemStack concussionGrenade = new ItemStack(Material.SNOW_BALL, 1);
                        ItemMeta meta = concussionGrenade.getItemMeta();
                        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Concussion Grenade" + ChatColor.RESET + " (" + item.getCharges() + " Charge)");
                        concussionGrenade.setItemMeta(meta);

                        p.getInventory().addItem(concussionGrenade);

                        transactionSuccess = true;

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Concussion Grenade purchased " +
                                "successfully."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                }else if (item.equals(ShopItem.GRAVITY_WELL)) {
                    if (PlayerScoreboard.getPoints(p) >= item.getCost()) {
                        PlayerScoreboard.deductPoints(p, item.getCost());

                        //give bow
                        ItemStack gravityWell = new ItemStack(Material.BUCKET, 1);
                        ItemMeta meta = gravityWell.getItemMeta();
                        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Gravity Well" + ChatColor.RESET + " (" + item.getCharges() + " Charge)");
                        gravityWell.setItemMeta(meta);

                        p.getInventory().addItem(gravityWell);

                        transactionSuccess = true;

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Gravity Well purchased " +
                                "successfully."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                }

                if (transactionSuccess) {
                    p.closeInventory();
                }
            } else if (slot == 18) {
                //damage upgrade
                if (vals[0] < 5) {
                    if (PlayerScoreboard.getPoints(p) >= Config.upgradeDamageCosts[vals[0]]) {
                        PlayerScoreboard.deductPoints(p, Config.upgradeDamageCosts[vals[0]]);

                        upgradeBow(p, true);
                        p.closeInventory();

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Damage upgrade purchased successfully" +
                                "."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                }
            } else if (slot == 19) {
                //knockback upgrade

                if (vals[1] < 2) {
                    if (PlayerScoreboard.getPoints(p) >= Config.upgradeKnockbackCosts[vals[1]]) {
                        PlayerScoreboard.deductPoints(p, Config.upgradeKnockbackCosts[vals[1]]);

                        upgradeBow(p, false);
                        p.closeInventory();

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Knockback upgrade purchased " +
                                "successfully."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You do not have enough points."));
                    }
                }
            }
        }
    }

    @EventHandler
    private void close(InventoryCloseEvent evt) {
        if (evt.getInventory().getTitle().equals(title) && evt.getPlayer().getUniqueId().equals(u)) {
            HandlerList.unregisterAll(this);
        }
    }

}
