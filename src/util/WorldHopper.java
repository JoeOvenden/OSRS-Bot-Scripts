package util;

import org.osbot.rs07.script.MethodProvider;

public class WorldHopper {
	
	MethodProvider api;
	long lastHopTime;
	long hopTime;
	int world;
	
	public WorldHopper(MethodProvider api) {
		this.api = api;
		this.lastHopTime = System.currentTimeMillis();
		setHopTime();
		this.world = api.getWorlds().getCurrentWorld();
	}
	
	public void setHopTime() {
		// Sets the amount of time to stay on world for before hopping.
		hopTime = MethodProvider.random(50, 70) * 1000 * 60;
		String msg = "Bot will hop in " + String.valueOf(hopTime / 60000) + " minutes";
		api.log(msg);
	}
	
	public boolean shouldHop() {
		if (System.currentTimeMillis() - lastHopTime > hopTime) {
			return true;
		}
		return false;
	}
	
	public void hop() {
		while (api.getWorlds().getCurrentWorld() == world) {
			api.worlds.hopToP2PWorld();
			Sleep.sleepUntil(() -> api.getWorlds().getCurrentWorld() == world, 5000);
		}
		world = api.getWorlds().getCurrentWorld();
		lastHopTime = System.currentTimeMillis();
		setHopTime();
	}
}
