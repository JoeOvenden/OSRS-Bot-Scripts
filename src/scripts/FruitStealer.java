package scripts;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import util.DepositAllTask;
import util.BankBankBoothTask;
import util.Paint;
import util.Sleep;
import util.Task;
import util.WebWalkTask;

import java.awt.*;

@ScriptManifest(name = "Fruit Stealer", author = "Joe", version = 1.0, info = "Steals fruit at hosidius", logo = "") 

public class FruitStealer extends Script {
	static enum States {
			STEALING,
			BANKING
	}
	States currentState = States.STEALING;
	String[] itemsToBank = {"Strange fruit", "Golovanova fruit top"};
	private long startTime;
	long runTime;
	Skill[] skills = {Skill.THIEVING};
	Area fruitStalls = new Area(1796, 3608, 1796, 3609);
	Area bank = Banks.HOSIDIUS_HOUSE;
	Task currentTask;
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	log("Cooking bot starting.");
    	startTime = System.currentTimeMillis();
    	for(final Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    

    @Override

    public void onExit() {
        //Code here will execute after the script ends
    	
    }
    
    public boolean checkAnimating() throws InterruptedException {
    	for (int i = 0; i < 10; i++) {
    		if (myPlayer().isAnimating()) {
    			return true;
    		}
    		sleep(Math.round(100));
    	}
    	return false;
    }
    
    
    public void steal() {
    	if (!fruitStalls.contains(myPosition())) {
    		getWalking().webWalk(fruitStalls);
    		Sleep.sleepUntil(() -> fruitStalls.contains(myPosition()), 5000);
    		return;
    	}
    	
    	RS2Object fruitStall = getObjects().closest("Fruit Stall");
    	int emptySlots = getInventory().getEmptySlots();
    	if (fruitStall != null && fruitStall.hasAction("Steal-from")) {
    		fruitStall.interact("Steal-from");
    		Sleep.sleepUntil(() -> getInventory().getEmptySlots() != emptySlots, 2000);
    	}
    }
    
    
    public void drop() {
    	if (!getInventory().onlyContains(itemsToBank)) {
    		getInventory().dropAllExcept(itemsToBank);
    		Sleep.sleepUntil(() -> getInventory().onlyContains(itemsToBank), 3000);
    	}
    }
    
    
    public void bankItems() throws InterruptedException {
    	processTask();
    }
    
    
    public void addNextTask() {
    	// Just walked somewhere and invent is full means we are at the bank -> Open bank
    	if (currentTask instanceof WebWalkTask) {
    		if (getInventory().isFull()) {
    			currentTask = new BankBankBoothTask(this);
    		}
    	}
    	
    	// Just opened bank -> Bank all
    	else if (currentTask instanceof BankBankBoothTask) {
    		currentTask = new DepositAllTask(this);
    	}
    	
    	// Just banked all -> go back to stalls
    	else if (currentTask instanceof DepositAllTask) {
    		currentTask = new WebWalkTask(this, fruitStalls);
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
    
    
    public void checkState() {
    	if (getInventory().isFull() && currentState != States.BANKING) {
    		currentState = States.BANKING;
    		currentTask = new WebWalkTask(this, bank);
    	}
    	else if (getInventory().isEmpty() && fruitStalls.contains(myPosition())) {
    		currentState = States.STEALING;
    	}
    }
    

    @Override
    public int onLoop() throws InterruptedException {
    	if (currentState == States.STEALING) {
    		steal();
    		drop();
    	}
    	else if (currentState == States.BANKING) {
    		bankItems();
    	}
    	
    	checkState();
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


