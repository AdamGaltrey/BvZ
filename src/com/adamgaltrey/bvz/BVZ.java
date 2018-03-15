package com.adamgaltrey.bvz;

import com.adamgaltrey.bvz.game.GUIShop;
import com.adamgaltrey.bvz.game.Game;
import com.adamgaltrey.bvz.utils.BungeeUtils;
import com.adamgaltrey.bvz.utils.FileUtils;
import com.adamgaltrey.bvz.entities.CustomEntityType;
import com.adamki11s.minigamecomms.instance.Comms;
import com.adamki11s.minigamecomms.instance.MinigamePayload;
import com.adamki11s.minigamecomms.instance.data.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.logging.Logger;

//import com.adamki11s.minigameinstance.//MinigameAPI;

public class BVZ extends JavaPlugin {

    // public static Game game;

    public static final File root = new File("plugins" + File.separator + "BvZ");

    public static Plugin p;

    public static Game game;

    public static Logger logger;

    static boolean reload = false;

    public static final String startPerm = "bvz.admin.start";

    BukkitTask updater;

    @Override
    public void onEnable() {

        logger = getLogger();

        Bukkit.getPluginManager().addPermission(new Permission(startPerm));
        getLogger().info("Registered permission for (/start) as " + startPerm);

        class AlwaysOnListener implements Listener {

            AlwaysOnListener(Plugin p) {
                Bukkit.getPluginManager().registerEvents(this, p);
            }

            @EventHandler
            private void extend(BlockPistonExtendEvent evt) {
                evt.setCancelled(true);
            }

            @EventHandler
            private void entityInteract(PlayerInteractEntityEvent evt) {
                if (evt.getRightClicked().getType().equals(EntityType.VILLAGER)) {
                    evt.setCancelled(true);
                    if (!Game.gameStarted) {
                        evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The game has not " +
                                "started yet!"));
                    } else if (!game.shuttingDown) {
                        new GUIShop(evt.getPlayer());
                    }
                }
            }


            @EventHandler
            private void drop(PlayerDropItemEvent evt) {
                evt.setCancelled(true);
            }

        }

        new AlwaysOnListener(this);

        p = this;
        // we don't need to check for null as in the plugin.yml we have set
        // So if it doesn't exist this plugin will not load

        getLogger().info("Using directory /plugins/BvZ");
        root.mkdir();

        Config.initConfig();

        SQLManager.init();

        //game = new Game(this);

        // getCommand("leave").setExecutor(game);
        /*
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				for(World w : Bukkit.getWorlds()){
					for(LivingEntity e : w.getLivingEntities()){
						if(!(e instanceof Player)){
							e.remove();
						}
					}
				}
				MinigameAPI.registerGame(map.get("map"), Integer.parseInt(map.get("vip")), Integer.parseInt(map.get("max")));
			}
		}, 200);*/

        // delete current world
        String wn = Config.world;


        if (!reload) {
            File w = new File(wn);
            if (w.exists()) {
                getLogger().info("Deleting '" + wn + "'...");
                FileUtils.deleteDirectory(w);
                getLogger().info("Deleted '" + wn + "' successfully.");
            }

            getLogger().info("Unzipping saved world... [" + wn + "]");

            // expect world name to be called <mapname>.zip
            // FileUtils.unZipIt(new File("plugins" + File.separator + "BvZ" +
            // File.separator + map.get("map") + ".zip"), w);
            w.mkdir();
            FileUtils.unZipIt(new File("plugins" + File.separator + "BvZ" + File.separator + wn + ".zip"), w);
            getLogger().info("Saved world unzipped successfully!");
        }


        // Config.initConfig("world");

        game = new Game(this);

        // register custom entities
        CustomEntityType.registerEntities();

        BungeeUtils.registerChannels(this);

        /*updater = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                //update every second
                SocketSender.sendData(new String[]{"id", "state", "players", "maxplayers"}, new Object[]{Config.thisID,
                        Game.gameStarted ? "GAME" : "LOBBY", Bukkit.getOnlinePlayers().size(), 4});
            }
        }, 0, 20);*/

        Comms.registerHandler(this, new MinigamePayload() {
            @Override
            //get the number of online players, as defined by your plugin, eg: Bukkit.getOnlinePlayers().size() - spectators or Bukkit.getOnlinePlayers().size()
            public int getPlayersOnline() {
                return Bukkit.getOnlinePlayers().size();
            }

            @Override
            //return the maximum number of players as defined by your minigame, eg: MyGame.getMaxPlayers()
            public int getMaxPlayers() {
                return Config.getMaxPlayers();
            }

            @Override
            public GameState getGameState() {
                //return the current game state, it has to be either LOBBY or GAME
                return Game.gameStarted ? GameState.GAME : GameState.LOBBY;
            }

            @Override
            public void connectionFailed() {
                //you can do something specific if a connection to the hub server fails, but normally you will probably want to leave this blank
            }

            @Override
            public void connectionSuccess() {
                //you can do something specific if a connection to the hub server is successful, but normally you will probably want to leave this blank
            }
        });
    }

    @Override
    public void onDisable() {
        if (updater != null) {
            updater.cancel();
        }
        //SocketSender.sendData(new String[]{"id", "state", "players", "maxplayers"}, new Object[]{Config.thisID, "REBOOTING", 0, 4});
    }

}
