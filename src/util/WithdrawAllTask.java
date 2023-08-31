package util;

import org.osbot.rs07.script.MethodProvider;

public class WithdrawAllTask extends Task {

	String item;
	
	public WithdrawAllTask(MethodProvider api, String item) {
		super(api);
		this.item = item;
	}

	@Override
	public boolean canProcess() {
		return api.getBank().contains(item);
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return api.inventory.contains(item);
	}

	@Override
	public void process() throws InterruptedException {
		api.getBank().withdrawAll(item);
	}

}
