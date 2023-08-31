package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.InventoryGUI;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.WalkingEvent;

import util.Sleep;
import util.Task;
import util.WithdrawAllTask;
import util.WorldHopper;
import util.BankHandler;
import util.DepositAllExceptTask;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "GE Firemaker", author = "Joe", version = 1.0, info = "Does firemaking at the GE", logo = "") 

public class FiremakerGE extends Script {
	enum State {
		FIREMAKING,
		GET_NEW_LINE,
		RUN_TO_LINE,
		BANKING
	}
	
	ArrayList<Task> tasks = new ArrayList<Task>();
	Task currentTask;
	int currentTaskIndex = 0;
	int tasksCount = 0;
	
	State currentState;
	
	private long startTime;
	long runTime;
	
	InventoryGUI gui;
	WorldHopper worldHopper;
	BankHandler bankHandler;
	int startX = 3171;
	int endX = 3158;
	int lowestY = 3493;
	int highestY = 3497;
	int lineY = 0;
	int currentX = 0;
	int fireId = 26185;
	String logName = "Willow logs";
			
	Skill[] skills = {Skill.FIREMAKING};
	
    @Override
    public void onStart() {
    	
        //Code here will execute before the loop is started
    	currentState = State.GET_NEW_LINE;
    	startTime = System.currentTimeMillis();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	worldHopper = new WorldHopper(this);
    	setupGUI();
    	setupBankHandler();
    }
    
    public void setupBankHandler() {
    	bankHandler = new BankHandler(this);
    	bankHandler.addTask(new DepositAllExceptTask(this, "Tinderbox"));
    	bankHandler.addTask(new WithdrawAllTask(this, logName));
    	bankHandler.finalise();
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new InventoryGUI(this, "Select the type of log:");
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
    
    
    public void getInfoFromGUI() {
    	logName = gui.getSelecterValue(0);
    }

    @Override
    public void onExit() {

    }
    
    public void checkEndScript() {
    	if (bank.isOpen() && !bank.contains(logName) && !inventory.contains(logName)) {
    		stop();
    	}
    }
    
    public int countFiresInLine(int y) {
    	@SuppressWarnings("unchecked")
		List<RS2Object> fires = getObjects().filter(o -> o.getId() == fireId && 
    			o.getPosition().getY()== y &&
    			o.getPosition().getX() <= startX && o.getPosition().getX() >= endX);
    	return fires.size();
    }
   
   	public int getFiremakingLineY() {
   		/*
   		 Function chooseFirmakingLine
			For each line going west from the start area, in order of closest to bank -> furthest
				If there are no fires in the line
					Return this line
				If the number of fires are less than the lowest one so far, store it

			Return the line with the lowest number of fires
   		 */
   		int lowestCount = 5000;
   		int bestY = -1;
   		int fireCount = 0;
   		for (int y = lowestY; y <= highestY; y++) {
   			fireCount = countFiresInLine(y);
   			if (fireCount == 0) {
   				bestY = y;
   				break;
   			}
   			if (fireCount < lowestCount) {
   				bestY = y;
   			}
   		}
   		currentState = State.RUN_TO_LINE;
   		return bestY;
   	}
   	
   	public boolean isFireAtPos() {
   		// Checks to see if there is a fire at the players position
   		RS2Object fire = getObjects().closest(obj -> obj.getId() == fireId && obj.getPosition().equals(myPosition()));
   		return fire != null;
   	}
   	
   	public int getNextEmptyTileX() {
   		RS2Object fire;
   		int currentX = myPosition().getX();
   		for (int x = currentX - 1; x >= endX; x--) {
   			int X = x;
   			fire = getObjects().closest(obj -> obj.getPosition().getX() == X && obj.getId() == fireId);
   			if (fire == null) {
   				return x;
   			}
   		}
   		return -1;
   	}
   	
   	public void firemake() throws InterruptedException {
   		if (myPosition().getX() < endX) {
   			currentState = State.GET_NEW_LINE;
   			return;
   		}
   		if (myPlayer().isAnimating()) {
   			return;
   		}
   		if (isFireAtPos()) {
   			int nextTileX = getNextEmptyTileX();
   			if (nextTileX == -1) {
   				currentState = State.GET_NEW_LINE;
   				return;
   			}
   			runToPos(new Position(nextTileX, lineY, 0));
   			Sleep.sleepUntil(() -> myPosition().getX() == nextTileX, 3000);
   			return;
   		}
   		int x = myPosition().getX();
   		inventory.interact("Use", "Tinderbox");
   		sleep(random(200,300));
        inventory.interact("Use", logName);
        Sleep.sleepUntil(() -> x != myPosition().getX(), 3000);
        // or is animating
   	}
   	
   	public void runToPos(Position pos) {
   		if (myPosition().equals(pos)) {
   			currentState = State.FIREMAKING;
   			return;
   		}
    	WalkingEvent myEvent = new WalkingEvent(pos); //making the event
    	myEvent.setMinDistanceThreshold(0);
    	execute(myEvent); //executing the event
    	Sleep.sleepUntil(() -> myPosition().equals(pos), 3000);
   	}
   	
   	public void bank() throws InterruptedException {
   		if (bankHandler.hasFinished()) {
   			currentState = State.GET_NEW_LINE;
   			return;
   		}
   		bankHandler.bank();
   	}
  
    @Override
    public int onLoop() throws InterruptedException {
    	if (!inventory.contains(logName)) {
    		currentState = State.BANKING;
    	}
    	if (currentState == State.FIREMAKING) {
    		firemake();
    	}
    	else if (currentState == State.GET_NEW_LINE) {
    		lineY = getFiremakingLineY();
    	}
    	else if (currentState == State.RUN_TO_LINE) {
    		runToPos(new Position(startX, lineY, 0));
    	}
    	else if (currentState == State.BANKING) {
    		bank();
    	}
    	checkEndScript();
    	if (worldHopper.shouldHop()) {
    		worldHopper.hop();
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