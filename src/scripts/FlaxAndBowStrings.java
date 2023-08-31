package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.ui.Skill;

import util.Sleep;
import util.Task;
import util.WebWalkTask;
import util.BankBankBoothTask;
import util.ClimbLadderTask;
import util.DepositAllTask;
import util.Misc;
import util.Paint;
import util.PickTask;
import util.SpinFlaxTask;

import java.awt.*;

@ScriptManifest(name = "Flax n' Strings", author = "Joe", version = 1.0, info = "Picks flax and turns it into bowstrings at camelot", logo = "") 

public class FlaxAndBowStrings extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	Skill[] skills = {Skill.CRAFTING};
	Misc misc = new Misc(this);
		
	Area bankArea = new Area(2722, 3493, 2728, 3493);
	Area flaxArea = new Area(2737, 3439, 2750, 3450);
	Area ladderArea = new Area(2713, 3470, 2715, 3472);
	int bowStrings = 0;
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	currentTask = new SpinFlaxTask(this);
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    

    @Override
    public void onExit() {

    }
    
    public void addNextTask() {
    	/*
    	 * IF CURRENT TASK INSTANCE OF [TASK]
    	 * 		CURREN TASK = NEW TASK
    	 */
    	
    	if (currentTask instanceof WebWalkTask) {
    		if (inventory.isEmpty()) {
    			currentTask = new PickTask(this, "Flax", "Pick");
    		}
    		
    		else if (bankArea.contains(myPosition())) {
    			currentTask = new BankBankBoothTask(this);
    		}
    		
    		else if (ladderArea.contains(myPosition())) {
    			currentTask = new ClimbLadderTask(this, "Ladder", "Climb-up", 25938);
    		}
    	}
    	
    	else if (currentTask instanceof PickTask) {
    		currentTask = new WebWalkTask(this, ladderArea);
    	}
    	
    	else if (currentTask instanceof ClimbLadderTask) {
    		if (myPosition().getZ() == 1) {
    			currentTask = new SpinFlaxTask(this);
    		}
    		else {
    			currentTask = new WebWalkTask(this, bankArea);
    		}
    	}
    	
    	else if (currentTask instanceof SpinFlaxTask) {
    		currentTask = new ClimbLadderTask(this, "Ladder", "Climb-down", 25939);
    	}
    	
    	else if (currentTask instanceof BankBankBoothTask) {
    		currentTask = new DepositAllTask(this);
    	}
    	
    	else if (currentTask instanceof DepositAllTask) {
    		currentTask = new WebWalkTask(this, flaxArea);
    	}
    	
    }

    
    public void processTask() throws InterruptedException {    	
    	// If task is processed, remove from queue
    	if (currentTask.isProcessed()) {
    		addNextTask();
    		return;
    	}
    	
    	// If task is not processing, then execute task
    	if (!currentTask.isProcessing()) {
    		
    		
    		if (currentTask instanceof SpinFlaxTask && !Misc.checkAnimating(1000)) {
    			return;
    		}
    		
    		currentTask.run();
    		if (!(currentTask instanceof PickTask)) {
    			Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 5000);
    		}
    	}
    	// Otherwise task is processing so wait for it to finish
    	return;
    }
    
    
   public void checkEndScript() {
   }
  
    

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	checkEndScript();
        return random(140, 190); //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	String n = String.valueOf((getExperienceTracker().getGainedXP(Skill.CRAFTING)) / 15);
    	Paint P = new Paint(this, g, skills, new String[] {n + " bowstrings crafted."});
    	P.paint(runTime);
    }

}