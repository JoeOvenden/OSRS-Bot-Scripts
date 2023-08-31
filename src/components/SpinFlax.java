package components;

import org.osbot.rs07.script.MethodProvider;

public abstract class SpinFlax {
	protected MethodProvider api;

	public SpinFlax(MethodProvider api) {
		this.api = api;
	}

	public abstract boolean canProcess();
	
	public abstract boolean isProcessing();
	
	public abstract boolean isProcessed();

	public abstract void process() throws InterruptedException;

	public void run() throws InterruptedException {
		if (canProcess())
			process();
	}
} 