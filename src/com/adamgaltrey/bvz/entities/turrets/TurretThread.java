package com.adamgaltrey.bvz.entities.turrets;



public class TurretThread implements Runnable {

	@Override
	public void run() {
		for(Turret t : TurretManager.purchased){
			t.activate();
		}
	}
	
	

}
