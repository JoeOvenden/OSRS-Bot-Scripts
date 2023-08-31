package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

public class PickTask extends Task {
	
	String objectName, interactText;
	RS2Object object;

	public PickTask(MethodProvider api, String objectName, String interactText) {
		super(api);
		this.objectName = objectName;
		this.interactText = interactText;
		this.object = null;
	}

	@Override
	public boolean canProcess() {
		if (object == null || !object.exists()) {
			object = api.getObjects().closest(true, objectName);
		}
		return object != null;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return api.inventory.isFull();
	}

	@Override
	public void process() throws InterruptedException {
		if (object != null) {
			object.interact(interactText);
		}
	}

}
