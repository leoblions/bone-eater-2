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
	 * Each room has its own records file
	 * edit mode for triggers is 'o'
	 * 
	 */
	final Color TRIGGER_COLOR = new Color(0, 0, 255, 70);
	public final int FIELDS = 6;// 0=ID, 1=LEV, 2=GX, 3=GY, 4=W, 5=H
	public final int DEFAULT_ACTION_ID = -1;
	public final int TF_ROOM_ID = 0;
	public final int TF_ACTION_ID = 1;
	public final int TFGRIDX = 2;// units are in tiles, not pixels
	public final int TFGRIDY = 3;
	public final int TFGRIDW = 4;
	public final int TFGRIDH = 5;
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
			e.printStackTrace();
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
		if(this.game.editor.editMode!='o') {
			return;
		}
		for (int r = 0; r < this.currentRecords.size(); r++) {

			int[] trecord = this.currentRecords.get(r);
			int gridX = trecord[TFGRIDX];
			int gridY = trecord[TFGRIDY];
			int gridW = trecord[TFGRIDW];
			int gridH = trecord[TFGRIDH];

			this.game.g.setColor(TRIGGER_COLOR);
			this.game.g.fillRect(gridX * tileSize -game.cameraX, gridY * tileSize-game.cameraY, gridW * tileSize - 1, gridH * tileSize - 1);

		}

	}

	public int matchRecordGXY(int gridX, int gridY) {
		// -1 if not found
		boolean matched = false;
		int matchIndex = -1;
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
			return -1;
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

	public void setTileGXY(int gridX, int gridY, boolean delete) {
		if (delete) {

			int matchIndex = matchRecordGXY(gridX, gridY);

			if (matchIndex != -1) {
				this.currentRecords.remove(matchIndex);
				System.out.println("deleted trigger record");

			}

		} else {

			if (matchRecordGXY(gridX, gridY) != -1) {
				return;

			} else {
				int[] newTRecord = new int[FIELDS];
				newTRecord[TF_ROOM_ID] = getUnusedRoomID();
				newTRecord[TF_ACTION_ID] = DEFAULT_ACTION_ID;
				newTRecord[TFGRIDX] = gridX;
				newTRecord[TFGRIDY] = gridY;
				newTRecord[TFGRIDW] = 1;
				newTRecord[TFGRIDH] = 1;
				this.currentRecords.add(newTRecord);
				System.out.println("added trigger record");

			}

		}

	}

	public void playerPressedTrigger(int triggerID,int actionID) {

		if (triggerDebounce <= 0 && this.game.playerPressActivate) {
			

			this.game.brain.handlePressTrigger(actionID);
			
			System.out.println("player pressed trigger triggerID " + triggerID);
			System.out.println("Trigger send actionID " + actionID);
			this.triggerDebounce = TRIGGER_DEBOUNCE;
		}
	}

	public void update() {
		int pgx = (this.game.player.worldX + 50) / this.game.tilegrid.tileSize;
		int pgy = (this.game.player.worldY + 50) / this.game.tilegrid.tileSize;
		this.game.hud.showInteract = false;
		for (int[] trecord : this.currentRecords) {
			int tgx1 = trecord[TFGRIDX];
			int tgy1 = trecord[TFGRIDY];
			int tgx2 = trecord[TFGRIDX] + trecord[TFGRIDW];
			int tgy2 = trecord[TFGRIDY] + trecord[TFGRIDH];
			if (pgx >= tgx1 && pgx <= tgx2 && pgy >= tgy1 && pgy <= tgy2) {
				int actionID = trecord[TF_ACTION_ID];
				int triggerID = trecord[TF_ROOM_ID];
				playerPressedTrigger(triggerID,actionID);
				this.game.hud.showInteract = true;

			}

		}
		if (this.triggerDebounce > 0)
			triggerDebounce--;

	}

}
