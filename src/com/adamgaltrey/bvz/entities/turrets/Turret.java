package com.adamgaltrey.bvz.entities.turrets;

import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Turret {

	// default is fire state, if false then in ice state
	private boolean fireState = true;

	public Location l;

	private static final int cooldown = Config.turretCooldownSeconds;

	private long last = 0;

	public Turret(Location l) {
		// init when a turret is purchased
		this.l = l;
		int rangeMax = (int) Math.ceil(Config.turretRange);
		Block base = l.getBlock();
		base.setType(Material.REDSTONE_BLOCK);
	}
	
	public void switchState(){
		//invert
		fireState ^= true;
		l.getBlock().setType(fireState ? Material.REDSTONE_BLOCK : Material.LAPIS_BLOCK);
	}

	// run every tick but only activate if off cooldown
	public void activate(){
		if(System.currentTimeMillis() - last > cooldown * 1000){
			//scan
			List<LivingEntity> close = getInRange(l);
			if(!close.isEmpty()){
				last = System.currentTimeMillis();
				//there are nearby entities
				World w = l.getWorld();
				if(fireState){
					//apply fire FX
					for(LivingEntity e : close){
						e.damage(2.0D);
                        w.playEffect(e.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
						w.playEffect(e.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
					}
				} else {
					//apply ice FX
					final List<FallingBlock> fx = new ArrayList<FallingBlock>();
					for(LivingEntity e : close){
						e.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 2));
						FallingBlock fb = l.getWorld().spawnFallingBlock(e.getLocation(), Material.ICE, (byte)0);
						fb.setVelocity(new Vector(0, 0.4,0));
						fx.add(fb);
					}
					Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {
						
						@Override
						public void run() {
							for(FallingBlock fb : fx){
								fb.remove();
								fb.getWorld().playEffect(fb.getLocation(), Effect.STEP_SOUND, (short) Material.ICE.getId()); 
							}
						}
					}, 15);
				}
			}
		}
	}

	private static List<LivingEntity> getInRange(Location l) {
		List<LivingEntity> r = new ArrayList<LivingEntity>();
		for (LivingEntity e : l.getWorld().getLivingEntities()) {
			if (dist(l, e.getLocation()) <= Config.turretRange) {
				r.add(e);
			}
		}
		return r;
	}

	private static double dist(Location l1, Location l2) {
		return Math.pow(l1.getX() - l2.getX(), 2) + Math.pow(l1.getZ() - l2.getZ(), 2);
	}

}
