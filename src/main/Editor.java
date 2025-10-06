package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

public class Editor {
	Game game;
	public static final char NORMAL = 'n';
	public static final char TILE = 't';
	public static final char TRIGGER = 'o';
	public static final char DECOR = 'd';
	public static final char WALL = 'w';
	public static final char COLLISION = 'c';
	public static final char PICKUP = 'p';
	private static final char ENTITY = 0;
	private final int MIN_ASSET_ID = -1;
	private int PREVIEW_HEIGHT = 100;

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
	public boolean previewAsset = true;
	private int previewX =0;
	private int previewY = 0;
	BufferedImage previewImage = null;

	public Editor(Game game) {
		this.game = game;
		arial16 = new Font("Arial", Font.BOLD, 16);
		this.editMode = NORMAL;
		editModeString = String.valueOf(this.editMode);
		assetIDstring = getAssetIDString();
		this.latchString = "";
		this.previewY = this.game.HEIGHT - PREVIEW_HEIGHT;
		

	}

	public String getAssetIDString() {
		return "Asset: " + String.format(" %d", assetID);
	}

	public void updateStrings() {
		editModeString = "Edit mode " + String.valueOf(this.editMode);
		assetIDstring = getAssetIDString();
	}

	public void setAssetID(int assetID) {
		int tempAssetID = assetID;

		this.updateStrings();
		this.setPreviewImage();
	}
	
	public void setPreviewImage() {
		
		switch (this.editMode) {
		case NORMAL:
			previewImage = null;
			break;
		case TILE:
			this.previewImage = this.game.tilegrid.getImage(assetID);
			break;
		case COLLISION:
			previewImage = null;
			break;
		case WALL:
			previewImage = null;
			break;
		case TRIGGER:
			previewImage = null;
			break;
		case PICKUP:
			this.previewImage = this.game.pickup.getImage(assetID);
			break;
		case DECOR:
			this.previewImage = this.game.decor.getImage(assetID);
			break;
		case ENTITY:
			this.previewImage = this.game.entity.getImage(this.assetID);
			break;
		}

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
			// this.game.wall.setTileXYK(gridX, gridY, assetID);
			System.out.println("paint wall " + gridX + " " + gridY + " " + assetID);
			break;
		case TRIGGER:
			// System.out.println("edit triggers");
			this.game.trigger.setTileXYK(gridX, gridY, assetID);
			break;
		case PICKUP:
			delete = assetID == -1;
			this.game.pickup.setTileGXY(gridX, gridY, delete);
			break;
		case DECOR:
			delete = assetID == -1;
			System.out.println("paint decor " + gridX + " " + gridY + " " + assetID);
			this.game.decor.setTileXYK(gridX, gridY, assetID);
			break;

		}

	}

	public void draw() {
		game.g.setColor(Color.red);
		game.g.setFont(arial16);
		game.g.drawString(String.format("Player wx:%d wy:%d", this.game.player.x, this.game.player.y), 10, 50);
		game.g.drawString(editModeString, 10, 70);
		game.g.drawString(assetIDstring, 10, 90);
		game.g.drawString(latchString, 10, 110);
		if(this.previewImage!=null ) {
			game.g.drawImage(previewImage, previewX,

					previewY, PREVIEW_HEIGHT, PREVIEW_HEIGHT, null);
		}

	}

	public void update() {

		if (latchEnable && latchActive) {
			paintAsset();
			// System.out.println("painting");
		}

	}

	public void saveOrLoadData(boolean save) {
		
			
		if(save) {
			this.game.tilegrid.saveTilegrid();
			this.game.pickup.saveCurrentData();
			this.game.collision.saveTilegrid();
			this.game.trigger.saveRecordsToFile();
			this.game.decor.saveGridCurrentRoom();
			this.game.entity.saveCurrentData();
			
		}else {
			this.game.tilegrid.loadTilegrid();
			this.game.pickup.loadCurrentData();
			this.game.collision.loadTilegrid();
			this.game.trigger.loadRecordsFromFile();
			this.game.decor.loadGridCurrentRoom();
			this.game.entity.loadCurrentData();
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

	public void clear() {
		// reset all components of current map
		
		this.game.tilegrid.reset();
		System.out.println("reset");
		this.game.pickup.reset();

		this.game.collision.reset();

		this.game.trigger.reset();

		this.game.decor.reset();
		
		this.game.entity.reset();

	}

	public void resetCurrent() {
		// reset current component

		switch (this.editMode) {
		case NORMAL:
			return;

		case TILE:
			this.game.tilegrid.reset();
			break;
		case PICKUP:
			this.game.pickup.reset();
			break;
		case COLLISION:
			this.game.collision.reset();
			break;
		case TRIGGER:
			this.game.trigger.reset();
			break;
		case DECOR:
			this.game.decor.reset();
			break;
		default:
			break;

		}

	}

}
