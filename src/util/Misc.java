package util;

import java.util.List;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

public class Misc {
	
	protected static MethodProvider api;
	
	public Misc(MethodProvider api) {
		Misc.api = api;
	}
	
	public static boolean containsItemWithNameContaining(List<String> itemList, String namePart) {
        for (String item : itemList) {
            if (item.toLowerCase().contains(namePart.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
	
	public boolean bank(List<String> items, long[] amounts) throws InterruptedException {
		if (!api.bank.isOpen()) {
			api.bank.open();
			Sleep.sleepUntil(() -> api.bank.isOpen(), 5000);
			return false;
		}
		
//		boolean depositAll = true;
//		for (Item item : api.inventory.getItems()) {
//			if (items.contains(item.getName())) {
//				depositAll = false;
//			}
//			if (!items.contains(item.getName())) {
//				
//			}
//		}
		return false;
	}
	
	public boolean checkAnimating() throws InterruptedException {
    	for (int i = 0; i < 10; i++) {
    		if (api.myPlayer().isAnimating()) {
    			return true;
    		}
    		MethodProvider.sleep(200);
    	}
    	return false;
    }

	public int getPrayerPointsPercentage(MethodProvider api) {
		return Math.round(api.getSkills().getDynamic(Skill.PRAYER) * 100 / api.getSkills().getStatic(Skill.PRAYER));
	}
	
	public static boolean checkAnimating(long ms) throws InterruptedException {
    	for (int i = 0; i < (ms / 100); i++) {
    		if (api.myPlayer().isAnimating()) {
    			return true;
    		}
    		MethodProvider.sleep(100);
    	}
    	return false;
    }
	
	public final static  boolean between(int value, int a, int b) {
		if (value >= Math.min(a, b) && value <= Math.max(a, b)) {
			return true;
		}
		return false;
	}
}