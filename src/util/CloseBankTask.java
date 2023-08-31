package util;

import org.osbot.rs07.script.MethodProvider;

public class CloseBankTask extends Task {

	public CloseBankTask(MethodProvider api) {
		super(api);
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
		return !api.getBank().isOpen();
	}

	@Override
	public void process() throws InterruptedException {
		api.getBank().close();
	}

}
