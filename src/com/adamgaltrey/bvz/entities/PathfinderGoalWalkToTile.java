package com.adamgaltrey.bvz.entities;

import com.adamgaltrey.bvz.BVZ;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

public class PathfinderGoalWalkToTile extends PathfinderGoal {

    float speed;
    private EntityCreature entitycreature;

    private Random rand;

    public PathfinderGoalWalkToTile(EntityCreature entitycreature, float speed) {
        this.rand = new Random();
        this.speed = speed;
        this.entitycreature = entitycreature;
    }

    @Override
    public boolean a() {
        return true;
        /*if (this.entitycreature.aH >= 100) {
			return false;
		} else {
			return true;
		}*/
    }

    public void setGoal(Location l) {
        tile = l;
    }

    private Location tile;
    private boolean go = true;

    @Override
    public void c() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            tile = getRandomTile();
            this.entitycreature.getNavigation().a(tile.getX(), tile.getY(), tile.getZ(), speed);
        }
        //update goal every 10 seconds
        if (go && Bukkit.getOnlinePlayers().size() > 0) {
            tile = getRandomTile();
            this.entitycreature.getNavigation().a(tile.getX(), tile.getY(), tile.getZ(), speed);
            go = false;
            Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {

                @Override
                public void run() {
                    go = true;
                }
            }, 200);
        }
    }

    private Location getRandomTile(){
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
        return players[rand.nextInt(players.length)].getLocation();
    }

}
