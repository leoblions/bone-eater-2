package main;

import java.awt.Color;
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
	final int INTERACT_SCREENX = 470;
	final int INTERACT_SCREENY = 470;
	private int healthwidth = 150;
	private int healthFilled = START_HEALTH_WIDTH;
	BufferedImage interact;
	BufferedImage[] icons;
	boolean showInteract = true;
	public int showActionPromptDelay = 0;

	public Hud(Game game) {
		this.game = game;
		this.initImages();
		Image image;
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

			if (this.showInteract)
				this.game.g.drawImage(interact, INTERACT_SCREENX, INTERACT_SCREENY, 90, 90, null);
		}

	}

	public void update() {
		showActionPromptDelay = showActionPromptDelay > 0 ? showActionPromptDelay - 1 : showActionPromptDelay;

	}

	public void updateHealthbar(int health) { 
		float filledPercent = (float)health / 100f;
		int newWidth = (int)(filledPercent * (float)MAX_HEALTHBAR_WIDTH);
		this.healthFilled = newWidth; 
		
		
	}

}
