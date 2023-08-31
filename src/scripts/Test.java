package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;


import org.osbot.rs07.api.ui.Skill;

import util.WithdrawAllTask;
import util.BankHandler;
import util.DepositAllTask;
import util.Paint;

import java.awt.*;

@ScriptManifest(name = "Test script", author = "Joe", version = 1.0, info = "Test", logo = "") 

public class Test extends Script {
	
	private long startTime;
	long runTime;
	BankHandler bankHandler;
	boolean finished = false;
	
	Skill[] skills = {};
	
    @Override
    public void onStart() {
        //Code here will execute before the loop is started
    	startTime = System.currentTimeMillis();
    	setupBankHandler();
    	
    	for(Skill skill : skills) {
    	    getExperienceTracker().start(skill);
    	}
    	
    }
    
    public void setupBankHandler() {
    	bankHandler = new BankHandler(this);
    	bankHandler.addTask(new DepositAllTask(this));
    	bankHandler.addTask(new WithdrawAllTask(this, "Air rune"));
    	bankHandler.addTask(new WithdrawAllTask(this, "Fire rune"));
    	bankHandler.finalise();
    }

    @Override
    public void onExit() {

    }
    
   public void checkEndScript() {
   }
  

    @Override
    public int onLoop() throws InterruptedException {
    	if (!finished) {
    		if (bankHandler.hasFinished()) {
    			finished = true;
    		}
    		else {
    			bankHandler.bank();
    		}
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