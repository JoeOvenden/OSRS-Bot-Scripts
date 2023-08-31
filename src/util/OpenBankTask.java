package util;

import org.osbot.rs07.script.MethodProvider;

public class OpenBankTask extends Task {

	public OpenBankTask(MethodProvider api) {
		super(api);
		// TODO Auto-generated constructor stub
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
		return api.bank.isOpen();
	}

	@Override
	public void process() throws InterruptedException {
		api.bank.open();
		Sleep.sleepUntil(() -> api.bank.isOpen(), 5000);
	}

}
