package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

public class MineTask extends Task {

	RS2Object rocks;
	String rockName;
	String interactText;
	boolean adjacent = true;
	
	public MineTask(MethodProvider api, String rockName, String interactText) {
		super(api);
		this.rockName = rockName;
		this.interactText = interactText;
	}
	
	public MineTask(MethodProvider api, String rockName, String interactText, boolean adjacent) {
		this(api, rockName, interactText);
		this.adjacent = adjacent;
	}

	@Override
	public boolean canProcess() {
		rocks = api.getObjects().closest(true, rockName);
		boolean valid = (rocks != null);
		if (adjacent) {
			valid = valid && rocks.getPosition().distance(api.myPosition()) <= 1;
		}
		return valid;
	}

	@Override
	public boolean isProcessing() {
		return api.myPlayer().isAnimating();
	}

	@Override
	public boolean isProcessed() {
		// TODO Auto-generated method stub
		return api.getInventory().isFull();
	}

	@Override
	public void process() throws InterruptedException {
		rocks.interact(interactText);

	}

}
