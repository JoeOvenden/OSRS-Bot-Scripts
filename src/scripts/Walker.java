package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.GUI;

import org.osbot.rs07.api.map.Position;

import util.Sleep;
import util.Task;
import util.WebWalkTask;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/*
 * Saltpetre place 1701, 3527
 * Hosidius bank 1749, 3599
 */

@ScriptManifest(name = "Walker", author = "Joe", version = 1.0, info = "Walks you places", logo = "") 

public class Walker extends Script {
	
	Task currentTask = null;
	
	private long startTime;
	long runTime;
	int x, y , z;
	
	Position destination;
	
	GUI gui;
		
    @Override
    public void onStart() {
    	setupGUI();
    	
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();
    	log(currentTask);
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new GUI(new String[] {"X:", "Y:", "Z:"});
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
    	
    	try {
            x = Integer.valueOf(gui.getTextboxValue(0));
            y = Integer.valueOf(gui.getTextboxValue(1));
            z = Integer.valueOf(gui.getTextboxValue(2));
            destination = new Position(x, y, z);
            currentTask = new WebWalkTask(this, destination);
    	}
    	catch (NullPointerException e) {
    		log(e);
    	}
    }

    @Override
    public void onExit() {
    	log("Ended");
    }

    
    public void processTask() throws InterruptedException {
    	// Processes first task in the queue
    	// If task is processed, remove from queue
    	if (currentTask != null && currentTask.isProcessed()) {
    		stop(false);
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
    
    @Override
    public int onLoop() throws InterruptedException {
    	if (currentTask == null) {
    		stop(false);
    	}
    	processTask();
        return 100; //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g);
    	P.paint(runTime);
    }

}