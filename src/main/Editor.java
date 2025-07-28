package main;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Editor {
	Game game;
	public static final char NORMAL = 'n';
	public static final char TILE = 't';
	public static final char TRIGGER = 'o';
	public static final char DECOR = 'd';
	public static final char WALL = 'w';
	public static final char COLLISION = 'c';
	private final int MIN_ASSET_ID = -1;
	
	Color smBorder = new Color(50, 50, 50, 50);
	private static String editModeString, assetIDstring, latchString;
	Font arial16;
	public char editMode; // n=normal, t=tile, o=trigger
	public boolean delete = false;
	public int assetID;
	char activeComponent = NORMAL;
	// latch is used to click and drag tiles
	public boolean latchEnable = true;
	public boolean latchActive = false;
	public boolean latch;

	public Editor(Game game) {
		this.game = game;
		arial16 = new Font("Arial", Font.PLAIN, 16);
		this.editMode = NORMAL;
		editModeString = String.valueOf(this.editMode);
		assetIDstring = getAssetIDString();
		this.latchString = "";

	}

	public String getAssetIDString() {
		return "Asset: " + String.format(" %d", assetID);
	}

	public void updateStrings() {
		editModeString = "Edit mode "+ String.valueOf(this.editMode);
		assetIDstring = getAssetIDString();
	}

	public void setAssetID(int assetID) {
		int tempAssetID = assetID;

		this.updateStrings();
	}

	public void paintAsset() {
		int gridX = (this.game.mouseX + game.cameraX) / this.game.tilegrid.tileSize;
		int gridY = (this.game.mouseY + game.cameraY) / this.game.tilegrid.tileSize;
		int gridXColl = (this.game.mouseX + game.cameraX) / this.game.COLL_GRID_SIZE;
		int gridYColl = (this.game.mouseY + game.cameraY) / this.game.COLL_GRID_SIZE;
		boolean delete = false;
		switch (this.editMode) {
		case NORMAL:
			System.out.println("Normal mode, nothing to edit here");
			break;
		case TILE:
			this.game.tilegrid.setTileXYK(gridX, gridY, assetID);
			System.out.println("paint tile " + gridX + " " + gridY + " " + assetID);
			break;
		case COLLISION:
			this.game.collision.setTileXYK(gridXColl, gridYColl, assetID);
			System.out.println("paint collision " + gridXColl + " " + gridYColl + " " + assetID);
			break;
		case WALL:
			this.game.wall.setTileXYK(gridX, gridY, assetID);
			System.out.println("paint wall " + gridX + " " + gridY + " " + assetID);
			break;
		case TRIGGER:
			 delete = assetID==0;
			this.game.trigger.setTileGXY(gridX,gridY,delete);
			break;
		case DECOR:
			 delete = assetID==0;
			 System.out.println("paint decor " + gridX + " " + gridY + " " + assetID);
			 this.game.decor.setTileXYK(gridX, gridY, assetID);
			break;

		}

	}

	public void draw() {
		game.g.setColor(Color.white);
		game.g.setFont(arial16);
		game.g.drawString(editModeString, 10, 70);
		game.g.drawString(assetIDstring, 10, 90);
		game.g.drawString(latchString, 10, 110);

	}

	public void update() {

		if (latchEnable && latchActive) {
			paintAsset();
			// System.out.println("painting");
		}

	}
	
	public void saveOrLoadData(boolean save){
		//true=save
		switch(this.editMode) {
		case NORMAL:
			return;
			
		case TILE: 
			if(save) {
				this.game.tilegrid.saveTilegrid();
			}else {
				this.game.tilegrid.loadTilegrid() ;
			}
			break;
		case COLLISION: 
			if(save) {
				this.game.collision.saveTilegrid();
			}else {
				this.game.collision.loadTilegrid() ;
			}
			break;
		case TRIGGER:
			if(save) {
				this.game.trigger.saveRecordsToFile();
			}else {
				this.game.trigger.loadRecordsFromFile() ;
			}
			break;
		case DECOR:
			if(save) {
				this.game.decor.saveGridCurrentRoom();
			}else {
				this.game.decor.loadGridCurrentRoom() ;
			}
			break;
		case WALL:
			if(save) {
				this.game.wall.saveTilegrid();
			}else {
				this.game.wall.loadTilegrid() ;
			}
			break;
			
		
		}
		
		
	}

	public void handleClick(int button, int down) {
		switch (button) {
		case 1:// LC
			if (down == 1 && this.latchEnable) {
				this.latchActive = true;
			} else if (down == 0 && this.latchEnable) {
				this.latchActive = false;

			}
			break;
		case 2:
			if (down == 0) {
				this.assetID += 1;
				this.updateStrings();
				System.out.println("assetID " + assetID);
			}

			break;
		case 3:
			if (down == 0) {
				this.assetID -= 1;
				if (assetID < MIN_ASSET_ID)
					assetID = MIN_ASSET_ID;
				this.updateStrings();
				System.out.println("assetID " + assetID);

			}

			break;
		default:
			break;

		}

	}
	


	public void toggleEditMode() {

	}


	public void toggleLatch() {
		this.latchEnable = !this.latchEnable;
		if (this.latchEnable) {
			this.latchString = "LATCH";
		} else {
			this.latchString = "";
		}
		System.out.println("Latch: " + this.latchEnable);
	}

	public void toggleEditDeleteMode() {
		delete = !delete;
		if (delete) {
			System.out.println("delete mode on");
		} else {
			System.out.println("delete mode off");
		}

	}
	

	public void click(int kind, int mouseX, int mouseY) {
		if (true) {
			this.handleClick(kind, 1);
			// System.out.printf("Mouse down %d %d %d\n",mouseX, mouseY, kind);
			switch (kind) {
			case 1:
				this.paintAsset();
				break;
			case 2:
				this.assetID += 1;
				this.updateStrings();
				System.out.println("assetID " + assetID);
				break;
			case 3:
				this.assetID -= 1;
				this.updateStrings();
				System.out.println("assetID " + assetID);
				break;
			default:
				break;

			}
		}

	}

}
