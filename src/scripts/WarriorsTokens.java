package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.GUI;

import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.PrayerButton;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;

import util.Sleep;
import util.Misc;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Warriors guild tokens", author = "Joe", version = 1.0, info = "Gets warriors guild tokens", logo = "") 

public class WarriorsTokens extends Script {
	enum State {
		ANIMATE,
		KILL,
		LOOT
	}
	
	State currentState = State.ANIMATE;
	
	private long startTime;
	long runTime;
	
	GUI gui;
	
	
	int prayerDrinkPercent = 55;
	Misc misc = new Misc(this);
	
	Skill[] skills = {Skill.ATTACK, Skill.HITPOINTS};
	Skill[] statBoosts = {Skill.ATTACK, Skill.STRENGTH};
	String[] potions = {"Super attack", "Super strength"};
	
	String[] lootItems = {"Rune platebody", "Rune platelegs", "Rune full helm", "Warrior guild token"};
	
	String mainWeapon = "";
	String shieldSlot = "";
	
	int emptySlots = 0;
	
    @Override
    public void onStart() {    	
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();    	
    	
    	// setupGUI();
    	
    	getSkillBeingTrained();
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	// getLootItems();
    	emptySlots = inventory.getEmptySlots();
    	
    	mainWeapon = equipment.getItemInSlot(3).getName();
    	if (equipment.isWearingItem(EquipmentSlot.SHIELD)) {
    		shieldSlot = equipment.getItemInSlot(5).getName();
    	}
    }
    
    public void getLootItems() {
    	// Unfinished
    	for (Item item : inventory.getItems()) {
    		if (item.getName().contains("platebody")) {
    			
    		}
    	}
    }
    
    public void getSkillBeingTrained() {
    	if (equipment.isWieldingWeaponThatContains("bow") || equipment.isWieldingWeaponThatContains("blowpipe")) {
    		skills[0] = Skill.RANGED;
    		return;
    	}
    	if (equipment.isWieldingWeaponThatContains("staff")) {
    		skills[0] = Skill.MAGIC;
    		return;
    	}
   
    	Skill[] attackStyles = {Skill.COOKING, Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.COOKING, Skill.COOKING};
    	int attackStyleConfig = getConfigs().get(46);
    	skills[0] = attackStyles[attackStyleConfig];
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new GUI(new String[] {"Type the skill that is to be trained:"});
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
    	skills[0] = Skill.valueOf(gui.getTextboxValue(0).toUpperCase());
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
	   if (currentState == State.ANIMATE && inventory.getSlotForNameThatContains("prayer potion") == -1) {
		   stop(false);
	   }
   }
   
   public int getPotionDoses(String name) {
	   int doses = 0;
	   for (int i = 1; i < 5; i++) {
		   doses += inventory.getAmount(name + " (" + String.valueOf(i) + ")");
	   }
	   return doses;
   }
   
   public void drinkPrayerPotion() throws InterruptedException {
	   getInventory().interactWithNameThatContains("Drink", "Prayer potion");
	   Sleep.sleepUntil(() -> misc.getPrayerPointsPercentage(this) < prayerDrinkPercent , 2000);
	   prayerDrinkPercent = random(20, 65);
	   sleep(600);
   }

   public int statAfterSuperPotion(Skill skill) {
	   // Assumes super potion
	   return (int) Math.floor(getSkills().getStatic(skill) * 1.15 + 5);
   }

   public void setPrayer(PrayerButton pray, boolean state) {
	   	if (getSkills().getDynamic(Skill.PRAYER) == 0) {
	   		return;
	   	}
	   	openTab(Tab.PRAYER);
  		prayer.set(pray, state);
		Sleep.sleepUntil(() -> prayer.isActivated(pray) == state, 5000);  
   }
   
   public void drinkStatBoosting() throws InterruptedException {
	   	for (int i = 0; i < statBoosts.length; i++) {
	   		Skill skill = statBoosts[i];
		   	if (getSkills().getDynamic(skill) <= statAfterSuperPotion(skill) - 6) {
		   		if (inventory.interactWithNameThatContains("Drink", potions[i])) {
		   			Sleep.sleepUntil(() -> getSkills().getDynamic(skill) > statAfterSuperPotion(skill) - 4, 5000);
		   		}
		   		sleep(600);
		   	}
	   	}
   }
   
   public void checkDrinkPotions() throws InterruptedException {
	   	if (misc.getPrayerPointsPercentage(this) < prayerDrinkPercent 
	   			&& getInventory().getSlotForNameThatContains("Prayer potion") != -1) {
	   		drinkPrayerPotion();
	   	}
	   	drinkStatBoosting();
   }

   	public void openTab(Tab tab) {
   		if (getTabs().isOpen(tab)) {
   			return;
   		}
   		getTabs().open(tab);
   		Sleep.sleepUntil(() -> getTabs().isOpen(tab), 5000);
   	}

   	public void animate() throws InterruptedException {
   		if (inventory.getEmptySlots() != emptySlots) {
   			currentState = State.KILL;
   			return;
   		}
   		
   	   	RS2Object animator = getObjects().closest("Magical animator");
   	   	if (animator != null) {
   	   		animator.interact("Animate");
   	   		Sleep.sleepUntil(() -> inventory.getEmptySlots() != emptySlots, 5000);
   	   	}
   	}
   	
   	public void loot() throws InterruptedException {
   		boolean allLooted = true;
   		for (String item : lootItems) {
   			GroundItem lootItem = getGroundItems().closest(item);
   			final int emptySlots = inventory.getEmptySlots();
   			if (lootItem != null) {
   				lootItem.interact("Take");
   				Sleep.sleepUntil(() -> inventory.getEmptySlots() != emptySlots, 5000);
   				allLooted = false;
   			}
   		}
   		
   		if (allLooted) {
   			currentState = State.ANIMATE;
   			sleep(600);
   			emptySlots = inventory.getEmptySlots();
   		}
   	}
   	
   	public void kill() throws InterruptedException {
   		GroundItem tokens = getGroundItems().closest("Warrior guild token");
   		if (tokens != null) {
   			currentState = State.LOOT;
   			sleep(random(5000, 8000));
   			return;
   		}
   		
   		if (myPlayer().isUnderAttack()) {
   			checkDrinkPotions();
   			return;
   		}
   	}

    @Override
    public int onLoop() throws InterruptedException {
    	if (currentState == State.ANIMATE) {
    		animate();
    	}
    	else if (currentState == State.LOOT) {
    		loot();
    	}
    	else if (currentState == State.KILL) {
    		kill();
    	}
    	checkEndScript();
        return random(100, 125); //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g, skills);
    	P.paint(runTime);
    }

}