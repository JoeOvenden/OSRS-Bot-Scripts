package scripts;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import util.Paint;
import util.Sleep;
import util.Task;

import java.awt.*;
import java.util.ArrayList;

@ScriptManifest(name = "Ammonite Crabs", author = "Joe", version = 1.0, info = "Kills ammonite crabs", logo = "") 

public class AmmoniteCrabs extends Script {
	static enum States {
			KILL,
			RUN_AWAY,
			RUN_BACK
	}
	
	private long startTime;
	long runTime;
	
	States currentState = States.KILL;
	Area ghostCorridor = new Area(2889, 9851, 2924, 9848);
	Area killArea = ghostCorridor;
	String targetName = "Ammonite Crab";
	String foodName = null;
	Integer hpEat = 30;
	ArrayList<Task> tasks = new ArrayList<Task>();
	boolean finished = false;
	Skill[] skills = {Skill.RANGED, Skill.HITPOINTS};
	Position killPos = new Position(3803, 3754, 0);
	Area deagroArea = new Area(3823, 3780, 3825, 3782);
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();
    	for(final Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }

    @Override
    public void onExit() {
        //Code here will execute after the script ends    	
    }
    
    public boolean inArea(Area area) {
    	return area.contains(myPlayer().getPosition());
    }
    
    public void runTo(Area area) {
    	getWalking().webWalk(area);
    	Sleep.sleepUntil(() -> inArea(area), 5000);
    }
    
    public void eatFood(String food) {
    	log("Must eat");
    	if (getInventory().contains(food)) {
			getInventory().interact("Eat", food);
			Sleep.sleepUntil(() -> getSkills().getDynamic(Skill.HITPOINTS) > hpEat, 2000);
		}
    	else {
    		finished = true;
    	}
    }
    
    public void kill() throws InterruptedException {
    	if (getSkills().getDynamic(Skill.HITPOINTS) <= hpEat && foodName != null) {
    		eatFood(foodName);
    	}
    	
    	/*
    	if (!inArea(killArea)) {
    		runTo(killArea);
    		return;
    	}
    	*/
    	
    	if (myPlayer().isUnderAttack() || checkAnimating()) {
    		return;
    	}
    	
    	NPC target = getNpcs().closest(targetName);
    	if (target != null) {
    		target.interact("Attack");
    		Sleep.sleepUntil(() -> myPlayer().isUnderAttack(), 5000);
    		return;
    	}
    	
    	// Otherwise target is null, need to reaggro
    	currentState = States.RUN_AWAY;
    }
    
    public boolean checkAnimating() throws InterruptedException {
    	for (int i = 0; i < 15; i++){
    		if (myPlayer().isAnimating()) {
    			return true;
    		}
    		sleep(100);
    	}
    	return false;
    }
    
    public void runAway() throws InterruptedException {
    	if (deagroArea.contains(myPosition())) {
    		sleep(random(600, 1200));
    		currentState = States.RUN_BACK;
    		return;
    	}
    	
    	getWalking().webWalk(deagroArea);
    	Sleep.sleepUntil(() -> deagroArea.contains(myPosition()), 5000);
    }
    
    public void runBack() {
    	if (myPosition().equals(killPos)) {
    		currentState = States.KILL;
    		return;
    	}
    	
    	WalkingEvent myEvent = new WalkingEvent(killPos); //making the event
    	myEvent.setMinDistanceThreshold(0);
    	execute(myEvent); //executing the event
    	Sleep.sleepUntil(() -> myPosition().equals(killPos), 3000);
    }
    
    @Override
    public int onLoop() throws InterruptedException {
    	if (finished) {
    		
    	}
    	else if (currentState == States.KILL) {
    		kill();
    	}
    	else if (currentState == States.RUN_AWAY) {
    		runAway();
    	}
    	else if (currentState == States.RUN_BACK) {
    		runBack();
    	}
        return 100; //The amount of time in milliseconds before the loop starts over
    }
    
    public final String formatValue(final long l) {
        return (l > 1_000_000) ? String.format("%.2fm", ((double) l / 1_000_000))
               : (l > 1000) ? String.format("%.1fk", ((double) l / 1000)) 
               : l + "";
    }
    
    public final String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
    
    public void addPaintText(ArrayList<String> textList, Skill skill) {
    	String xp = formatValue(getExperienceTracker().getGainedXP(skill));
    	String xpPerHour = formatValue(getExperienceTracker().getGainedXPPerHour(skill));
    	int levels = getExperienceTracker().getGainedLevels(skill);
    	textList.add(skill.name() + " xp gained: " + xp + " (" + xpPerHour + ")");
    	textList.add(skill.name() + " levels gained: " + levels);
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    	runTime = System.currentTimeMillis() - startTime;
    	Paint P = new Paint(this, g, skills);
    	P.paint(runTime);
    }

}


