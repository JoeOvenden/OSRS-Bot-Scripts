package gui;

public class FletchingGUI extends GUI {
	public FletchingGUI() {
		super();
    	String[] logs = {"Normal", "Oak", "Willow", "Maple", "Yew", "Magic", "Redwood"};
    	String[] makeOptions = {"Shafts", "Shortbow", "Longbow", "Stock", "Shield"};
    	String[] cutOrString = {"Cut", "String"};

    	addSelecter("Select log:", logs);
    	addSelecter("Select what to make:", makeOptions);
    	addSelecter("Select cut or string:", cutOrString);
    	
    	packDialogue();
	}
	
    public String getSelectedLog() {
    	String log = (String) selecters.get(0).getSelectedItem();
    	if (log == "Normal") {
    		return "Logs";
    	}
    	return log;
    }
    
    public int getMakeOption() {
    	// Returns the correct widget number for the make option
    	String makeOption = (String) selecters.get(1).getSelectedItem();
    	int widgetNumber;
    	if (makeOption == "Shafts") {
    		widgetNumber = 14;
    	}
    	else if (makeOption == "Shortbow") {
    		widgetNumber = 15;
    	}
    	else if (makeOption == "Longbow") {
    		widgetNumber = 16;
    	}
    	else if (makeOption == "Stock") {
    		widgetNumber = 17;
    	}
    	else {
    		widgetNumber = 18;
    	}
    	
    	// Normal logs have the option of making javelin shafts which take the widget 15 slot.
    	if (getSelectedLog() == "Logs" && widgetNumber > 14) {
    		widgetNumber += 1;
    	}
    	return widgetNumber;
    }
    
    public String getCutString() {
    	return (String) selecters.get(2).getSelectedItem();
    	
    }
}
