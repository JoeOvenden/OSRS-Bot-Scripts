package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.GUI;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.World;
import org.osbot.rs07.api.model.NPC;

import util.Sleep;
import util.Task;
import util.BankHandler;
import util.DepositAllExceptTask;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Shop Buyer and Seller", author = "Joe", version = 1.0, info = "Buys and sells item at shop", logo = "") 

public class ShopBuyAndSell extends Script {
	
	enum State {
		BUYING_SELLING,
		HOP_WORLD,
		BANKING
	}
	
	Task currentTask;
	State currentState = State.BANKING;
	boolean finished = false;
	
	private long startTime;
	long runTime;
	
	GUI gui;
	
	String npcName = "";
	String itemName = "";
	int itemAmount = 0;
	long endAmount = 0;
	int itemDefaultStock = 0;
	boolean buy = false;
	boolean ironman = true;
	boolean endScript = false;
	
	BankHandler bankHandler;
		
	int world;
	
	Skill[] skills = {};
	
    @Override
    public void onStart() {
    	
        //Code here will execute before the loop is started
    	currentTask = null;
    	startTime = System.currentTimeMillis();
    	
    	bankHandler = new BankHandler(this);
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    	setupBankHandler();
    	setupGUI();
    	world = worlds.getCurrentWorld();
    }
    
    public void setupBankHandler() {
    	bankHandler = new BankHandler(this);
    	bankHandler.addTask(new DepositAllExceptTask(this, "Coins"));
    	bankHandler.finalise();
    }

    public void setupGUI() {
    	try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new GUI(new String[] {"Item name:", "Amount per world:", 
						"End amount:", "Default stock amount:", "Npc name:"});
				gui.addCheckbox("Buy:");
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
    	itemName = gui.getTextboxValue(0);
    	itemAmount = Integer.valueOf(gui.getTextboxValue(1));
    	endAmount = Integer.valueOf(gui.getTextboxValue(2));
    	itemDefaultStock = Integer.valueOf(gui.getTextboxValue(3));
    	npcName = gui.getTextboxValue(4);
    	buy = gui.isCheckboxChecked(0);
    }

    @Override
    public void onExit() {

    }
    
    public void checkEndScript() {
    	if (endScript) {
    		stop(false);
    		return;
    	}
    	long currentAmount = inventory.getAmount(itemName);
    	if (buy && currentAmount >= endAmount) {
    		stop(false);
    		return;
    	}
    	/*
    	else if (!buy && currentAmount <= endAmount) {
    		stop(false);
    	}
    	*/
    }
   
    public void buyAndSell() throws InterruptedException {
    	// If store is not open then open it
    	if (buy && inventory.isFull()) {
    		currentState = State.BANKING;
    		return;
    	}
    	if (!store.isOpen()) {
    		NPC shop = getNpcs().closest(npcName);
    		if (shop != null) {
    			shop.interact("Trade");
    			Sleep.sleepUntil(() -> store.isOpen(), 5000);
    		}
    		return;
    	}
    	
    	// Calculate amount to buy
    	int stock = store.getAmount(itemName);
    	int buySellAmount = 0;
    	
    	if (ironman && stock > itemDefaultStock) {
    		sleep(random(350, 450));
    		currentState = State.HOP_WORLD;
    		return;
    	}
    	
    	if (buy) {
    		buySellAmount = itemAmount - (itemDefaultStock - stock);
    	}
    	else {
    		buySellAmount = itemAmount - (stock - itemDefaultStock);
    	}
    	
    	// Hop world if we have already sold/bought enough, or depending on stock.
    	if (buySellAmount <= 0) {
    		sleep(random(350, 450));
    		currentState = State.HOP_WORLD;
    		return;
    	}
    	long currentAmount = inventory.getAmount(itemName);
    	if (buy) {
    		store.buy(itemName, buySellAmount);
    		long endAmount = currentAmount + buySellAmount;
    		Sleep.sleepUntil(() -> inventory.getAmount(itemName) != endAmount, 5000);
    	}
    	else {
    		long endAmount = currentAmount - buySellAmount;
    		store.sell(itemName, buySellAmount);
    		Sleep.sleepUntil(() -> inventory.getAmount(itemName) != endAmount || inventory.getAmount(itemName) == 0, 5000);
    	}
    }
    
    private int nextWorldInOrder(boolean members) {
        return getWorlds().getAvailableWorlds(true)
                .stream()
                .filter(world -> !world.isPvpWorld() && world.isMembers() == members && !world.getActivity().contains("skill") && !world.getActivity().contains("Deadman") && world.getId() > getWorlds().getCurrentWorld())
                .min(Comparator.comparingInt(World::getId))
                .map(World::getId)
                .orElseGet(() -> members ? 302 : 301);
    }
    
    public void hopWorld() throws InterruptedException {
    	// If store is open, then close it
    	if (store.isOpen()) {
    		store.close();
    		Sleep.sleepUntil(() -> !store.isOpen(), 5000);
    		return;
    	}
    	
    	// If we have hopped then change state and return
    	if (world != worlds.getCurrentWorld()) {
    		world = worlds.getCurrentWorld();
    		currentState = State.BUYING_SELLING;
    		return;
    	}
    	
    	worlds.hop(nextWorldInOrder(true));
    	Sleep.sleepUntil(() -> world != worlds.getCurrentWorld(), 5000);
    	sleep(1000);
    }
    
    public void onMessage(Message message){
   		String txt = message.getMessage().toLowerCase();
   		if (txt.contains("don't have enough coins")) {
   			endScript = true;
   		}
	}

    @Override
    public int onLoop() throws InterruptedException {
    	if (currentState == State.BUYING_SELLING) {
    		buyAndSell();
    	}
    	else if (currentState == State.HOP_WORLD) {
    		hopWorld();
    	}
    	else if (currentState == State.BANKING) {
    		if (!bankHandler.hasFinished()) {
    			bankHandler.bank();
    		}
    		else {
    			currentState = State.BUYING_SELLING;
    		}
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