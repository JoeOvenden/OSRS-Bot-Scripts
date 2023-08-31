package util;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

public class OpenObjectWidget extends Task {
	
	String action, objectName; 
	RS2Widget widget;
	RS2Object object = null;

	public OpenObjectWidget(MethodProvider api, String action, String objectName, RS2Widget widget) {
		super(api);
		this.action = action;
		this.objectName = objectName;
		this.widget = widget;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canProcess() {
		object = api.getObjects().closest(objectName);
		return object != null;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return widget != null;
	}

	@Override
	public void process() throws InterruptedException {
		object.interact(action, objectName);
	}

}
