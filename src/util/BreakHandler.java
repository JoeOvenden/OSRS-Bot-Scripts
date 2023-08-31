package util;

import org.osbot.rs07.api.Client;
import org.osbot.rs07.script.MethodProvider;

public class BreakHandler {
	
	MethodProvider api;
	long breakStartTime = System.currentTimeMillis();
	boolean isBreaking = false;
	int breakLength = 0;
	
	public BreakHandler(MethodProvider api) {
		this.api = api;
	}
	
	public void breakFor(int mins) {
		breakStartTime = System.currentTimeMillis();
		isBreaking = true;
		breakLength = mins;
	}
	
	public void handleBreak() {
		// TODO: Logout
		if(!api.getClient().getLoginState().equals(Client.LoginState.LOGGED_OUT)) {
			
		}
	}
	
	public boolean isOnBreak() {
		return isBreaking;
	}
	
	public int getTimeSinceBreak() {
		return (int) ((System.currentTimeMillis() - breakStartTime) / (60000));
	}
	
	public boolean breakFinished() {
		if (getTimeSinceBreak() > breakLength) {
			return true;
		}
		return false;
	}
	
	public void endBreak() {
		isBreaking = false;
		// TODO: Login
	}
}
