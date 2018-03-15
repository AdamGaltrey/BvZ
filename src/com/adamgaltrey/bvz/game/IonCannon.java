package com.adamgaltrey.bvz.game;

import com.adamgaltrey.bvz.utils.InstantFirework;
import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.data.IonCannonData;
import com.adamgaltrey.bvz.entities.GoalUpdater;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IonCannon implements Runnable {

    public static ItemStack ionCannon;
    static IonCannonData data = Config.getIonCannonData();

    static boolean onCooldown;
    static int cooldown = data.getCooldown();

    //public static final FireworkEffectPlayer fep = new FireworkEffectPlayer();
    public static final InstantFirework fwk = new InstantFirework();

    public static void initItem() {
        ionCannon = new ItemStack(Material.NETHER_STAR);
        ItemMeta met = ionCannon.getItemMeta();
        met.setDisplayName(ChatColor.AQUA + "Ion Cannon");
        met.setLore(new ArrayList<String>(Arrays.asList(ChatColor.RESET + "Left/Right click to fire.")));
        ionCannon.setItemMeta(met);
    }

    @Override
    public void run() {
        if (onCooldown) {
            cooldown--;
            if (cooldown <= 0) {
                onCooldown = false;
                cooldown = data.getCooldown();
            }
        }
    }

    public static void fireIonCannon(Player p) {
        if (onCooldown) {
            if (cooldown > 1) {
                p.sendMessage(ChatColor
                        .translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The ion cannon will be ready in " + cooldown + " seconds."));
            } else {
                p.sendMessage(ChatColor
                        .translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The ion cannon will be ready in 1 second."));
            }
        } else {

            // fire
            Block targ = p.getTargetBlock((Set<Material>) null, 80);
            if (targ == null || targ.getType().equals(Material.AIR) || targ.getLocation().distance(p.getLocation()) > 80 || targ.getLocation().distance
                    (Config.traderLocation.getBukkitLocation()) <= 7) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "Invalid target designated."));
            } else {
                onCooldown = true;
                final Location root = targ.getLocation();
                // lets fire strike down a few lightning bolts
                targ.getLocation().getWorld().strikeLightning(root);
                FireworkEffect effect = FireworkEffect.builder().flicker(true).with(Type.BURST).withColor(Color.AQUA)
                        .withColor(Color.WHITE).withColor(Color.YELLOW).build();

                for (int i = 0; i <= 2; i++) {
                    fwk.firework(effect, root.add(0, i, 0));
                }

                //send out falling blocks
                int blocks = 12, radius = 2;
                final Set<Integer> list = new HashSet<Integer>(10);
                final World w = root.getWorld();

                w.playSound(root, Sound.ENDERMAN_TELEPORT, 20, 20);

                for (int i = 0; i < blocks; i++) {
                    double t = 2 * Math.PI * i / blocks;
                    int x = (int) Math.round(radius * Math.cos(t));
                    int z = (int) Math.round(radius * Math.sin(t));
                    Location other = targ.getRelative(x, 2, z).getLocation();
                    other.getWorld().strikeLightning(other);
                    // System.out.println("x, z = " + x + ", " + z);
                    FallingBlock fb = targ.getWorld().spawnFallingBlock(other, Material.ICE, (byte) 0);
                    fb.setVelocity(new Vector(x * 0.08, 0.35, z * 0.08));
                    list.add(fb.getEntityId());
                }

                Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {

                    @Override
                    public void run() {
                        for (Entity e : root.getWorld().getEntitiesByClass(Zombie.class)) {
                            if (e.getLocation().getWorld().getName().equals(root.getWorld().getName())) {
                                //same world
                                if (e.getLocation().distance(root) <= data.getRadius()) {
                                    //kill
                                    LivingEntity le = (LivingEntity) e;
                                    GoalUpdater.died(le.getEntityId());
                                    le.remove();
                                }
                            }
                        }
                        for (Entity e : w.getEntities()) {
                            if (list.contains(e.getEntityId()) && !(e instanceof LivingEntity)) {
                                e.remove();
                                Location l = e.getLocation();
                                l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 1, false, false);
                                FireworkEffect effect = FireworkEffect.builder().with(Type.BURST).withColor(Color.RED)
                                        .withColor(Color.WHITE).withColor(Color.AQUA).build();
                                for (int i = 0; i <= 2; i++) {
                                    fwk.firework(effect, l.add(0, i, 0));
                                }
                            }
                        }
                    }
                }, 10);
            }

        }
    }

}
