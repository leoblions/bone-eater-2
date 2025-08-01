package main;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class Console {
	/*
	 * Activate the dev console with ` parses single or multi part commands to
	 * activate cheats and some editing tasks
	 */
	Game game;

	private boolean isReset = false;
	boolean allowed;
	String text;
	String lastCommand;
	Color background = new Color(100, 100, 100, 150);
	Color frame = new Color(100, 200, 100, 150);
	Color fontColor = Color.white;
	int height, width, screenX, screenY;
	int currentLineY, prevLineY;
	int debounceCounter = 0;
	final int DEBOUNCE_COUNTER = 10;
	private final int Y_SPACING = 15;

	private final String STRING_SEPARATOR = " ";

	public Console(Game game) {
		this.game = game;
		allowed = true;

		height = 50;
		width = 300;
		screenX = 150;
		screenY = 500;
		text = "";
		lastCommand = "Dev console:";
		currentLineY = (screenY + height - Y_SPACING);
		prevLineY = screenY + height - (2 * Y_SPACING);

	}

	public void draw() {
		if (game.gameState == 'c') {
			game.g.setColor(background);
			game.g.fillRect(screenX, screenY, width, height);
			game.g.setColor(frame);
			game.g.drawRect(screenX, screenY, width, height);
			game.g.setColor(fontColor);
			game.g.drawString(lastCommand, screenX, prevLineY);
			game.g.drawString(text, screenX, currentLineY);
		}

	}

	private void runCommand(String command) {
		String[] splitCommand = command.split(STRING_SEPARATOR);
		int wordAmount = splitCommand.length;
		System.out.println("The command is " + command);
		int kind;
		if (wordAmount == 0) {
			System.out.println("Err: command evaluated to zero symbols");
			return;
		} else {
			switch (splitCommand[0]) {
			case "HEAL":
				// game.player.fullHeal();
				break;
			case "STORE":
				// game.gameState=GameState.STORE;
				break;
			case "TAI":
				// game.entityManager.frozen =!game.entityManager.frozen ;
				// System.out.printf("Entity frozen %b\n",game.entityManager.frozen);
				break;
			case "SAVE":
				game.editor.saveOrLoadData(true);
			case "LOAD":
				game.editor.saveOrLoadData(false);
			case "WARP":
				if (wordAmount != 2)
					break;
				int warpID = Integer.parseInt(splitCommand[1]);
				System.out.println("Warp to ID " + warpID);
				// game.warp.warpToID(warpID);
				break;
			case "WARPLOC":
				if (wordAmount != 3)
					break;
				int level = Integer.parseInt(splitCommand[1]);

				int gridX = Integer.parseInt(splitCommand[2]);

				int gridY = Integer.parseInt(splitCommand[3]);
				// game.warp.warpToLocation(level, gridX, gridY);
				break;
			case "FILLTILE":
				kind = Integer.parseInt(splitCommand[1]);
				// game.tileManager.fillTile(kind);
				break;
			case "ADDITEM":
				kind = Integer.parseInt(splitCommand[1]);
				// game.inventory.addItem(kind, 1);
				break;
			case "TILE":
				game.editor.editMode = Editor.TILE;
				if (wordAmount != 2)
					break;
				try {
					kind = Integer.parseInt(splitCommand[1]);
					}catch(Exception e) {
						kind = 0;
						if(splitCommand[1].contentEquals("RESET")) {
							game.tilegrid.initGrid();
						}
					}
				game.editor.setAssetID(kind);
				break;
			case "WALL":
				game.editor.editMode = Editor.WALL;
				if (wordAmount != 2)
					break;
				try {
					kind = Integer.parseInt(splitCommand[1]);
					}catch(Exception e) {
						kind = 0;
						if(splitCommand[1].contentEquals("RESET")) {
							game.tilegrid.initGrid();
						}
					}
				game.editor.setAssetID(kind);
				break;
			case "COLLISION":
			case "COLL":
				game.editor.editMode = Editor.COLLISION;
				if (wordAmount != 2)
					break;
				try {
					kind = Integer.parseInt(splitCommand[1]);
					}catch(Exception e) {
						kind = 0;
						if(splitCommand[1].contentEquals("RESET")) {
							game.collision.initGrid();
						}
					}
				game.editor.setAssetID(kind);
				break;
			case "DECOR":
				game.editor.editMode = Editor.DECOR;
				if (wordAmount != 2)
					break;
				try {
				kind = Integer.parseInt(splitCommand[1]);
				}catch(Exception e) {
					kind = 0;
					if(splitCommand[1].contentEquals("RESET")) {
						game.tilegrid.initGrid();
					}
				}
				game.editor.setAssetID(kind);
				break;
			case "EDIT":
			case "EDITMODE":

				if (wordAmount != 2)
					break;
				char editmode = (char) (splitCommand[1].toLowerCase().charAt(0));
				game.editor.editMode = editmode;
				game.editor.updateStrings();
				System.out.println("Edit mode is: " + editmode);
				break;
			case "NORMAL":

				game.editor.editMode = 'n';
				System.out.println("Edit mode off ");
				break;
			case "BARRIER":
				game.editor.editMode = 'b';
				if (wordAmount != 2)
					break;
				kind = Integer.parseInt(splitCommand[1]);
				game.editor.setAssetID(kind);
				break;
			case "WIDGET":
				game.editor.editMode = 'w';
				if (wordAmount != 2)
					break;
				kind = Integer.parseInt(splitCommand[1]);
				game.editor.setAssetID(kind);
				break;
			case "ENTITY":
				game.editor.editMode = 'e';
				if (wordAmount != 2)
					break;
				kind = Integer.parseInt(splitCommand[1]);
				game.editor.setAssetID(kind);
				break;
			case "GRID":

				// String locationString = String.format("Player gridX: %d, gridY:
				// %d\n",game.player.tilePlayer[0],game.player.tilePlayer[1]);
				// System.out.println(locationString);
				break;
			case "LOC":
			case "LOCATION":
				String locationStringM = String.format("Player worldX: %d, worldY: %d\n", game.player.worldX,
						game.player.worldY);
				String levelString = String.format("Current level is: %d\n", game.level);
				System.out.println(levelString);
				System.out.println(locationStringM);
				break;
			case "LEVEL":
				int newLevel = Integer.parseInt(splitCommand[1]);
				this.game.brain.changeLevel(newLevel);
				levelString = String.format("Current level changed to: %d\n", game.level);
				System.out.println(levelString);
				break;

			default:
				System.err.printf("M No command found: %s\n", command);
			}
		}

	}

	private void runCommand_000(String command) {
		String[] splitCommand = command.split(STRING_SEPARATOR);
		int wordAmount = splitCommand.length;
		System.out.println("The command is " + command);

		switch (wordAmount) {
		case 0:
			System.out.println("The command was empty");
			break;
		case 1:
			switch (splitCommand[0]) {
			case "HEAL":
				// game.player.fullHeal();
				break;
			case "TAI":
				// game.entity.active =!game.entity.active ;
				/// System.out.printf("Entity active %b\n",game.entity.active);
				break;
			default:
				System.err.printf("0 No command found: %s\n", splitCommand[0]);
			}
			break;
		case 2:
			break;

		default:
			System.err.printf("M No command found: %s\n", command);
		}
	}

	public void sendKeyEvent(KeyEvent e) {
		if (!(game.gameState == 'c') || debounceCounter > 0) {
			return;
		} else {
			debounceCounter = DEBOUNCE_COUNTER;
		}

		String test = KeyEvent.getKeyText(e.getKeyCode());
		switch (test) {
		case "Backspace":
			int len = text.length();
			if (len > 0) {
				text = text.substring(0, len - 1);
				break;
			}
			break;
		case "Enter":
			runCommand(text);
			lastCommand = text;
			text = "";
			break;
		case "Space":
			text += " ";
			break;
		case "Up":
			text = "" + lastCommand;
			break;
		case "Down":
			text = "";
			break;
		case "Back Quote":
			this.game.toggleConsole();

			this.isReset = false;
			break;
		default:
			text += test;
			break;
		}
	}

//	public void requestActivate() {
//	if(!active && isReset) {
//
//		game.gameState=GameState.PAUSED;
//		active = true;
//	}else {
//		game.gameState=GameState.PLAY;
//	}
//	}

	public void update() {
		// isReset = true;
		if (game.gameState == 'c') {
			if (debounceCounter > 0)
				debounceCounter--;

		}

	}

//	public void toggleDevConsole() {
//		if(!active) {
//			game.gameState=GameState.PAUSED;
//			this.active = true;
//		}else {
//			game.gameState=GameState.PLAY;
//		}
//		
//	}

}
