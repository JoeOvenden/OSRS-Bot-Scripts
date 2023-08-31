package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.FishingGUI;

import org.osbot.rs07.api.ui.Skill;

import util.DropAllExceptTask;
import util.FishTask;
import util.Sleep;
import util.Task;
import util.WorldHopper;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "AIO Fish", author = "Joe", version = 1.0, info = "Fishes at barbarian outpost", logo = "") 

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

public class AIOFisher extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	FishingGUI gui;
	WorldHopper worldHopper;
	
	String[] requiredItems;
	Skill[] skills;
	String spotText;
	String lureText;
		
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	setupGUI();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	currentTask = new DropAllExceptTask(this, requiredItems);
    	startTime = System.currentTimeMillis();
    	worldHopper = new WorldHopper(this);
    }
    
    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new FishingGUI();
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
    	requiredItems = gui.getRequiredItems();
    	skills = gui.getSkills();
    	spotText = gui.getSpotText();
    	lureText = gui.getLureText();
    }

    

    @Override
    public void onExit() {

    }
    
    public void addNextTask() throws InterruptedException {
    	// If task of fishing is completed, then need to go to bank
    	if (currentTask instanceof FishTask) {
    		currentTask = new DropAllExceptTask(this, requiredItems);
    	}
    	else if (currentTask instanceof DropAllExceptTask) {
    		currentTask = new FishTask(this, spotText, lureText);
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
    		sleep(random(3000, 8000));
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 12000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	return;
    }
    
    
   public void checkEndScript() {
	   if (!getInventory().contains(requiredItems)) {
   		log("Ran out of supplies so script is ending.");
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