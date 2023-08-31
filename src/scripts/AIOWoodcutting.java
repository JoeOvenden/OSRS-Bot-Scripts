package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.WoodcuttingGUI;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.ui.Skill;

import util.DepositAllExceptTask;
import util.DropAllExceptTask;
import util.OpenBankTask;
import util.Sleep;
import util.Task;
import util.WebWalkTask;
import util.WoodcutTask;
import util.WorldHopper;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "AIO Woodcutter", author = "Joe", version = 1.0, info = "Cuts down trees", logo = "") 

/*
 * Tasks:
 * 	Run from bank to fishing spot
 *  Fish till full invent
 *  Run to bank
 *  Bank fish
 */

/*
 * Improvements:
 * -Store the position of the fishing spot that we are attempting to interact with
 *  If nearest fishing spot changes then interact with that one
 * 
 * -If there are no nearby fishing spots available, run to fishingArea
 */

public class AIOWoodcutting extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	Area bankArea;
	Area treeArea;
	String logName;
	String treeName;
	boolean AFK = true;
	boolean powerChop = false;
	Skill[] skills = {Skill.WOODCUTTING};
	String[] requiredItems = {"Rune axe"};
	int nestCount = 0;
	
	WoodcuttingGUI gui;
	WorldHopper worldHopper;
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	worldHopper = new WorldHopper(this);
    	setupGUI();
    }
    
    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new WoodcuttingGUI();
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
    	bankArea = gui.getBankArea();
    	treeArea = gui.getTreeArea();
    	logName = gui.getLogName();
    	treeName = gui.getTreeName();
    	AFK = gui.getAFK();
    	powerChop = gui.getPowerChop();
    	if (bankArea == null || treeArea == null || logName == null || treeName == null) {
    		log("Error, null information.");
    		stop(false);
    	}
    	if (powerChop) {
    		currentTask = new WoodcutTask(this, treeName, "Chop down");
    	}
    	else {
    		currentTask = new WebWalkTask(this, bankArea);
    	}
    }


    @Override
    public void onExit() {

    }
    
    public void addNextTask() {
    	// If task of fishing is completed, then need to go to bank
    	if (currentTask instanceof WoodcutTask) {
    		if (powerChop) {
    			currentTask = new DropAllExceptTask(this, requiredItems);
    		}
    		else {
    			currentTask = new WebWalkTask(this, bankArea);
    		}
    	}
    	else if (currentTask instanceof DropAllExceptTask) {
    		currentTask = new WoodcutTask(this, treeName, "Chop down");
    	}
    	/*
    	 * If we just walked to the bank, then next task is to open bank
    	 * Otherwise, we just walked to fishing spot, so fish
    	 */
    	else if (currentTask instanceof WebWalkTask) {
    		if (getInventory().contains(logName)) {
    			currentTask = new OpenBankTask(this);
    		}
    		else {
    			currentTask = new WoodcutTask(this, treeName, "Chop down");
    		}
    	}
    	
    	// If we just opened the bank, then deposit all fish
    	else if(currentTask instanceof OpenBankTask) {
    		currentTask = new DepositAllExceptTask(this, requiredItems);
    	}
    	
    	
    	// If we just finished banking, then walk to fishing spot
    	else if(currentTask instanceof DepositAllExceptTask) {
    		currentTask = new WebWalkTask(this, treeArea);
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
    		if (currentTask instanceof WoodcutTask && !inventory.isEmpty() && AFK) {
    			sleep(random(3000, 8000));
    			mouse.moveOutsideScreen();
    		}
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 5000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	return;
    }
    
    
   public void checkEndScript() {
	   // Add timer
   }
   
   
   public void pickupNest() {
	   GroundItem nest = getGroundItems().closest("Bird nest");
	   
	   if (nest != null && !getInventory().isFull()) {
		   long nests = inventory.getAmount("Bird nest");
		   nest.interact("Take");
		   Sleep.sleepUntil(() -> nests != inventory.getAmount("Bird nest"), 10000);
		   nestCount += inventory.getAmount("Bird nest") - nests;
	   }
   }
    

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	// pickupNest();
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
    	Paint P = new Paint(this, g, skills, new String[] {"Bird nests: " + String.valueOf(nestCount)});
    	P.paint(runTime);
    }

}