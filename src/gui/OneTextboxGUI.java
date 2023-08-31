package gui;

public class OneTextboxGUI extends GUI {
	public OneTextboxGUI(String text) {
		super();
		addTextbox(text);
		packDialogue();
	}
	
	public String getText() {
		return textboxes.get(0).getText();
	}
}
