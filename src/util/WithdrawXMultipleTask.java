package util;

import org.osbot.rs07.script.MethodProvider;

public class WithdrawXMultipleTask extends Task {
	
	String[] itemNames;
	int[] itemAmounts;

	public WithdrawXMultipleTask(MethodProvider api, String[] itemNames, int[] itemAmounts) {
		super(api);
		this.itemNames = itemNames;
		this.itemAmounts = itemAmounts;
	}

	@Override
	public boolean canProcess() {
		return api.bank.isOpen();
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return api.inventory.contains(itemNames[0]) && api.inventory.contains(itemNames[1]);
	}

	@Override
	public void process() throws InterruptedException {
		int count = itemNames.length;
		String name;
		for (int i = 0; i < count; i++) {
			name = itemNames[i];
			long itemCount = api.inventory.getAmount(name);
			long withdrawCount = itemAmounts[i] - itemCount;
			if (withdrawCount > 0) {
				api.bank.withdraw(name, itemAmounts[i]);
			}
		}
		Sleep.sleepUntil(() -> api.inventory.contains(itemNames), 5000);
	}

}
