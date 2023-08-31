package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.AgilityGUI;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.ui.Skill;

import util.Sleep;
import util.Task;
import util.Misc;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "AIO Agility", author = "Joe", version = 1.0, info = "Does agility", logo = "") 

public class AgilityAIO extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
		
	AgilityGUI gui;
	Misc misc = new Misc(this);
	
	String[] obstacleNames;
	String currentObstacle;
	Position startPos;
	int obstacleIndex = 0;
	int obstacleCount;
	int[] exceptionIndexes;
	int exceptionPointer = 0;
	Position[] exceptionPositions;
	int prevXp = 0;
	int energyThreshold = random(40, 70);
	long marks = 0;
	boolean success = false;
	Entity obstacle;
	Skill[] skills = {Skill.AGILITY};
	
    @Override
    public void onStart() {
    	
        //Code here will execute before the loop is started
    	currentTask = null;
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	prevXp = getXp();
    	setupGUI();
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new AgilityGUI();
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
    	obstacleNames = gui.getObstacleNames();
    	startPos = gui.getStartPosition();
    	if (obstacleNames == null || startPos == null) {
    		stop(false);
    	}
    	currentObstacle = obstacleNames[0];
    	obstacleCount = obstacleNames.length;
    	exceptionIndexes = gui.getExceptionIndex();
    	exceptionPositions = gui.getExceptionPositions();
    }

    @Override
    public void onExit() {

    }
    
    public void addNextTask() {
    	/*
    	 * IF CURRENT TASK INSTANCE OF [TASK]
    	 * 		CURREN TASK = NEW TASK
    	 */
    	
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
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 5000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	mouse.moveOutsideScreen();
    	return;
    }
    
    public int getXp() {
    	return getExperienceTracker().getSkills().getExperience(Skill.AGILITY);
    }
    
    
   public void checkEndScript() {
	   
   }
   
   public void handleExceptions() {
	   	if (exceptionIndexes == null) {
	   		return;
	   	}

	   	int index = indexOf(exceptionIndexes, obstacleIndex);
	   	if (index != -1) {
	   		getWalking().webWalk(exceptionPositions[index]);
	   		Sleep.sleepUntil(() -> (getObjects().closest(currentObstacle) != null), 5000);
	   	}
   }
   
   public void pickupMark() {
	   	GroundItem mark = getGroundItems().closest("Mark of grace");
	   	if (mark == null) {
	   		return;
	   	}
	   	
	   	if (getMap().canReach(mark)) {
	   		long markCount = inventory.getAmount("Mark of grace");
	   		mark.interact();
	   		Sleep.sleepUntil(() -> inventory.getAmount("Mark of grace") != markCount, 5000);
	   		marks += inventory.getAmount("Mark of grace") - markCount;
	   	}
   }
   
   public void drinkPotion() {
	   if (settings.getRunEnergy() < energyThreshold) {
		   inventory.interactWithNameThatContains("Drink", "Energy potion");
		   energyThreshold = random(40, 70);
	   }
   }
   
   public void rotateToSeeNextObstacle() {
	   Entity nextObstacle = getObjects().closest(obstacleNames[(obstacleIndex + 1) % obstacleCount]);
	   if (nextObstacle != null && !nextObstacle.isVisible()) {
		   nextObstacle.hover();
	   }
   }
   
    public void interactObstacle() {
    	// If player is still navigating an obstacle
    	if (myPlayer().isAnimating()) {
    		return;
    	}
    	
    	// If player has fallen down, then z coordinate is 0.
    	if (obstacleIndex != 0 && myPlayer().getZ() == 0) {
    		obstacleIndex = 0;
    		currentObstacle = obstacleNames[obstacleIndex];
    	}
    	    	
    	// If player has recieved an xp drop, then onto the next obstacle
    	int currentXp = getXp();
    	if (currentXp != prevXp) {
    		prevXp = currentXp;
    		obstacleIndex += 1;
    		obstacleIndex %= obstacleCount;
    		currentObstacle = obstacleNames[obstacleIndex];
    	}
    	
    	// If not animating, then try to interact with an obstacle.
    	obstacle = getObjects().closest(currentObstacle);
    	// rotateToSeeNextObstacle();
    	if (obstacle != null) {
    		success = obstacle.interact();
    		if (!success && !obstacle.isVisible()) {
    			getWalking().walk(obstacle.getArea(3));
    			Sleep.sleepUntil(() -> obstacle.isVisible(), 5000);
    			success = obstacle.interact();
    		}
    		Sleep.sleepUntil(() -> (getXp() != prevXp || success == false || myPlayer().getZ() == 0), 10000);
    	}
    	
    }
    
    public static int indexOf(String[] array, String itemToFind) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(itemToFind)) {
                return i;
            }
        }
        return -1;
    }
    
    public static int indexOf(int[] array, int itemToFind) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == itemToFind) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public int onLoop() throws InterruptedException {
    	if (obstacleIndex == 0) {
    		getWalking().webWalk(startPos);
    		Sleep.sleepUntil(() -> (getObjects().closest(currentObstacle) != null), 5000);
    	}
    	handleExceptions();
    	pickupMark();
    	drinkPotion();
    	interactObstacle();
    	checkEndScript();
        return 100; //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g, skills, new String[] {"Marks: " + String.valueOf(marks)});
    	P.paint(runTime);
    }

}