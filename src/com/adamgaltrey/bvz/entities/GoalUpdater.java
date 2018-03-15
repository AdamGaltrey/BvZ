package com.adamgaltrey.bvz.entities;

import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.utils.ParticleEffect;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GoalUpdater implements Runnable {
	
	public static Map<Integer, BVZ_Zombie> goals = new HashMap<Integer, BVZ_Zombie>();

	public static void died(int id) {
        //System.out.println("Zombs alive = " + goals.size());
        if (goals.containsKey(id)) {
            goals.remove(id);
        }
        //System.out.println("Zombs alive now = " + goals.size());
    }

    public static void log(BVZ_Zombie e){
		goals.put(e.getId(), e);
	}
	
	static final Random r = new Random();
	
	public static Location obtainGoal(){
		if(Config.lavaPit == null){
			return null;
		}
		//return new Location(Bukkit.getWorld("Map1"),-2479,25,3177);
		return Config.lavaPit[r.nextInt(Config.lavaPit.length)].getBukkitLocation();
	}

	@Override
	public void run() {
		for(BVZ_Zombie e : goals.values()){
			e.walkToGoal();
		}
	}

    public static void forceRepath(){
        for(BVZ_Zombie e : goals.values()){
            e.forceFinalPath();
        }
    }

    public static void polymorph() {
        ParticleEffect fx = new ParticleEffect(ParticleEffect.ParticleType.HEART, 1, 1, 0, 2, 0);
        ParticleEffect fx2 = new ParticleEffect(ParticleEffect.ParticleType.SMOKE_NORMAL, 1, 1, 0, 2, 0);
        for (BVZ_Zombie e : goals.values()) {
            e.setBaby(true);
            fx.sendToLocation(e.thisLocation());
            fx2.sendToLocation(e.thisLocation());
        }
    }

    public static void forcePathing(int eid) {
        if (goals.containsKey(eid)) {
            goals.get(eid).forceFinalPath();
        }
    }

}
