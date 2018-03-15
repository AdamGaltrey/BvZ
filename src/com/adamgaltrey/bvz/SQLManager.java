package com.adamgaltrey.bvz;

import com.adamgaltrey.bvz.data.PlayerStats;
import com.adamgaltrey.bvz.sql.SyncSQL;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Adam on 24/08/2015.
 */
public class SQLManager {

    private static final String table = "bvzstats";

    private static SyncSQL sql;

    public static boolean statsEnabled = true;

    private static Map<UUID, PlayerStats> stats = new HashMap<UUID, PlayerStats>();

    public static PlayerStats getStats(UUID uuid) {
        return stats.get(uuid);
    }

    public static void init() {
        sql = new SyncSQL(Config.sqlHost, Config.sqlDB, Config.sqlUser, Config.sqlPass);

        BVZ.logger.info("Connecting to MySQL server...");

        if (sql.initialise()) {
            BVZ.logger.info("MySQL connection established.");

            try {
                if (!sql.doesTableExist(table)) {
                    //lets create our table
                    sql.standardQuery("CREATE TABLE " + table + " (`id` INTEGER UNIQUE PRIMARY KEY AUTO_INCREMENT, `uuid` VARCHAR(50) UNIQUE NOT NULL, " +
                            "`gamesplayed` BIGINT UNSIGNED DEFAULT 0, `gameswon`  BIGINT UNSIGNED DEFAULT 0, `wavescleared`  BIGINT UNSIGNED DEFAULT 0, " +
                            "`zombiekills` BIGINT UNSIGNED DEFAULT 0, `maxwave` SMALLINT UNSIGNED DEFAULT 0);");

                    BVZ.logger.info("Created BvZ stats table (" + table + ").");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } else {
            BVZ.logger.severe("Failed to connect to MySQL. Stats are disabled!");
            statsEnabled = false;
        }
    }

    public static void onQuit(final UUID uuid) {
        if (stats.containsKey(uuid)) {
            stats.remove(uuid);
        }
    }

    /*
     private long gamesPlayed, gamesWon, wavesCleared, zombieKills, maxWave;
     */

    public static void updateEndOfGame(UUID uuid, boolean won) {
        if (!statsEnabled) {
            return;
        }
        if (!won) {
            asyncStatement("UPDATE " + table + " SET gamesplayed=gamesplayed+1 WHERE uuid='" + uuid.toString() + "';");
        } else {
            asyncStatement("UPDATE " + table + " SET gamesplayed=gamesplayed+1, gameswon=gameswon+1 WHERE uuid='" + uuid.toString() + "';");
        }
    }

    public static void updateEndOfWave(UUID uuid, int zombieKills, int wave) {
        if (!statsEnabled) {
            return;
        }
        PlayerStats ss = stats.get(uuid);
        ss.addZombieKills(zombieKills);
        ss.waveCleared(wave);

        asyncStatement("UPDATE " + table + " SET wavescleared=wavescleared+1, zombiekills=zombiekills+" + zombieKills + ", " +
                "maxwave=" + ss.getMaxWave() + " WHERE uuid='" + uuid.toString() + "';");
    }

    public static void onJoin(final UUID uuid) {
        if (!statsEnabled) {
            return;
        }
        final String uuidString = uuid.toString();
        Bukkit.getScheduler().runTaskAsynchronously(BVZ.p, new Runnable() {
            @Override
            public void run() {
                try {
                    ResultSet set = sql.sqlQuery("SELECT * FROM " + table + " WHERE uuid='" + uuidString + "';");

                    if (set.isBeforeFirst()) {
                        set.next();
                        //and load
                        PlayerStats ss = new PlayerStats(set.getLong("gamesplayed"), set.getLong("gameswon"), set.getLong("wavescleared"),
                                set.getLong("zombiekills"), set.getLong("maxwave"));
                        set.close();

                        stats.put(uuid, ss);
                    } else {
                        set.close();
                        //insert and init
                        SQLManager.sql.standardQuery("INSERT INTO " + table + " (uuid) VALUES ('" + uuidString + "');");
                        stats.put(uuid, new PlayerStats(0, 0, 0, 0, 0));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void asyncStatement(final String stmt) {
        if (!statsEnabled) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(BVZ.p, new Runnable() {
            @Override
            public void run() {
                try {
                    SQLManager.sql.standardQuery(stmt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
