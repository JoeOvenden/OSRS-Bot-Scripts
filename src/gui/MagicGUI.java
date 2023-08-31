package gui;

public class MagicGUI extends GUI {
	public MagicGUI() {
		super();
    	String[] spells = {"LOW_LEVEL_ALCHEMY", "HIGH_LEVEL_ALCHEMY", "LVL_1_ENCHANT", 
    			"LVL_2_ENCHANT", "LVL_3_ENCHANT", "LVL_4_ENCHANT", "LVL_5_ENCHANT",
    			"LVL_6_ENCHANT", "LVL_7_ENCHANT"};

    	addSelecter("Choose a spell:", spells);
    	addTextbox("Choose item to cast upon");
    	
    	packDialogue();
	}
	
	public String getSpell() {
		return (String) selecters.get(0).getSelectedItem();
	}
	
	public String getItem() {
		return textboxes.get(0).getText();
	}
    
    
}
