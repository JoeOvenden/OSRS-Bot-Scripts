package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.GUI;

import org.osbot.rs07.api.ui.Skill;

import util.Sleep;
import util.Task;
import util.WorldHopper;
import util.BankHandler;
import util.DepositAllTask;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Skeleton script", author = "Joe", version = 1.0, info = "", logo = "") 

public class Skeleton extends Script {
	
	ArrayList<Task> tasks = new ArrayList<Task>();
	Task currentTask;
	int currentTaskIndex = 0;
	int tasksCount = 0;
	
	private long startTime;
	long runTime;
	
	BankHandler bankHandler;
	
	GUI gui;
	WorldHopper worldHopper;
	
	Skill[] skills = {};
	
    @Override
    public void onStart() {
    	
        //Code here will execute before the loop is started
    	currentTask = null;
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	setupBankHandler();
    	setupGUI();
    	worldHopper = new WorldHopper(this);
    	setupTasks();
    }
    
    public void setupBankHandler() {
    	bankHandler = new BankHandler(this);
    	bankHandler.addTask(new DepositAllTask(this));
    	bankHandler.finalise();
    }

    public void setupTasks() {
    	// add tasks in chronological order like so
    	// tasks.add(new OpenBankTask(this)); 
    	tasksCount = tasks.size();
    	currentTaskIndex = 0;
    	currentTask = tasks.get(currentTaskIndex);
    }
    
    public void getNextTask() {
    	currentTaskIndex += 1;
    	currentTaskIndex %= tasksCount;
    	currentTask = tasks.get(currentTaskIndex);
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new GUI();
				gui.open();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (gui == null || !gui.isStarted()) {
    	    stop(false);
    	    return;
    	}
    	else {
    		getInfoFromGUI();
    	}
    }
    
    
    public void getInfoFromGUI(){

    }

    @Override
    public void onExit() {

    }
    
    public void processTask() throws InterruptedException {
    	// Processes first task in the queue
    	log(currentTask);
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
    	mouse.moveOutsideScreen();
    	return;
    }
    
    
   public void checkEndScript() {
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