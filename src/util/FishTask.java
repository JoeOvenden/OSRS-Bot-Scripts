package util;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;

public class FishTask extends Task {


	private String spotText;
	private String interactText;
	private NPC fishingSpot;

	public FishTask(MethodProvider api, String spotText, String interactText) {
		super(api);
		this.spotText = spotText;
		this.interactText = interactText;
	}

	@Override
	public boolean canProcess() {
		fishingSpot = api.getNpcs().closest(spotText);
		if (fishingSpot != null) {
			return true;
		}
		return false;
	}

	@Override
	public void process(){
		fishingSpot.interact(interactText);
	}
	
	@Override
	public boolean isProcessed() {
		return api.getInventory().isFull();
	}

	@Override
	public boolean isProcessing() {
		return api.myPlayer().isAnimating();
	}
}
