package scripts;

import org.osbot.rs07.api.Client;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.OneTextboxGUI;
import util.Paint;
import util.Sleep;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Rogue's Den Cooker", author = "Joe", version = 1.0, info = "Cooks food at rogue's den.", logo = "") 

public class RoguesCooker extends Script {
	static enum States {
			COOKING,
			BANKING,
			LOGOUT
	}
	States currentState = States.COOKING;
	String rawFood;
	private long startTime;
	long runTime;
	Skill[] skills = {Skill.COOKING};
	
	OneTextboxGUI gui;
	
    @Override
    public void onStart() {
    	setupGUI();
    	
    	startTime = System.currentTimeMillis();
    	for(final Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    }
    
    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new OneTextboxGUI("Type the name of what you want to cook:");
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
    	rawFood = gui.getText();
    }

    

    @Override

    public void onExit() {
        //Code here will execute after the script ends
    	log("Cooking bot has exited.");
    	
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
    
    
    public void logout() {
    	/*
    	 * If player is logged out
    	 * 		Stop script
    	 */
    	if(getClient().getLoginState().equals(Client.LoginState.LOGGED_OUT)) {
    		stop();
    		return;
    	}
    	
    	/*
    	 * Player is not logged out
    	 * 
    	 * If logout tab is open
    	 * 		Logout
    	 */
    	
    	if (getLogoutTab().isOpen()) {
    		getLogoutTab().logOut();
    		Sleep.sleepUntil(() -> getClient().getLoginState().equals(Client.LoginState.LOGGED_OUT), 5000);
    		stop();
    		return;
    	}
    	
    	/*
    	 * Player is not logged out
    	 * AND logout tab is not open
    	 * 
    	 * SO open logout tab
    	 */
    	
    	getLogoutTab().open();
    	Sleep.sleepUntil(() -> getLogoutTab().isOpen(), 5000);
    	return;
    }
    
    
    public void cookFood() throws InterruptedException {
    	
    	// If player is already cooking, then there is nothing to be done.
    	if (checkAnimating()) {
    		return;
    	}
    	
    	// If there is no raw food in inventory, then player must bank.
    	if (!getInventory().contains(rawFood)) {
    		currentState = States.BANKING;
    		return;
    	}
    	
    	/*
    	 * If cooking widget is not null, this means the player has clicked on
		 * the fire and is being prompted to cook the fish.
    	 */
    	RS2Widget cookingWidget = getWidgets().get(270, 14);
    	if (cookingWidget != null && cookingWidget.interact("Cook")) {
    		Sleep.sleepUntil(() -> myPlayer().isAnimating(), 5000);
    	    return;
    	}
    	
    	/*
    	 * If player is not cooking but fire interface is not open, then
    	 * player must interact with the fire.
    	 */
    	RS2Object fire = getObjects().closest("Fire");
    	if (fire != null && fire.interact("Cook")) {
    		Sleep.sleepUntil(() -> getWidgets().get(270, 14) != null, 5000);
    	    return;
    	}
    	
    }
    
    
    public void bank() {
    	/*
    	 * If inventory contains the raw food to be cooked
    	 * then if the bank is closed, change state to cooking.
    	 * Else close bank.
    	 */
    	if (getInventory().contains(rawFood)) {
    		if (!getBank().isOpen()) {
    			currentState = States.COOKING;
    		}
    		else {
    			getBank().close();
    			Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
    		}
    		return;
    	}
    	
    	/*
    	 * Inventory doesn't contain raw food
    	 * 
    	 * If bank is not open
    	 * 		Open bank
    	 */
    	if (!getBank().isOpen()) {
    		NPC em_benedict = getNpcs().closest("Emerald Benedict");
        	if (em_benedict != null) {
        		em_benedict.interact("Bank");
        		Sleep.sleepUntil(() -> getBank().isOpen(), 5000);
        		return;
        	}
    	}
    	
    	/*
    	 * Inventory doesn't contain raw food
    	 * AND bank is open
    	 * 
    	 * If no raw food in bank
    	 * 		log out.
    	 */
    	
    	if (!getBank().contains(rawFood)) {
    		currentState = States.LOGOUT;
    		return;
    	}
    	
    	/*
    	 * Inventory doesn't contain raw food
    	 * AND bank is open
    	 * 
    	 * If inventory is empty
    	 * 		withdraw the raw food.
    	 */
    	
    	if (getInventory().isEmpty()) {
    		getBank().withdrawAll(rawFood);
    		Sleep.sleepUntil(() -> getInventory().contains(rawFood), 5000);
    		return;
    	}
    	
    	/*
    	 * Inventory doesn't contain raw food
    	 * AND bank is open
    	 * AND inventory is not empty
    	 * 
    	 * So bank all
    	 */
    	
    	getBank().depositAll();
    	return;
    }

    @Override
    public int onLoop() throws InterruptedException {
    	if (currentState == States.COOKING) {
    		cookFood();
    	}
    	else if (currentState == States.BANKING) {
    		bank();
    	}
    	else if (currentState == States.LOGOUT) {
    		logout();
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


