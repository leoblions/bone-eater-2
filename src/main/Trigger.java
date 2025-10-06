package main;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import javax.imageio.ImageIO;

public class Trigger {
	Game game;
	public final String TRIGGER_RECORDS_FILENAME = "locations_trigger";
	/**
	 * Each room has its own records file edit mode for triggers is 'o'
	 * 
	 * Player presses or touches trigger square, this class calls brain to perform
	 * an action
	 * 
	 */
	// colors for trigger edit mode
	final Color TRIGGER_COLOR1 = new Color(255, 0, 0, 70);
	final Color TRIGGER_COLOR2 = new Color(0, 255, 0, 70);
	final Color TRIGGER_COLOR3 = new Color(0, 0, 255, 70);
	final Color TRIGGER_COLOR4 = new Color(200, 200, 0, 70);
	public static final int FIELDS = 6;// 0=ID, 1=LEV, 2=GX, 3=GY, 4=W, 5=H
	public static final int DEFAULT_ACTION_ID = 0;

	// TF denotes which subscript of data in trigger record
	public static final int TF_ROOM_ID = 0; // which trigger in current room
	public static final int TF_KIND = 1; // what kind of trigger
	public static final int TFGRIDX = 2;// units are in tiles, not pixels
	public final int TFGRIDY = 3;
	public final int TFGRIDW = 4;
	public final int TFGRIDH = 5;
	// Trigger kinds
	public static final int NOT_FOUND = -1;
	public static final int TK_NO_TRIGGER = 0;
	public static final int TK_TRIGGER_TOUCH = 1;
	public static final int TK_TRIGGER_PRESS = 2;
	public static final int TK_TRIGGER_RESETTOUCH = 3;
	public static final int TK_TRIGGER_RESETPRESS = 4;

	public final int TRIGGER_DEBOUNCE = 25;
	public int triggerDebounce = 0;
	public int tileSize;
	public LinkedList<int[]> currentRecords;

	public Trigger(Game game) {
		this.game = game;
		tileSize = this.game.TILE_SIZE;
		this.currentRecords = new LinkedList<>();
		loadRecordsFromFile();
		// filterAllRecordsToCurrentRoomRecords();
	}

	public void initRecords() {

		this.currentRecords = new LinkedList<>();

	}

	public void reset() {

		LinkedList<int[]> recordsNew = new LinkedList<>();
		currentRecords = recordsNew;

		System.out.println("Reset trigger data ");

	}

	public void loadRecordsFromFile() {
		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), TRIGGER_RECORDS_FILENAME + this.game.level + ".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setReadable(true);
			int[][] allRecordsTmp = Utils.openCSVto2DAInt(completePath.toString());
			LinkedList<int[]> recordsNew = new LinkedList<>(Arrays.asList(allRecordsTmp));
			currentRecords = recordsNew;
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Load trigger data " + dataPath.toString());

	}

	public void saveRecordsToFile() {
		
		this.currentRecords.removeIf(Objects::isNull);
		int tg[][] = Utils.LOIAtoint2DA(currentRecords);
		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), TRIGGER_RECORDS_FILENAME + this.game.level + ".csv");
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setWritable(true);
			Utils.writeInt2DAToCSV(tg, completePath.toString());
		} catch (Exception e) {
			System.out.printf("Failed to save trigger data %s \n",completePath);
			//e.printStackTrace();
		}

		System.out.println("Save trigger data " + dataPath.toString());
	}
//	private void filterAllRecordsToCurrentRoomRecords() {
//		LinkedList<int[]> currentRoomRecordsList = new LinkedList<>();
//		for(int[] record : this.allRecords) {
//			if(this.game.level==record[TF_ACTION_ID]) {
//				currentRoomRecordsList.add(record);
//			}
//		}
//		this.currentRoomRecords = currentRoomRecordsList;
//	}

	public void draw() {
//		if(this.game.editor.editMode!='o') {
//			return;
//		}
		for (int r = 0; r < this.currentRecords.size(); r++) {

			int[] trecord = this.currentRecords.get(r);
			int gridX = trecord[TFGRIDX];
			int gridY = trecord[TFGRIDY];
			int gridW = trecord[TFGRIDW];
			int gridH = trecord[TFGRIDH];
			int kind = trecord[TF_KIND];
			if (kind == TK_NO_TRIGGER) {
				continue;
			}
			// switch expression
			Color tc = switch (kind) {
			case 0 -> TRIGGER_COLOR1;
			case 1 -> TRIGGER_COLOR1;
			case 2 -> TRIGGER_COLOR2;
			case 3 -> TRIGGER_COLOR3;
			case 4 -> TRIGGER_COLOR4;
			default -> TRIGGER_COLOR1;

			};
			this.game.g.setColor(tc);
			this.game.g.fillRect(gridX * tileSize - game.cameraX, gridY * tileSize - game.cameraY, gridW * tileSize - 1,
					gridH * tileSize - 1);

		}

	}

	public int matchRecordGXY(int gridX, int gridY) {
		// finds index of matching trigger square by its location
		boolean matched = false;
		int matchIndex = NOT_FOUND;
		// room records
		for (int r = 0; r < this.currentRecords.size(); r++) {
			int[] trecord = this.currentRecords.get(r);
			if ((trecord[TFGRIDX] == gridX) && (trecord[TFGRIDY] == gridY)) {
				matched = true;
				matchIndex = r;
				break;
			}
		}
		if (matched) {
			return matchIndex;

		} else {
			return NOT_FOUND;
		}
	}

	private int getUnusedRoomID() {
		int testID = 0;
		boolean matched = true;
		while (matched) {
			for (int r = 0; r < this.currentRecords.size(); r++) {
				matched = false;
				int[] trecord = this.currentRecords.get(r);
				if (trecord[TF_ROOM_ID] == testID) {
					matched = true;

					break;
				}

			}
			if (matched == false) {
				return testID;
			} else {
				testID++;
			}

		}
		return testID;

	}

	public void setTileXYK(int gridX, int gridY, int kind) {
		// System.out.println("edit triggers " + kind);
		if (kind == TK_NO_TRIGGER) {

			int matchIndex = matchRecordGXY(gridX, gridY);

			if (matchIndex != NOT_FOUND) {
				this.currentRecords.remove(matchIndex);
				System.out.println("deleted trigger record");

			}

		} else {
			int matchingTriggerIndex = -1;

			matchingTriggerIndex = matchRecordGXY(gridX, gridY);

			// int kind = this.game.editor.assetID;
//			if (kind > TK_TRIGGER) {
//				kind = TK_TRIGGER;
//			}
			int[] newTRecord = new int[FIELDS];
			newTRecord[TF_ROOM_ID] = getUnusedRoomID();
			newTRecord[TF_KIND] = kind;
			newTRecord[TFGRIDX] = gridX;
			newTRecord[TFGRIDY] = gridY;
			newTRecord[TFGRIDW] = 1;
			newTRecord[TFGRIDH] = 1;
			newTRecord[TF_KIND] = kind;
			if (matchingTriggerIndex == NOT_FOUND) {

				this.currentRecords.add(newTRecord);
				System.out.printf("added trigger record  %d %d %d \n", gridX, gridY, kind);
				System.out.printf("length of trigger records %d \n", this.currentRecords.size());
			} else {
				this.currentRecords.set(matchingTriggerIndex, newTRecord);
				System.out.printf("edit trigger record  %d %d %d \n", gridX, gridY, kind);
			}

		}

	}

	// public void playerPressedTrigger(int triggerID, int actionID) {
	public void playerPressedTrigger(int[] triggerRecord) {
		int kind = triggerRecord[TF_KIND];
		int triggerID = triggerRecord[TF_ROOM_ID];

		if (triggerDebounce <= 0 && this.game.playerPressActivate) {
			// PLAYER PRESSED TRIGGER
			this.game.brain.handlePressTrigger(triggerRecord);

			System.out.println("player pressed trigger triggerID " + triggerID);
			System.out.println("Trigger send kind " + kind);
			this.triggerDebounce = TRIGGER_DEBOUNCE;
		} else if (triggerRecord[TF_KIND] == TK_TRIGGER_TOUCH) {
			// player bumped trigger
			this.game.brain.handlePressTrigger(triggerRecord);

			System.out.println("player pressed trigger triggerID " + triggerID);
			System.out.println("Trigger send kind " + kind);
			this.triggerDebounce = TRIGGER_DEBOUNCE;
		}
	}

	public void update() {
		// check player trigger tile collisions
		int pgx = (this.game.player.x + 50) / this.game.tilegrid.tileSize;
		int pgy = (this.game.player.y + 50) / this.game.tilegrid.tileSize;
		this.game.hud.showInteract = false;
		for (int[] trecord : this.currentRecords) {
			int tgx1 = trecord[TFGRIDX];
			int tgy1 = trecord[TFGRIDY];
			int tgx2 = trecord[TFGRIDX] + trecord[TFGRIDW];
			int tgy2 = trecord[TFGRIDY] + trecord[TFGRIDH];
			int kind = trecord[TF_KIND];
			if (pgx >= tgx1 && pgx <= tgx2 && pgy >= tgy1 && pgy <= tgy2) {
				// int actionID = trecord[TF_KIND];
				int triggerID = trecord[TF_ROOM_ID];
				// playerPressedTrigger(triggerID, actionID);
				playerPressedTrigger(trecord);
				this.game.hud.showInteract = true;

			}

		}
		if (this.triggerDebounce > 0)
			triggerDebounce--;

	}

}
