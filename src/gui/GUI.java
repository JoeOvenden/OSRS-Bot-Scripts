package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class GUI {
    JDialog mainDialog;
    JPanel mainPanel = new JPanel();
    ArrayList<JComboBox<String>> selecters = new ArrayList<JComboBox<String>>();
    ArrayList<JTextField> textboxes = new ArrayList<>();;
    ArrayList<JCheckBox> checkboxes = new ArrayList<>();;
    boolean started;

    public GUI() {
    	setUpMainPanel();
    	addStartButton();
    }
    
    public GUI(String[] textboxLabels) {
    	this();
    	for (String textboxLabel : textboxLabels) {
    		addTextbox(textboxLabel);
    	}
    	packDialogue();
    }
    
    public void setUpMainPanel() {
        mainDialog = new JDialog();
        mainDialog.setTitle("Bot");
        mainDialog.setModal(true);
        mainDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainDialog.getContentPane().add(mainPanel);
    }
    
    public void addStartButton() {
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            started = true;
            close();
        });
        mainPanel.add(startButton);
    }
    
    public void addSelecter(String labelText, String[] options) {
        // Make selector
    	
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel selectionLabel = new JLabel(labelText);
        selectionPanel.add(selectionLabel);

        JComboBox<String> selecter = new JComboBox<>(options);
        selectionPanel.add(selecter);
        
        selecters.add(selecter);
        mainPanel.add(selectionPanel);
    }
    
    public void addSelecter(String labelText, ArrayList<String> options) {
        // Make selector
    	addSelecter(labelText, options.toArray(new String[0]));
    }
    
    public void addTextbox(String labelText) {
        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel selectionLabel = new JLabel(labelText);
        textboxPanel.add(selectionLabel);

        JTextField textbox = new JTextField(10);
        textboxPanel.add(textbox);
        
        textboxes.add(textbox);
        mainPanel.add(textboxPanel);
    }
    
    public void addCheckbox(String labelText) {
        JPanel textboxPanel = new JPanel();
        textboxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel selectionLabel = new JLabel(labelText);
        textboxPanel.add(selectionLabel);

        JCheckBox checkbox = new JCheckBox();
        textboxPanel.add(checkbox);
        
        checkboxes.add(checkbox);
        mainPanel.add(textboxPanel);
    }
    
    public String getTextboxValue(int index) {
    	return textboxes.get(index).getText();
    }
    
    public boolean isCheckboxChecked(int index) {
    	return checkboxes.get(index).isSelected();
    }
    
    public String getSelecterValue(int index) {
    	if (index >= 0 && index < selecters.size()) {
    		return selecters.get(index).getSelectedItem().toString();
    	}
    	return null;
    }
    
    public void packDialogue() {
    	mainDialog.pack();
    }

    public boolean isStarted() {
        return started;
    }

    public void open() {
        mainDialog.setVisible(true);
    }

    public void close() {
        mainDialog.setVisible(false);
        mainDialog.dispose();
    }
    
}
