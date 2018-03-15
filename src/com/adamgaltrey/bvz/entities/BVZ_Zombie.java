package com.adamgaltrey.bvz.entities;

import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.game.GameTask;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.PathEntity;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;


public class BVZ_Zombie extends EntityZombie {

    private Location goal;

    static boolean notSet = true;
    static boolean xLong = Config.xLong;
    static boolean playersCoordsGreater;
    static double multiplier = 1, add;
    public static org.bukkit.World bukkitWorld;

    public BVZ_Zombie(final World world) {
        super(world);

        // System.out.println("spawned");

        GoalUpdater.log(this);

        try {

            // erase pathfinder goals
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);

            bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());


            /*this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, BVZ_Zombie.class, 1.0D, false));
            this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, BVZ_Zombie.class, true));*/

            //this.goalSelector.a(1, new PathfinderGoalWalkToTile(this, 1.2F));
            final int id = super.getId();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        //System.out.println("ZOMBIE SPAWNED");

        if (notSet) {
            notSet = false;

            if (xLong) {
                playersCoordsGreater = Config.getPlayerSpawnLocation().getX() > this.locX;
            } else {
                playersCoordsGreater = Config.getPlayerSpawnLocation().getZ() > this.locZ;
            }

            multiplier = playersCoordsGreater ? 1 : -1;
            add = 16 * multiplier;

            bukkitWorld = Bukkit.getWorld(Config.getPlayerSpawnLocation().getWorld());
        }

    }

    private void baby(){
        this.setBaby(true);
    }

    long last = 0;

	/*public void tryUpdateGoal() {
        if (Bukkit.getOnlinePlayers().length > 0) {
			System.out.println("UPDATED GOAL");
			long t = System.currentTimeMillis();
			if (t - last >= 10000) {
				last = t;
				// new targ
				walkTo(Bukkit.getOnlinePlayers()[new Random().nextInt(Bukkit.getOnlinePlayers().length)].getLocation());
			}
		}
	}*/

    /*
        1) Obtain our final end point goal
        2) If it is within 16 blocks then walk directly to it
        3) If it is not within 16 blocks, calculate a step order with a 16 block range and step along this path
     */

    private Queue<Location> walkPoints = new LinkedList<Location>();
    Location tempGoal;
    private boolean calculated = false;

    private boolean permaSleep = false;

    private long sleepTill = 0;

    public void forceFinalPath() {
        goal = null;
        permaSleep = false;
        sleepTill = 0;
        defaultMethod = false;
    }

    public void walkToGoal2() {

        if (permaSleep) {
            //ignore redundant pathfinding checks
            return;
        } else if (System.currentTimeMillis() < sleepTill) {
            return;
        }

        //System.out.println(this.getId());

        if (goal == null) {
            goal = GoalUpdater.obtainGoal();
            //System.out.println("Goal obtained");
        }

        if (walkPoints.isEmpty() && distLateral(goal) < 14) {
            walkTo(goal);
            permaSleep = true;
            //System.out.println("Goal within range, sleeping...");
        } else {
            //need to walk in the direction of players

            if (!calculated) {
                //calculate walk point
                Location stepPoint = thisLocation();

                //System.out.println("Beginning calculations...");

                int count = 0;

                while (stepPoint.distance(goal) > 14) {
                    //while we are too far add to our point
                    stepPoint = step16(stepPoint);

                    walkPoints.add(stepPoint);

                    count++;

                    if (count > 15) {
                        //System.out.println("ALERT, abnormal count size = " + count);
                        GameTask.alive.remove(this.getId());
                        world.removeEntity(this);
                        break;
                    }

                    //System.out.println("Point -> " + new Loc(stepPoint).toString());
                }

                //System.out.println("Calculated " + walkPoints.size() + " walk points for BvZ entity");

                calculated = true;

            } else {
                /*
                    1) Peek at our next tile which we are walking to
                    2) If we are less than 1 dist away then try walk to the next one
                    3) Else sleep
                 */

                if (tempGoal == null) {
                    //we need a new goal to walk to
                    if (!walkPoints.isEmpty()) {
                        tempGoal = walkPoints.poll();
                        walkTo(tempGoal);
                        //System.out.println("New temp goal, we are walking...");
                    } else {
                        tempGoal = goal;
                        walkTo(goal);
                        permaSleep = true;
                        //System.out.println("Temp goals exceeded, beginning final walk phase");
                    }
                }

                //now check the distance
                double d;
                if ((d = distLateral(tempGoal)) <= 4) {
                    //set a new goal
                    tempGoal = null;
                    //System.out.println("Within 2 blocks of temp goal, requesting new goal...");
                } else {
                    //assume it takes a mob 200ms to walk a block
                    sleepTill = (long) (System.currentTimeMillis() + (d * 100));
                    //System.out.println("Sleeping for " + (((int) d) * 200));
                }

            }

        }
    }

    public Location thisLocation() {
        return new Location(bukkitWorld, this.locX, this.locY, this.locZ);
    }

    private static Location step16(Location from) {
        return xLong ? from.add(add, 0, 0) : from.add(0, 0, add);
    }

    //this walk to goal method is called twice a second, so it needs to be efficient! We can't make numerous pathing operations so frequently

    /*
        Optimized walk to goal method

        1) Obtain our end goal, the lava pit!

        2) If the distance to our end goal is <= 16 then walk there directly and sleep forever (no more pathing operations needed)

        3) If not, calculate a path of tiles to walk to along the way
     */

    private boolean defaultMethod = true;

    public void walkToGoal() {

        if (permaSleep) {
            //ignore redundant pathfinding checks
            return;
        }

        if (defaultMethod && this.isBaby()) {
            //baby's need constant pathing because of their high speed
            defaultMethod = false;
        }

        if (defaultMethod) {
            walkToGoal2();
        } else {


            if (goal == null) {
                goal = GoalUpdater.obtainGoal();
            }

            if (tempGoal == null && dist(goal) < 16) {
                walkTo(goal);

                if (dist(goal) < 4) {
                    permaSleep = true;
                }
            } else {

                if (tempGoal == null) {
                    //calculate a new temporary goal
                    Location walkingTo = new Location(goal.getWorld(), goal.getX(), goal.getY(), goal.getZ());

                    int its = 0;

                    while (thisLocation().distance(walkingTo) > 16) {
                        walkingTo = getMidpoint(thisLocation(), walkingTo);
                        its++;

                        if (its > 8) {
                            break;
                        }
                    }

                    tempGoal = walkingTo;
                } else if (distLateral(tempGoal) < 4) {
                    tempGoal = null;
                } else {
                    walkTo(tempGoal);
                }

            }
        }
    }

    private Location getMidpoint(Location l1, Location l2) {
        double xD = Math.abs(l1.getX() - l2.getX()) / 2D, zD = Math.abs(l1.getZ() - l2.getZ()) / 2D,
                yD = Math.abs(l1.getY() - l2.getY()) / 2D;

        double minX = l1.getX() > l2.getX() ? l2.getX() : l1.getX(),
                minY = l1.getY() > l2.getY() ? l2.getY() : l1.getY(),
                minZ = l1.getZ() > l2.getZ() ? l2.getZ() : l1.getZ();

        return new Location(l1.getWorld(), minX + xD, minY + yD, minZ + zD);
    }

    private double distLateral(Location g) {
        Location thisL = thisLocation();
        double xD = Math.abs(thisL.getX() - g.getX()), zD = Math.abs(thisL.getZ() - g.getZ());
        return Math.sqrt((xD * xD) + (zD * zD));
    }

    private double dist(Location goal) {
        Location thisL = new Location(goal.getWorld(), this.locX, this.locY, this.locZ);
        return thisL.distance(goal);
    }

    public void walkTo(Location l) {
        // super.getNavigation().a
        // Navigation nav = ((EntityInsentient) ((CraftEntity)
        // e).getHandle()).getNavigation();

        //((EntityCreature) this).getNavigation().a(l.getX(), l.getY(), l.getZ(), 1.22f);

        /*l = new Location(l.getWorld(), l.getX(), l.getY() + 1, l.getZ());

        Material type = l.getBlock().getType();

        l.getBlock().setType(type == org.bukkit.Material.DIAMOND_BLOCK ? Material.GOLD_BLOCK : Material.DIAMOND_BLOCK);*/

        /*l = new Location(l.getWorld(), l.getX(), l.getY() + 6, l.getZ());

        Material type = l.getBlock().getType();

        l.getBlock().setType(type == org.bukkit.Material.DIAMOND_BLOCK ? Material.GOLD_BLOCK : Material.DIAMOND_BLOCK);*/

        PathEntity path = super.getNavigatioentityn().a(l.getX(), l.getY(), l.getZ());
        super.getNavigation().a(path, isBaby() ? 0.8f : 1.3f);

        //super.getNavigation().a(l.getX(), l.getY(), l.getZ(), 1.3f);
        //System.out.println("pathing");
    }

}
