package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.OneTextboxGUI;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.PrayerButton;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;

import util.Sleep;
import util.Misc;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Warriors guild defender", author = "Joe", version = 1.0, info = "Gets defenders at the warrios guild", logo = "") 

public class WGuildDefender extends Script {
	enum State {
		KILL,
		LEAVE_AND_RE_ENTER
	}
	
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	State currentState = State.KILL;
	
	OneTextboxGUI gui;
	
	NPC currentTarget = null;
	String food = "Salmon";
	int hpEatPercent = 50;
	int prayerDrinkPercent = 55;
	Misc misc = new Misc(this);
	
	boolean needToLeave = false;
	
	Position insideByDoor = new Position(2851, 3540, 2);
	Position outsideDoor = new Position(2842, 3539, 2);
	
	Skill[] skills = {Skill.STRENGTH, Skill.HITPOINTS};
	Skill[] statBoosts = {Skill.ATTACK, Skill.STRENGTH};
	String[] potions = {"Super attack", "Super strength"};
	String target = "Cyclops";
	
    @Override
    public void onStart() {
    	// setupGUI();
    	
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
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
    
   public void checkEndScript() {
	   if (inventory.getSlotForNameThatContains("Prayer potion") == -1
			   || inventory.getAmount("Warrior guild token") == 0) {
		   stop();
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
   
  	public void openTab(Tab tab) {
   		if (getTabs().isOpen(tab)) {
   			return;
   		}
   		getTabs().open(tab);
   		Sleep.sleepUntil(() -> getTabs().isOpen(tab), 5000);
   	}
   
   public void setPrayer(PrayerButton pray, boolean state) {
	   	if (getSkills().getDynamic(Skill.PRAYER) == 0) {
	   		return;
	   	}
	   	if (prayer.isActivated(pray)) {
	   		return;
	   	}
	   	openTab(Tab.PRAYER);
 		prayer.set(pray, state);
		Sleep.sleepUntil(() -> prayer.isActivated(pray) == state, 5000);  
  }
   
   public void drinkPrayerPotion() throws InterruptedException {
	   getInventory().interactWithNameThatContains("Drink", "Prayer potion");
	   Sleep.sleepUntil(() -> misc.getPrayerPointsPercentage(this) < prayerDrinkPercent , 2000);
	   prayerDrinkPercent = random(20, 65);
   }
   
   public void drinkStatBoosting() throws InterruptedException {
	   	for (int i = 0; i < statBoosts.length; i++) {
	   		Skill skill = statBoosts[i];
		   	if (getSkills().getDynamic(skill) == getSkills().getStatic(skill)) {
		   		if (inventory.interactWithNameThatContains("Drink", potions[i])) {
		   			Sleep.sleepUntil(() -> getSkills().getDynamic(skill) != getSkills().getStatic(skill), 5000);
		   			sleep(800);
		   		}
		   	}
	   	}
   }
       
   public void kill() throws InterruptedException {
	   	setPrayer(PrayerButton.PROTECT_FROM_MELEE, true);	   
	   	
	   	if (myPlayer().getHealthPercent() <= hpEatPercent && food != null) {
			eatFood(food);
		}
	   	
	   	if (misc.getPrayerPointsPercentage(this) < prayerDrinkPercent 
	   			&& getInventory().getSlotForNameThatContains("Prayer potion") != -1) {
	   		drinkPrayerPotion();
	   	}
	   	
	   	drinkStatBoosting();
	   	
		// If we only want to attack one enemy at at a time, and we are already in combat
		// then do nothing.
		if (myPlayer().isUnderAttack() && currentTarget != null && currentTarget.getHealthPercent() != 0) {
			return;
		}
		
		// Otherwise, attack an enemy.
		currentTarget = getNpcs().closest(n -> n.getName().equals(target) && 
					  	(!n.isUnderAttack() || n.isInteracting(myPlayer())) &&
					  	n != null && n.getHealthPercent() != 0);
		if (currentTarget != null) {
			currentTarget.interact("Attack");
			Sleep.sleepUntil(() -> myPlayer().isUnderAttack(), 5000);
			return;
		}
   }
   
   	public void loot() {
   		GroundItem defender = getGroundItems().closest(n -> n.getName().contains("defender"));
   		int emptySlots = inventory.getEmptySlots();
   		if (defender != null) {
   			defender.interact("Take");
   			Sleep.sleepUntil(() -> inventory.getEmptySlots() != emptySlots, 5000);
   			if (emptySlots != inventory.getEmptySlots() && myPosition().getZ() == 2) {
   				currentState = State.LEAVE_AND_RE_ENTER;
   				needToLeave = true;
   			}
   		}
   		
   	}
   	
   	public void leaveAndReenter() {
   		if (needToLeave) {
   			if (outsideDoor.getArea(2).contains(myPosition())) {
   				needToLeave = false;
   				return;
   			}
   			getWalking().webWalk(outsideDoor);
   			return;
   		}
   		
   		if (insideByDoor.getArea(2).contains(myPosition())) {
   			currentState = State.KILL;
   			return;
   		}
   		
   		getWalking().webWalk(insideByDoor);
   		Sleep.sleepUntil(() -> insideByDoor.getArea(2).contains(myPosition()) || getDialogues().isPendingContinuation(), 5000);
   		
   	}
   	
   	public void dropTrash() {
   		if (inventory.contains("Vial")) {
   			inventory.dropAll("Vial");
   			Sleep.sleepUntil(() -> !inventory.contains("Vial"), 5000);
   		}
   	}
   

    @Override
    public int onLoop() throws InterruptedException {
    	if (currentState == State.KILL) {
    		loot();
    		kill();
    		dropTrash();
    	}
    	else if (currentState == State.LEAVE_AND_RE_ENTER) {
    		leaveAndReenter();
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