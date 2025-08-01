package main;

import java.awt.Color;
import java.awt.Point;
import java.time.temporal.ValueRange;

public class Pathfind {
	Game game;
	boolean[][] wallgrid; // false = no wall, true = wall
	boolean[][] checkGrid; // used by enemies to find player w/o walking thru walls
	int[][] pfGrid; // used for updating pfGrid
	int rows, cols;
	Color healthBarColor = new Color(50, 200, 50, 250);
	int halfSquare;
	Pacer pfPacer;
	public static final int SOLID = 1;
	public static final int WALL = 1;
	public static final int OPEN = 0;
	public final int PF_GRID_PASSES = 15;
	public final int GRID_UPDATE_PERIOD = 30;
	public final int TARGET_OFFSET_X = 30;
	public final int TARGET_OFFSET_Y = 30;
	private boolean DRAW_WALL_GRID = false;
	private boolean DRAW_PF_NUMBERS = true;
	private final boolean SUPPRESS_OOBE = true;
	private final int [] modifiersX = {0,1,1,1,0,-1,-1,-1,0};
	private final int [] modifiersY = {-1,-1,0,1,1,1,0,-1,0};

	public Pathfind(Game game) {
		this.game = game;
//		this.rows = (game.HEIGHT / game.tilegrid.tileSize) + 1;
//		this.cols = (game.WIDTH / game.tilegrid.tileSize) + 1;
		this.rows = Game.ROWS*2;
		this.cols = Game.COLS*2;
		this.wallgrid = new boolean[rows][cols];
		this.checkGrid = new boolean[rows][cols];
		this.pfGrid = new int[rows][cols];
		halfSquare = game.tilegrid.tileSize / 2;
		pfPacer = new Pacer(GRID_UPDATE_PERIOD);

	}
	
	

	private boolean screenGridPositionIsSolid(int sgridX, int sgridY) {
		int soffsetGridX = (game.cameraX + halfSquare) / game.tilegrid.tileSize;
		int soffsetGridY = (game.cameraY + halfSquare) / game.tilegrid.tileSize;
		int gridX = sgridX + soffsetGridX;
		int gridY = sgridY + soffsetGridY;
		int kind;
		try {
			kind = game.tilegrid.getTileYX(gridY,gridX);
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return Collision.tileIsSolid(kind);

	}
	
	private boolean kindIsSolid(int kind) {
		//true if wall
		if (kind>15 &&kind>-1) {
			return true;
		}else {return false;
		
		}
	}

	public void updateWallGrid() {
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				this.wallgrid[y][x] = game.collision.tileIsSolid(this.game.collision.getTileYX(y, x));
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
	
	private int cellMarkedNeighborsAmount( boolean[][] grid, int gridX, int gridY) {
		int acc = 0;
				acc += cellExistsAndGTOne(grid,   gridX-1, gridY -1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX-1, gridY   ) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX-1, gridY +1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX,   gridY -1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX,   gridY +1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX+1, gridY -1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX+1, gridY   ) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX+1, gridY +1) ? 1 : 0;
				
			return acc;
		
	}
	private int cellMarkedNeighborsAmount4W( boolean[][] grid, int gridX, int gridY) {
		int acc = 0;
				acc += cellExistsAndGTOne(grid,   gridX-1, gridY   ) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX,   gridY -1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX,   gridY +1) ? 1 : 0;
				acc += cellExistsAndGTOne(grid,   gridX+1, gridY   ) ? 1 : 0;
				
			return acc;
		
	}
	
	private boolean cellHasMarkedCardinalNeighbor( boolean[][] grid, int gridX, int gridY) {
		if(
				
				cellExistsAndGTOne(grid,   gridX-1, gridY   ) ||
				
				cellExistsAndGTOne(grid,   gridX,   gridY -1) ||
				cellExistsAndGTOne(grid,   gridX,   gridY +1) ||
		
				cellExistsAndGTOne(grid,   gridX+1, gridY   ) 
			
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
	
public char getDirectionTowardsPlayer(EntityUnit eunit) {
		
		//int screenX = (int) worldP.getX() - game.cameraX;
		//int screenY = (int) worldP.getY() - game.cameraY;
		int ENTITY_OFFSET_X = 0;
		int ENTITY_OFFSET_Y = 0;
		int entityGX =  (eunit.worldX+eunit.width/2+ENTITY_OFFSET_X) / Game.COLL_GRID_SIZE;
		int entityGY =  ( eunit.worldY+eunit.height/2+ENTITY_OFFSET_Y) / Game.COLL_GRID_SIZE;
		int test, max;
		boolean isWall=false;
		int solid = game.collision.SOLID;
		
		max = 0;
		char dir = 'n';
		try {// none
			test = pfGrid[entityGY][entityGX];
			isWall = solid==this.game.collision.grid[entityGY][entityGX];
			if  (test>max && !isWall) {
				max = test;
				dir = 'n';
			}
		}catch(Exception e) {}
		
		try {// left
			test = pfGrid[entityGY][entityGX-1];
			//System.out.println("LT "+L);
			isWall = solid==this.game.collision.grid[entityGY][entityGX-1];
			if  (test>max && !isWall) {
				max = test;
				dir = 'l';
			}
		}catch(Exception e) {}
		
		try {//right
			test = pfGrid[entityGY][entityGX+1];
			//System.out.println("RT "+R);
			isWall = solid==this.game.collision.grid[entityGY][entityGX+1];
			if  (test>max && !isWall) {
				max = test;
				dir = 'r';
			}
		}catch(Exception e) {}
		
		try {//up
			test = pfGrid[entityGY-1][entityGX];
			
			isWall = solid==this.game.collision.grid[entityGY-1][entityGX];
			//System.out.println("UP "+test+ " wall "+isWall);
			if  (test>max && !isWall) {
				max = test;
				dir = 'u';
			}
		}catch(Exception e) {}
		
		try {
			test = pfGrid[entityGY+1][entityGX];
			isWall = this.wallgrid[entityGY+1][entityGX];
			if  (test>max && !isWall) {
				max = test;
				dir = 'd';
			}
		}catch(Exception e) {}
		//System.out.println("DIR "+dir+ " max "+max );
		return dir;
				
		
		
	}
	
	public char getDirectionTowardsPlayer(int entityWX, int entityWY) {
		
		//int screenX = (int) worldP.getX() - game.cameraX;
		//int screenY = (int) worldP.getY() - game.cameraY;
		int ENTITY_OFFSET_X = 0;
		int ENTITY_OFFSET_Y = 0;
		int entityGX =  (entityWX+ENTITY_OFFSET_X) / Game.COLL_GRID_SIZE;
		int entityGY =  (entityWY+ENTITY_OFFSET_Y) / Game.COLL_GRID_SIZE;
		int L, R, U, D, N, max;
		boolean isWall=false;
		max = 0;
		char dir = 'n';
		try {
			N = pfGrid[entityGY][entityGX];
			isWall = this.wallgrid[entityGY][entityGX];
			if  (N>max && !isWall) {
				max = N;
				dir = 'n';
			}
		}catch(Exception e) {}
		try {
			L = pfGrid[entityGY][entityGX-1];
			//System.out.println("LT "+L);
			isWall = this.wallgrid[entityGY][entityGX-1];
			if  (L>max && !isWall) {
				max = L;
				dir = 'l';
			}
		}catch(Exception e) {}
		try {
			R = pfGrid[entityGY][entityGX+1];
			//System.out.println("RT "+R);
			isWall = this.wallgrid[entityGY][entityGX+1];
			if  (R>max && !isWall) {
				max = R;
				dir = 'r';
			}
		}catch(Exception e) {}
		try {
			U = pfGrid[entityGY-1][entityGX];
			//System.out.println("UP "+U);
			isWall = this.wallgrid[entityGY-1][entityGX];
			if  (U>max && !isWall) {
				max = U;
				dir = 'u';
			}
		}catch(Exception e) {}
		try {
			D = pfGrid[entityGY+1][entityGX];
			isWall = this.wallgrid[entityGY+1][entityGX];
			if  (D>max && !isWall) {
				max = D;
				dir = 'd';
			}
		}catch(Exception e) {}
		
		return dir;
				
		
		
	}
	
public int getDirectionTowardsPlayer8way(int entityWX, int entityWY) {
		
		/**
		 * 0 top
		 * 1 trc
		 * 2 right
		 * 3 brc
		 * 4 bottom
		 * 5 blc
		 * 6 left
		 * 7 tlc
		 * 8 neutral
		 */
	
		int entityGX =  (entityWX) / Game.TILE_SIZE;
		int entityGY =  (entityWY) / Game.TILE_SIZE;
		int testValue; 
		int maxDirection = -1;
		boolean isWall=false;
		int maxValue = 0;
		int xcoord = 0;
		int ycoord = 0;
		for (int i = 0;i<9;i++) {
			xcoord = entityGX + modifiersX[i];
			ycoord = entityGY + modifiersY[i];
			try {
				testValue = pfGrid[ycoord][xcoord];
				isWall = this.wallgrid[ycoord][xcoord];
				
				if  (testValue>maxValue && !isWall) {
					maxDirection = i;
					maxValue=testValue;
				}
				
			}catch(Exception e) {}
			
		}
		
		
		
		return maxDirection;
				
		
		
	}
	
	public void updatePFGrid() {
		this.checkGrid = new boolean[rows][cols];
		this.pfGrid = new int[rows][cols];
		int pgX = (game.player.worldX +TARGET_OFFSET_X )/ Game.COLL_GRID_SIZE;
		int pgY = (game.player.worldY +TARGET_OFFSET_Y )/ Game.COLL_GRID_SIZE;
		try {

			checkGrid[pgY][pgX]= true; 
			pfGrid[pgY][pgX]= 5; 
		}catch(ArrayIndexOutOfBoundsException e){
			if(!SUPPRESS_OOBE)e.printStackTrace();
		}
		
		for(int i = 0; i< PF_GRID_PASSES ; i++) {
		//for(int i = PF_GRID_PASSES; i >=0 ; i--) {
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
					int acc;
					if(( acc = cellMarkedNeighborsAmount(checkGrid,x,y))!=0) {
						pfGrid[y][x]+=(acc+amountToAdd);
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
						int w = Game.COLL_GRID_SIZE;
						int h = Game.COLL_GRID_SIZE;

						int screenX = x * Game.COLL_GRID_SIZE - game.cameraX;
						int screenY = y * Game.COLL_GRID_SIZE - game.cameraY;

						game.g.setColor(healthBarColor);
						game.g.fillRect(screenX, screenY, w, h);
						
					}

				}
			}
		}
		if(DRAW_PF_NUMBERS) {
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < cols; x++) {
					if (this.pfGrid[y][x] != SOLID) {
						int w = Game.COLL_GRID_SIZE;
						int h = Game.COLL_GRID_SIZE;

						int screenX = x * Game.COLL_GRID_SIZE - game.cameraX;
						int screenY = y * Game.COLL_GRID_SIZE - game.cameraY;
						int alpha = pfGrid[y][x] ;

						game.g.setColor(new Color(2, 2, 2,250 ));
						//game.g.fillRect(screenX, screenY, w, h);
						game.g.drawString(Integer.toString(alpha), screenX+25, screenY+25);
					}

				}
			}
		}
		

	}

	public void update() {
		if (game.tilegrid.grid==null) {
			return;
		}
		if(this.pfPacer.check()) {
			updateWallGrid();
			updatePFGrid();
			
		}
		
	}

}
