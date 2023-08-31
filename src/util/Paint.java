package util;

import java.awt.Graphics2D;
import java.util.ArrayList;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;


public class Paint {
	protected MethodProvider api;
	
	ArrayList<String> textList;
	Skill[] skills;
	String[] lines;
	Graphics2D g;
	
	public Paint(MethodProvider api, Graphics2D g, Skill[] skills, String[] lines) {
		this.api = api;
		this.g = g;
		this.skills = skills;
		this.textList = new ArrayList<String>();
		this.lines = lines;
	}
	
	public Paint(MethodProvider api, Graphics2D g, Skill[] skills) {
		this(api, g, skills, null);
	}
	
	public Paint(MethodProvider api, Graphics2D g) {
		this(api, g, null, null);
	}
	
	public void paint(long runTime) {
    	addSkills();
		addLines();
		addRunTime(runTime); 
		drawText();
	}
	
	public void addLines() {
		if (lines == null) {
			return;
		}
		for (String line : lines) {
			addPaintText(line);
		}
	}
	
	public void addSkills() {
		if (skills == null) {
			return;
		}
		for (Skill skill : skills) {
			addPaintText(textList, skill);
		}
	}
	
	public void addRunTime(long runTime) {
		textList.add("Runtime: " + formatTime(runTime));
	}
	
	public void drawText() {
		int y = 60;
    	int yDifference = 20;
    	int x = 10;
		
    	for (String text: textList) {
    		g.drawString(text, x, y);
    		y += yDifference;
    	}
	}
	
	public void addPaintText(String line) {
		textList.add(line);
	}
	
    public void addPaintText(ArrayList<String> textList, Skill skill) {
    	String xp = formatValue(api.getExperienceTracker().getGainedXP(skill));
    	String xpPerHour = formatValue(api.getExperienceTracker().getGainedXPPerHour(skill));
    	int levels = api.getExperienceTracker().getGainedLevels(skill);
    	textList.add(skill.name() + " xp gained: " + xp + " (" + xpPerHour + ")");
    	textList.add(skill.name() + " levels gained: " + levels);
    }
    
    public final String formatValue(final long l) {
        return (l > 1_000_000) ? String.format("%.2fm", ((double) l / 1_000_000))
               : (l > 1000) ? String.format("%.1fk", ((double) l / 1000)) 
               : l + "";
    }
    
    public final String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
