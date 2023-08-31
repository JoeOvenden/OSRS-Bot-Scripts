package util;

import java.util.function.BooleanSupplier;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

public class InteractWidgetTask extends Task {
	
	String action;
	RS2Widget widget;
	final BooleanSupplier condition ;

	public InteractWidgetTask(MethodProvider api, String action, RS2Widget widget, final BooleanSupplier condition) {
		super(api);
		this.action = action;
		this.widget = widget;
		this.condition = condition;
	}

	@Override
	public boolean canProcess() {
		return widget != null && widget.isVisible();
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean isProcessed() {
		return condition.getAsBoolean();
	}

	@Override
	public void process() throws InterruptedException {
		widget.interact(action);
	}

}
