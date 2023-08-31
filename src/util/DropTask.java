package util;

import org.osbot.rs07.script.MethodProvider;

// Drops one item
// If inventory doesn't contain item, then isProcessed() automatically returns true

public class DropTask extends Task {
	
	String itemName;
	long itemCount;

	public DropTask(MethodProvider api, String itemName) {
		super(api);
		this.itemName = itemName;
		this.itemCount = api.inventory.getAmount(itemName);
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
		return api.inventory.getAmount(itemName) != itemCount || itemCount == 0;
	}

	@Override
	public void process() throws InterruptedException {
		api.inventory.drop(itemName);
	}

}
