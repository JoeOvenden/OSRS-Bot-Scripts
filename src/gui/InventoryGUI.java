package gui;

import java.util.ArrayList;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.MethodProvider;


public class InventoryGUI extends GUI {
	
	protected MethodProvider api;
	
	public InventoryGUI(MethodProvider api, String label) {
		super();
		this.api = api;
		ArrayList<String> items = new ArrayList<String>();
		Item[] invent = api.inventory.getItems();
		for (Item item : invent) {
			if (item != null && !items.contains(item.getName())) {
				items.add(item.getName());
			}
		}
		addSelecter(label, items);
		packDialogue();;
	}
}
