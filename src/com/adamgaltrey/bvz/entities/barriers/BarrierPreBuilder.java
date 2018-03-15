package com.adamgaltrey.bvz.entities.barriers;

import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.utils.Loc;
import com.adamgaltrey.bvz.entities.GoalUpdater;
import org.bukkit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarrierPreBuilder {

    public static Map<String, Loc[]> pre = new HashMap<String, Loc[]>();
    final static int stutter = 125;

    public static void buildActual(final String name) {
        Bukkit.getScheduler().runTaskAsynchronously(BVZ.p, new Runnable() {

            @Override
            public void run() {
                for (final Loc l : pre.get(name)) {
                    Bukkit.getScheduler().runTask(BVZ.p, new Runnable() {

                        @Override
                        public void run() {
                            Location bl = l.getBukkitLocation();
                            bl.getBlock().getRelative(0, 1, 0).setType(Material.FENCE);
                            bl.getWorld().playEffect(bl.getBlock().getRelative(0,1,0).getLocation(), Effect.STEP_SOUND, Material.WOOD.getId());
                        }
                    });
                    try {
                        Thread.sleep(stutter);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Bukkit.getScheduler().runTaskTimer(BVZ.p, new Runnable() {
                    @Override
                    public void run() {
                        //fix pathing for sleeper zombies
                        GoalUpdater.forceRepath();
                    }
                }, 0, 25);

            }
        });
    }

    /*
        Cataclysm procedure
        1) Create impassable terrain
        2) Remove the impassable terrain
        3) Check over all purchased barrier and replace them if needed
     */

    public static List<Location> getLocList(Loc l1, Loc l2) {
        double minX, minY, minZ, maxX, maxY, maxZ;

        minX = l1.getX() > l2.getX() ? l2.getX() : l1.getX();
        minY = l1.getY() > l2.getY() ? l2.getY() : l1.getY();
        minZ = l1.getZ() > l2.getZ() ? l2.getZ() : l1.getZ();

        maxX = l1.getX() < l2.getX() ? l2.getX() : l1.getX();
        maxY = l1.getY() < l2.getY() ? l2.getY() : l1.getY();
        maxZ = l1.getZ() < l2.getZ() ? l2.getZ() : l1.getZ();

        ArrayList<Location> l = new ArrayList<Location>();

        World w = l1.getBukkitLocation().getWorld();

        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    l.add(new Location(w, x, y, z));
                }
            }
        }

        return l;
    }


}
