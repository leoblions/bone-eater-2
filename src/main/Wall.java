package main;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class Wall {
	 int ROWS = 25;
	 int COLS = 25;
	final int MAX_KIND = 24;
	final int DEFAULT_FILL = -1;
	final int ILLEGAL_TILE = -1;
	final Color WALL_COLOR = new Color(255,255,255,70);
	final Color SPECIAL_COLOR = new Color(0,0,255,70);
	final boolean COLL_DEFAULT_VALUE = false;
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
	public boolean collGrid[][]=null;
	private BufferedImage placeholder;
	
	public Wall(Game game) {
		this.game = game;
		this.tileSize = this.game.TILE_SIZE;
		try {
			this.images = this.initImages();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		initGrid();
		
	}
	
	private void initGrid() {
		this.grid=new int[ROWS][COLS];
		for(int y = 0;y< ROWS;y++) {
			for(int x = 0;x< COLS;x++) {
				this.grid[y][x]=DEFAULT_FILL;
				
			}
		}
		
	}
	public int[][] nullGrid() {
		int newgrid[][] =new int[ROWS][COLS];
		for(int y = 0;y< ROWS;y++) {
			for(int x = 0;x< COLS;x++) {
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
		boolean newgrid[][] =new boolean[ROWS][COLS];
		for(int y = 0;y< ROWS;y++) {
			for(int x = 0;x< COLS;x++) {
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
		Path completePath = Paths.get(dataPath.toString(), "wall"+this.game.level+".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setWritable(true);
			Utils.writeInt2DAToCSV(tg, completePath.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Save wall data " + completePath.toString());
	}
	
	public void loadTilegrid() {

		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), "wall"+this.game.level+".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setReadable(true);
			int [][] tg = Utils.openCSVto2DAInt(completePath.toString());
			this.grid = tg;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Load wall data " + completePath.toString());

	}
	
	
	public BufferedImage[] initImages() throws IOException {
		try {
            this.placeholder = ImageIO.read(getClass().getResource("/images/goldbug.png")); // Image in resources/images/icon.png
        } catch (IOException e) {
            e.printStackTrace();
        }
		BufferedImage[] items1 = new Imageutils(game).spriteSheetCutter("/images/tileWallOverlay.png", 6, 4,  Game.TILE_SIZE,  Game.TILE_SIZE);

		//BufferedImage[] items2 = new Imageutils(game).spriteSheetCutter("/images/tileWall.png", 4, 4, 100, 100);

		//BufferedImage[] itemImages = Imageutils.appendArray(items1, items2);
		
		return items1;
		
		
		
		
	}
	
	public void setTileXYK(int gridX, int gridY, int kind) {
		System.out.println("add wall");
		int x = Utils.clamp(0, COLS-1, gridX);
		int y = Utils.clamp(0, ROWS-1, gridY);
		this.grid[y][x] = Utils.clamp(-1, this.images.length, kind);
	}
	
	public void draw() {
//		if(this.game.editor.editMode=='n') {
//			return;
//		}
		for(int y = 0;y< ROWS;y++) {
			for(int x = 0;x< COLS;x++) {
				int cellValue = this.grid[y][x];
				try {
					image = this.images[cellValue];
				}catch(Exception e) {
					continue;
				}
				
				
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
	
	
	
	public void update() {
		
	}

}
