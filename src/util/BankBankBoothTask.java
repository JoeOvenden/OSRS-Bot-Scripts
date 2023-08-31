package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

public class BankBankBoothTask extends Task {

	RS2Object bankBooth;
	String bankName = "Bank booth";
	
	public BankBankBoothTask(MethodProvider api) {
		super(api);
	}
	
	public BankBankBoothTask(MethodProvider api, String bankName) {
		this(api);
		this.bankName = bankName;
	}

	@Override
	public boolean canProcess() {
		bankBooth = api.getObjects().closest(true, "Bank booth");
		return bankBooth != null;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return api.getBank().isOpen();
	}

	@Override
	public void process() throws InterruptedException {
		bankBooth.interact("Bank");
	}

}
