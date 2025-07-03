package main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Background {
	final int IMAGES_AMOUNT = 25;
	Game game;
	//Image image;
	BufferedImage imagedefault;
	BufferedImage[] images;
	int width, height;

	public Background(Game game) {
		this.game = game;
		this.initImages();
		Image image;
	}

	private void initImages() {
		this.images = new BufferedImage[IMAGES_AMOUNT];
		try {
			this.imagedefault = ImageIO.read(getClass().getResource("/backgrounds/backrooms.png"));
			this.images[0] = ImageIO.read(getClass().getResource("/backgrounds/background0.png")); // Image in
																									// resources/images/icon.png
			this.images[1] = ImageIO.read(getClass().getResource("/backgrounds/background1.png"));
			this.images[2] = ImageIO.read(getClass().getResource("/backgrounds/background2.png"));
			this.images[3] = ImageIO.read(getClass().getResource("/backgrounds/background3.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void draw() {
		BufferedImage image = this.images[this.game.level];
		if (image != null) {

			this.game.g.drawImage(image, 0, 0, this.width, this.height, null);
		}else if (imagedefault != null) {
			this.game.g.drawImage(imagedefault, 0, 0, this.width, this.height, null);
		}

	}

	public void update() {
		//this.image = this.images[this.game.level];
		this.width = this.imagedefault.getWidth(null);
		this.height = this.imagedefault.getHeight(null);

	}

}
