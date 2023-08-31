package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.api.ui.Spells.NormalSpells;

import util.Sleep;
import util.Task;
import util.BankBankerTask;
import util.CloseBankTask;
import util.DepositAllExceptTask;
import util.Misc;
import util.Paint;
import util.WithdrawAllTask;

import java.awt.*;

@ScriptManifest(name = "Jewellery Enchanter", author = "Joe", version = 1.0, info = "Enchants jewellery", logo = "") 

public class JewelleryEnchanter extends Script {
	
	public enum State {
		CASTING,
		BANKING
	}
	
	Task currentTask = new BankBankerTask(this);
	State currentState = State.BANKING;
	boolean finished = false;
	Misc misc = new Misc(this);
	
	private long startTime;
	long runTime;
	
	String item = "Ruby amulet";
	NormalSpells spell = Spells.NormalSpells.LVL_3_ENCHANT;
	Skill[] skills = {Skill.MAGIC};
	String[] requiredItems = {item, "Cosmic rune"};
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
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
    	if (currentTask instanceof BankBankerTask) {
    		currentTask = new DepositAllExceptTask(this, requiredItems);
    	}
    	else if (currentTask instanceof DepositAllExceptTask) {
    		currentTask = new WithdrawAllTask(this, item);
    	}
    	else if (currentTask instanceof WithdrawAllTask) {
    		currentTask = new CloseBankTask(this);
    	}
    	else if (currentTask instanceof CloseBankTask) {
    		currentTask = new BankBankerTask(this);
    		currentState = State.CASTING;
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
    		currentTask.run();
        	Sleep.sleepUntil(() -> currentTask.isProcessing() || currentTask.isProcessed(), 5000);
    	}
    	// Otherwise task is processing so wait for it to finish
    	mouse.moveOutsideScreen();
    	return;
    }
    
    
    public void cast() throws InterruptedException {
    	if (!inventory.contains(item)) {
    		log("TEST");
    		currentState = State.BANKING;
    		return;
    	}
    	
    	if (inventory.getAmount(item) != 27 && Misc.checkAnimating(4000)) {
    		mouse.moveOutsideScreen();
    		return;
    	}
    	
    	// If magic tab is open then cast spell
    	if (getTabs().getOpen().equals(Tab.MAGIC)) {
    		magic.castSpell(spell);
    		Sleep.sleepUntil(() -> getTabs().getOpen().equals(Tab.INVENTORY), 5000);
    		sleep(random(100,200));
    	}
    	
    	// If inventory is open then try to enchant item
    	if (getTabs().getOpen().equals(Tab.INVENTORY)) {
    		boolean valid = inventory.interact("Cast", item);
    		Sleep.sleepUntil(() -> myPlayer().isAnimating(), 5000);
    		
    		// If not valid the cast has failed, go to magic tab
    		if (!valid) {
    			getTabs().open(Tab.MAGIC);
    			Sleep.sleepUntil(() -> getTabs().getOpen().equals(Tab.MAGIC), 5000);
    		}
    		return;
    	}
    	
    	else {
    		getTabs().open(Tab.MAGIC);
			Sleep.sleepUntil(() -> getTabs().getOpen().equals(Tab.MAGIC), 5000);
    	}
    }
    
    public void checkEndScript() {
    	if (bank.isOpen() && !bank.contains(item)) {
    		stop(false);
    	}
    }
  
    

    @Override
    public int onLoop() throws InterruptedException {
    	if (currentState == State.BANKING) {
    		processTask();
    	}
    	else {
    		cast();
    	}
    	checkEndScript();
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