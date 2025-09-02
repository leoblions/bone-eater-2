package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Hud {
	final int HEALTHBAR_SCREENX = 25;
	final int HEALTHBAR_SCREENY = 25;
	final Color HEALTH_BAR_FG  = new Color(255,10,10,255);
	final int MAX_HEALTHBAR_WIDTH = 150;
	final int START_HEALTH_WIDTH = 150;

	Game game;
	BufferedImage healthbar;
	String objectiveString = "";
	final int INTERACT_SCREENX_OFFSET = -100;
	final int INTERACT_SCREENY_OFFSET = -100;
	private int interactScreenX, interactScreenY;
	private int healthwidth = 150;
	private int healthFilled = START_HEALTH_WIDTH;
	BufferedImage interact;
	BufferedImage[] icons;
	boolean showInteract = true;
	public int showActionPromptDelay = 0;
	private int Swidth = 0;
	
	private Font objectiveFont = new Font("Serif", Font.BOLD, 20);
	private Color objectiveColor = new Color(255,255,255,255);

	public Hud(Game game) {
		this.game = game;
		Swidth = game.getWidth();
		this.initImages();
		updatePositions();
		updateObjectiveString();
		Image image;
	}
	
	public void updatePositions(){
		this.interactScreenX = game.width + INTERACT_SCREENX_OFFSET;
		this.interactScreenY = game.height +INTERACT_SCREENY_OFFSET;
		
	}
	
	public void updateObjectiveString() {
		this.objectiveString = String.format("Objectives: %d/%d",game.brain.objectivesComplete,game.brain.objectivesTotal);
	}

	private void initImages() {
		BufferedImage spriteSheet;
		try {
			spriteSheet = ImageIO.read(getClass().getResource("/images/hud.png"));
			// Image in resources/images/icon.png
			this.healthbar = spriteSheet.getSubimage(0, 200, 300, 100);
			this.icons = this.game.imageutils.spriteSheetCutter("/images/hud.png", 4, 2, 100, 100);
			this.interact = icons[3];
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void draw() {
		if (healthbar != null) {
			this.game.g.setColor(Color.BLACK);

			this.game.g.fillRect(HEALTHBAR_SCREENX + 52, HEALTHBAR_SCREENY + 40, this.healthwidth, 13);
			this.game.g.setColor(HEALTH_BAR_FG);
			this.game.g.fillRect(HEALTHBAR_SCREENX + 52, HEALTHBAR_SCREENY + 40, this.healthFilled, 12);
			this.game.g.drawImage(healthbar, HEALTHBAR_SCREENX, HEALTHBAR_SCREENY, 300, 100, null);

			if (this.showInteract) {
				this.game.g.drawImage(interact, this.interactScreenX, this.interactScreenY, 90, 90, null);
			}
			this.game.g.setColor(objectiveColor);
			this.game.g.setFont(objectiveFont);
	 
			this.game.g.drawString(objectiveString, this.interactScreenX-100, 50);
		}

	}

	public void update() {
		showActionPromptDelay = showActionPromptDelay > 0 ? showActionPromptDelay - 1 : showActionPromptDelay;
		if(Swidth==0) {
			updatePositions();
		}
		updatePositions();

	}

	public void updateHealthbar(int health) { 
		float filledPercent = (float)health / 100f;
		int newWidth = (int)(filledPercent * (float)MAX_HEALTHBAR_WIDTH);
		this.healthFilled = newWidth; 
		
		
	}

}
