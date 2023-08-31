package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.map.Position;

import util.Sleep;
import util.SpinFlaxTask;
import util.Task;
import util.WebWalkTask;
import util.WithdrawAllTask;
import util.WorldHopper;
import util.Paint;
import util.CloseBankTask;
import util.DepositAllTask;
import util.OpenBankTask;

import java.awt.*;
import java.util.ArrayList;

@ScriptManifest(name = "Bow Strings", author = "Joe", version = 1.0, info = "Makes bow strings at lumbridge", logo = "") 

public class BowStrings extends Script {
	
	ArrayList<Task> tasks = new ArrayList<Task>();
	Task currentTask;
	int currentTaskIndex = 0;
	int tasksCount = 0;
	
	private long startTime;
	long runTime;
	
	Position spinArea = new Position(3209, 3213, 1);
	Position bankArea = new Position(3208, 3220, 2);
	
	WorldHopper worldHopper;
	
	Skill[] skills = {Skill.CRAFTING};
	
    @Override
    public void onStart() {
    	
        //Code here will execute before the loop is started
    	currentTask = new OpenBankTask(this);
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	worldHopper = new WorldHopper(this);
    	setupTasks();
    }

    public void setupTasks() { 
    	tasks.add(new OpenBankTask(this));
    	tasks.add(new DepositAllTask(this));
    	tasks.add(new WithdrawAllTask(this, "Flax"));
    	tasks.add(new CloseBankTask(this));
    	tasks.add(new WebWalkTask(this, spinArea));
    	tasks.add(new SpinFlaxTask(this));
    	tasks.add(new WebWalkTask(this, bankArea));
    	
    	tasksCount = tasks.size();
    	currentTaskIndex = 0;
    	currentTask = tasks.get(currentTaskIndex);
    }
    
    public void getNextTask() {
    	currentTaskIndex += 1;
    	currentTaskIndex %= tasksCount;
    	currentTask = tasks.get(currentTaskIndex);
    }

    @Override
    public void onExit() {

    }
    
    public void processTask() throws InterruptedException {
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
    	if (currentTask instanceof SpinFlaxTask) {
    		mouse.moveOutsideScreen();
    	}
    	return;
    }
    
    
   public void checkEndScript() {
	   if (bank.isOpen() && !bank.contains("Flax") && !inventory.contains("Flax")) {
		   stop();
	   }
   }

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	checkEndScript();
    	if (worldHopper.shouldHop()) {
    		worldHopper.hop();
    	}
        return 100; //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g, skills);
    	P.paint(runTime);
    }

}