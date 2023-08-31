package util;

import org.osbot.rs07.script.MethodProvider;

// Drops one item
// If inventory doesn't contain item, then isProcessed() automatically returns true

public class DropAllTask extends Task {
	
	public DropAllTask(MethodProvider api) {
		super(api);
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
		return api.inventory.isEmpty();
	}

	@Override
	public void process() throws InterruptedException {
		api.inventory.dropAll();
	}

}
