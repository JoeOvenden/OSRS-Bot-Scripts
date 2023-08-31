package util;

import org.osbot.rs07.script.MethodProvider;

public class MakeAll {
	
	protected MethodProvider api;
	String item1;
	String item2;
	String makeItem;

	public MakeAll(MethodProvider api, String item1, String item2, String makeItem) {
		this.api = api;
		this.item1 = item1;
		this.item2 = item2;
		this.makeItem = makeItem;
	}
	
	public void go() throws InterruptedException {

	    if ((api.getInventory().contains(item1)) && (api.getInventory().contains("ITEM2")) && !api.myPlayer().isAnimating()) {
	        api.inventory.interact("Use", item1);
	        api.inventory.interact("Use", item2);
	        
	        Sleep.sleepUntil(() -> api.getWidgets().isVisible(309, 4), 5000);
	        
	        api.getWidgets().interact(309, 4, "Make " + makeItem);
	        Sleep.sleepUntil(() -> api.myPlayer().isAnimating(), 5000);
	    }
	    
    }
}
