package main;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Button extends Rectangle{
	BufferedImage image;
	String text;
	boolean highlighted;
	int paddingX = 20;
	int paddingY = 25;
	public Button(int x, int y, int width, int height, BufferedImage image, String text) {
		super(x,y,width,height);
		this.image=image;
		this.text=text;
		this.highlighted=false;
		
	}

}
