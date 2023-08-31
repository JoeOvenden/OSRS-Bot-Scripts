package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;

import util.BankBankBoothTask;
import util.DepositAllTask;
import util.DropAllTask;
import util.MineTask;
import util.Sleep;
import util.Task;
import util.WebWalkTask;
import util.Paint;
import util.PowerMineTask;

import java.awt.*;

@ScriptManifest(name = "Al-kharid Iron", author = "Joe", version = 1.0, info = "Mines and banks iron at Al-kharid", logo = "") 

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

public class AlkharidIron extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	boolean bankIron = false;
	
	Area bankArea = new Area(3269, 3169, 3269, 3166);
	Position minePos = new Position(3295, 3310, 0);
	Skill[] skills = {Skill.MINING};
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	currentTask = new PowerMineTask(this, "Iron rocks", "Mine");
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    

    @Override
    public void onExit() {

    }
    
    public void addNextTask_BANKING() {
    	if (currentTask instanceof MineTask) {
    		currentTask = new WebWalkTask(this, bankArea);
    	}
    	
    	/*
    	 * If we just walked to the bank, then next task is to open bank
    	 * Otherwise, we just walked to fishing spot, so fish
    	 */
    	else if (currentTask instanceof WebWalkTask) {
    		if (inventory.isFull()) {
    			currentTask = new BankBankBoothTask(this);
    		}
    		else {
    			currentTask = new MineTask(this, "Iron rocks", "Mine");
    		}
    	}
    	
    	// If we just opened the bank, then deposit all fish
    	else if(currentTask instanceof BankBankBoothTask) {
    		currentTask = new DepositAllTask(this);
    	}
    	
    	
    	// If we just finished banking, then walk to fishing spot
    	else if(currentTask instanceof DepositAllTask) {
    		currentTask = new WebWalkTask(this, minePos);
    	}
    }
    
    public void addNextTask_POWERMINE() {
    	if (currentTask instanceof PowerMineTask) {
    		currentTask = new DropAllTask(this);
    	}
    	else if (currentTask instanceof DropAllTask) {
    		currentTask = new PowerMineTask(this, "Iron rocks", "Mine");
    	}
    }
    
    public void addNextTask() {
    	// If task of fishing is completed, then need to go to bank
    	if (bankIron) {
    		addNextTask_BANKING();
    	}
    	else {
    		addNextTask_POWERMINE();
    	}
   
 
    	
    }

    
    public void processTask() throws InterruptedException {
    	// Processes first task in the queue
    	log(currentTask);
    	// If task is processed, remove from queue
    	if (currentTask.isProcessed()) {
    		addNextTask();
    		return;
    	}
    	
    	// If task is not processing, then execute task
    	if (!currentTask.isProcessing()) {
    		boolean ran = currentTask.run();
    		if (ran) {
    			Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 3000);
    		}
    	}
    	// Otherwise task is processing so wait for it to finish
    	return;
    }
    
    
   public void checkEndScript() {
	   // Add timer
   }

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	checkEndScript();
        return 100; //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	String[] ironText = {};
    	if (bankIron) {
    		String n = String.valueOf((getExperienceTracker().getGainedXP(Skill.MINING)) / 35);
    		ironText[0] = "Iron ore: " + String.valueOf(n);
    	}
    	Paint P = new Paint(this, g, skills, ironText);
    	P.paint(runTime);
    }

}