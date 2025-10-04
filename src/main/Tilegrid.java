package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class Tilegrid {
//	 int Game.ROWS = 25;
//	 int Game.COLS = 25;
	final int MAX_KIND = 31;
	final int DEFAULT_FILL =0;
	final int ILLEGAL_TILE = -1;
	final Color WALL_COLOR = new Color(255,255,255,70);
	final Color SPECIAL_COLOR = new Color(0,0,255,70);
	/*
	 * 0-15 = floor
	 * 16-31 = wall
	 * -1 = delete
	 */
	Game game;
	BufferedImage image;
	BufferedImage[] images;
	public  int tileSize =  Game.TILE_SIZE;
	public int grid[][]=null;
	private BufferedImage placeholder;
	
	public Tilegrid(Game game) {
		this.game = game;
		this.tileSize = this.game.TILE_SIZE;
		try {
			this.images = this.initImages();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		initGrid();
		
	}
	
	public void initGrid() {
		this.grid=new int[Game.ROWS][Game.COLS];
		for(int y = 0;y< Game.ROWS;y++) {
			for(int x = 0;x< Game.COLS;x++) {
				this.grid[y][x]=DEFAULT_FILL;
				
			}
		}
		
	}
	public int[][] nullGrid() {
		int newgrid[][] =new int[Game.ROWS][Game.COLS];
		for(int y = 0;y< Game.ROWS;y++) {
			for(int x = 0;x< Game.COLS;x++) {
				newgrid[y][x]=DEFAULT_FILL;
				
			}
		}
		return newgrid;
		
	}
	public int getTileYX(int gridY,int gridX) {
		try {
			return this.grid[gridY][gridX];
		}catch(Exception e){
			return ILLEGAL_TILE;
		}
	}
	public boolean[][] nullGridBL() {
		boolean newgrid[][] =new boolean[Game.ROWS][Game.COLS];
		for(int y = 0;y< Game.ROWS;y++) {
			for(int x = 0;x< Game.COLS;x++) {
				newgrid[y][x]=false;
				
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
		Path completePath = Paths.get(dataPath.toString(), "level"+this.game.level+".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setWritable(true);
			Utils.writeInt2DAToCSV(tg, completePath.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Save tilegrid data " + dataPath.toString());
	}
	
	public void loadTilegrid() {

		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), "level"+this.game.level+".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setReadable(true);
			int [][] tg = Utils.openCSVto2DAInt(completePath.toString());
			this.grid = tg;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Load tilegrid data " + dataPath.toString());

	}
	
	public void reset() {

		
			
		
		this.grid = Utils.fill2DI(Game.COLS, Game.ROWS, DEFAULT_FILL);
		
		System.out.println("Reset tilegrid data "  );

	}
	
	
	public BufferedImage[] initImages() throws IOException {
		try {
            this.placeholder = ImageIO.read(getClass().getResource("/images/goldbug.png")); // Image in resources/images/icon.png
        } catch (IOException e) {
            e.printStackTrace();
        }
		BufferedImage[] items1 = new Imageutils(game).spriteSheetCutter("/images/tileFloor.png", 4, 4,  Game.TILE_SIZE, Game.TILE_SIZE);

		BufferedImage[] items2 = new Imageutils(game).spriteSheetCutter("/images/tileWall1.png", 4, 4,  Game.TILE_SIZE,  Game.TILE_SIZE);

		BufferedImage[] itemImages = Imageutils.appendArray(items1, items2);
		
		return itemImages;
		
		
		
		
	}
	
	public void setTileXYK(int gridX, int gridY, int kind) {
		int x = Utils.clamp(0, Game.COLS-1, gridX);
		int y = Utils.clamp(0, Game.ROWS-1, gridY);
		this.grid[y][x] = Utils.clamp(0, MAX_KIND, kind);
	}
	
	public void draw() {
//		if(this.game.editor.editMode=='n') {
//			return;
//		}
		for(int y = 0;y< Game.ROWS;y++) {
			for(int x = 0;x< Game.COLS;x++) {
				int cellValue = this.grid[y][x];
				image = this.images[cellValue];
				
//				switch(cellValue) {
//				case 0:
//					this.game.g.setColor(WALL_COLOR);
//					break;
//				case 1:
//					continue;
//					//this.game.g.setColor(WALL_COLOR);
//					//break;
//				case 3:
//					this.game.g.setColor(SPECIAL_COLOR);
//					break;
//				default:
//					this.game.g.setColor(Color.BLUE);
//					break;
////				}
//				this.game.g.fillRect(x*tileSize, y*tileSize, tileSize-1, tileSize-1);
				this.game.g.drawImage(image,x*tileSize-game.cameraX,y*tileSize-game.cameraY,tileSize+1,tileSize+1,null);
				
			}
		}
		
	}
	
	public void drawWall() {
		if(this.game.editor.editMode=='n') {
			return;
		}
		for(int y = 0;y< Game.ROWS;y++) {
			for(int x = 0;x< Game.COLS;x++) {
				int cellValue = this.grid[y][x];
				if (cellValue>15) {
			
					this.game.g.setColor(WALL_COLOR);
				
				this.game.g.fillRect(x*tileSize, y*tileSize, tileSize-1, tileSize-1);
				
			}
			}}
		
	}
	
	public void update() {
		
	}

}
