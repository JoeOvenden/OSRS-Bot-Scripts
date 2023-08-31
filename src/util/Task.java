package util;

import org.osbot.rs07.script.MethodProvider;

public abstract class Task {
	protected MethodProvider api;

	public Task(MethodProvider api) {
		this.api = api;
	}

	public abstract boolean canProcess();
	
	public abstract boolean isProcessing();
	
	public abstract boolean isProcessed();

	public abstract void process() throws InterruptedException;

	public boolean run() throws InterruptedException {
		if (canProcess()) {
			process();
			return true;
		}
		return false;
	}
} 