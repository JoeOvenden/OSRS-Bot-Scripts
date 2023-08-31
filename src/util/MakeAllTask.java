package util;

import org.osbot.rs07.script.MethodProvider;

public class MakeAllTask extends Task {

	String item1;
	String item2;
	String makeText;
	int widgetNumber;
	
	public MakeAllTask(MethodProvider api, String item1, String item2, int widgetNumber, String makeText) {
		super(api);
		this.item1 = item1;
		this.item2 = item2;
		this.widgetNumber = widgetNumber;
		this.makeText = makeText;
	}

	@Override
	public boolean canProcess() {
		return api.inventory.contains(item1) && api.inventory.contains(item2);
	}

	@Override
	public boolean isProcessing() {
		return api.myPlayer().isAnimating();
	}

	@Override
	public boolean isProcessed() {
		return !(api.inventory.contains(item1) && api.inventory.contains(item2));
	}

	@Override
	public void process() throws InterruptedException {
		api.inventory.interact("Use", item1);
        api.inventory.interact("Use", item2);
        
        Sleep.sleepUntil(() -> api.getWidgets().isVisible(270, 0), 5000);
        
        api.getWidgets().interact(270, widgetNumber, makeText);
        Sleep.sleepUntil(() -> api.myPlayer().isAnimating(), 5000);
	}

}
