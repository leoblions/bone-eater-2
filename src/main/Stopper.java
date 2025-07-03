package main;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Stopper {
	Game game;
	Image image;
	Image[] images;
	public Stopper(Game game) {
		this.game = game;
		this.initImages();
		Image image;
	}
	private void initImages() {
		try {
            this.image = ImageIO.read(getClass().getResource("/images/goldbug.png")); // Image in resources/images/icon.png
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void draw() {
		
	}
	
	public void update() {
		
	}

}
