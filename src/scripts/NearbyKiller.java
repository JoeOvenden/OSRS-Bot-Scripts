package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.OneTextboxGUI;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;

import util.Sleep;
import util.Task;
import util.Misc;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Nearby killer", author = "Joe", version = 1.0, info = "Kills nearby monsters", logo = "") 

public class NearbyKiller extends Script {
	
	Task currentTask;
	boolean finished = false;
	boolean attackAll = true;
	
	private long startTime;
	long runTime;
	
	OneTextboxGUI gui;
	
	NPC currentTarget = null;
	String food = "Salmon";
	int hpEatPercent = 50;
	int prayerDrinkPercent = 55;
	Misc misc = new Misc(this);
	
	Skill[] skills = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS};
	Skill[] statBoosts = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE};
	String[] potions = {"Super attack", "Super strength", "Super defence"};
	String target;
	
    @Override
    public void onStart() {
    	setupGUI();
    	
        //Code here will execute before the loop is started
    	currentTask = null;
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new OneTextboxGUI("Enter the name of monster to kill:");
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
    	target = gui.getText();
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
    
    
   public void checkEndScript() {
	   if (!inventory.contains(food)) {
		   stop(false);
	   }
   }
   
   private NPC getNPCAttacking() {
       for (NPC npc : npcs.getAll()) {
           if (npc.isInteracting(myPlayer()) && !myPlayer().isAnimating()) {
               return npc;
           }
       }
       return null;
   }
  
   public void getNewTarget() {
	   // If under attack, new target is the npc attacking us
	   if (myPlayer().isUnderAttack()) {
		   currentTarget = getNPCAttacking();
		   return;
	   }
	   
	   // Otherwise find closest npc with name matching target
	   currentTarget = getNpcs().closest(target);
   }
   
   public void eatFood(String food) {
	   long foodCount = inventory.getAmount(food);
	   	if (getInventory().contains(food)) {
			getInventory().interact("Eat", food);
			Sleep.sleepUntil(() -> foodCount != inventory.getAmount(food), 2000);
		}
	   	hpEatPercent = random(60, 80);
   }
   
   public void drinkPrayerPotion() throws InterruptedException {
	   getInventory().interactWithNameThatContains("Drink", "Prayer potion");
	   Sleep.sleepUntil(() -> misc.getPrayerPointsPercentage(this) < prayerDrinkPercent , 2000);
	   prayerDrinkPercent = random(20, 65);
   }
       
   public void kill() throws InterruptedException {
	   	if (myPlayer().getHealthPercent() <= hpEatPercent && food != null) {
			eatFood(food);
		}
	   	
	   	if (misc.getPrayerPointsPercentage(this) < prayerDrinkPercent 
	   			&& getInventory().getSlotForNameThatContains("Prayer potion") != -1) {
	   		drinkPrayerPotion();
	   	}
	   	
	   	for (int i = 0; i < statBoosts.length; i++) {
	   		Skill skill = statBoosts[i];
		   	if (getSkills().getDynamic(skill) == getSkills().getStatic(skill)) {
		   		if (inventory.interactWithNameThatContains("Drink", potions[i])) {
		   			Sleep.sleepUntil(() -> getSkills().getDynamic(skill) != getSkills().getStatic(skill), 5000);
		   		}
		   		
		   	}
	   	}

	   	
	   	// If we want to agro all enemies nearby then find the closest enemy that we can reach
	   	// and that isn't attacking us and attack it.
		if (attackAll) {
			NPC npc = getNpcs().closest(n -> target.equals(n.getName()) && getMap().canReach(n)
					&& !n.isInteracting(myPlayer()));
			if (npc != null) {
				npc.interact("Attack");
				Sleep.sleepUntil(() -> npc.isInteracting(myPlayer()), 5000);
			}
			return;
		}
		
		// If we only want to attack one enemy at at a time, and we are already in combat
		// then do nothing.
		if (myPlayer().isUnderAttack() || misc.checkAnimating()) {
			return;
		}
		
		// Otherwise, attack an enemy.
		currentTarget = getNpcs().closest(target);
		if (currentTarget != null) {
			currentTarget.interact("Attack");
			Sleep.sleepUntil(() -> myPlayer().isUnderAttack(), 5000);
			return;
		}
   }

    @Override
    public int onLoop() throws InterruptedException {
    	kill();
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