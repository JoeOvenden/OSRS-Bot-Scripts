package gui;

import org.osbot.rs07.api.ui.Skill;

public class GenericMakeGUI extends GUI {
	public GenericMakeGUI() {
		super();
		addTextbox("First item");
		addTextbox("First item amount");
		addTextbox("Second item");
		addTextbox("Second item amount");
		addTextbox("Make text");
		addTextbox("Option number");
		addTextbox("Skill being trained:");
		packDialogue();
	}
	
	public String[] getItems() {
		return new String[] {textboxes.get(0).getText(), textboxes.get(2).getText()};
	}
	
	public int[] getItemAmounts() {
		return new int[] {Integer.valueOf(textboxes.get(1).getText()), Integer.valueOf(textboxes.get(3).getText())};
	}
	
	public String getMakeText() {
		return textboxes.get(4).getText();
	}
	
	public int getWidgetNumber() {
		return Integer.valueOf(textboxes.get(5).getText()) + 13;
	}
	
	public Skill getSkill() {
		return Skill.valueOf(textboxes.get(6).getText().toUpperCase());
	}
}
