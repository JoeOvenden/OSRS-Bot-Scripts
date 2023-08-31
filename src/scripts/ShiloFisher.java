package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.ui.Skill;

import util.BankBankerTask;
import util.DepositAllItemTask;
import util.FishTask;
import util.Sleep;
import util.Task;
import util.WebWalkTask;
import util.Paint;

import java.awt.*;

@ScriptManifest(name = "Shilo Village Fisher", author = "Joe", version = 1.0, info = "Fishes trout and salmon at shil village", logo = "") 

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

public class ShiloFisher extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	Area bankArea = new Area(2850, 2953, 2853, 2956);
	Area fishingArea = new Area(2845, 2966, 2850, 2969);
	String[] fishNames = {"Raw trout", "Raw salmon"};
	Skill[] skills = {Skill.FISHING};
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	currentTask = new BankBankerTask(this);
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    

    @Override
    public void onExit() {

    }
    
    public void addNextTask() {
    	// If task of fishing is completed, then need to go to bank
    	if (currentTask instanceof FishTask) {
    		currentTask = new WebWalkTask(this, bankArea);
    	}
    	
    	/*
    	 * If we just walked to the bank, then next task is to open bank
    	 * Otherwise, we just walked to fishing spot, so fish
    	 */
    	else if (currentTask instanceof WebWalkTask) {
    		if (bankArea.contains(myPosition())) {
    			currentTask = new BankBankerTask(this);
    		}
    		else {
    			currentTask = new FishTask(this, "Rod Fishing Spot", "Lure");
    		}
    	}
    	
    	// If we just opened the bank, then deposit all fish
    	else if(currentTask instanceof BankBankerTask) {
    		currentTask = new DepositAllItemTask(this, fishNames);
    	}
    	
    	
    	// If we just finished banking, then walk to fishing spot
    	else if(currentTask instanceof DepositAllItemTask) {
    		currentTask = new WebWalkTask(this, fishingArea);
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
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 12000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	return;
    }
    
    
   public void checkEndScript() {
	   if (!getInventory().contains("Feather")) {
   		log("No feathers so script will now stop.");
   		stop();
   	}
   }
    

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	checkEndScript();
    	if (!myPlayer().isVisible()) {
    	}
        return 122; //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g, skills);
    	P.paint(runTime);
    }

}