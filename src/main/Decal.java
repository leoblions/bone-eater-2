package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Decal {
	

	
	
	final int DECALS_AMOUNT = 10;
	final int DECAL_KINDS = 10;

	private static final String SPRITE1 = "/images/blood50.png";

	private static final String SPRITE2 = "/images/gib50.png";

	private static final String SPRITE3 = "/images/debris50.png";
	
	Game game;
	BufferedImage[] images;
	Random random;
	
	
	public DecalRecord[] decalRecords ;
	private int nextDecalSlot = 0;
	public int widths[];
	public int heights[];


	public Decal(Game game) {
		this.game = game;
		

		this.decalRecords = new DecalRecord[DECALS_AMOUNT];
		this.widths = new int[DECAL_KINDS];
		this.heights = new int[DECAL_KINDS];
		for (int i = 0; i< DECAL_KINDS;i++) {
			this.widths[i]=50;
			this.heights[i]=50;
		}
		try {
			initImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	public void reset() {
		for(int i = 0;i<this.decalRecords.length;i++) {
			this.decalRecords[i] = null;
		}
	}

	public void putDecalAtTile(int worldX, int worldY, int kind) {
		DecalRecord dr = new DecalRecord(worldX,worldY,kind);
		this.decalRecords[this.nextDecalSlot] = dr;
		if(this.nextDecalSlot<this.decalRecords.length) {
			this.nextDecalSlot+=1;
		}else {
			this.nextDecalSlot=0;
		}
		
	}

	

	

	public int clamp(int minval, int maxval, int test) {
		if (test < minval)
			return minval;
		if (test > maxval)
			return maxval;
		return test;
	}




	public void draw() {


		for (int x = 0; x < this.decalRecords.length; x++) {
			DecalRecord drCurrent = this.decalRecords[x];
			if(drCurrent!=null) {
				int screenX = drCurrent.worldX() - game.cameraX;
				int screenY = drCurrent.worldY() - game.cameraY;
				int kind = drCurrent.kind();
				game.g.drawImage(images[kind], screenX  , screenY ,
						widths[kind], heights[kind], null);
				
			}
			
		}

	}

	

	public void update() {
	}

	

	private void initImages() throws IOException {
		BufferedImage[] blood = new Imageutils(game).spriteSheetCutter(SPRITE1, 4, 1, 50, 50); // 16x
																													// 100x100
		BufferedImage[] gibs = new Imageutils(game).spriteSheetCutter(SPRITE2, 4, 1, 50, 50);// 8x
																														// 100x200
		BufferedImage[] debris = new Imageutils(game).spriteSheetCutter(SPRITE3, 4, 1, 50, 50);// 16x
																													// 100x100
		this.images = Imageutils.appendArray(blood, gibs);
		this.images = Imageutils.appendArray(images, debris);

	}

	

	
	record DecalRecord(int worldX, int worldY, int kind) {
		
	}

}
