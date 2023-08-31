package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

public class WoodcutTask extends Task {

	RS2Object tree;
	String treeName;
	String interactText;
	
	public WoodcutTask(MethodProvider api, String treeName, String interactText) {
		super(api);
		this.treeName = treeName;
		this.interactText = interactText;
	}

	@Override
	public boolean canProcess() {
		tree = api.getObjects().closest(treeName);
		return tree != null && !tree.hasAction("Talk to");
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
		tree.interact(interactText);

	}

}
