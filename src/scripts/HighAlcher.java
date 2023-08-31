package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.InventoryGUI;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;

import util.Sleep;
import util.WorldHopper;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "High Alcher", author = "Joe", version = 1.0, info = "High alchs", logo = "") 

public class HighAlcher extends Script {
		
	private long startTime;
	long runTime;
	long lastXpDrop;
	int xpGained;
	boolean alchQueued = false;
	int alchs;
	
	InventoryGUI gui;
	WorldHopper worldHopper;
	
	Skill[] skills = {Skill.MAGIC};
	String itemName;
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();
    	lastXpDrop = startTime;
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	xpGained = getExperienceTracker().getGainedXP(Skill.MAGIC);
    	
    	setupGUI();
    	worldHopper = new WorldHopper(this);
    }
    
    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new InventoryGUI(this, "Choose item to alch:");
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
    	itemName = gui.getSelecterValue(0);
    }
    

    @Override
    public void onExit() {

    }

    
    public void checkEndScript() {
    	if (!inventory.contains(itemName) || !inventory.contains("Nature rune")) {
    		stop();
    	}
    }
  
    public void alch() {
    	// If/else if is best for this method, otherwise bot just clicks suspiciously fast
    	
    	// If magic tab is open then cast alch
    	if (getTabs().getOpen().equals(Tab.MAGIC)) {
    		magic.castSpell(Spells.NormalSpells.HIGH_LEVEL_ALCHEMY);
    		Sleep.sleepUntil(() -> getTabs().getOpen().equals(Tab.INVENTORY), 5000);
    	}
    	
    	// If inventory is open then try to alch item
    	else if (getTabs().getOpen().equals(Tab.INVENTORY)) {
    		boolean valid = inventory.interact("Cast", itemName);
    		
    		// If valid then we have queued an alch
    		if (valid) {
    			alchQueued = true;
    		}
    		
    		// Otherwise high alch was not selected and so we must open magic tab to cast
    		else {
    			getTabs().open(Tab.MAGIC);
    			Sleep.sleepUntil(() -> getTabs().getOpen().equals(Tab.MAGIC), 5000);
    		}
    	}
    	else {
    		getTabs().open(Tab.MAGIC);
    		Sleep.sleepUntil(() -> getTabs().getOpen().equals(Tab.MAGIC), 5000);
    	}
    }
   
    public boolean checkAlchQueued() throws InterruptedException {
    	/*
    	 * If an alch has just been performed then no alch is currently queued
    	 * and we sleep for a little bit to avoid bot clicking on the alch spell crazy fast
    	 * while the magic tab has only been open for a split second.
    	 */
    	int alchsNew = getExperienceTracker().getGainedXP(Skill.MAGIC) / 65;
    	if (alchsNew != alchs) {
    		alchQueued = false;
    		alchs = alchsNew;
    		sleep(random(400, 550));
    	}
    	return alchQueued;
    }
    
    public int timeSinceLastXpDrop() {
    	// returns approximate time since last xp drop in seconds
    	return Math.round((System.currentTimeMillis() - lastXpDrop) / 1000);
    }
    
    public void checkInterruption() {
    	if (getExperienceTracker().getGainedXP(Skill.MAGIC) != xpGained) {
    		lastXpDrop = System.currentTimeMillis();
    		xpGained = getExperienceTracker().getGainedXP(Skill.MAGIC);
    	}
    	else if (timeSinceLastXpDrop() > 8) {
    		alchQueued = false;
    	}
    }

    @Override
    public int onLoop() throws InterruptedException {
    	// If there is no alch queued, then queue an alch
    	if (!checkAlchQueued()) {
    		alch();
    	}
    	checkInterruption();
    	checkEndScript();
    	if (worldHopper.shouldHop()) {
    		worldHopper.hop();
    	}
        return random(600, 700); //The amount of time in milliseconds before the loop starts over
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g, skills);
    	P.paint(runTime);
    }

}