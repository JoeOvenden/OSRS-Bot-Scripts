package util;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;

public class NPCInteractTask extends Task{
	
	private String name;
	private String interactText;
	private NPC npc;
	
	public NPCInteractTask(MethodProvider api, String name, String interactText) {
		super(api);
		this.name = name;
		this.interactText = interactText;
	}

	@Override
	public boolean canProcess() {
		npc = api.getNpcs().closest(name);
		return npc != null;
	}

	@Override
	public boolean isProcessed() {
		return false;
	}

	@Override
	public void process() throws InterruptedException {
		npc.interact(interactText);
		
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

}
