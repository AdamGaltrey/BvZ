package com.adamgaltrey.bvz.data;

import org.bukkit.configuration.ConfigurationSection;

public class IonCannonData {
	
	/* Ion Cannon Data
	 * ioncannon:
	 * 		receiveat: <wave>
	 * 		cooldown: <seconds>
	 * 		radiusofeffect: <blocks>
	 * 		sloweffect:
	 * 			power:
	 * 			duration:*/
	
	private final int waveReceived, cooldown, radius;
	
	//pass in "ioncannon"
	public IonCannonData(ConfigurationSection io){
		waveReceived = io.getInt("receiveat");
		cooldown = io.getInt("cooldown");
		radius = io.getInt("radiusofeffect");
	}

	public int getWaveReceived() {
		return waveReceived;
	}

	public int getCooldown() {
		return cooldown;
	}

	public int getRadius() {
		return radius;
	}

}
