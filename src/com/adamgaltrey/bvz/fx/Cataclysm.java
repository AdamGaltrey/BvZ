package com.adamgaltrey.bvz.fx;

import com.adamgaltrey.bvz.utils.ParticleEffect;
import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.entities.GoalUpdater;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Adam on 25/08/2015.
 */
public class Cataclysm {

    private final Location source;

    private List<Block> points = new LinkedList<Block>();

    private final Random r = new Random();

    public static int activeCataclysms = 0;

    public Cataclysm(Location source) {
        this.source = source;

        Block b = source.getBlock();

        for (int y = 1; y <= 5; y++) {
            int radius = 0;

            if (y <= 3) {
                radius = 2;
            } else if (y == 4) {
                radius = 1;
            } else {
                radius = 0;
            }

            //3 @y1, 2 @y2, 1 @y3
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block at = b.getRelative(x, y, z);
                    if ((at.getType().equals(Material.AIR) || at.getTypeId() == 37 || at.getTypeId() == 38) && (Math.abs(x) == radius || Math.abs(z)
                            == radius) && !at.getType().equals(Material.FENCE)) {
                        //Add to potential point
                        points.add(at);
                    }
                }
            }
        }
    }

    final int speed = 1;
    final double secondsDuration = 4.5;

    ParticleEffect fx = new ParticleEffect(ParticleEffect.ParticleType.FIREWORKS_SPARK, 1, 1, 0, 1, 0);

    public void init() {
        GoalUpdater.forceRepath();
        BukkitRunnable run = new BukkitRunnable() {

            int index = 0;

            Material[] steps = new Material[]{Material.BEDROCK, Material.GLASS, Material.BEDROCK, Material.SANDSTONE, Material.SANDSTONE};
            int matIndex = 0;
            int y = 0;

            @Override
            public void run() {
                if (index < points.size()) {
                    //build a point, retrieved from the top
                    Block b = points.get(index);


                    if (y == 0) {
                        y = b.getY();
                    } else if (b.getY() > y) {
                        y = b.getY();
                        matIndex++;
                    }

                    //Material m = mats[r.nextInt(mats.length)];

                    b.setType(steps[matIndex]);
                    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, steps[matIndex].getId());

                    if(index + 1 == points.size()) {
                        //last one
                        fx.sendToLocation(b.getLocation());
                    }

                    index++;
                } else {
                    BukkitRunnable destroy = new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (points.isEmpty()) {
                                GoalUpdater.forceRepath();
                                cancel();
                            } else {
                                //remove a point
                                Block b = points.remove(points.size() - 1);
                                Material type = b.getType();
                                if (!b.getType().equals(Material.FENCE)) {
                                    b.setType(Material.AIR);
                                }
                                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, type.getId());
                            }
                        }
                    };
                    destroy.runTaskTimer(BVZ.p, (int) (secondsDuration * 20D), speed);

                    cancel();
                }
            }
        };
        run.runTaskTimer(BVZ.p, 0, speed);
    }
}
