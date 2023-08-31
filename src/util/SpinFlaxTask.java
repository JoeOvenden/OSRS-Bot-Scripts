package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

public class SpinFlaxTask extends Task {
	
	RS2Object spinningWheel;
	
	public SpinFlaxTask(MethodProvider api) {
		super(api);
	}

	@Override
	public boolean canProcess() {
		spinningWheel = api.getObjects().closest("Spinning wheel");
		return spinningWheel != null;
	}

	public boolean isProcessing(){
		boolean animating = api.myPlayer().isAnimating();
		Sleep.sleepUntil(() -> api.myPlayer().isAnimating(), 1000);
		animating = api.myPlayer().isAnimating();
		return animating;
	}

	@Override
	public boolean isProcessed() {
		return !api.inventory.contains("Flax");
	}

	@Override
	public void process() throws InterruptedException {
		if (!api.inventory.contains("Flax")) {
			return;
		}
		// If widget is open then spin the flax
		if (!api.widgets.isVisible(270, 16)) {
			spinningWheel.interact("Spin");
			Sleep.sleepUntil(() -> api.widgets.isVisible(270, 16), 5000);
		}
		
		if (api.widgets.isVisible(270, 16)) {
			api.getWidgets().interact(270, 16, "Spin");
		}
	}

}
