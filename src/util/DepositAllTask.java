package util;

import org.osbot.rs07.script.MethodProvider;

public class DepositAllTask extends Task {

	public DepositAllTask(MethodProvider api) {
		super(api);
	}

	@Override
	public boolean canProcess() {
		return api.getBank().isOpen();
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return api.getInventory().isEmpty();
	}

	@Override
	public void process() throws InterruptedException {
		api.getBank().depositAll();
	}

}
