package util;

import java.util.ArrayList;

import org.osbot.rs07.script.MethodProvider;

public class TaskHandler {
	
	protected MethodProvider api;
	ArrayList<Task> tasks = new ArrayList<Task>();
	Task currentTask;
	boolean remove = false;			// If true, tasks are removed when they have processed
	int currentTaskIndex = 0;
	int tasksCount = 0;
	
	public TaskHandler(MethodProvider api) {
		this.api = api;
	}
	
    public void addTask(Task task) {
    	// add tasks in chronological order like so
    	// tasks.add(new OpenBankTask(this)); 
    	tasks.add(task);
    	tasksCount += 1;
    	if (tasksCount == 1) {
    		setCurrentTask();
    	}
    }
    
    public void getNextTask() {
    	if (remove) {
    		tasks.remove(currentTaskIndex);
    	}
    	else {
    		currentTaskIndex += 1;
    	}
    	currentTaskIndex %= tasksCount;
    	setCurrentTask();
    }
    
    public int getCurrentTaskIndex() {
    	return currentTaskIndex;
    }
    
    public void setCurrentTask() {
    	currentTask = getCurrentTask();
    }
    
    public Task getCurrentTask() {
    	return tasks.get(currentTaskIndex);
    }
    
    public void setRemove(boolean remove) {
    	this.remove = remove;
    }
    
    public boolean hasTask(Task task) {
    	return tasks.contains(task);
    }
    
    public boolean isFinished() {
    	return tasks.size() == 0;
    }
    
    public void reset() {
    	tasks.clear();
    }
    
    public void processTasks() throws InterruptedException {
    	// Processes first task in the queue
    	// If task is processed, remove from queue
    	if (currentTask.isProcessed()) {
    		getNextTask();
    		return;
    	}
    	
    	// If task is not processing, then execute task
    	if (!currentTask.isProcessing()) {
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 5000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	return;
    }
}
