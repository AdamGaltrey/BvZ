package com.adamgaltrey.bvz;

import com.adamgaltrey.bvz.data.IonCannonData;
import com.adamgaltrey.bvz.data.WaveData;
import com.adamgaltrey.bvz.entities.BVZ_Trader;
import com.adamgaltrey.bvz.utils.Loc;
import com.adamgaltrey.bvz.entities.barriers.BarrierPreBuilder;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import com.adamki11s.minigameinstance.//MinigameAPI;

public class Config {

    public static int lockVip, maxPlayers, startTimerSeconds, turretCost, turretCooldownSeconds, lordPoints, endGameDurationSeconds;

    public static double turretRange;

    private static Loc playerSpawnLocation;

    public static int minY, commsPort, gameMinutes;

    private static Loc[] mobSpawnArea;

    public static Loc[] lavaPit;

    // maps uniquename of the barrier to Loc{barrierPurchase, barrierTarget}
    // we will scan blocks to generate barrier
    private static final Map<String, Loc[]> barriers = new HashMap<String, Loc[]>();
    public static final Map<String, Integer> barrierCost = new HashMap<String, Integer>();
    // barriers:
    // barriername:
    // purchase:
    // actual:

    // wave data
    private static Map<Integer, WaveData> waves = new HashMap<Integer, WaveData>();

    // ion cannon data
    private static IonCannonData ionCannonData;

	/*
     * waves: wavenumber: spawndelay: minMS - maxMS mobs: - type:%spawn -
	 * type2:%spawn
	 */

	/*
     * Ion Cannon Data ioncannon: receiveat: <wave> cooldown: <seconds>
	 * radiusofeffect: <blocks>(double) sloweffect: power: duration:
	 */

    public static double minX, minZ, maxX, maxZ, floorY, pushForce;

    public static boolean xLong;

    static double minMobX, minMobZ, maxMobX, maxMobZ;

    public static String world, redirectServer, commsHost, thisID;

    public static Loc traderLocation, mapMiddle;

    protected static String sqlHost, sqlDB, sqlUser, sqlPass;

    public static int[] upgradeDamageCosts = new int[5];
    public static int[] upgradeKnockbackCosts = new int[2];

    public static void initConfig() {
        // gamedata = mapname
        File config = new File(BVZ.root + File.separator + "config.yml");
        System.out.println("Using config : " + config.getAbsolutePath());

        final FileConfiguration io = YamlConfiguration.loadConfiguration(config);

        if (!config.exists()) {
            try {
                config.createNewFile();

                //SQL

                io.set("sql.host", "localhost");
                io.set("sql.database", "minecraft");
                io.set("sql.username", "admin");
                io.set("sql.password", "password");

                //end game
                io.set("end.game.duration.seconds", 9);

                //for explosions
                io.set("map.middle", "world,0,70,-29");

                // generate default
                io.set("minigame.comms.host", "localhost");
                io.set("minigame.comms.port", 34560);
                io.set("minigame.comms.id.this", "bvz1");

                io.set("bow.upgrade.damage.1", 10);
                io.set("bow.upgrade.damage.2", 25);
                io.set("bow.upgrade.damage.3", 40);
                io.set("bow.upgrade.damage.4", 80);
                io.set("bow.upgrade.damage.5", 150);

                io.set("bow.upgrade.knockback.1", 100);
                io.set("bow.upgrade.knockback.2", 250);

                io.set("game.duration.minutes", 12);

                io.set("redirect.server", "hub");
                io.set("timer.countdown", 45);
                io.set("turret.cost", 40);
                io.set("turret.cooldown.seconds", 30);
                io.set("turret.range", 4.5);
                io.set("lava.pit.1", "world,144,10,160");
                io.set("lava.pit.2", "world,129,10,160");

                io.set("trader.spawn", "world,0,77,-54");
                io.set("player.spawn", "world,137,20,156");
                io.set("player.minY", 19);
                io.set("mob.spawn.1", "world,143,16,197");
                io.set("mob.spawn.2", "world,132,17,202");

                io.set("ioncannon.receiveat", 2);
                io.set("ioncannon.cooldown", 60);
                io.set("ioncannon.radiusofeffect", 5);
                io.set("ioncannon.sloweffect.power", 1);
                io.set("ioncannon.sloweffect.duration", 5);

                io.set("barriers.testbarrier.purchase", "world,143,23,157");
                io.set("barriers.testbarrier.cost", 100);
                io.set("barriers.testbarrier.actual.1", "world,141,14,188");
                io.set("barriers.testbarrier.actual.2", "world,132,14,187");

				/*
                 * for (String mobtype :
				 * io.getConfigurationSection("mobs").getKeys(false)) { String
				 * range = io.getString("mobs." + mobtype); String[] data =
				 * range.split("-");&
				 */
                io.set("waves.1.spawndelay.min", 2000);
                io.set("waves.1.spawndelay.max", 4000);
                // only bottom value is inclusive
                io.set("waves.1.mobs.ZOMBIE", "0-100.1");

                io.set("waves.2.spawndelay.min", 2000);
                io.set("waves.2.spawndelay.max", 4000);
                io.set("waves.2.mobs.ZOMBIE", "0-100.1");

                io.set("waves.3.spawndelay.min", 2000);
                io.set("waves.3.spawndelay.max", 4000);
                io.set("waves.3.mobs.ZOMBIE", "0-100.1");

                io.set("waves.4.spawndelay.min", 2000);
                io.set("waves.4.spawndelay.max", 4000);
                io.set("waves.4.mobs.ZOMBIE", "0-100.1");

                io.set("waves.5.spawndelay.min", 2000);
                io.set("waves.5.spawndelay.max", 4000);
                io.set("waves.5.mobs.ZOMBIE", "0-80");
                io.set("waves.5.mobs.ARMOURED_ZOMBIE", "80-100.1");

                io.set("waves.6.spawndelay.min", 2000);
                io.set("waves.6.spawndelay.max", 4000);
                io.set("waves.6.mobs.ZOMBIE", "0-100.1");

                io.set("waves.7.spawndelay.min", 2000);
                io.set("waves.7.spawndelay.max", 4000);
                io.set("waves.7.mobs.ZOMBIE", "0-100.1");

                io.set("waves.8.spawndelay.min", 2000);
                io.set("waves.8.spawndelay.max", 4000);
                io.set("waves.8.mobs.ZOMBIE", "0-100.1");

                io.set("waves.9.spawndelay.min", 2000);
                io.set("waves.9.spawndelay.max", 4000);
                io.set("waves.9.mobs.ZOMBIE", "0-80");
                io.set("waves.9.mobs.ARMOURED_ZOMBIE", "80-100.1");

                io.set("waves.10.spawndelay.min", 2000);
                io.set("waves.10.spawndelay.max", 4000);
                io.set("waves.10.mobs.ZOMBIE", "0-100.1");

                io.set("waves.11.spawndelay.min", 2000);
                io.set("waves.11.spawndelay.max", 4000);
                io.set("waves.11.mobs.ZOMBIE", "0-100.1");

                io.set("waves.12.spawndelay.min", 2000);
                io.set("waves.12.spawndelay.max", 4000);
                io.set("waves.12.mobs.ZOMBIE", "0-80");
                io.set("waves.12.mobs.ARMOURED_ZOMBIE", "80-100.1");

                io.set("players.vip", 4);
                io.set("players.max", 5);
                io.save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 1; i <= 5; i++) {
            upgradeDamageCosts[i - 1] = io.getInt("bow.upgrade.damage." + i);

            if (i <= 2) {
                upgradeKnockbackCosts[i - 1] = io.getInt("bow.upgrade.knockback." + i);
            }
        }

        sqlHost = io.getString("sql.host");
        sqlDB = io.getString("sql.database");
        sqlUser = io.getString("sql.username");
        sqlPass = io.getString("sql.password");

        mapMiddle = new Loc(io.getString("map.middle"));

		/*
		 * Ion Cannon Data ioncannon: receiveat: <wave> cooldown: <seconds>
		 * radiusofeffect: <blocks> sloweffect: power: duration:
		 */

        endGameDurationSeconds = io.getInt("end.game.duration.seconds");

        gameMinutes = io.getInt("game.duration.minutes") + 1;
        thisID = io.getString("minigame.comms.id.this");
        redirectServer = io.getString("redirect.server");
        commsHost = io.getString("minigame.comms.host");
        commsPort = io.getInt("minigame.comms.port");

        BVZ.logger.info("Redirect server registered [" + redirectServer + "].");

        traderLocation = new Loc(io.getString("trader.spawn"));
        lockVip = io.getInt("players.vip");
        lordPoints = io.getInt("lord.defaultpoints", 50);
        // lockVip = Integer.parseInt("1");
        maxPlayers = io.getInt("players.max");
        // maxPlayers = Integer.parseInt("1");
        turretCost = io.getInt("turret.cost", 1);
        turretCooldownSeconds = io.getInt("turret.cooldown.seconds", 40);
        turretRange = io.getDouble("turret.range", 4.5);
        startTimerSeconds = io.getInt("timer.countdown");
        playerSpawnLocation = new Loc(io.getString("player.spawn"));
        minY = io.getInt("player.minY");
        mobSpawnArea = new Loc[2];
        mobSpawnArea[0] = new Loc(io.getString("mob.spawn.1"));
        mobSpawnArea[1] = new Loc(io.getString("mob.spawn.2"));

        double xD = Math.abs(mobSpawnArea[0].getX() - playerSpawnLocation.getX()), zD = Math.abs(mobSpawnArea[0].getZ() - playerSpawnLocation.getZ());

        //the arena spans more in the X direction if the X distance is larger
        xLong = xD > zD;

        Bukkit.getScheduler().runTask(BVZ.p, new Runnable() {

            @Override
            public void run() {
                List<Location> allLava = BarrierPreBuilder.getLocList(new Loc(io.getString("lava.pit.1")),
                        new Loc(io.getString("lava.pit.2")));
                Loc[] a = new Loc[allLava.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = new Loc(allLava.get(i));
                }
                lavaPit = a;
                // WalkGoalUpdater.w = lava.getBukkitLocation().getWorld();
                //System.out.println("Lava pit size = " + a.length);
            }
        });

        ionCannonData = new IonCannonData(io.getConfigurationSection("ioncannon"));

        for (String waveNum : io.getConfigurationSection("waves").getKeys(false)) {
            int n = Integer.parseInt(waveNum);
            waves.put(n, new WaveData(io.getConfigurationSection("waves." + waveNum)));
        }

        if (waves.size() > gameMinutes) {
            BVZ.logger.warning("You have more waves than there are game minutes. The game will still run normally but some waves will not be processed as the" +
                    " game will end before they have a chance to run.");
        } else if (waves.size() < gameMinutes) {
            BVZ.logger.severe("*********** SEVERE ERROR ***********");
            for (int i = 0; i < 3; i++) {
                BVZ.logger.severe("You have less waves defined (" + waves.size() + ") than game minutes (" + gameMinutes + "). Please define more waves or " +
                        "reduce the game minutes to the same number of defined waves!");
            }
            Bukkit.getPluginManager().disablePlugin(BVZ.p);
            return;
        }

        // barriers:
        // barriername:
        // purchase:
        // actual:

        // 0=purchase loc
        for (final String barrierName : io.getConfigurationSection("barriers").getKeys(false)) {
            final Loc[] l = new Loc[3];
            l[0] = new Loc(io.getString("barriers." + barrierName + ".purchase"));
            l[1] = new Loc(io.getString("barriers." + barrierName + ".actual.1"));
            l[2] = new Loc(io.getString("barriers." + barrierName + ".actual.2"));
            barriers.put(barrierName, l);
            barrierCost.put(barrierName, io.getInt("barriers." + barrierName + ".cost"));
            Bukkit.getScheduler().runTask(BVZ.p, new Runnable() {

                @Override
                public void run() {
                    List<Location> bar = BarrierPreBuilder.getLocList(l[1], l[2]);
                    Loc[] a = new Loc[bar.size()];
                    for (int i = 0; i < a.length; i++) {
                        a[i] = new Loc(bar.get(i));
                    }
                    BarrierPreBuilder.pre.put(barrierName, a);
                }
            });
        }

        // pre process and cache for location checks
        double mobX = mobSpawnArea[0].getX(), mobZ = mobSpawnArea[0].getZ(), mobX2 = mobSpawnArea[1].getX(), mobZ2 = mobSpawnArea[1].getZ();

        // mobx = 5, mobx2 = 5
        minMobX = mobX > mobX2 ? mobX2 : mobX;
        maxMobX = mobX > mobX2 ? mobX : mobX2;

        minMobZ = mobZ > mobZ2 ? mobZ2 : mobZ;
        maxMobZ = mobZ > mobZ2 ? mobZ : mobZ2;

        floorY = mobSpawnArea[0].getY();
        world = mobSpawnArea[0].getWorld();

        /*
            Spawn villager
         */

        Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {
            @Override
            public void run() {
                Location l = traderLocation.getBukkitLocation();
                World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
                final BVZ_Trader customEntity = new BVZ_Trader(mcWorld);
                customEntity.setLocation(l.getX(), l.getY() + 1, l.getZ(), -170, 0);
                mcWorld.addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            }
        }, 0);

        //contact hub
        //server-id : game-state : players : max-players
        //SocketSender.sendData(new String[]{"id", "state", "players", "maxplayers"}, new Object[]{Config.thisID, "LOADING", 0, 4});
    }

    public static int getLockVip() {
        return lockVip;
    }

    public static int getMaxPlayers() {
        return maxPlayers;
    }

    public static int getStartTimerSeconds() {
        return startTimerSeconds;
    }

    public static Loc getPlayerSpawnLocation() {
        return playerSpawnLocation;
    }

    public static Loc[] getMobSpawnArea() {
        return mobSpawnArea;
    }

    public static Map<String, Loc[]> getBarriers() {
        return barriers;
    }

    public static Map<Integer, WaveData> getWaves() {
        return waves;
    }

    public static IonCannonData getIonCannonData() {
        return ionCannonData;
    }

    public static boolean isOutOfBounds(double x, double z) {
        // find if in between the two points
        return (x >= minX && x <= maxX && z >= minZ && z <= maxZ);
    }

    private static final Random r = new Random();

    public static Location getRandomSpawnLocation() {
        int x = r.nextInt((int) Math.abs(maxMobX - minMobX)), z = r.nextInt((int) Math.abs(maxMobZ - minMobZ));
        return new Location(Bukkit.getWorld(world), minMobX + x, floorY, minMobZ + z);
    }

}
