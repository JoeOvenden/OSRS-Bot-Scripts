package util;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.MethodProvider;

public class WalkingEventTask extends Task {
	
	Position destination;

	public WalkingEventTask(MethodProvider api, Position destination) {
		super(api);
		this.destination = destination;
	}

	@Override
	public boolean canProcess() {
		return true;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return destination.equals(api.myPosition());
	}

	@Override
	public void process() throws InterruptedException {
		if (api.myPosition().distance(destination) > 1) {
			api.getWalking().webWalk(destination);
	    	Sleep.sleepUntil(() -> api.myPosition().distance(destination) <= 1, 5000);
		}
		
		if (api.myPosition().distance(destination) <= 1) {
			WalkingEvent myEvent = new WalkingEvent(destination); //making the event
	    	myEvent.setMinDistanceThreshold(0);
	    	api.execute(myEvent); //executing the event
	    	Sleep.sleepUntil(() -> destination.equals(api.myPosition()), 5000);
		}
	}

}
