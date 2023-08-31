package util;

import org.osbot.rs07.script.MethodProvider;

public class DropAllExceptTask extends Task {
	
	String[] keepItems;

	public DropAllExceptTask(MethodProvider api, String[] keepItems) {
		super(api);
		this.keepItems = keepItems;
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
		return api.getInventory().onlyContains(keepItems);
	}

	@Override
	public void process() throws InterruptedException {
		api.getInventory().dropAllExcept(keepItems);
		Sleep.sleepUntil(() -> isProcessed(), 5000);
	}

}
