package com.adamgaltrey.bvz.data;

import com.adamgaltrey.bvz.entities.BVZ_Zombie;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

public enum BVZEntity {

	ZOMBIE, BABY_ZOMBIE, ARMOURED_ZOMBIE, TANK_ZOMBIE, TEST;

	public int spawn(Location l) {
		switch (this) {
		case ZOMBIE:
			/*Entity e = l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
			return e.getEntityId();*/
			return spawnZombie(l, this);
		case BABY_ZOMBIE:

			/*Entity eBaby = l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
			EntityZombie ez = ((CraftZombie) eBaby).getHandle();
			ez.setVillager(true);
			ez.setBaby(true);
			return eBaby.getEntityId();*/

			// return spawnZombie(l, BVZEntity.BABY_ZOMBIE);
			return spawnZombie(l, this);
		case ARMOURED_ZOMBIE:

			/*Entity eArmoured = l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
			LivingEntity armoured = (LivingEntity) eArmoured;
			EntityEquipment ee = armoured.getEquipment(); //
			ee.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
			ee.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
			ee.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS)); //
			ee.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
			return eArmoured.getEntityId();*/

			// return spawnZombie(l, BVZEntity.ARMOURED_ZOMBIE);
			return spawnZombie(l, this);
		case TANK_ZOMBIE:
			/*Entity eTank = l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
			LivingEntity le = (LivingEntity) eTank;
			EntityEquipment ee2 = le.getEquipment();
			ee2.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			ee2.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			ee2.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
			ee2.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			setGoals(eTank);*/
			// spawnZombie(l);
			//return eTank.getEntityId();
			// return spawnZombie(l, BVZEntity.TANK_ZOMBIE);
			return spawnZombie(l, this);
		}
		return -1;
	}

	private int spawnZombie(final Location l, BVZEntity e) {
		try {
			/*
			 * Class[] args = new Class[3]; args[0] = Class.class; args[1] =
			 * String.class; args[2] = int.class;
			 * 
			 * Method a = EntityTypes.class.getDeclaredMethod("a", args);
			 * a.setAccessible(true);
			 * 
			 * a.invoke(a, BVZ_Zombie.class, "Zombie", 54);
			 * 
			 * EntityZombie zombie = new EntityZombie((World) l.getWorld());
			 * zombie.setPosition(l.getX(), l.getY(), l.getZ());
			 */
			World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
			final BVZ_Zombie customEntity = new BVZ_Zombie(mcWorld);
			customEntity.setLocation(l.getX(), l.getY() + 1, l.getZ(), -170, 0);
			mcWorld.addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);

			if (e == BVZEntity.BABY_ZOMBIE) {
				customEntity.setVillager(true);
				customEntity.setBaby(true);
			}
			
			if(e == BVZEntity.ARMOURED_ZOMBIE){
				//0 is hand
				customEntity.setEquipment(1, CraftItemStack.asNMSCopy(new ItemStack(Material.CHAINMAIL_BOOTS)));
				customEntity.setEquipment(2, CraftItemStack.asNMSCopy(new ItemStack(Material.CHAINMAIL_LEGGINGS)));
				customEntity.setEquipment(3, CraftItemStack.asNMSCopy(new ItemStack(Material.CHAINMAIL_CHESTPLATE)));
				customEntity.setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(Material.CHAINMAIL_HELMET)));
			}
			
			if(e == BVZEntity.TANK_ZOMBIE){
				customEntity.setEquipment(1, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_BOOTS)));
				customEntity.setEquipment(2, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_LEGGINGS)));
				customEntity.setEquipment(3, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_CHESTPLATE)));
				customEntity.setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_HELMET)));
			}

			return customEntity.getId();
			// return (CraftZombie) customEntity.getBukkitEntity();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	private void setGoals(Entity e) {
		// BVZ_Zombie z = (BVZ_Zombie) e;
		/*
		 * try { Field gsa =
		 * net.minecraft.server.v1_7_R4.PathfinderGoalSelector.
		 * class.getDeclaredField("a"); gsa.setAccessible(true);
		 * gsa.set(gsa.get("b"), new UnsafeList()); gsa.set(gsa.get("c"), new
		 * UnsafeList()); } catch (Exception ex) { ex.printStackTrace(); }
		 * 
		 * Navigation nav = ((EntityInsentient) ((CraftEntity)
		 * e).getHandle()).getNavigation(); Location l =
		 * Bukkit.getOnlinePlayers()[new
		 * Random().nextInt(Bukkit.getOnlinePlayers().length)].getLocation();
		 * nav.a(l.getX(), l.getY(), l.getZ(), 0.3f);
		 * System.out.println("Set mob goal");
		 */
	}

}
