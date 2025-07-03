package main;


import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Pickup {
	private static final String DATA_FILE_PREFIX = "item";
	private static final String DATA_FILE_SUFFIX = ".csv";
	public final int ITEM_SCALE_PX = 25;
	public final int ITEM_TLC_OFFSET= Game.TILE_SIZE/2;
	public final float ITEM_DRAWSIZE_FACTOR = 0.5f;
	public final int BOB_PIXELS_MAX = 10;
	public final int BOB_RATE = 10;
	final int MAX_KIND = 32;
	public final int MINIMUM_RANDOM_GRIDX = 10;
	public final int MINIMUM_RANDOM_GRIDY = 10;
	public final int RANDOM_ITEM_DENSITY = 50;
	public final int ITEM_DEFAULT_W = 50;
	public final int ITEM_DEFAULT_H = 50;
	private final static String SPRITE_SHEET_ITEMS1 = "/images/itemA.png";
	private final static String SPRITE_SHEET_ITEMS2 = "/images/itemB.png";
	public final int BLANK_ITEM_TYPE = -1;
	private boolean modified = false;
	private int bobPixels = 0;
	private int bobDelta = 1;
	private int[] cullRegion;
	
	public BufferedImage[] itemImages;
	static Game game;
	int[][] itemGrid;
	Random random;
	
	Rectangle testRectangle;
	Pickup(Game game) {
		this.game=game;;
		//this.itemGrid = new int[game.ROWS][game.COLS];
		itemGrid = Utils.initBlankGrid(game.ROWS, game.COLS, BLANK_ITEM_TYPE);
		cullRegion = new int[4];
		
		testRectangle = new Rectangle(
				0,
				0,
				ITEM_DEFAULT_W,
				ITEM_DEFAULT_H);
		
		try {
			this.itemImages = getImages() ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void calculateBob() {
		if(bobPixels < BOB_PIXELS_MAX && bobPixels >= 0) {
			bobPixels += bobDelta;
		}else {
			bobDelta *=-1;
			bobPixels += bobDelta;
		}
	}
	
	
	public void randomPlacePickup(int amount, int kind) {
		int itemsPlaced = 0;
		this.random = new Random();
		int tmp=0;
		// loop through tileGrid
		// check if tile is colliding or open
		// use random number to decide place item or not
		do {
			for (int y = 1; y< Game.ROWS;y++) {
				for (int x = 1; x< Game.COLS;x++) {
					tmp = random.nextInt(RANDOM_ITEM_DENSITY);
					if(game.tilegrid.getTileYX(x, y)==1 || tmp!=10 ||
							(x <  MINIMUM_RANDOM_GRIDX &&
							y < MINIMUM_RANDOM_GRIDX)
							
							) {
						continue;
					}
					try {
						itemGrid[y][x] = kind;
					}catch(Exception e) {
						
					}
					itemsPlaced++;
					if (itemsPlaced>=amount)break;
				}if (itemsPlaced>=amount)break;
			}
		}while(itemsPlaced<amount);
		
		
	}
	
	
	
	public void pickupPickup(int item) {
		System.out.println("Picked up item "+ item);
		//game.inventory.addPickup(item, 1);
		
		//game.sound.clipPlayFlags[2]=true;
	
		
	}
	
	 class PickupRecord{
		int gridX,gridY, kind;
		PickupRecord(int gridX, int gridY, int kind){
			this.kind=kind;
			this.gridX=gridX;
			this.gridY=gridY;
		}
	}
	 
		public void updateCullRegion(int[] cullRegion, int radiusGrid) {
			//int[] visible = getVisibleArea();
			int pgX = game.player.worldX / Game.TILE_SIZE;
			int pgY = game.player.worldY / Game.TILE_SIZE;
			int cullRectX = pgX - radiusGrid;
			int cullRectY = pgY - radiusGrid;
			int startgx = cullRectX;
			int startgy = cullRectX;
			int maxy = Game.ROWS-1;
			int maxx = Game.COLS-1;
			int endgx = cullRectX + (2*radiusGrid);
			int endgy = cullRectY + (2*radiusGrid);
			
			// limit to real coordinates
			startgx = clamp(0,maxx,startgx);
			startgy = clamp(0,maxy,startgy );
			endgx = clamp(0,maxx,endgx);
			endgy = clamp(0,maxy,endgy);
			
		}
	
	
	public void update() {
		updateCullRegion(cullRegion, 15);
		calculateBob();
		try {
			itemsTouchedByPlayer();
		}catch(Exception e) {
			
		}
		
//		for (int item: itemsTouchedByPlayer) {
//			if(item!=null) {
//				
//			}
//			
//		}
		
	}
	
	public static BufferedImage[] getImages() throws IOException {
		BufferedImage[] items1 = new Imageutils(game).spriteSheetCutter(SPRITE_SHEET_ITEMS1, 4, 4, 50, 50);

		BufferedImage[] items2 = new Imageutils(game).spriteSheetCutter(SPRITE_SHEET_ITEMS2, 4, 4, 50, 50);

		BufferedImage[] itemImages = Imageutils.appendArray(items1, items2);
		
		return itemImages;
		
		
		
		
	}
	
	public void scaleImages(BufferedImage[] bufferedImages) {
		Image tmp_image;
		BufferedImage tmp_bimage;
		for(int i = 0; i< bufferedImages.length;i++) {
			if (bufferedImages[i]==null)continue;
			tmp_image = bufferedImages[i].getScaledInstance(ITEM_SCALE_PX,ITEM_SCALE_PX,Image.SCALE_SMOOTH);
			tmp_bimage = new BufferedImage(ITEM_SCALE_PX,ITEM_SCALE_PX,BufferedImage.TYPE_INT_ARGB);
			tmp_bimage.getGraphics().drawImage(tmp_image, 0, 0, null);
			bufferedImages[i] = tmp_bimage;
		}
	}
	
	
	
	public int clamp(int min, int max, int test) {
		if(test>max) {
			return max;
		}else if (test< min) {
			return min;
		}else {
			return test;
		}
	}
	/**
	 * adds items that collide with player to a list
	 */
	public void itemsTouchedByPlayer() {
		
		//Rectangle itemRect;
		
		//Rectangle playerRect = game.player.wpSolidArea;
		
		// check items n unculled area
		int pgX = game.player.worldX / Game.TILE_SIZE;
		int pgY = game.player.worldY/ Game.TILE_SIZE;
		int kind = itemGrid[pgY][pgX];
		if (kind!=BLANK_ITEM_TYPE) {
			itemGrid[pgY][pgX] = BLANK_ITEM_TYPE;
			//System.out.println("Got item "+kind);
			pickupPickup(kind);
		}

		
		
		
		
				
	}
	/**
	 * draws the items on screen, also adds onscreen items to a list
	 */
	public void draw() {
		int[] visible = game.visibleArea;
		int TopLeftCornerX = game.cameraX;
		int TopLeftCornerY = game.cameraY;
		int maxy = itemGrid.length;
		int maxx = itemGrid[0].length;
		int startx = clamp(0,maxx,visible[0]-50);
		int starty = clamp(0,maxy,visible[1]-50);
		int endx = clamp(0,maxx,visible[2]+50);
		int endy = clamp(0,maxy,visible[3]+50);
		int screenX,screenY;
		int tmp;
		
		
		
		for (int y = starty; y < endy; y++) {
			
			for (int x = startx; x < endx; x++) {
				if (itemGrid[y][x]!=-1) {
					
					int kind=itemGrid[y][x];
					int worldX = x * Game.TILE_SIZE;
					int worldY = y * Game.TILE_SIZE;
					screenX = worldX - TopLeftCornerX;
					screenY = worldY - TopLeftCornerY;
					
					game.g.drawImage(
							itemImages[kind],
							screenX ,
							screenY+bobPixels,
							ITEM_DEFAULT_W,
							ITEM_DEFAULT_H,
							null);
				} 
				
			}
			
		}
	}
	
	public void addPickup(int tileGridX, int tileGridY, int kind) {
		modified = true;
		try {
			itemGrid[tileGridY][tileGridX] = kind;
		}catch(Exception e) {
			
		}
		
		 
		
	}
	
	
	public boolean validateAssetID(int testAssetID) {
		int maximum = MAX_KIND;
		int actualAssetID = testAssetID;
		if (testAssetID > maximum) {
			testAssetID = 0;
		}else if(testAssetID <  0) {
			testAssetID = maximum;
		}else {
			actualAssetID = testAssetID;
		}
		try {
			BufferedImage asset = this.itemImages[actualAssetID];
		}catch(Exception e) {
			return false;
		}
		
		
		return true;
	}


	public void paintPickup(int gridX, int gridY, int kind) {
		try {
			this.itemGrid[gridY][gridX] = kind;
			modified=true;
			if(game.editor.delete) {
				kind = -1;
			}
			System.err.println(kind);
		}
		catch(Exception e){
			e.printStackTrace();
		} 
	
	}



	
	
		
		
	}


