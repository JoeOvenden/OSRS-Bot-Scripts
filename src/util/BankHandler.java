package util;


import org.osbot.rs07.script.MethodProvider;

public class BankHandler {
	
	protected MethodProvider api;
	TaskHandler taskHandler;
	Task openBank;
	Task closeBank;
	boolean hasStarted = false;
	boolean finished = false;
	
	public BankHandler(MethodProvider api) {
		this.api = api;
		this.taskHandler = new TaskHandler(api);
		this.openBank = new OpenBankTask(api);
		this.closeBank = new CloseBankTask(api);
		this.taskHandler.addTask(openBank);
	}
	
	public void clear() {
		taskHandler.reset();
		taskHandler.addTask(openBank);
	}
	
	public void addTask(Task task) {
		taskHandler.addTask(task);
	}
	
	public boolean hasFinished() {
		// If finished then reset booleans and return true
		if (finished) {
			finished = false;
			hasStarted = false;
			return true;
		}
		return false;
	}
	
	public Task getCurrentTask() {
		return taskHandler.getCurrentTask();
	}
	
	public void finalise() {
		taskHandler.addTask(new CloseBankTask(api));
	}
	
	public void bank() throws InterruptedException {
		if (taskHandler.getCurrentTaskIndex() != 0) {
			hasStarted = true;
		}
		else if (taskHandler.getCurrentTaskIndex() == 0 && hasStarted) {
			finished = true;
			return;
		}
		taskHandler.processTasks();
	}
}
