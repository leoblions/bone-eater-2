package main;

import java.awt.Color;
import java.awt.Point;
import java.time.temporal.ValueRange;

public class Pathfind {
	Game gp;
	boolean[][] wallgrid; // false = no wall, true = wall
	boolean[][] checkGrid; // used by enemies to find player w/o walking thru walls
	int[][] pfGrid; // used for updating pfGrid
	int rows, cols;
	Color healthBarColor = new Color(50, 200, 50, 80);
	int halfSquare;
	Pacer pfPacer;
	public final int PF_GRID_PASSES = 15;
	public final int GRID_UPDATE_PERIOD = 30;
	public final int TARGET_OFFSET_X = 30;
	public final int TARGET_OFFSET_Y = 30;
	private boolean DRAW_WALL_GRID = false;
	private boolean DRAW_PF_NUMBERS = false;
	private final boolean SUPPRESS_OOBE = true;

	public Pathfind(Game gp) {
		this.gp = gp;
		this.rows = (gp.HEIGHT / gp.tilegrid.tileSize) + 1;
		this.cols = (gp.WIDTH / gp.tilegrid.tileSize) + 1;
		this.wallgrid = new boolean[rows][cols];
		this.checkGrid = new boolean[rows][cols];
		this.pfGrid = new int[rows][cols];
		halfSquare = gp.tilegrid.tileSize / 2;
		pfPacer = new Pacer(GRID_UPDATE_PERIOD);

	}

	private boolean screenGridPositionIsSolid(int sgridX, int sgridY) {
		int soffsetGridX = (gp.cameraX + halfSquare) / gp.tilegrid.tileSize;
		int soffsetGridY = (gp.cameraY + halfSquare) / gp.tilegrid.tileSize;
		int gridX = sgridX + soffsetGridX;
		int gridY = sgridY + soffsetGridY;
		int kind;
		try {
			kind = gp.tilegrid.getTileYX(gridY,gridX);
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return Collision.tileKindIsSolid(kind);

	}

	public void updateWallGrid() {
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (screenGridPositionIsSolid(x, y)) {
					this.wallgrid[y][x] = true;
				} else {
					this.wallgrid[y][x] = false;
				}
			}
		}
	}
	
	private boolean cellHasMarkedNeighbor( boolean[][] grid, int gridX, int gridY) {
		if(
				cellExistsAndGTOne(grid,   gridX-1, gridY -1) ||
				cellExistsAndGTOne(grid,   gridX-1, gridY   ) ||
				cellExistsAndGTOne(grid,   gridX-1, gridY +1) ||
				cellExistsAndGTOne(grid,   gridX,   gridY -1) ||
				cellExistsAndGTOne(grid,   gridX,   gridY +1) ||
				cellExistsAndGTOne(grid,   gridX+1, gridY -1) ||
				cellExistsAndGTOne(grid,   gridX+1, gridY   ) ||
				cellExistsAndGTOne(grid,   gridX+1, gridY +1) 
				) {
			return true;
		}else {
			return false;
		}
		
	}
	
	private boolean cellExistsAndGTOne( boolean[][] grid, int gridX, int gridY) {
		try {
			boolean cellValue = grid[gridY][gridX];
			return cellValue;
		}catch(Exception e) {
			return false;
		}
	}
	
	public char getDirectionTowardsPlayer(Point worldP) {
		
		int screenX = (int) worldP.getX() - gp.cameraX;
		int screenY = (int) worldP.getY() - gp.cameraY;
		int screenGX = screenX / gp.tilegrid.tileSize;
		int screenGY = screenY / gp.tilegrid.tileSize;
		int L, R, U, D, max;
		max = 0;
		char dir = 'N';
		try {
			L = pfGrid[screenGY][screenGX-1];
			if  (L>max) {
				max = L;
				dir = 'L';
			}
		}catch(Exception e) {}
		try {
			R = pfGrid[screenGY][screenGX+1];
			if  (R>max) {
				max = R;
				dir = 'R';
			}
		}catch(Exception e) {}
		try {
			U = pfGrid[screenGY-1][screenGX];
			if  (U>max) {
				max = U;
				dir = 'U';
			}
		}catch(Exception e) {}
		try {
			D = pfGrid[screenGY+1][screenGX];
			if  (D>max) {
				max = D;
				dir = 'D';
			}
		}catch(Exception e) {}
		
		return dir;
				
		
		
	}
	
	public void updatePFGrid() {
		this.checkGrid = new boolean[rows][cols];
		this.pfGrid = new int[rows][cols];
		int pgX = (gp.player.screenX +TARGET_OFFSET_X )/ gp.tilegrid.tileSize;
		int pgY = (gp.player.screenY +TARGET_OFFSET_Y )/ gp.tilegrid.tileSize;
		try {

			checkGrid[pgY][pgX]= true; 
			pfGrid[pgY][pgX]= 5; 
		}catch(ArrayIndexOutOfBoundsException e){
			if(!SUPPRESS_OOBE)e.printStackTrace();
		}
		
		for(int i = 0; i< PF_GRID_PASSES ; i++) {
			pfPass(i);
		}
		
	}
	
	public void pfPass(int amountToAdd) {
		// update check grid
				for (int y = 0; y < rows; y++) {
					for (int x = 0; x < cols; x++) {
						
						if (!wallgrid[y][x] && pfGrid[y][x]>0) {
							checkGrid[y][x]=true;
						}
						
					}
				}
		// update pfGrid
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				boolean tileIsWall = wallgrid[y][x];
				if (tileIsWall) {
					continue; // don't mark walls
				}else {
					if(cellHasMarkedNeighbor(checkGrid,x,y)) {
						pfGrid[y][x]+=amountToAdd;
					}
				}
			}
		}
		
	}

	public void draw() {
		if(DRAW_WALL_GRID) {
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < cols; x++) {
					if (this.wallgrid[y][x] ) {
						int w = 50;
						int h = 50;

						int boxX = x * gp.tilegrid.tileSize;
						int boxY = y * gp.tilegrid.tileSize;

						gp.g.setColor(healthBarColor);
						gp.g.fillRect(boxX, boxY, w, h);
					}

				}
			}
		}
		if(DRAW_PF_NUMBERS) {
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < cols; x++) {
					if (this.pfGrid[y][x] > 0) {
						int w = 50;
						int h = 50;

						int boxX = x * gp.tilegrid.tileSize;
						int boxY = y * gp.tilegrid.tileSize;

						gp.g.setColor(new Color(50, 200, 50, pfGrid[y][x]));
						gp.g.fillRect(boxX, boxY, w, h);
					}

				}
			}
		}
		

	}

	public void update() {
		if (gp.tilegrid.grid==null) {
			return;
		}
		updateWallGrid();
		updatePFGrid();
	}

}
