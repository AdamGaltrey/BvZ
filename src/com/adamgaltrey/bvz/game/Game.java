package com.adamgaltrey.bvz.game;

import com.adamgaltrey.bvz.data.PlayerScoreboard;
import com.adamgaltrey.bvz.data.PlayerStats;
import com.adamgaltrey.bvz.game.end.EndGame;
import com.adamgaltrey.bvz.utils.BungeeUtils;
import com.adamgaltrey.bvz.utils.InstantFirework;
import com.adamgaltrey.bvz.utils.Loc;
import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.SQLManager;
import com.adamgaltrey.bvz.entities.GoalUpdater;
import com.adamgaltrey.bvz.entities.barriers.BarrierManager;
import com.adamgaltrey.bvz.entities.turrets.TurretManager;
import com.adamgaltrey.bvz.entities.turrets.TurretThread;
import com.adamgaltrey.bvz.fx.Cataclysm;
import com.adamgaltrey.bvz.fx.GravityWell;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;

public class Game implements Listener {

    public static Countdown cd;

    public static GameTask gameTask;

    public static boolean countdownStarted, gameStarted;

    private static ItemStack turretItem;

    private Map<UUID, Location> last = new HashMap<UUID, Location>();

    public Game(Plugin p) {
        Bukkit.getPluginManager().registerEvents(this, p);

        // init ion cannon
        IonCannon.initItem();
        Bukkit.getScheduler().runTaskTimer(BVZ.p, new IonCannon(), 0, 20);

        // init turrets
        Bukkit.getScheduler().runTaskTimer(BVZ.p, new TurretThread(), 0, 10);

        // init scoreboard
        PlayerScoreboard.init();

        IonCannon.initItem();

        // init goal task
        Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {

            @Override
            public void run() {
                Bukkit.getScheduler().runTaskTimer(BVZ.p, new GoalUpdater(), 0, 10);
                //TODO weather!?
                //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/weather clear " + Integer.MAX_VALUE);
            }
        }, 40);

        Bukkit.getScheduler().runTaskTimer(BVZ.p, new Runnable() {

            @Override
            public void run() {
                if (!shuttingDown) {
                    for (Player p : Bukkit.getOnlinePlayers()) {

                        if (p.getLocation().getY() < Config.minY) {
                            p.teleport(last.get(p.getUniqueId()));
                            continue;
                        }

                        if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                            //they're on the ground
                            last.put(p.getUniqueId(), p.getLocation());
                        }
                    }
                }
            }
        }, 0, 3);

        Bukkit.getScheduler().runTaskTimer(BVZ.p, new Runnable() {
            @Override
            public void run() {
                // runs every 10 seconds
                for (World w : Bukkit.getWorlds()) {
                    w.setTime(0);
                }

				/*
                 * int threeMinsInMS = 1000 * 60 * 3; if (!gameStarted &&
				 * Bukkit.getOnlinePlayers().length == 0 &&
				 * System.currentTimeMillis() - lastJoin > threeMinsInMS) {
				 * terminateGame(); }
				 */
            }
        }, 0, 200);

        turretItem = new ItemStack(Material.BONE);
        ItemMeta m = turretItem.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Turret Wand");
        m.setLore(new ArrayList<String>(Arrays.asList(ChatColor.RESET + "Look at a turret and click to buy it.", ChatColor.RESET
                + "Click a purchased turret to change", ChatColor.RESET + "its state between fire and ice.")));
        turretItem.setItemMeta(m);
    }

    public void fireSpread(final BlockIgniteEvent evt) {
        if (evt.getCause() == IgniteCause.SPREAD) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    private void hit(ProjectileHitEvent evt) {
        if (evt.getEntity() instanceof Snowball) {

            Location l = evt.getEntity().getLocation();
            Random r = new Random();

            l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 2.0F, false, false);

            for (Entity e : l.getWorld().getNearbyEntities(l, 2.8, 2, 2.8)) {
                if (e instanceof LivingEntity && e instanceof Zombie) {
                    e.setVelocity(new Vector(0, 2.8, 0));
                    GoalUpdater.forcePathing(e.getEntityId());
                    GoalUpdater.died(e.getEntityId());
                }
            }

        }

        /*else if (evt.getEntity() instanceof Egg) {
            new GravityWell(evt.getEntity().getLocation()).start();
        }*/

        evt.getEntity().remove();
    }

    @EventHandler
    private void ignite(BlockIgniteEvent evt) {
        evt.setCancelled(true);
    }

    @EventHandler
    private void combust(EntityCombustEvent evt) {
        evt.setCancelled(true);
    }

    @EventHandler
    private void entityDMG(EntityDamageEvent evt) {
        if (evt.getEntity() instanceof Player || evt.getEntity().getType().equals(EntityType.VILLAGER)) {
            evt.setCancelled(true);
        } else if (evt.getEntity() instanceof Zombie) {
            if (gameStarted && !shuttingDown) {

                if (evt.getCause() == DamageCause.LAVA || evt.getCause() == DamageCause.FIRE) {
                    Bukkit.broadcastMessage(ChatColor
                            .translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You have lost!"));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        SQLManager.updateEndOfGame(p.getUniqueId(), false);
                    }

                    terminateServer(false);
                    return;
                }

                if (evt.getCause().equals(DamageCause.ENTITY_EXPLOSION) || evt.getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
                    evt.setCancelled(true);
                    return;
                }

                Zombie z = (Zombie) evt.getEntity();

                if (z.isBaby()) {
                    // 1 hit kill
                    evt.setDamage(1000);
                }

                if (evt instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) evt;

                    if (e.getDamager() instanceof Projectile) {
                        Projectile proj = (Projectile) e.getDamager();

                        if (proj.getShooter() instanceof Player) {
                            Player firer = ((Player) proj.getShooter());
                            GameTask.lastHurt.put(evt.getEntity().getEntityId(), (firer.getName()));

                            if (proj instanceof Arrow) {

                                //bleed
                                evt.getEntity().getWorld().playEffect(evt.getEntity().getLocation().add(0, 1, 0), Effect.STEP_SOUND,
                                        Material.REDSTONE_BLOCK.getId());

                                //check meta
                                Arrow arrow = (Arrow) proj;
                                if (arrow.hasMetadata("effect")) {

                                    //force constant pathing on this mob
                                    GoalUpdater.forcePathing(e.getEntity().getEntityId());

                                    String val = arrow.getMetadata("effect").get(0).asString();

                                    if (val.equals("slow")) {

                                        ItemStack helm = z.getEquipment().getHelmet();

                                        z.removePotionEffect(PotionEffectType.SLOW);

                                        if (helm != null) {
                                            if (helm.getType().equals(Material.CHAINMAIL_HELMET)) {
                                                //slow 1
                                                z.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                                            } else if (helm.getType().equals(Material.DIAMOND_HELMET)) {
                                                //diamond!?
                                                z.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2));
                                            }
                                        }

                                        z.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 8, 5));


                                        frwk.firework(iceFX, z.getLocation());

                                        evt.setDamage(0);
                                    } else if (val.equals("buckshot")) {
                                        frwk.firework(buckshotFX, z.getLocation());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    FireworkEffect iceFX = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.AQUA).trail(true)
            .build(),
            buckshotFX = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.RED).withColor(Color.ORANGE).withFade(Color.YELLOW).build();

    private InstantFirework frwk = new InstantFirework();

    @EventHandler
    private void launch(ProjectileLaunchEvent evt) {
        if (evt.getEntity() instanceof Arrow && evt.getEntity().getShooter() instanceof Player) {
            final Player p = (Player) evt.getEntity().getShooter();

            ItemStack stack = p.getItemInHand();

            String display = ChatColor.stripColor(stack.getItemMeta().getDisplayName());

            if (display == null) {
                return;
            }

            if (display.startsWith("Ice Bow")) {
                //fire!
                if (stack.getDurability() == 1) {
                    p.setItemInHand(null);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "Your ice bow has ran out of charges!"));
                } else {
                    stack.setDurability((short) (stack.getDurability() - 1));
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.AQUA + "Ice Bow" + ChatColor.RESET + " (" + stack.getDurability() + " Charges)");
                    stack.setItemMeta(meta);
                    p.setItemInHand(stack);
                }

                Arrow a = (Arrow) evt.getEntity();
                a.setMetadata("effect", new FixedMetadataValue(BVZ.p, "slow"));
            } else if (display.startsWith("Buckshot")) {
                if (stack.getDurability() == 1) {
                    p.setItemInHand(null);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "Your buckshot bow has ran out of charges!"));
                } else {
                    stack.setDurability((short) (stack.getDurability() - 1));
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + "Buckshot Bow" + ChatColor.RESET + " (" + stack.getDurability() + " Charges)");
                    stack.setItemMeta(meta);
                    p.setItemInHand(stack);
                }

                final Arrow aa = (Arrow) evt.getEntity();
                final org.bukkit.util.Vector vel = aa.getVelocity();

                evt.setCancelled(true);

                Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {
                    @Override
                    public void run() {
                        float speed = 2.45F;

                        int neg = p.getEyeLocation().getDirection().getZ() < 0 ? -1 : 1;

                        for (int i = 0; i <= 3; i++) {
                            Arrow a1 = p.getLocation().getWorld().spawnArrow(aa.getLocation().add(i, 0, neg), vel, speed, 0);

                            a1.setShooter(p);
                            a1.setMetadata("effect", new FixedMetadataValue(BVZ.p, "buckshot"));

                            Arrow a2 = p.getLocation().getWorld().spawnArrow(aa.getLocation().add(-i, 0, neg), vel, speed, 0);

                            a2.setShooter(p);
                            a2.setMetadata("effect", new FixedMetadataValue(BVZ.p, "buckshot"));
                        }

                        Arrow a3 = p.getLocation().getWorld().spawnArrow(aa.getLocation().add(0, 0, neg), vel, speed, 0);
                        a3.setShooter(p);
                        a3.setMetadata("effect", new FixedMetadataValue(BVZ.p, "buckshot"));
                    }
                }, 1);
            } else if (stack != null && stack.getType().equals(Material.BOW)) {
                stack.setDurability(Short.MAX_VALUE);
            }

        }
    }

    @EventHandler
    private void form(EntityChangeBlockEvent evt) {
        // stop falling blocks landing for anything but fences
        if (evt.getBlock().getType().equals(Material.ICE)) {
            evt.setCancelled(true);
        } else {
            evt.getBlock().getWorld().playEffect(evt.getBlock().getLocation(), Effect.STEP_SOUND, (byte) Material.WOOD.getId());
        }
    }

    @EventHandler
    private void extend(BlockPistonExtendEvent evt) {
        evt.setCancelled(true);
    }


    @EventHandler
    private void interact(PlayerInteractEvent evt) {
        if (gameStarted) {

            Player p = evt.getPlayer();

            if (evt.getAction() == Action.RIGHT_CLICK_BLOCK && evt.getClickedBlock().getType().equals(Material.STONE_BUTTON)) {
                BarrierManager.passBarrierPurchase(evt.getPlayer(), new Loc(evt.getClickedBlock().getLocation()));
                evt.setCancelled(true);
                return;
            }

            if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK) || evt.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (p.getItemInHand() != null && p.getItemInHand().getType().equals(Material.MONSTER_EGG)) {
                    //polymorph
                    GoalUpdater.polymorph();
                    evt.setCancelled(true);

                    if (p.getItemInHand().getAmount() == 1) {
                        p.setItemInHand(null);
                    } else {
                        p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                        p.updateInventory();
                    }
                    return;
                }
            }

            if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK) || evt.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (p.getItemInHand() != null && p.getItemInHand().getType().equals(Material.BUCKET)) {
                    //gravity well

                    Block targ = p.getTargetBlock((HashSet<Byte>) null, 50);
                    if (targ == null || targ.getType().equals(Material.AIR) || (targ.getLocation().getBlockY() >= (Config.minY - 2)) || targ.getLocation()
                            .distance(Config.getPlayerSpawnLocation().getBukkitLocation()) < 12) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "Invalid target designated."));
                    } else {

                        new GravityWell(targ.getLocation()).start();

                        if (p.getItemInHand().getAmount() == 1) {
                            p.setItemInHand(null);
                        } else {
                            p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                            p.updateInventory();
                        }
                    }
                    evt.setCancelled(true);
                    return;
                }
            }

            if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK) || evt.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (p.getItemInHand() != null && p.getItemInHand().getType().equals(Material.BLAZE_ROD)) {
                    //cataclysm

                    Block targ = p.getTargetBlock((HashSet<Byte>) null, 50);
                    //stop placing on top of flowers!!
                    if (targ.getTypeId() == 37 || targ.getTypeId() == 38 || targ.getTypeId() == Material.FENCE.getId()) {
                        targ = targ.getRelative(0, -1, 0);
                    }
                    if (targ == null || targ.getType().equals(Material.AIR) || (targ.getLocation().getBlockY() >= (Config.minY - 2))) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "Invalid target designated."));
                    } else {

                        new Cataclysm(targ.getLocation()).init();

                        if (p.getItemInHand().getAmount() == 1) {
                            p.setItemInHand(null);
                        } else {
                            p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                            p.updateInventory();
                        }
                    }
                    evt.setCancelled(true);
                    return;
                }
            }

            /*
            if (evt.getAction() == Action.RIGHT_CLICK_AIR || evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player p = evt.getPlayer();
                if (p.getItemInHand() != null) {
                    ItemStack i = p.getItemInHand();
                    if (i.hasItemMeta() && i.getItemMeta().hasDisplayName() && ChatColor.stripColor(i.getItemMeta().getDisplayName()).equalsIgnoreCase("Apocolypse Rod")) {
                        p.setItemInHand(null);
                        evt.setCancelled(true);
                        for (World w : Bukkit.getWorlds()) {
                            for (LivingEntity e : w.getLivingEntities()) {
                                if (!(e instanceof Player)) {
                                    e.remove();
                                }
                            }
                        }
                        return;
                    }
                }
            }*/

            if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material m = evt.getClickedBlock().getType();
                if (m == Material.DISPENSER || m == Material.ANVIL || m == Material.FURNACE) {
                    evt.setCancelled(true);
                    return;
                }
            }

            if (evt.getPlayer().getItemInHand() != null) {
                if (evt.getPlayer().getItemInHand().getType().equals(Material.BONE)) {
                    Block targ = evt.getPlayer().getTargetBlock((Set<Material>) null, 200);
                    if (targ != null
                            && (targ.getType().equals(Material.GOLD_BLOCK) || targ.getType().equals(Material.LAPIS_BLOCK) || targ.getType()
                            .equals(Material.REDSTONE_BLOCK))) {
                        TurretManager.turretClicked(evt.getPlayer(), targ.getLocation());
                        evt.setCancelled(true);
                        return;
                    }
                } else if (evt.getPlayer().getItemInHand().getType().equals(Material.NETHER_STAR)) {
                    IonCannon.fireIonCannon(evt.getPlayer());
                }
            }
        }
    }

    public static Set<String> lords = new HashSet<String>();

    /*
    @EventHandler
	private void recv(PlayerDataReceiveEvent evt) {
		PlayerData pd = evt.getPlayerData();
		Stats s = pd.getStats();
		if (!s.getAllStats().containsKey("bvz")) {
			// put default
			MongoUsersAPI.sendAddStat(pd, "bvz", "wins", "0");
			MongoUsersAPI.sendAddStat(pd, "bvz", "losses", "0");
			MongoUsersAPI.sendAddStat(pd, "bvz", "coins", "0");
			MongoUsersAPI.sendAddStat(pd, "bvz", "kills", "0");
		} else {
			Set<String> k = new HashSet<String>(s.getGameStats("bvz").keySet());
			String[] fields = new String[] { "wins", "losses", "coins", "kills" };
			for (String f : fields) {
				if (!k.contains(f)) {
					MongoUsersAPI.sendAddStat(pd, "bvz", f, "0");
				}
			}
		}

		if (MongoUsersAPI.hasPermission(pd.getUsername(), "minigame.lordperks")) {
			lords.add(pd.getUsername());
			PlayerScoreboard.addPoints(Bukkit.getPlayerExact(pd.getUsername()), Config.lordPoints, false);
		}
	}*/

    @EventHandler
    private void login(PlayerLoginEvent evt) {
        if (shuttingDown) {
            evt.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The server is rebooting.");
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent evt) {
        evt.setJoinMessage(null);
        Player p = evt.getPlayer();
        p.setAllowFlight(false);
        p.setFlying(false);
        p.getInventory().clear();
        if (Bukkit.getOnlinePlayers().size() > Config.getMaxPlayers()) {
            // only here as a precaution
            // disregard.add(p.getName());
            p.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "This server is full."));
            return;
            //MinigameAPI.redirectPlayer(p);
        } else if (gameStarted) {
            p.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The game has already started."));
            return;
        } else {
            p.teleport(Config.getPlayerSpawnLocation().getBukkitLocation());
        }

        //load stats ASYNC
        if (SQLManager.statsEnabled) {
            SQLManager.onJoin(p.getUniqueId());
        }

        PlayerScoreboard.addPlayer(p);

        p.setGameMode(GameMode.SURVIVAL);
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Infinity Bow");
        bow.setItemMeta(meta);
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        p.getInventory().addItem(bow);
        p.getInventory().setItem(15, new ItemStack(Material.ARROW, 1));
        p.getInventory().addItem(turretItem);

        p.updateInventory();

        p.teleport(Config.getPlayerSpawnLocation().getBukkitLocation());

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &c" + p.getName()
                + "&a has joined the game &b[&a" + Bukkit.getOnlinePlayers().size() + "/" + Config.getMaxPlayers() + "&b]"));

        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "This minigame is currently in BETA.");
        // p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +
        // "If you discover any bugs please notify a staff member.");

        if (Bukkit.getOnlinePlayers().size() == Config.getMaxPlayers()) {
            cd = new Countdown(Config.getStartTimerSeconds(), false);
            BukkitTask bt = Bukkit.getScheduler().runTaskTimer(BVZ.p, cd, 0, 20);
            cd.setTask(bt);
            countdownStarted = true;
        }

    }

    @EventHandler
    private void target(EntityTargetEvent evt) {
        evt.setCancelled(true);
    }

    @EventHandler
    private void place(BlockPlaceEvent evt) {
        evt.setCancelled(true);
    }

    @EventHandler
    private void breakBlock(BlockBreakEvent evt) {
        evt.setCancelled(true);
    }

    @EventHandler
    private void spawn(CreatureSpawnEvent evt) {
        if (evt.getSpawnReason() != SpawnReason.CUSTOM) {
            evt.setCancelled(true);
        } else {
            if (evt.getEntity().getType().equals(EntityType.ZOMBIE)) {
                Zombie z = (Zombie) evt.getEntity();

                ItemStack helm = z.getEquipment().getHelmet();

                if (helm != null) {
                    if (helm.getType().equals(Material.CHAINMAIL_HELMET)) {
                        //slow 1
                        z.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                    } else if (helm.getType().equals(Material.DIAMOND_HELMET)) {
                        //diamond!?
                        z.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2));
                    }
                }
            }
        }
    }

    @EventHandler
    public void disconnect(PlayerQuitEvent evt) {
        evt.setQuitMessage(null);
        Player p = evt.getPlayer();
        PlayerScoreboard.removeScoreboard(p);

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &c" + p.getName() + "&a has left the game &b[&a"
                + (Bukkit.getOnlinePlayers().size() - 1) + "/" + Config.getMaxPlayers() + "&b]"));

        if (gameStarted && Bukkit.getOnlinePlayers().size() == 1) {
            //no players will be left
            terminateServer(true);
        } else {
            //unload stats
            SQLManager.onQuit(p.getUniqueId());
        }

    }

    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent evt) {
        if (evt.getMessage().equalsIgnoreCase("/start")) {
            //if (evt.getPlayer().hasPermission("*") || evt.getPlayer().isOp() || evt.getPlayer().getName().equalsIgnoreCase("Adamki11s")) {
            //MinigameAPI.startGame();
            //TODO START GAME BROADCAST

            if (!evt.getPlayer().hasPermission(BVZ.startPerm)) {
                evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "You don't have permission to " +
                        "start the game."));
                evt.setCancelled(true);
                return;
            }

            if (gameStarted) {
                evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The game has already been " +
                        "started."));
                evt.setCancelled(true);
                return;
            }

            if (countdownStarted) {
                evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The countdown has already been " +
                        "started."));
            } else {
                cd = new Countdown(Config.getStartTimerSeconds(), true);
                BukkitTask bt = Bukkit.getScheduler().runTaskTimer(BVZ.p, cd, 0, 20);
                cd.setTask(bt);
                countdownStarted = true;
                evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "The countdown has been started."));
            }
            /*} else {
                evt.getPlayer().sendMessage(
						ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &cYou do not have permission to do this."));
			}*/
        } else if (evt.getMessage().equalsIgnoreCase("/leave")) {

            BungeeUtils.redirectPlayer(evt.getPlayer(), Config.redirectServer);

            //TODO leave the server!
            evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "You have left the game!"));
            //MinigameAPI.redirectPlayer(evt.getPlayer());
        } else if (evt.getMessage().equalsIgnoreCase("/stats")) {

            Player p = evt.getPlayer();

            if (!SQLManager.statsEnabled) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Statistics have been disabled."));
            } else {

                PlayerStats stats = SQLManager.getStats(p.getUniqueId());

                if (stats == null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN + "Your stats have not loaded yet."));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r--------------- &b[&a&lBvZ Stats&b] &r---------------"));
                    p.sendMessage(ChatColor.GREEN + "Games Played: " + ChatColor.RESET + stats.getGamesPlayed() + ChatColor.GREEN + "   Games Won: " + ChatColor
                            .RESET + stats.getGamesWon());
                    p.sendMessage(ChatColor.GREEN + "Zombies Killed: " + ChatColor.RESET + stats.getZombieKills() + ChatColor.GREEN + "   Waves Cleared: " +
                            ChatColor.RESET + stats.getWavesCleared());

                    double wlRatio = (double) stats.getGamesWon() / (double) stats.getGamesPlayed(),
                            killsGameRatio = (double) stats.getZombieKills() / (double) stats.getGamesPlayed(),
                            avgWave = (double) stats.getWavesCleared() / (double) stats.getGamesPlayed();

                    DecimalFormat frmt = new DecimalFormat("######.##");

                    p.sendMessage(ChatColor.GREEN + "Win/Loss Ratio: " + ChatColor.RESET + frmt.format(wlRatio) + ChatColor.GREEN + "   Kills/Game Ratio: " +
                            ChatColor.RESET + frmt.format(killsGameRatio));
                    p.sendMessage(ChatColor.GREEN + "Highest Wave Reached: " + ChatColor.RESET + stats.getMaxWave() + ChatColor.GREEN + "   Average Wave: " +
                            ChatColor.RESET + frmt.format(avgWave));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r-------------------------------------------"));
                }
            }

        } else {
            evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "/stats " + ChatColor.GRAY
                    + "to view your stats."));
            evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "/leave " + ChatColor.GRAY
                    + "to leave the game."));
            if (evt.getPlayer().hasPermission("*") || evt.getPlayer().isOp() || evt.getPlayer().getName().equalsIgnoreCase("Adamki11s")) {
                evt.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED + "/start " + ChatColor.GRAY
                        + "to force start the game."));
            }
        }
        evt.setCancelled(true);
    }

    @EventHandler
    private void ignite(final EntityCombustEvent evt) {
        if (!(evt.getEntity() instanceof Player)) {
            evt.setCancelled(true);
        }
    }

    public void startGame() {

        //clear old entities
        /*for (World w : Bukkit.getWorlds()) {
            for (LivingEntity le : w.getLivingEntities()) {
                if (!(le instanceof Player)) {
                    le.remove();
                }
            }
        }*/

        gameStarted = true;
        gameTask = new GameTask();
        BukkitTask bt = Bukkit.getScheduler().runTaskTimer(BVZ.p, gameTask, 0, 1);
        gameTask.setTask(bt);
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN
                + "The game has started! How long will you survive..."));
    }

    public static boolean shuttingDown = false;

    public void terminateServer(boolean won) {

        if (shuttingDown) {
            return;
        }

        /*SocketSender.sendData(new String[]{"id", "state", "players", "maxplayers"}, new Object[]{Config.thisID, "REBOOTING",
                Bukkit.getOnlinePlayers().size(), 4});*/

        shuttingDown = true;

        if (won) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.YELLOW + "Congratulations, You have won!"));
        }

        EndGame endTask = new EndGame(new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    //MinigameAPI.redirectPlayer(p);
                    //TODO redirect player
                    BungeeUtils.redirectPlayer(p, Config.redirectServer);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN
                            + "The game has ended!"));
                }
                Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {

                    @Override
                    public void run() {
                        Bukkit.getServer().shutdown();
                    }
                }, 60);
            }
        }, won);
        Bukkit.getScheduler().runTaskTimer(BVZ.p, endTask, 0, 1);
    }

    public void resetCountdown() {
        countdownStarted = false;
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
                + "The game countdown has been reset."));
    }

    public void removePlayer(Player p) {
        if (countdownStarted) {
            // allow to quit
            //MinigameAPI.redirectPlayer(p);
            p.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.GREEN
                    + "You have been removed from the game!"));
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &c" + p.getName() + "&a has left the game &b[&a"
                + Bukkit.getOnlinePlayers().size() + "/" + Config.getMaxPlayers() + "&b]"));
    }

}
