package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//import main.Tile.TileType;

public class Decor {
	// sprite location data
	private static final String DATA_FILE_PREFIX = "decor";
	private static final String DATA_FILE_SUFFIX = ".csv";
	private static final String SPRITE_SHEET_URL = "/images/decorCommon.png";
	private final int DRAW_RANGE_TILES = 7;

	final int SAFOFFSETX = 0;
	final int SAFOFFSETY = 1;
	final int SAFWIDTH = 2;
	final int SAFHEIGHT = 3;

	// image data
	// 100x100 outdoor
	private static final String SPRITE_URL_OUTDOOR1 = "/images/decor_100_100_1.png";
	// 100x200 outdoor
	private static final String SPRITE_URL_OUTDOOR2 = "/images/decor_100_200_1.png";
	// 100x100
	private static final String SPRITE_URL_INDOOR1 = "/images/decor_100_100_2.png";
	// private static int[] sizeArray;
	Game game;
	BufferedImage[] images;
	Random random;
	// public int[][] grid;
	public final int maxDecorOnScreen = 200;
	final int DELETE_KIND = -1;
	public final int TREE_DECOR_SIZE = 100;
	public final int COMMON_DECOR_SIZE = 50;
	public final int defaultDecorSizePx = 50;
	public final int defaultDecorSizePxX = 25;
	public final int defaultDecorSizePxY = 25;
	public final int minTilesDrawX = 16;
	public final int minTilesDrawY = 12;
	public final int RANDOM_ITEM_DENSITY = 50;
	public final int MINIMUM_RANDOM_GRIDX = 300;
	public final int Y_CUTOFF_OFFSET = 40;
	// public Dictionary<int, Integer> kindMap;
	int drawableRange;
	final int MAX_KIND = 32;
	public final int WALL_TILE_TYPE = 1;
	public final int BLANK_DECOR_TYPE = -1;
	private boolean modified = false;
	private int xstart, xend, ystart, yend, yCutoff;
	int rows, cols;
	int[][] grid;

	int[][] sizeArray; // offsetX, offsetY, width, height

	public Decor(Game game) {
		this.game = game;
		this.cols = Game.COLS;
		this.rows = Game.ROWS;

		// grid = new int[game.ROWS][game.COLS];
		grid = Utils.initBlankGrid(game.ROWS, game.COLS, BLANK_DECOR_TYPE);
		// BufferedImage[] decorImages = new BufferedImage[10];
		try {
			initDecorImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		drawWallShadow();

		this.grid = Utils.initGrid(rows, cols, BLANK_DECOR_TYPE);
		loadGridCurrentRoom();
		adjustSizesAndOffsets();

	}

	public void loadGridCurrentRoom() {
		String fileName = DATA_FILE_PREFIX + this.game.level + DATA_FILE_SUFFIX;

		try {

			ArrayList<int[]> decorRecords = Utils.loadRecordsFromFile(fileName);
			int rows = decorRecords.size();
			if (rows == 0) {
				System.err.println("Array has no rows");
				this.grid = Utils.initBlankGrid(rows, rows, BLANK_DECOR_TYPE);
				return;
			}
			int newGrid[][] = Utils.ALOItoint2DA(decorRecords);
			this.grid = newGrid;
		} catch (FileNotFoundException e) {
			System.out.println("Decor file does not exist: " + fileName);
			this.grid = Utils.initBlankGrid(rows, rows, BLANK_DECOR_TYPE);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void saveGridCurrentRoom() {
		String fileName = DATA_FILE_PREFIX + this.game.level + DATA_FILE_SUFFIX;

		try {

			ArrayList<int[]> dataALOI = Utils.int2DAtoALOI(this.grid);
			Utils.saveRecordsToFile(fileName, dataALOI);
			int rows = dataALOI.size();
			if (rows == 0) {
				System.err.println("Array has no rows");
				int tempgrid[][] = Utils.initBlankGrid(rows, rows, BLANK_DECOR_TYPE);
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setTileXYK(int gridX, int gridY, int kind) {
		int x = Utils.clamp(0, cols - 1, gridX);
		int y = Utils.clamp(0, rows - 1, gridY);
		this.grid[y][x] = Utils.clamp(0, MAX_KIND, kind);
	}

	private void adjustSizesAndOffsets() {
		// offsetX, offsetY, width, height
		int colsSA = 4;
		sizeArray = new int[images.length][colsSA];
		for (int i = 0; i < sizeArray.length; i++) {
			if (i > 0 && i <= 15) {
				sizeArray[i][SAFOFFSETX] = 0;
				sizeArray[i][SAFOFFSETY] = 0;
				sizeArray[i][SAFWIDTH] = 100;
				sizeArray[i][SAFHEIGHT] = 100;
			} else if (i > 15 && i <= 23) {
				sizeArray[i][SAFOFFSETX] = 0;
				sizeArray[i][SAFOFFSETY] = 0;
				sizeArray[i][SAFWIDTH] = 100;
				sizeArray[i][SAFHEIGHT] = 200;
			}
			if (i > 23 && i <= 39) {
				sizeArray[i][SAFOFFSETX] = 0;
				sizeArray[i][SAFOFFSETY] = -50;
				sizeArray[i][SAFWIDTH] = 100;
				sizeArray[i][SAFHEIGHT] = 100;
			} else {
				if (i > 0 && i < 16) {
					sizeArray[i][SAFOFFSETX] = 0;
					sizeArray[i][SAFOFFSETY] = 0;
					sizeArray[i][SAFWIDTH] = 100;
					sizeArray[i][SAFHEIGHT] = 100;
				}
			}
		}

	}

	public void drawWallShadow() {
		int kind, aboveKind;
		for (int y = 0; y < game.ROWS - 1; y++) {
			for (int x = 0; x < game.COLS; x++) {
				// place shadows on wall tiles
				try {
					kind = game.tilegrid.getTileYX(y, x);
					aboveKind = game.tilegrid.getTileYX(y + 1, x);
					boolean solid = Collision.tileIsSolid(kind);
					boolean solidAbove = Collision.tileIsSolid(aboveKind);
					if (true == solid && !solidAbove) {
						grid[y][x] = kind;
					}
				} catch (Exception e) {
					// throw away index oob exceptions
				}

			}

		}

	}

	public void putDecorOnTileType(int kind, int decorKind) {
		// int kind;
		for (int y = 0; y < game.ROWS - 1; y++) {
			for (int x = 0; x < game.COLS; x++) {
				// place shadows on wall tiles
				kind = game.tilegrid.getTileYX(y, x);
				if (kind == WALL_TILE_TYPE && game.tilegrid.getTileYX(y + 1, x) != WALL_TILE_TYPE) {

					grid[y][x] = kind;
				}

			}
		}

	}

	public int clamp(int minval, int maxval, int test) {
		if (test < minval)
			return minval;
		if (test > maxval)
			return maxval;
		return test;
	}

	/**
	 * xmin, ymin, xmax, ymax visible on decor gred
	 * 
	 * @return
	 */
//	public int[] gridRange() {
//		
//		int[] range = new int[4];
//		range[0] = game.cameraX / game.TILE_SIZE; // x1
//		range[1] = game.cameraY / game.TILE_SIZE; //y1
//		range[2] = (game.WIDTH + game.cameraX) / game.TILE_SIZE; //x2
//		range[3] = (game.HEIGHT + game.cameraY) / game.TILE_SIZE; //y2
//		int[] playerGP = this.game.player.getGridPosition();
//		
//		
//		
//		return range;
//	}

//	private void updateDrawRange_0() {
//		int[] drawableRange = gridRange();
//
//		// sprite culling distances
//		xstart = drawableRange[0] - 4;
//		ystart = drawableRange[1] - 4;
//		xend = drawableRange[2] + 4;
//		yend = drawableRange[3] + 4;
//
//		if (ystart < 0)
//			ystart = 0;
//		if (xstart < 0)
//			xstart = 0;
//		if (xend > game.COLS)
//			xend = game.COLS;
//		if (yend > game.ROWS)
//			yend = game.ROWS;
//		
//	}

	private void updateDrawRange() {
		int playerGP[] = this.game.player.getGridPosition();
		int x1 = playerGP[0] - DRAW_RANGE_TILES;
		int x2 = playerGP[0] + DRAW_RANGE_TILES;
		int y1 = playerGP[1] - DRAW_RANGE_TILES;
		int y2 = playerGP[1] + DRAW_RANGE_TILES;
		x1 = Math.max(0, x1);
		y1 = Math.max(0, y1);
		x2 = Math.max(x2, cols - 1);
		y2 = Math.max(y2, rows - 1);

		// sprite culling distances
		xstart = x1;
		ystart = y1;
		xend = x2;
		yend = y2;

	}

	public void draw() {
		// render tiles above yCutoff first, then render Actors, then render lower Decor
		// Sprites on top

		int screenX, screenY;
//		clamp(0, Game.COLS, xend);
//		clamp(0, Game.ROWS, yend);
//		yCutoff = (game.player.worldY+Y_CUTOFF_OFFSET)/Game.TILE_SIZE;
//
//		clamp(0, Game.ROWS, yCutoff);
//		
//		xstart = 0;
//		ystart = 0;
//		xend = 25;
		yCutoff = 25;

		for (int x = xstart; x < xend; x++) {
			for (int y = ystart; y < yCutoff; y++) {
				int kind = -1;
				try {
					kind = grid[y][x];
				} catch (Exception e) {
				}
				;

				// System.err.println(kind);
				if (kind != BLANK_DECOR_TYPE) {
					// System.out.println("tree");
					int worldX = x * Game.TILE_SIZE;
					int worldY = y * Game.TILE_SIZE;
					screenX = worldX - game.cameraX;
					screenY = worldY - game.cameraY;
					int size[] = sizeArray[kind];

					game.g.drawImage(images[kind], screenX + size[SAFOFFSETX], screenY + size[SAFOFFSETY],
							size[SAFWIDTH], size[SAFHEIGHT], null);

				}

			}
		}

	}

	public void drawLower() {

		// render tiles above yCutoff first, then render Actors, then render lower Decor
		// Sprites on top

		int screenX, screenY;
		clamp(0, game.COLS, xend);
		clamp(0, game.ROWS, yend);
		int kind;

		for (int x = xstart; x < xend; x++) {
			for (int y = yCutoff; y < yend; y++) {
				try {
					kind = grid[y][x];
				} catch (ArrayIndexOutOfBoundsException e) {
					kind = BLANK_DECOR_TYPE;
				}

				if (kind != BLANK_DECOR_TYPE) {
					int worldX = x * game.TILE_SIZE;
					int worldY = y * game.TILE_SIZE;
					screenX = worldX - game.cameraX;
					screenY = worldY - game.cameraY;
					int size[] = sizeArray[kind];

					game.g.drawImage(images[kind], screenX + size[SAFOFFSETX], screenY + size[SAFOFFSETY],
							size[SAFWIDTH], size[SAFHEIGHT], null);

				}

			}
		}

	}

	public void update() {
		updateDrawRange();
	}

	public boolean visibleOnScreen(int worldX, int worldY) {
		int buffer = 100;
		int swx = game.cameraX;
		int swy = game.cameraY;
		if (worldX > swx - buffer && worldY > swy - buffer && worldX < swx + buffer + Game.WIDTH
				&& worldX < swy + buffer + Game.HEIGHT) {
			return true;
		} else {
			return false;
		}

	}

	private void initDecorImages() throws IOException {
		BufferedImage[] decorOutdoor = new Imageutils(game).spriteSheetCutter(SPRITE_URL_OUTDOOR1, 4, 4, 100, 100); // 16x
																													// 100x100
		BufferedImage[] decorOutdoorTall = new Imageutils(game).spriteSheetCutter(SPRITE_URL_OUTDOOR2, 4, 2, 100, 200);// 8x
																														// 100x200
		BufferedImage[] common2Decor = new Imageutils(game).spriteSheetCutter(SPRITE_URL_INDOOR1, 4, 4, 100, 100);// 16x
																													// 100x100
		this.images = Imageutils.appendArray(decorOutdoor, decorOutdoorTall);
		this.images = Imageutils.appendArray(images, common2Decor);

	}

	public int clampKind(int kind) {
		if (kind > MAX_KIND) {
			kind = MAX_KIND;
			System.out.println("Decor kind clamped to max. " + kind);
			return kind;
		} else if (kind < BLANK_DECOR_TYPE) {
			kind = BLANK_DECOR_TYPE;
			System.out.println("Decor kind clamped to min. " + kind);
			return kind;
		} else {
			return kind;
		}
	}

	public void paintDecor(int gridX, int gridY, int kind) {
		modified = true;
		if (game.editor.delete) {
			kind = -1;
		}
		try {
			kind = clampKind(kind);
			this.grid[gridY][gridX] = kind;

			System.err.println(kind);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
