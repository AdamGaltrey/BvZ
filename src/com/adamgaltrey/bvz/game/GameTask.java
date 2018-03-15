package com.adamgaltrey.bvz.game;

import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.SQLManager;
import com.adamgaltrey.bvz.data.BVZEntity;
import com.adamgaltrey.bvz.data.PlayerScoreboard;
import com.adamgaltrey.bvz.data.WaveData;
import com.adamgaltrey.bvz.entities.BVZ_Zombie;
import com.adamgaltrey.bvz.entities.GoalUpdater;
import com.adamgaltrey.bvz.fx.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameTask implements Runnable, Listener {

    private static BukkitTask task;

    private static byte tickMarker = 0;

    private static int secondsElapsed = 0;

    private static int wave = 1;

    private static WaveData waveData = Config.getWaves().get(wave);

    private static long intermittentTicks = 0;
    private static int msWait = 0;

    public static Set<Integer> alive = new HashSet<Integer>();
    public static Map<Integer, String> lastHurt = new HashMap<Integer, String>();

    public GameTask() {
        Bukkit.getPluginManager().registerEvents(this, BVZ.p);
        if (wave == Config.getIonCannonData().getWaveReceived()) {
            // give to player with most points
            String s = PlayerScoreboard.getWhoHasMostPoints();
            Player p = Bukkit.getPlayerExact(s);
            p.getInventory().addItem(IonCannon.ionCannon);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.YELLOW + p.getName() + " &ahas received the ion cannon!"));
        }
    }

    @EventHandler
    private void spawn(CreatureSpawnEvent evt) {
        if (evt.getSpawnReason() != SpawnReason.CUSTOM) {
            evt.setCancelled(true);
        }
    }

    private static long lastKill;
    private static int killStreak = 0;

    private Map<UUID, Integer> waveKills = new HashMap<UUID, Integer>();

    private void addKill(UUID uuid) {
        if (waveKills.containsKey(uuid)) {
            waveKills.put(uuid, waveKills.get(uuid) + 1);
        } else {
            waveKills.put(uuid, 1);
        }
    }

    @EventHandler
    private void entityDeath(EntityDeathEvent evt) {
        evt.getDrops().clear();
        evt.setDroppedExp(0);

        GoalUpdater.died(evt.getEntity().getEntityId());

        if (alive.remove(evt.getEntity().getEntityId())) {
            // entity is one we spawned

            LivingEntity e = evt.getEntity();
            Player killer = e.getKiller();
            if (killer != null) {
                PlayerScoreboard.addPoints(killer, Game.lords.contains(killer.getName()) ? 2 : 1, true);
                addKill(killer.getUniqueId());
            } else if (lastHurt.containsKey(e.getEntityId())) {
                Player l = Bukkit.getPlayerExact(lastHurt.remove(e.getEntityId()));
                if (l != null) {
                    PlayerScoreboard.addPoints(l, Game.lords.contains(l.getName()) ? 2 : 1, true);
                    addKill(l.getUniqueId());
                }
            }

            if (System.currentTimeMillis() - lastKill <= 4000 && !Game.shuttingDown) {
                // if killed within 3 seconds or less
                killStreak++;
                if (killStreak >= 3) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.YELLOW
                            + "Your team is on a " + killStreak + " mob kill streak!"));
                    /*for (Player p : Bukkit.getOnlinePlayers()) {
                        p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
						p.updateInventory();
					}*/
                    //todo: Removed gold ingots, points will be used as currency
                }
            } else {
                killStreak = 0;
            }

            lastKill = System.currentTimeMillis();

            /*
            if (waveData == null && GoalUpdater.goals.size() == 0) {
                // end the game, you won! ONLY END WHEN ALL ZOMBIES ARE DEAD

                for (Player p : Bukkit.getOnlinePlayers()) {
                    SQLManager.updateEndOfGame(p.getUniqueId(), false);
                }

                BVZ.game.terminateServer(true);
                task.cancel();
                return;
            }*/
        }
    }

    private static int totalTime = 60 * (Config.gameMinutes - 1);

    @Override
    public void run() {

        if (BVZ.game.shuttingDown) {
            return;
        }

        // run every tick, that means the resolution is 50 milliseconds
        tickMarker++;
        intermittentTicks++;

        if (tickMarker >= 20) {
            tickMarker = 0;

            if (waveData != null) {
                secondsElapsed++;
                totalTime--;
            } else {

                for (LivingEntity e : BVZ_Zombie.bukkitWorld.getLivingEntities()) {
                    if (e instanceof Zombie) {
                        e.remove();
                    }
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    SQLManager.updateEndOfGame(p.getUniqueId(), true);
                }
                BVZ.game.terminateServer(true);
            }

            PlayerScoreboard.updateSecondsElapsed(totalTime);

            if (secondsElapsed >= 60) {
                // begin next wave
                wave++;

                if((wave + 1) == Config.gameMinutes){
                    Title title = new Title("", "Final Wave, Good Luck!", 2, 2, 2);
                    title.setSubtitleColor(ChatColor.RED);
                    title.broadcast();
                } else if(wave < Config.gameMinutes){
                    Title title = new Title("", "Wave " + wave, 2, 2, 2);
                    title.setTitleColor(ChatColor.WHITE);
                    title.setSubtitleColor(ChatColor.RED);
                    title.broadcast();
                }

                if (wave > 1) {
                    //System.out.println("Wave = " + wave + "// stats size = " + waveKills.size());
                    //update stats for previous wave
                    for (Map.Entry<UUID, Integer> e : waveKills.entrySet()) {
                        SQLManager.updateEndOfWave(e.getKey(), e.getValue(), wave - 1);
                        //System.out.println("Updated stats for wave = " + (wave - 1) + ", kills = " + e.getValue());
                    }
                    waveKills.clear();
                }

                if (wave >= Config.gameMinutes) {
                    waveData = null;
                } else if (wave <= Config.gameMinutes && Config.getWaves().containsKey(wave)) {
                    // continue sending
                    waveData = Config.getWaves().get(wave);
                    // let new mobs spawn
                    msWait = 0;
                   // Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.YELLOW + "Wave &a" + wave
                     //       + "&c has begun!"));

                    if (wave == Config.getIonCannonData().getWaveReceived()) {
                        // give to player with most points
                        String s = PlayerScoreboard.getWhoHasMostPoints();
                        Player p = Bukkit.getPlayerExact(s);
                        p.getInventory().addItem(IonCannon.ionCannon);
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.YELLOW + p.getName() + " &ahas " +
                                "received the ion cannon!"));
                    }
                }


                secondsElapsed = 0;
            }
        }


        if (waveData != null) {
            // send a mob
            if (intermittentTicks * 50 > msWait) {
                // each tick represents 50s, as 20 ticks per second
                intermittentTicks = 0;
                msWait = waveData.getRandomDelay();
                BVZEntity e = waveData.getRandomEntity();
                if (e == null) {
                    System.out.println("Selected entity was null! Check wave configuration.");
                } else {
                    int eid = e.spawn(Config.getRandomSpawnLocation());
                    alive.add(eid);
                    /*for (World w : Bukkit.getWorlds()) {
                        for (LivingEntity le : w.getLivingEntities()) {
                            if (le.getEntityId() == eid) {
                                le.setCustomName(ChatColor.RED + "BvZ");
                                le.setCustomNameVisible(true);
                            }
                        }
                    }*/
                }
            }
        }
    }

    private boolean livingZombies() {
        for (LivingEntity e : BVZ_Zombie.bukkitWorld.getLivingEntities()) {
            if (e instanceof Zombie) {
                return true;
            }
        }
        return false;
    }

    public void setTask(BukkitTask t) {
        task = t;
    }

}
