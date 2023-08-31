package scripts;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import gui.GUI;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.PrayerButton;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;

import util.Sleep;
import util.BreakHandler;
import util.Misc;
import util.Paint;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

@ScriptManifest(name = "Nightmare zone", author = "Joe", version = 1.0, info = "Does nightmare zone", logo = "")

public class NightmareZone extends Script {

	enum State {
		INSIDE_NMZ, RESTOCKING
	}

	State currentState = State.RESTOCKING;

	private long startTime;
	long runTime;
	long lastOverloadTime;

	GUI gui;
	RS2Widget absorpWidget;

	int startOverloadDoses = 32;

	int prayerDrinkPercent = 55;
	int absorbDrink = 0;
	int absorbDoses = 6;
	int absorptionPoints = 0;
	Misc misc = new Misc(this);
	BreakHandler breakHandler = new BreakHandler(this);

	// List of prayers that you want activated
	List<PrayerButton> prayers = Arrays.asList();
	Skill[] skills = { Skill.ATTACK, Skill.HITPOINTS };
	Skill[] statBoosts = { Skill.ATTACK, Skill.STRENGTH };
	String[] potions = { "Super attack", "Super strength" };

	boolean absorption = true;
	boolean overload = true;
	boolean dreamIsSetup = false;
	boolean boughtPotions = false;

	long lastRapidHeal;
	long lastSpecChange;
	int specEnergy = 100;

	List<String> powerups = Arrays.asList("Zapper", "Recurrent damage", "Power surge");
	boolean powerSurge = false;
	boolean mainWepHasSpec = false;
	String mainWeapon = "";
	String shieldSlot = "";
	String specWep = "Granite maul";

	@Override
	public void onStart() {
		// Code here will execute before the loop is started
		startTime = System.currentTimeMillis();
		lastOverloadTime = startTime;
		lastRapidHeal = System.currentTimeMillis();
		lastSpecChange = System.currentTimeMillis();
		setAbsorb();

		// setupGUI();

		getSkillBeingTrained();
		for (Skill skill : skills) {
			getExperienceTracker().start(skill);
		}

		mainWeapon = equipment.getItemInSlot(3).getName();
		if (mainWeapon.contains("Magic shortbow")) {
			mainWepHasSpec = true;
		}
		if (equipment.isWearingItem(EquipmentSlot.SHIELD)) {
			shieldSlot = equipment.getItemInSlot(5).getName();
		}
		if (!inventory.contains(specWep)) {
			specWep = mainWeapon;
		}
	}

	public void getSkillBeingTrained() {
		if (equipment.isWieldingWeaponThatContains("bow") || equipment.isWieldingWeaponThatContains("blowpipe")) {
			skills[0] = Skill.RANGED;
			return;
		}
		if (equipment.isWieldingWeaponThatContains("staff")) {
			skills[0] = Skill.MAGIC;
			return;
		}

		Skill[] attackStyles = { Skill.COOKING, Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.COOKING,
				Skill.COOKING };
		int attackStyleConfig = getConfigs().get(46);
		skills[0] = attackStyles[attackStyleConfig];
	}

	public void setupGUI() {
		try {
			SwingUtilities.invokeAndWait(() -> {
				gui = new GUI(new String[] { "Type the skill that is to be trained:" });
				gui.open();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (gui == null || !gui.isStarted()) {
			stop(false);
			return;
		} else {
			getInfoFromGUI();
		}
	}

	public void getInfoFromGUI() {
		skills[0] = Skill.valueOf(gui.getTextboxValue(0).toUpperCase());
	}

	@Override
	public void onExit() {

	}

	public void addNextTask() {
		/*
		 * IF CURRENT TASK INSTANCE OF [TASK] CURREN TASK = NEW TASK
		 */

	}

	public void checkEndScript() {
		if (myPosition().getZ() == 0) {
			stop(false);
		}
	}

	public int getPotionDoses(String name) {
		int doses = 0;
		for (int i = 1; i < 5; i++) {
			doses += inventory.getAmount(name + " (" + String.valueOf(i) + ")") * i;
		}
		return doses;
	}

	public void drinkPrayerPotion() throws InterruptedException {
		getInventory().interactWithNameThatContains("Drink", "Prayer potion");
		Sleep.sleepUntil(() -> misc.getPrayerPointsPercentage(this) < prayerDrinkPercent, 2000);
		prayerDrinkPercent = random(20, 65);
		sleep(600);
	}

	public int statAfterSuperPotion(Skill skill) {
		// Assumes super potion
		return (int) Math.floor(getSkills().getStatic(skill) * 1.15 + 5);
	}

	public int getCurrentAbsorptionLevel() {
		absorpWidget = getWidgets().get(202, 2, 5);
		if (absorpWidget != null && absorpWidget.isVisible() && absorpWidget.getMessage() != null)
			return Integer.parseInt(absorpWidget.getMessage().replace(",", ""));
		return 0;
	}

	public void setAbsorb() {
		absorbDrink = random(200, 300);
		absorbDoses = random(6, 7);
	}

	public boolean drinkAbsorptionPotion() throws InterruptedException {
		if (inventory.getSlotForNameThatContains("Absorption") == -1) {
			return false;
		}
		getInventory().interactWithNameThatContains("Drink", "Absorption");
		Sleep.sleepUntil(() -> getCurrentAbsorptionLevel() > absorptionPoints, 5000);
		absorptionPoints = getCurrentAbsorptionLevel();
		sleep(600);
		return true;
	}

	public long timeSinceOverload() {
		return ((System.currentTimeMillis() - lastOverloadTime) / 1000);
	}

	public void setPrayer(PrayerButton pray, boolean state) {
		if (getSkills().getDynamic(Skill.PRAYER) == 0) {
			return;
		}
		openTab(Tab.PRAYER);
		prayer.set(pray, state);
		Sleep.sleepUntil(() -> prayer.isActivated(pray) == state, 5000);
	}

	public void checkAccidentalPrayers() {
		for (PrayerButton pray : PrayerButton.values()) {
			if (getPrayer().isActivated(pray) && !prayers.contains(pray)) {
				setPrayer(pray, false);
			}
		}
	}

	public void drinkOverload() throws InterruptedException {
		if (inventory.getSlotForNameThatContains("Overload") == -1) {
			return;
		}

		if (getSkills().getDynamic(Skill.HITPOINTS) < 51) {
			return;
		}

		if (timeSinceOverload() > 297 && getSkills().getDynamic(Skill.PRAYER) > 20) {
			setPrayer(PrayerButton.PROTECT_FROM_MELEE, true);
		} else if (timeSinceOverload() > 3 && prayer.isActivated(PrayerButton.PROTECT_FROM_MELEE)) {
			setPrayer(PrayerButton.PROTECT_FROM_MELEE, false);
		}

		if (getSkills().getDynamic(Skill.STRENGTH) == getSkills().getStatic(Skill.STRENGTH)) {
			if (inventory.interactWithNameThatContains("Drink", "Overload")) {
				Sleep.sleepUntil(() -> getSkills().getDynamic(Skill.STRENGTH) != getSkills().getStatic(Skill.STRENGTH),
						5000);
				lastOverloadTime = System.currentTimeMillis();
			}
			sleep(600);
		}
	}

	public void drinkStatBoosting() throws InterruptedException {
		for (int i = 0; i < statBoosts.length; i++) {
			Skill skill = statBoosts[i];
			if (getSkills().getDynamic(skill) <= statAfterSuperPotion(skill) - 4) {
				if (inventory.interactWithNameThatContains("Drink", potions[i])) {
					Sleep.sleepUntil(() -> getSkills().getDynamic(skill) > statAfterSuperPotion(skill) - 4, 5000);
				}
				sleep(600);
			}
		}
	}

	public void checkDrinkPotions() throws InterruptedException {
		absorptionPoints = getCurrentAbsorptionLevel();
		if (absorptionPoints < absorbDrink) {
			for (int i = 0; i < absorbDoses; i++) {
				boolean success = drinkAbsorptionPotion();
				if (!success) {
					return;
				}
			}
			setAbsorb();
		}

		if (misc.getPrayerPointsPercentage(this) < prayerDrinkPercent
				&& getInventory().getSlotForNameThatContains("Prayer potion") != -1) {
			drinkPrayerPotion();
		}

		if (overload) {
			drinkOverload();
		} else {
			drinkStatBoosting();
		}
	}

	public void activatePowerups() {
		RS2Object powerup = getObjects().closest(n -> powerups.contains(stripFormatting(n.getName())));
		if (powerup != null) {
			powerup.interact();
			Sleep.sleepUntil(() -> powerup == null || !powerup.hasAction("Activate"), 5000);
			String powerupName = stripFormatting(powerup.getName());
			if (powerupName.equals("Power surge")) {
				powerSurge = true;
			}
		}
	}

	public void flickRapidHeal() {
		setPrayer(PrayerButton.RAPID_HEAL, true);
		setPrayer(PrayerButton.RAPID_HEAL, false);
		checkAccidentalPrayers();
	}

	public void checkFlickRapidHeal() {
		if ((System.currentTimeMillis() - lastRapidHeal) / 1000 > 25) {
			flickRapidHeal();
			lastRapidHeal = System.currentTimeMillis();
		}
	}

	public void checkEatRockCake() {
		int hp = getSkills().getDynamic(Skill.HITPOINTS);

		// If hp is in the interval [2, 9], then rock cake.
		// This interval is chosen so that the player does not rock cake when they need
		// to overload.
		if (Misc.between(hp, 2, 9) && timeSinceOverload() < 290) {
			inventory.interact("Guzzle", "Dwarven rock cake");
			Sleep.sleepUntil(() -> hp != getSkills().getDynamic(Skill.HITPOINTS), 3000);
		}
	}

	public void openTab(Tab tab) {
		if (getTabs().isOpen(tab)) {
			return;
		}
		getTabs().open(tab);
		Sleep.sleepUntil(() -> getTabs().isOpen(tab), 5000);
	}

	public void equipWeapon(String wep) {
		if (!inventory.contains(wep)) {
			return;
		}
		openTab(Tab.INVENTORY);
		if (equipment.isWieldingWeapon(wep)) {
			return;
		}
		inventory.interact("Wield", wep);
		Sleep.sleepUntil(() -> equipment.isWieldingWeapon(wep), 5000);
	}

	public void equipShield(String item) {
		openTab(Tab.INVENTORY);
		if (equipment.isWearingItem(EquipmentSlot.SHIELD, item)) {
			return;
		}
		if (inventory.getItem(item).hasAction("Wield")) {
			inventory.interact("Wield", item);
		} else {
			inventory.interact("Wear", item);
		}
		Sleep.sleepUntil(() -> equipment.isWearingItem(EquipmentSlot.SHIELD, item), 5000);
	}

	public void getSpecEnergy() {
		int newValue = Integer.valueOf(getWidgets().get(160, 36).getMessage());
		if (specEnergy != newValue) {
			specEnergy = newValue;
			lastSpecChange = System.currentTimeMillis();
		}
	}

	public boolean isSpecActive() {
		return getConfigs().get(301) == 1;
	}

	public void useSpec() throws InterruptedException {
		if (powerSurge) {
			equipWeapon(specWep);
		} else {
			equipWeapon(mainWeapon);
			if (shieldSlot != "") {
				equipShield(shieldSlot);
			}
		}

		// If using main wep (i.e. no powersurge) and main weapon has no spec then don't
		// use spec
		if (!powerSurge && !mainWepHasSpec) {
			return;
		}

		if (isSpecActive()) {
			return;
		}

		// If have enough energy, use special
		getSpecEnergy();
		if (specEnergy >= 50) {
			getWidgets().get(160, 40).interact("Use");
			if (powerSurge && specWep == "Granite maul") {
				sleep(random(200, 350));
				return;
			}
			Sleep.sleepUntil(() -> isSpecActive(), 3000);
		}
	}

	public void pickupArrows() {
		GroundItem arrows = getGroundItems().closest(n -> n.getName().equals("Rune arrow") && n.getAmount() >= 20);
		if (arrows != null) {
			arrows.interact("Take");
		}
	}

	public void onMessage(Message message) {
		String txt = message.getMessage().toLowerCase();
		if (txt.contains("special attack power has ended")) {
			powerSurge = false;
		}
	}

	public int handleNmz() throws InterruptedException {
		// If z coordinate is 0, player has died and it is time to restock.
		if (myPosition().getZ() == 0) {
			stop();
			currentState = State.RESTOCKING;
			return 100;
		}

		useSpec();
		if (!powerSurge) {
			checkFlickRapidHeal();
			checkEatRockCake();
			// pickupArrows();
		}
		checkDrinkPotions();
		activatePowerups();
		checkEndScript();

		if (!client.isHumanInputEnabled() && !powerSurge) {
			mouse.moveOutsideScreen();
		}
		if (powerSurge) {
			if (System.currentTimeMillis() - lastSpecChange > 3000) {
				powerSurge = false;
			}
			return random(50, 100);
		}
		return random(500, 1500); // The amount of time in milliseconds before the loop starts over
	}

	public void takePotion(String potionName, int doses) {
		int currentDoses = getPotionDoses(potionName);
		if (widgets.get(162, 42).isVisible()) {
			getKeyboard().typeString(String.valueOf(doses));
			Sleep.sleepUntil(() -> getPotionDoses(potionName) != currentDoses, 5000);
			return;
		}

		RS2Object barrel = getObjects().closest(potionName + " potion");
		if (barrel != null) {
			barrel.interact("Take");
			Sleep.sleepUntil(() -> widgets.get(162, 42).isVisible(), 8000);
		}
	}

	public void setupDream() throws InterruptedException {
		if (!dialogues.inDialogue()) {
			NPC dom = getNpcs().closest("Dominic Onion");
			if (dom != null) {
				dom.interact("Dream");
				Sleep.sleepUntil(() -> dialogues.inDialogue(), 5000);
				return;
			}
		}

		dialogues.completeDialogue("Previous: Rumble (hard)", "Yes");
		Sleep.sleepUntil(() -> !dialogues.inDialogue(), 5000);
		if (!dialogues.inDialogue()) {
			dreamIsSetup = true;
		}

		/*
		 * if (dialogues.isPendingOption()) { RS2Widget optionTitleWidget =
		 * widgets.get(219, 1, 0); if (optionTitleWidget == null) { return; } String
		 * title = optionTitleWidget.getMessage(); if
		 * (title.contains("Which dream would you")) { dialogues.selectOption(4); } else
		 * if (title.contains("Agree to pay")){ dialogues.selectOption(1); }
		 * Sleep.sleepUntil(() -> !dialogues.isPendingOption(), 5000); }
		 */
	}

	public void rockCakeTo(int targetHp) throws InterruptedException {
		int hp = getSkills().getDynamic(Skill.HITPOINTS);
		int guzzleDamage = (int) (hp / 10) + 1;
		if (hp - guzzleDamage >= targetHp) {
			inventory.interact("Guzzle", "Dwarven rock cake");
		} else {
			inventory.interact("Eat", "Dwarven rock cake");
		}
		Sleep.sleepUntil(() -> hp != getSkills().getDynamic(Skill.HITPOINTS), 5000);
		sleep(random(500, 600));
	}

	public void enterDream() {
		RS2Widget enterDreamWidget = widgets.get(129, 6);
		if (enterDreamWidget == null) {
			RS2Object potion = getObjects().closest(n -> n.getName().contains("Potion"));
			if (potion != null) {
				potion.interact();
				Sleep.sleepUntil(() -> enterDreamWidget != null, 5000);
				return;
			}
		}

		enterDreamWidget.interact("Continue");
		Sleep.sleepUntil(() -> myPosition().getZ() == 3, 5000);
	}

	public int getDosesFromWidget(RS2Widget w) {
		String msg = w.getMessage();
		return Integer.valueOf(msg.substring(1, msg.length() - 1));
	}

	public void buyPotions() {
		// Widget for tab icon
		RS2Widget benefitsTabWidget = widgets.get(206, 2, 4);
		if (benefitsTabWidget == null) {
			RS2Object chest = getObjects().closest("Rewards chest");
			if (chest != null) {
				chest.interact("Search");
				Sleep.sleepUntil(() -> benefitsTabWidget != null, 5000);
			}
			return;
		}

		RS2Widget ovlPurchaseWidget = widgets.get(206, 6, 6);
		if (ovlPurchaseWidget == null || !ovlPurchaseWidget.isVisible()) {
			benefitsTabWidget.interact("Benefits");
			Sleep.sleepUntil(() -> ovlPurchaseWidget != null, 5000);
			return;
		}

		int points = Integer.valueOf(widgets.get(206, 2, 6).getMessage().split("\\s+")[2].replaceAll(",", ""));

		if (points < 1500) {
			boughtPotions = true;
			return;
		}

		RS2Widget absPurchaseWidget = widgets.get(206, 6, 9);
		RS2Widget ovlAmountWidget = widgets.get(206, 6, 8);
		RS2Widget absAmountWidget = widgets.get(206, 6, 11);

		int ovlDoses = getDosesFromWidget(ovlAmountWidget);
		int absDoses = getDosesFromWidget(absAmountWidget);

		if (ovlDoses != 255) {
			ovlPurchaseWidget.interact("Buy-50");
			Sleep.sleepUntil(() -> ovlDoses != getDosesFromWidget(ovlAmountWidget), 5000);
			return;
		}

		if (absDoses != 255) {
			absPurchaseWidget.interact("Buy-50");
			Sleep.sleepUntil(() -> absDoses != getDosesFromWidget(absAmountWidget), 5000);
			return;
		}

		boughtPotions = true;
	}

	public void restock() throws InterruptedException {
		if (myPosition().getZ() == 3) {
			currentState = State.INSIDE_NMZ;
			return;
		}

		if (boughtPotions == false) {
			buyPotions();
			return;
		}

		// The number of overload doses to take out so that we start with
		// startOverloadDoses doses.
		int ovlDoses = startOverloadDoses - getPotionDoses("Overload");
		if (ovlDoses > 0) {
			takePotion("Overload", ovlDoses);
			return;
		}

		// If we have got the correct number of overload doses and inv is not full then
		// take out
		// absorption.
		if (!inventory.isFull()) {
			takePotion("Absorption", 99);
			return;
		}

		// If we have both absorption and overload then setup dream
		if (!dreamIsSetup) {
			setupDream();
			return;
		}

		int startHp = 52;
		// If dream is setup but hp is not at 51, rockcake to 51
		if (getSkills().getDynamic(Skill.HITPOINTS) > startHp) {
			rockCakeTo(startHp);
			return;
		}

		// If dream is ready and hp is at 51 then enter dream
		enterDream();
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (currentState == State.INSIDE_NMZ) {
			return handleNmz();
		} else if (currentState == State.RESTOCKING) {
			restock();
			return 100;
		}
		return 100;
	}

	@Override
	public void onPaint(Graphics2D g) {
		// This is where you will put your code for paint(s)
		runTime = System.currentTimeMillis() - startTime;
		Paint P = new Paint(this, g, skills);
		P.paint(runTime);
	}

}