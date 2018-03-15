package com.adamgaltrey.bvz.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class WaveData {

	/*
	 * waves: wavenumber: spawndelay: min: milliseconds max: milliseconds
	 * #chance range eg 0-10 and 11-100 is inclusive mobs: - type:0-100 as
	 * double - type2:0-100 as double
	 */

	// pass in "waves.<number>"

	private final int minDelay, maxDelay;
	// entity -> double(min, max)
	// ONLY LOWER BOUND IS INCLUSIVE
	private final Map<BVZEntity, double[]> spawns = new HashMap<BVZEntity, double[]>();
	
	private final Random r = new Random();

	public WaveData(ConfigurationSection io) {
		minDelay = io.getInt("spawndelay.min");
		maxDelay = io.getInt("spawndelay.max");
		for (String mobtype : io.getConfigurationSection("mobs").getKeys(false)) {
			String range = io.getString("mobs." + mobtype);
			String[] data = range.split("-");
			BVZEntity bvzEntity = BVZEntity.valueOf(mobtype);
			if (bvzEntity == null) {
				throw new NullPointerException("Invalid BVZ entity type '" + mobtype + "' specified for wave '" + io.getName() + "'.");
			} else {
				spawns.put(bvzEntity, new double[] { Double.parseDouble(data[0]), Double.parseDouble(data[1]) });
			}
		}
	}

	public int getMinDelay() {
		return minDelay;
	}

	public int getMaxDelay() {
		return maxDelay;
	}

	public Map<BVZEntity, double[]> getSpawns() {
		return spawns;
	}

	public BVZEntity getRandomEntity() {
		// picks random number in range 1-100, only bottom value is inclusive
		double chosen = (r.nextDouble() * 100) + 1;
		for (Entry<BVZEntity, double[]> e : spawns.entrySet()) {
			if (chosen >= e.getValue()[0] && chosen < e.getValue()[1]) {
				return e.getKey();
			}
		}
		return null;
	}

	public int getRandomDelay() {
		return r.nextInt(maxDelay) + minDelay;
	}

}
