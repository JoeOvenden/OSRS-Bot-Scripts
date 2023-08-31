package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.GenericMakeGUI;

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
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "AIO Make", author = "Joe", version = 1.0, info = "Makes stuff at bank", logo = "") 

public class AIOMake extends Script {
	
	Task currentTask;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	String[] items;
	int[] itemAmounts = {0, 0};
	Misc misc = new Misc(this);
	
	GenericMakeGUI gui;
	
	String makeText;
	int widgetNumber;
	Skill[] skills = {null};
	
    @Override
    public void onStart() {
    	setupGUI();
    	setupTrackers();
    }
    
    
    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new GenericMakeGUI();
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
    	items = gui.getItems();
    	itemAmounts = gui.getItemAmounts();
    	makeText = gui.getMakeText();
    	widgetNumber = gui.getWidgetNumber();
    	skills[0] = gui.getSkill();
    	log(items[0] + " " + items[1]);
    	log(itemAmounts[0]);
    	log(itemAmounts[1]);
    	log(makeText);
    	log(widgetNumber);
    	log(skills);
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

    public void addNextTask() throws InterruptedException {
    	if (currentTask instanceof OpenBankTask) {
    		currentTask = new DepositAllExceptTask(this, items);
    	}
    	else if (currentTask instanceof DepositAllExceptTask) {
    		currentTask = new WithdrawXMultipleTask(this, items, itemAmounts);
    	}
    	else if (currentTask instanceof WithdrawAllTask || currentTask instanceof WithdrawXMultipleTask) {
    		currentTask = new CloseBankTask(this);
    		sleep(300);
    	}
    	else if (currentTask instanceof CloseBankTask) {
    		currentTask = new MakeAllTask(this, items[0], items[1], widgetNumber, makeText);
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
    		if (currentTask instanceof MakeAllTask && misc.checkAnimating()) {
    			return;
    		}
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 12000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	
    	if (currentTask instanceof MakeAllTask) {
    		mouse.moveOutsideScreen();
    	}
    	return;
    }
    
    
   public void checkEndScript() {
	   for (int i = 0; i < items.length; i++) {
		   if (!bank.contains(items[i]) && !inventory.contains(items[i])) {
			   stop();
			   return;
		   }
	   }
   }
    

    @Override
    public int onLoop() throws InterruptedException {
    	processTask();
    	if (getBank().isOpen()) {
    		checkEndScript();
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