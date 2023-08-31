package util;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.MethodProvider;

public class WebWalkTask extends Task {

	Area areaDestination;
	Position posDestination;
	
	public WebWalkTask(MethodProvider api, Area areaDestination) {
		super(api);
		this.areaDestination = areaDestination;
	}
	
	public WebWalkTask(MethodProvider api, Position posDestination) {
		super(api);
		this.posDestination = posDestination;
	}

	@Override
	public boolean canProcess() {
		// TODO: refine
		return true;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		if (posDestination != null) {
			return posDestination.getArea(2).contains(api.myPosition());
			// return posDestination.equals(api.myPosition());
		}
		else if (areaDestination != null) {
			return areaDestination.contains(api.myPosition());
		}
		return false;
	}

	@Override
	public void process() throws InterruptedException {
		if (posDestination != null) {
			api.getWalking().webWalk(posDestination);
		}
		else if (areaDestination != null) {
			api.getWalking().webWalk(areaDestination);
		}
	}

}
