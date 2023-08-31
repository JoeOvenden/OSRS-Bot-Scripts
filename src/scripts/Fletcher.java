package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.FletchingGUI;

import org.osbot.rs07.api.ui.Skill;

import util.DepositAllExceptTask;
import util.CloseBankTask;
import util.MakeAllTask;
import util.Misc;
import util.OpenBankTask;
import util.Sleep;
import util.Task;
import util.WithdrawAllTask;
import util.WithdrawXMultipleTask;
import util.WorldHopper;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Fletcher", author = "Joe", version = 1.0, info = "Fletches", logo = "") 

public class Fletcher extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	String cut = "Cut";
	String string = "String";
	String logName;
	String[] requiredItems = {""};
	String[] withdrawItems = {"Bow string", ""};
	int[] itemAmounts = {14, 14};
	Misc misc = new Misc(this);
	
	String cutOrString;
	
	int widgetNumber;
	Skill[] skills = {Skill.FLETCHING};
	FletchingGUI gui;
	WorldHopper worldHopper;
	
    @Override
    public void onStart() {
    	setupGUI();
    	setupTrackers();
    	worldHopper = new WorldHopper(this);
    }
    
    
    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new FletchingGUI();
				gui.open();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if (!gui.isStarted()) {
    	    stop(false);
    	    return;
    	}
    	else {
    		getInfoFromGUI();
    	}
    }
    
    
    public void getInfoFromGUI(){
    	logName = gui.getSelectedLog();
    	widgetNumber = gui.getMakeOption();
    	cutOrString = gui.getCutString();
    	if (cutOrString == string) {
    		log(widgetNumber);
    		if (widgetNumber == 15) {
    			logName = logName + " shortbow (u)";
    		}
    		else if (widgetNumber == 16) {
    			logName = logName + " longbow (u)";
    		}
    		else {
    			log("Invalid options.");
    			stop(false);
    		}
    		withdrawItems[1] = logName;
    	}
    	else if (cutOrString == cut) {
    		requiredItems[0] = "Knife";
    		if (logName != "Logs") {
    			logName += " logs";
    		}
    		else {
    			widgetNumber += 1;
    		}
    	}
    }
    
    
    public void setupTrackers() {
    	currentTask = new OpenBankTask(this);
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    

    @Override
    public void onExit() {

    }
    
    public void addMakeTask() {
    	if (cutOrString == cut) {
    		currentTask = new MakeAllTask(this, "Knife", logName, widgetNumber, "Make");
    	}
    	else if (cutOrString == string) {
    		currentTask = new MakeAllTask(this, "Bow string", logName, 14, string);
    	}
    }
    
    public void withdrawTask() {
    	if (cutOrString == cut) {
    		currentTask = new WithdrawAllTask(this, logName);
    	}
    	else if (cutOrString == string) {
    		currentTask = new WithdrawXMultipleTask(this, withdrawItems, itemAmounts);
    	}
    }
    
    public void addNextTask() throws InterruptedException {
    	if (currentTask instanceof OpenBankTask) {
    		currentTask = new DepositAllExceptTask(this, requiredItems);
    	}
    	else if (currentTask instanceof DepositAllExceptTask) {
    		withdrawTask();
    	}
    	else if (currentTask instanceof WithdrawAllTask || currentTask instanceof WithdrawXMultipleTask) {
    		currentTask = new CloseBankTask(this);
    	}
    	else if (currentTask instanceof CloseBankTask) {
    		addMakeTask();
    		sleep(800);
    	}
    	else if (currentTask instanceof MakeAllTask) {
    		currentTask = new OpenBankTask(this);
    	}
    }

    
    public void processTask() throws InterruptedException {
    	// Processes first task in the queue
    	
    	// If task is processed, remove from queue
    	if (currentTask.isProcessed()) {
    		addNextTask();
    		return;
    	}
    	
    	// If task is not processing, then execute task
    	if (!currentTask.isProcessing()) {
    		if (currentTask instanceof MakeAllTask && !inventory.isFull() && misc.checkAnimating()) {
    			return;
    		}
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 12000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	mouse.moveOutsideScreen();
    	return;
    }
    
    
   public void checkEndScript() {
	   if (!getBank().contains(logName)) {
   			stop(false);
   	   }
	   
	   if (cutOrString == string && !getBank().contains("Bow string")) {
		   stop(false);
	   }
   }
    

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	if (getBank().isOpen()) {
    		checkEndScript();
    	}
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