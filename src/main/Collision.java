package main;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class Collision {
	/*
	 * handles a variety of collision tasks for player, projectiles, enemies,
	 * attacks
	 */

	int BUFFER_ZONE = 2;
	public static final int DEFAULT_FILL = 0;
	public static final int SOLID = 1;

	public static final int OPEN = 0;
	double playerCenterDistance;
	static int halfTile = Game.TILE_SIZE / 2;
	int ROWS = Game.ROWS * 2;
	int COLS = Game.COLS * 2;
	final int MAX_KIND = 31;
	final int ILLEGAL_TILE = -1;
	final Color WALL_COLOR = new Color(255, 255, 255, 70);
	final Color SPECIAL_COLOR = new Color(0, 0, 255, 70);

	Game game;
	public int tileSize = Game.COLL_GRID_SIZE;
	public int grid[][] = null;
	private BufferedImage placeholder;

	public Collision(Game game) {

		this.game = game;
		this.tileSize = this.game.COLL_GRID_SIZE;

		initGrid();

	}

	public static enum Direction4W {
		UP, DOWN, LEFT, RIGHT, NONE
	}

//	public int tileAtWorldCoord(int wx, int wy) {
//		return game.tilegrid.getTileYX(wy / game.tilegrid.tileSize, wx / game.tilegrid.tileSize);
//
//	}

	public static int tileAtWorldCoord(Game game, int wx, int wy) {
		return game.tilegrid.getTileYX(wy / game.tilegrid.tileSize, wx / game.tilegrid.tileSize);

	}

//	public int wallAtWorldCoord(int wx, int wy) {
//		return game.wall.getTileYX(wy / game.wall.tileSize, wx / game.wall.tileSize);
//
//	}

//	public static int wallAtWorldCoord(Game game, int wx, int wy) {
//		return game.wall.getTileYX(wy / game.wall.tileSize, wx / game.wall.tileSize);
//
//	}
//	public static int solidAtWorldCoord(Game game, int wx, int wy) {
//		return game.wall.getTileYX(wy / Game.COLL_GRID_SIZE, wx / Game.COLL_GRID_SIZE);
//
//	}
	public boolean solidAtWorldCoord( int wx, int wy) {
		return SOLID == this.getTileYX(wy / Game.COLL_GRID_SIZE, wx / Game.COLL_GRID_SIZE);

	}

	public static boolean tileIsSolid(int kind) {
		if(kind==SOLID) {
			return true;
		}else {
			return false;
		}
	}



//	public static boolean pointCollideWithSolidTile(Game game, int worldX, int worldY) {
//		
//		try {
//
//			return  tileIsSolid(solidAtWorldCoord(game, worldX, worldY));
//			 
//		} catch (ArrayIndexOutOfBoundsException e) {
//			return true;
//		} 
//	}

	public boolean[] collideTileTestWXY(int testX, int testY, int width, int height) {

		boolean[] collisions = new boolean[] { false, false, false, false };
		if (grid == null) {
			return collisions;
		}

		// up coll
		if (solidAtWorldCoord(testX + (width / 2), testY - BUFFER_ZONE)) {
			//System.out.println("collide up");
			collisions[0] = true;
		}
		// down coll
		if (solidAtWorldCoord(testX + (width / 2), testY + (height) + BUFFER_ZONE)) {
			//System.out.println("collide dn");
			collisions[1] = true;
		}
		// left coll
		if (solidAtWorldCoord(testX - BUFFER_ZONE, testY + height / 2)) {
			//System.out.println("collide lt");
			collisions[2] = true;
		}
		// right coll
		if (solidAtWorldCoord(testX + width + BUFFER_ZONE, testY + height / 2)) {
			//System.out.println("collide rt");
			collisions[3] = true;

		}
		return collisions;

	}

	public boolean[] collideTilePlayerTestWXY(int testX, int testY) {

		boolean[] collisions = new boolean[] { false, false, false, false };
		if ( grid == null) {
			return collisions;
		}
		Player p = game.player;

		
		return collideTileTestWXY(  testX,   testY,   p.width,   p.height) ;

	}

	public boolean[] collideTilePlayer() {

		boolean[] collisions = new boolean[] { false, false, false, false };
		if ( grid == null) {
			return collisions;
		}
		Player p = game.player;

		
		return  collideTileTestWXY(  p.x,   p.y,   p.width,   p.height) ;

	}

//	/**
//	 * returns array of bools, any of which will be true if the rectangles collide
//	 * in that direction with a tile
//	 * 
//	 * @param r
//	 * @return
//	 */
//	public boolean[] collideTileRect(Rectangle r) {
//
//		boolean[] collisions = new boolean[] { false, false, false, false };
//		// Player p = game.player;
//
//		// up coll
//		int tmp = wallAtWorldCoord(r.x + (r.width / 2), r.y - BUFFER_ZONE);
//		if (tileIsSolid(tmp)) {
//			// System.out.println("collide up");
//			collisions[0] = true;
//		}
//		// down coll
//		tmp = wallAtWorldCoord(r.x + (r.width / 2), r.y + (r.height) + BUFFER_ZONE);
//		if (tileIsSolid(tmp)) {
//			// System.out.println("collide dn");
//			collisions[1] = true;
//		}
//		// left coll
//		tmp = wallAtWorldCoord(r.x - BUFFER_ZONE, r.y + r.height / 2);
//		if (tileIsSolid(tmp)) {
//			// System.out.println("collide lt");
//			collisions[2] = true;
//		}
//		// right coll
//		tmp = wallAtWorldCoord(r.x + r.width + BUFFER_ZONE, r.y + r.height / 2);
//		if (tileIsSolid(tmp)) {
//			// System.out.println("collide rt");
//			collisions[3] = true;
//
//		}
//		return collisions;
//
//	}

//	public boolean collideTileRectDirection(Rectangle r, char direction) {
//
//		// up coll
//		int tmp = wallAtWorldCoord(r.x + (r.width / 2), r.y - BUFFER_ZONE);
//		if (tileIsSolid(tmp) && direction == 'u') {
//			return true;
//		}
//		// down coll
//		tmp = wallAtWorldCoord(r.x + (r.width / 2), r.y + (r.height) + BUFFER_ZONE);
//		if (tileIsSolid(tmp) && direction == 'd') {
//			return true;
//		}
//		// left coll
//		tmp = wallAtWorldCoord(r.x - BUFFER_ZONE, r.y + r.height / 2);
//		if (tileIsSolid(tmp) && direction == 'l') {
//			return true;
//		}
//		// right coll
//		tmp = wallAtWorldCoord(r.x + r.width + BUFFER_ZONE, r.y + r.height / 2);
//		if (tileIsSolid(tmp) && direction == 'r') {
//			return true;
//
//		}
//		return false;
//
//	}

//	public boolean collideTileRectDirection(Rectangle r, Direction4W direction) {
//
//		// up coll
//		int tmp = wallAtWorldCoord(r.x + (r.width / 2), r.y - BUFFER_ZONE);
//		if (tileIsSolid(tmp) && direction == Direction4W.UP) {
//			// System.out.println("collide up");
//			return true;
//		}
//		// down coll
//		tmp = wallAtWorldCoord(r.x + (r.width / 2), r.y + (r.height) + BUFFER_ZONE);
//		if (tileIsSolid(tmp) && direction == Direction4W.DOWN) {
//			// System.out.println("collide dn");
//			return true;
//		}
//		// left coll
//		tmp = wallAtWorldCoord(r.x - BUFFER_ZONE, r.y + r.height / 2);
//		if (tileIsSolid(tmp) && direction == Direction4W.LEFT) {
//			// System.out.println("collide lt");
//			return true;
//		}
//		// right coll
//		tmp = wallAtWorldCoord(r.x + r.width + BUFFER_ZONE, r.y + r.height / 2);
//		if (tileIsSolid(tmp) && direction == Direction4W.RIGHT) {
//			// System.out.println("collide rt");
//			return true;
//
//		}
//		return false;
//
//	}

//	public boolean collideTileRectAny(Rectangle r) {
//
//		// boolean[] collisions = new boolean[] {false,false,false,false};
//		// Player p = game.player;
//
//		// up coll
//		int tmp = wallAtWorldCoord(r.x, r.y);
//		if (tileIsSolid(tmp)) {
//			System.out.println("collide up");
//			return true;
//		}
//		// down coll
//		tmp = wallAtWorldCoord(r.x, r.y);
//		if (tileIsSolid(tmp)) {
//			System.out.println("collide dn");
//			return true;
//		}
//		// left coll
//		tmp = wallAtWorldCoord(r.x, r.y);
//		if (tileIsSolid(tmp)) {
//			System.out.println("collide lt");
//			return true;
//		}
//		// right coll
//		tmp = wallAtWorldCoord(r.x, r.y);
//		if (tileIsSolid(tmp)) {
//			System.out.println("collide rt");
//			return true;
//
//		}
//		return false;
//
//	}

	public void initGrid() {
		this.grid = new int[ROWS][COLS];
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				this.grid[y][x] = DEFAULT_FILL;

			}
		}

	}

	public int[][] nullGrid() {
		int newgrid[][] = new int[ROWS][COLS];
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				newgrid[y][x] = DEFAULT_FILL;

			}
		}
		return newgrid;

	}

	public int getTileYX(int gridY, int gridX) {
		try {
			return this.grid[gridY][gridX];
		} catch (Exception e) {
			return ILLEGAL_TILE;
		}
	}

	public boolean[][] nullGridBL() {
		boolean newgrid[][] = new boolean[ROWS][COLS];
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				newgrid[y][x] = false;

			}
		}
		return newgrid;

	}

	public void saveTilegrid() {
		int tg[][] = this.grid;
		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), "collision" + this.game.level + ".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setWritable(true);
			Utils.writeInt2DAToCSV(tg, completePath.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Save tilegrid data " + dataPath.toString());
	}

	public void loadTilegrid() {

		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), "collision" + this.game.level + ".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setReadable(true);
			int[][] tg = Utils.openCSVto2DAInt(completePath.toString());
			this.grid = tg;
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Load tilegrid data " + dataPath.toString());

	}



	public void setTileXYK(int gridX, int gridY, int kind) {
		int x = Utils.clamp(0, COLS - 1, gridX);
		int y = Utils.clamp(0, ROWS - 1, gridY);
		this.grid[y][x] = Utils.clamp(0, MAX_KIND, kind);
	}

	public void draw() {
//		if(this.game.editor.editMode=='n') {
//			return;
//		}
		int ts = Game.COLL_GRID_SIZE;
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				int cellValue = this.grid[y][x];

				switch(cellValue) {
				case OPEN:
					this.game.g.setColor(WALL_COLOR);
					break;

				case SOLID:
					this.game.g.setColor(WALL_COLOR);
					this.game.g.fillRect(x*ts-game.cameraX,y*ts-game.cameraY,ts-1,ts-1);

					break;
				
				default:
					break;
				}
			}
		}

	}

	

	public void update() {

	}

}
