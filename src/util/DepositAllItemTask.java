package util;

import org.osbot.rs07.script.MethodProvider;

public class DepositAllItemTask extends Task {
	
	String[] itemNames;
	
	public DepositAllItemTask(MethodProvider api, String[] itemNames) {
		super(api);
		this.itemNames = itemNames;
		// TODO Auto-generated constructor stub
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
		return !api.getInventory().contains(itemNames);
	}

	@Override
	public void process() throws InterruptedException {
		api.getBank().depositAll(itemNames);
	}

}
