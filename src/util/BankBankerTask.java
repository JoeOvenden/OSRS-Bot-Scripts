package util;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;

public class BankBankerTask extends Task {
	
	private NPC banker;

	public BankBankerTask(MethodProvider api) {
		super(api);
	}

	@Override
	public boolean canProcess() {
		banker = api.getNpcs().closest("Banker");
		return banker != null;
	}

	@Override
	public boolean isProcessed() {
		return api.getBank().isOpen();
	}

	@Override
	public void process() throws InterruptedException {
		banker.interact("Bank");
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

}
