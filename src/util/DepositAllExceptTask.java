package util;

import org.osbot.rs07.script.MethodProvider;

public class DepositAllExceptTask extends Task {

	String[] requiredItems;
	
	public DepositAllExceptTask(MethodProvider api, String[] requiredItems) {
		super(api);
		this.requiredItems = requiredItems;
	}
	
	public DepositAllExceptTask(MethodProvider api, String requiredItem) {
		this(api, new String[] {requiredItem});
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
		return api.getInventory().onlyContains(requiredItems);
	}

	@Override
	public void process() throws InterruptedException {
		/*
		 * Currently only banks other items, doesnt take out required items
		 */
		if (!api.getInventory().onlyContains(requiredItems)) {
			api.getBank().depositAllExcept(requiredItems);
		}
		// TODO
		if (!api.getInventory().contains(requiredItems)) {
			
		}
	}

}
