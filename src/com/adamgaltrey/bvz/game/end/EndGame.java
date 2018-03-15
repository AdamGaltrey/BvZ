package com.adamgaltrey.bvz.game.end;

import com.adamgaltrey.bvz.utils.InstantFirework;
import com.adamgaltrey.bvz.Config;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Created by Adam on 25/08/2015.
 */
public class EndGame implements Runnable {

    private int duration = Config.endGameDurationSeconds;
    private int tickStutter = 8;

    private Runnable callback;
    private final Location mapMiddle, pSpawn;
    private final int maxRange;
    private final Random rand = new Random();

    private final boolean won;

    /*
        Scheduler to run async every tick
     */
    public EndGame(Runnable callback, boolean won) {
        this.won = won;
        this.callback = callback;
        mapMiddle = Config.mapMiddle.getBukkitLocation();
        pSpawn = Config.getPlayerSpawnLocation().getBukkitLocation();
        maxRange = (int) mapMiddle.distance(pSpawn);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setAllowFlight(true);
            p.setFlying(true);
            //p.setVelocity(new Vector(0.3, 2, 0.3));
        }

        for (LivingEntity e : pSpawn.getWorld().getLivingEntities()) {
            if (!(e instanceof Player) && !e.getType().equals(EntityType.VILLAGER)) {
                e.setVelocity(new Vector(((double)rand.nextInt(2) - 1) * rand.nextDouble(), rand.nextInt(3) + 3,
                        ((double)rand.nextInt(2) - 1) * rand.nextDouble()));
            }
        }
    }

    int ticks = 0;
    int curStutter = 0;
    int fwkCount = 0;

    private InstantFirework frwk = new InstantFirework();
    FireworkEffect fx = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withColor(Color.ORANGE).withColor(Color.YELLOW).trail(true)
            .withFade(Color.BLACK).flicker(true).build();

    FireworkEffect WONfx = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.LIME).withColor(Color.WHITE).withColor(Color.AQUA)
            .withColor(Color.RED).trail(true).withFade(Color.WHITE).flicker(true).build();


    @Override
    public void run() {
        if (callback == null) {
            return;
        }

        ticks++;
        curStutter++;

        if (curStutter >= tickStutter) {
            curStutter = 0;
            fwkCount++;

            //play fx

            //y offset -3 -> 3
            Location targ = null;

            //keep explosions away from the player spawn

            if(!won) {
                while (targ == null || targ.distance(pSpawn) <= 9) {
                    int xoff = rand.nextInt(maxRange * 2) - maxRange, zoff = rand.nextInt(maxRange * 2) - maxRange, yoff = rand.nextInt(8) - 2;

                    targ = new Location(mapMiddle.getWorld(), mapMiddle.getX() + xoff, mapMiddle.getY() + yoff, mapMiddle.getZ() + zoff);
                }

                targ.getWorld().createExplosion(targ.getX(), targ.getY(), targ.getZ(), 5F, true, true);
            } else {
                //we won, more fireworks higher in the sky, keep them close to the player
                while (targ == null || targ.distance(pSpawn) > 35) {
                    int xoff = rand.nextInt(maxRange * 2) - maxRange, zoff = rand.nextInt(maxRange * 2) - maxRange, yoff = rand.nextInt(20) + 5;

                    targ = new Location(mapMiddle.getWorld(), mapMiddle.getX() + xoff, mapMiddle.getY() + yoff, mapMiddle.getZ() + zoff);
                }

                //targ.getWorld().createExplosion(targ.getX(), targ.getY(), targ.getZ(), 5F, true, true);
            }

           /* for (int i = 0; i < 4; i++) {
                FallingBlock fb = targ.getWorld().spawnFallingBlock(targ, ids[rand.nextInt(ids.length)], (byte) 0);
                fb.setVelocity(new Vector(rand.nextFloat() / 6F * (rand.nextBoolean() ? -1 : 1), rand.nextFloat() / 6F * (rand.nextBoolean() ? -1 : 1),
                        rand.nextFloat() / 6F
                        * (rand.nextBoolean() ? -1 : 1)).multiply(12));
            }*/

            frwk.firework(won ? WONfx : fx, targ.add(0, 4, 0));
        }

        if (ticks >= 20) {
            duration--;
            ticks = 0;

            if (duration <= 0) {
                callback.run();
                callback = null;
            }
        }
    }
}
