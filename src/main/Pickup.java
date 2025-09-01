package main;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Pickup {
	// statuses
	public static final int VISIBLE = 1;
	public static final int HIDDEN = 0;
	public static final int COLLECTED = 1;
	public static final int ACTIVE = 0;
	public static final int INACTIVE = 1;
	// level Data Fields
	//
	public static final int F_GRIDX = 0;
	public static final int F_GRIDY = 1;
	public static final int F_KIND = 2;
	public static final int F_VISIBLE = 3;
	public static final int F_COLLECTED = 4;

	private final int CHECK_TOUCHED_ITEM_PERIOD = 100;

	public final int ITEM_SCALE_PX = 25;
	public final int ITEM_TLC_OFFSET = Game.TILE_SIZE / 2;
	public final float ITEM_DRAWSIZE_FACTOR = 0.5f;
	public final int BOB_PIXELS_MAX = 10;
	public final int BOB_RATE = 10;
	final int MAX_KIND = 32;
	private final int MINIMUM_RANDOM_GRIDX = 10;
	private final int MINIMUM_RANDOM_GRIDY = 10;
	private final static int RANDOM_ITEM_DENSITY = 50;
	private final static int SPRITE_WIDTH = 100;
	private static final int SPRITE_HEIGHT = 100;
	private final int DRAW_WIDTH = 50;
	private final int DRAW_HEIGHT = 50;
	private final static String SPRITE_SHEET_ITEMS1 = "/images/pickups1.png";
	private final static String SPRITE_SHEET_ITEMS2 = "/images/pickups1.png";
	public final int BLANK_ITEM_TYPE = -1;
	private boolean modified = false;
	private int bobPixels = 0;
	private int bobDelta = 1;
	private int[] cullRegion;
	Rectangle testRect;

	public BufferedImage[] itemImages;
	static Game game;
	public ArrayList<int[]> levelData; // level data stored as ALOI
	// Fields: gridX, gridY, kind, culled-state, ready/collected
	Random random;
	Pacer checkToucheditemPacer;

	Rectangle testRectangle;

	Pickup(Game game) {
		this.game = game;
		checkToucheditemPacer = new Pacer(CHECK_TOUCHED_ITEM_PERIOD);
		// this.itemGrid = new int[game.ROWS][game.COLS];
		levelData = new ArrayList<int[]>();
		cullRegion = new int[4];
		testRect = new Rectangle(0, 0, 75, 75);
		testRectangle = new Rectangle(0, 0, DRAW_WIDTH+25, DRAW_HEIGHT+25);

		try {
			this.itemImages = getImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void calculateBob() {
		if (bobPixels < BOB_PIXELS_MAX && bobPixels >= 0) {
			bobPixels += bobDelta;
		} else {
			bobDelta *= -1;
			bobPixels += bobDelta;
		}
	}

	private void addPickup(int gridX, int gridY, int kind) {
		int[] pickup = { gridX, gridY, kind, VISIBLE, ACTIVE };
		this.levelData.add(pickup);
	}

	public void saveCurrentData() {
		String filename = this.game.pickup.getDataFileString();
		Utils.saveRecordsToFile(filename, levelData);
	}

	public void loadCurrentData() {
		String filename = getDataFileString();
		try {
			levelData = Utils.loadRecordsFromFile(filename);
		} catch (FileNotFoundException e) {
			System.err.println("Editor failed to load Pickup data");
			e.printStackTrace();
		}
	}

	public void randomPlacePickup(int amount, int kind) {
		int itemsPlaced = 0;
		this.random = new Random();
		int tmp = 0;
		// loop through tileGrid
		// check if tile is colliding or open
		// use random number to decide place item or not
		do {
			for (int y = 1; y < Game.ROWS; y++) {
				for (int x = 1; x < Game.COLS; x++) {
					tmp = random.nextInt(RANDOM_ITEM_DENSITY);
					if (game.tilegrid.getTileYX(x, y) == 1 || tmp != 10
							|| (x < MINIMUM_RANDOM_GRIDX && y < MINIMUM_RANDOM_GRIDX)

					) {
						continue;
					}
					try {
						addPickup(x, y, kind);
					} catch (Exception e) {

					}
					itemsPlaced++;
					if (itemsPlaced >= amount)
						break;
				}
				if (itemsPlaced >= amount)
					break;
			}
		} while (itemsPlaced < amount);

	}

	private void checkPickupTouchedPlayer() {
		for (int[] pickup : levelData) {
			if (pickup != null && pickup[F_COLLECTED] == ACTIVE && pickup[F_VISIBLE] == VISIBLE) {
				int kind = pickup[F_KIND];
				testRect.x = pickup[F_GRIDX] * Game.TILE_SIZE ;
				testRect.y = pickup[F_GRIDY] * Game.TILE_SIZE ;
				if (game.player.intersects(testRect)) {
					System.out.println("Player pickup item " + kind);
					pickup[F_COLLECTED] = COLLECTED;
					pickup[F_VISIBLE] = HIDDEN;
					this.game.sound.playSE(Sound.S_PICKUP);
				}

			}
		}

	}

	class PickupRecord {
		int gridX, gridY, kind;

		PickupRecord(int gridX, int gridY, int kind) {
			this.kind = kind;
			this.gridX = gridX;
			this.gridY = gridY;
		}
	}

	private void updateCullRegion(int[] cullRegion, int radiusGrid) {
		// int[] visible = getVisibleArea();
		int pgX = game.player.x / Game.TILE_SIZE;
		int pgY = game.player.y / Game.TILE_SIZE;
		int cullRectX = pgX - radiusGrid;
		int cullRectY = pgY - radiusGrid;
		int startgx = cullRectX;
		int startgy = cullRectX;
		int maxy = Game.ROWS - 1;
		int maxx = Game.COLS - 1;
		int endgx = cullRectX + (2 * radiusGrid);
		int endgy = cullRectY + (2 * radiusGrid);

		// limit to real coordinates
		startgx = clamp(0, maxx, startgx);
		startgy = clamp(0, maxy, startgy);
		endgx = clamp(0, maxx, endgx);
		endgy = clamp(0, maxy, endgy);

	}

	public void update() {

		calculateBob();
		if (checkToucheditemPacer.check()) {
			checkPickupTouchedPlayer();
		}

	}

	private BufferedImage[] getImages() throws IOException {
		BufferedImage[] items1 = new Imageutils(game).spriteSheetCutter(SPRITE_SHEET_ITEMS1, 4, 4, SPRITE_WIDTH,
				SPRITE_HEIGHT);

		BufferedImage[] items2 = new Imageutils(game).spriteSheetCutter(SPRITE_SHEET_ITEMS2, 4, 4, SPRITE_WIDTH,
				SPRITE_HEIGHT);

		BufferedImage[] itemImages = Imageutils.appendArray(items1, items2);

		return itemImages;

	}

	private void scaleImages(BufferedImage[] bufferedImages) {
		Image tmp_image;
		BufferedImage tmp_bimage;
		for (int i = 0; i < bufferedImages.length; i++) {
			if (bufferedImages[i] == null)
				continue;
			tmp_image = bufferedImages[i].getScaledInstance(ITEM_SCALE_PX, ITEM_SCALE_PX, Image.SCALE_SMOOTH);
			tmp_bimage = new BufferedImage(ITEM_SCALE_PX, ITEM_SCALE_PX, BufferedImage.TYPE_INT_ARGB);
			tmp_bimage.getGraphics().drawImage(tmp_image, 0, 0, null);
			bufferedImages[i] = tmp_bimage;
		}
	}

	private int clamp(int min, int max, int test) {
		if (test > max) {
			return max;
		} else if (test < min) {
			return min;
		} else {
			return test;
		}
	}

	/**
	 * draws the items on screen, also adds onscreen items to a list
	 */
	public void draw() {

		int TopLeftCornerX = game.cameraX;
		int TopLeftCornerY = game.cameraY;

		int screenX, screenY;

		for (int[] pickup : this.levelData) {
			if (pickup != null && pickup[F_VISIBLE] == VISIBLE && pickup[F_COLLECTED] == ACTIVE) {
				int kind = pickup[F_KIND];
				int worldX = pickup[F_GRIDX] * Game.TILE_SIZE;
				int worldY = pickup[F_GRIDY] * Game.TILE_SIZE;
				screenX = worldX - TopLeftCornerX;
				screenY = worldY - TopLeftCornerY;

				game.g.drawImage(itemImages[kind], screenX, screenY + bobPixels, DRAW_WIDTH, DRAW_HEIGHT, null);

			}

		}
	}

	public boolean validateAssetID(int testAssetID) {
		int maximum = MAX_KIND;
		int actualAssetID = testAssetID;
		if (testAssetID > maximum) {
			testAssetID = 0;
		} else if (testAssetID < 0) {
			testAssetID = maximum;
		} else {
			actualAssetID = testAssetID;
		}
		try {
			BufferedImage asset = this.itemImages[actualAssetID];
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private int getIndexLevelData(int gridX, int gridY) {
		for (int i = 0; i < this.levelData.size(); i++) {
			int[] pickup = this.levelData.get(i);
			if (pickup != null && pickup[F_VISIBLE] == VISIBLE && pickup[F_COLLECTED] == ACTIVE) {
				int kind = pickup[F_KIND];
				int tgridX = pickup[F_GRIDX];
				int tgridY = pickup[F_GRIDY];
				if (tgridX == gridX && tgridY == gridY) {
					return i;
				}

			}

		}
		return -1;

	}

	public void paintPickup(int gridX, int gridY, int kind) {
		try {
			int existingItemIndex = getIndexLevelData(gridX, gridY);
			if (-1 == existingItemIndex) {
				addPickup(gridX, gridY, kind);
			} else {
				int[] recordToModify = this.levelData.get(existingItemIndex);
				recordToModify[F_KIND] = kind;
				recordToModify[F_COLLECTED] = ACTIVE;
				recordToModify[F_VISIBLE] = VISIBLE;
			}
			modified = true;
			if (game.editor.delete) {
				kind = -1;
			}
			System.err.println(kind);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getDataFileString() {
		return String.format("pickup %d.csv", game.level);
	}

	public void setTileGXY(int gridX, int gridY, boolean delete) {
		int kind = this.game.editor.assetID;
		if (delete) {
			kind = -1;
		}

		try {
			int existingItemIndex = getIndexLevelData(gridX, gridY);
			if (-1 == existingItemIndex && kind != -1) {
				addPickup(gridX, gridY, kind);
			} else {
				int[] recordToModify = this.levelData.get(existingItemIndex);
				if (delete) {
					this.levelData.remove(existingItemIndex);
				} else {
					recordToModify[F_KIND] = kind;
					recordToModify[F_COLLECTED] = ACTIVE;
					recordToModify[F_VISIBLE] = VISIBLE;
				}

			}
			modified = true;
			if (game.editor.delete) {
				kind = -1;
			}
			System.err.println(kind);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
