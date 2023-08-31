package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

public class ClimbLadderTask extends Task {
	
	String objectName, interactText;
	RS2Object object;
	int startZ;
	int id = -1;

	public ClimbLadderTask(MethodProvider api, String objectName, String interactText) {
		super(api);
		this.objectName = objectName;
		this.interactText = interactText;
		this.startZ = api.myPosition().getZ();
	}
	
	public ClimbLadderTask(MethodProvider api, String objectName, String interactText, int id) {
		this(api, objectName, interactText);
		this.id = id;
	}

	@Override
	public boolean canProcess() {
		if (id != -1) {
			object = api.getObjects().closest(id);
		}
		else {
			object = api.getObjects().closest(objectName);
		}
		return object != null;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return api.myPosition().getZ() != startZ;
	}

	@Override
	public void process() throws InterruptedException {
		object.interact(interactText);
	}

}
