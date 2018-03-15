package com.adamgaltrey.bvz.entities;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;

import java.lang.reflect.Field;


public class BVZ_Trader extends EntityVillager {

    public BVZ_Trader(final World world) {
        super(world);

        // System.out.println("spawned");

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


            // add goals
            this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));

            //this.goalSelector.a(1, new PathfinderGoalWalkToTile(this, 1.2F));
            final int id = super.getId();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        //System.out.println("ZOMBIE SPAWNED");
    }


}
