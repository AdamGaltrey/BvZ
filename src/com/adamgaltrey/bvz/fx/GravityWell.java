package com.adamgaltrey.bvz.fx;

import com.adamgaltrey.bvz.utils.ParticleEffect;
import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.entities.GoalUpdater;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 26/08/2015.
 */
public class GravityWell {

    private static final double radius = 16;

    private final Location source;
    private final Vector sourceVector;

    /*
        We will calculate the force to exert by the following formula

        Distance = d -> Distance from mob to point source

        The further away the mob is the weaker the force

        So exert force 1/d
     */

    public GravityWell(Location source) {
        this.source = source;
        this.sourceVector = source.toVector();
    }

    ParticleEffect fx = new ParticleEffect(ParticleEffect.ParticleType.PORTAL, 1, 8, 0, .5, 0);

    public void start() {
        BukkitRunnable run = new BukkitRunnable() {

            //5 seconds
            int durationTicks = 20 * 5;
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks >= durationTicks) {
                    cancel();

                    List<EntityWrapper> affected = getEntities();

                    /*
                    x = a + r cos(θ)
                    y = b + r sin(θ)
                     */

                    int n = 0;

                    int points = 10;
                    double radius = 2.46;

                    for (EntityWrapper wrapped : affected) {
                        LivingEntity e = wrapped.e;

                        n++;
                        if (n > 10) {
                            //10 points on our circle
                            n = 0;
                        }


                        double t = 2 * Math.PI * n / points;
                        int x = (int) Math.round(radius * Math.cos(t));
                        int z = (int) Math.round(radius * Math.sin(t));
                        e.setVelocity(new Vector(x * 0.28, 0, z * 0.28));
                    }

                    source.getWorld().createExplosion(source.getX(), source.getY() + 1.5, source.getZ(), 2.0F, false, false);
                    GoalUpdater.forceRepath();
                } else {
                    //affect
                    fx.sendToLocation(source);
                    fx.sendToLocation(source.getBlock().getRelative(BlockFace.UP).getLocation());

                    List<EntityWrapper> affected = getEntities();
                    for (EntityWrapper wrapped : affected) {
                        wrapped.applyForce();
                    }

                }
            }
        };

        run.runTaskTimer(BVZ.p, 0, 1);
    }

    class EntityWrapper {
        public LivingEntity e;
        private double force;
        private Vector applied;

        static final double genForce = .0065;

        public EntityWrapper(LivingEntity e) {
            this.e = e;
            double dist = e.getLocation().distance(source);

            if (dist <= radius) {
                //here for the force we want an exponential curve, tending to 0 at 0 distance

                /*
                    This seem ok

                    ( (2.718) ^ (0.1 * (x + 10)) - 2.5)
                 */

                //force = Math.exp(dist * 10) * genForce;

                force = (Math.exp(0.1D * (dist + 10D)) - 2D) / 8D;

                if (force > 3) {
                    force = 3;
                }

                //System.out.println(((int) dist) + "=" + force);
            }

            /*
                We want a vector in direction of O -> source, so vector = source - O
             */
            double dx = source.getX() - e.getLocation().getX(), dz = source.getZ() - e.getLocation().getZ();

            //need to 0 their velocity the closer they get
            //so velocity * dist/radius
            applied = new Vector(dx, 0.02, dz).normalize().multiply(force);
        }

        public void applyForce() {
            e.setVelocity(applied);
        }

    }

    private List<EntityWrapper> getEntities() {
        List<EntityWrapper> affected = new ArrayList<EntityWrapper>();
        for (Entity e : source.getWorld().getNearbyEntities(source, radius, 3, radius)) {
            if (e instanceof Zombie) {
                affected.add(new EntityWrapper((LivingEntity) e));
            }
        }
        return affected;
    }
}
