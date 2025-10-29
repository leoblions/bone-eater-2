package main;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Brain {
	final String ACTION_RECORDS_FILE = "action_records";
	final String ACTION_TABLE_FILE = "action_table";
	final String WARP_DESTINATIONS_FILE = "warp_destinations.csv";
	final int[] dummyRecord = { -1, -1, -1, -1 };
	// action record fields
	final int FIELDS = 4;
	final int AFID = 0;
	final int AFKIND = 1;
	final int AFARG = 2;
	final int AFNEXT = 3;
	// action kinds
	final int AKIND_WARP = 0;
	final int AKIND_DIALOG = 1;
	final int AKIND_QUESTFLAG = 2;
	final int AKIND_ADDITEM = 3;
	final int AKIND_SUBITEM = 4;
	final int AKIND_HEAL = 5;
	//warp destinations
	final int WD_FIELDS = 4;
	final int WD_ID = 0;
	final int WD_GRIDX = 1;
	final int WD_GRIDY = 2;
	final int WD_LEVEL = 3;
	
	Game game;
	int stage = 0;
	Image image;
	Image[] images;
	
	public ArrayList<int[]> actionRecords; // 0=actionID, 1=actionKind, 2=ActionArg, 3=nextActionID
	public ArrayList<int[]> warpDestinations;
	public ArrayDeque<Integer> queuedActions;
	int MAX_QUEUED_ACTIONS = 10;
	int BLANK_ACTION_ID = -1;
	
	int objectivesTotal=5;
	int objectivesComplete=0;
	boolean advanceLevelOnObjectivesComplete = false;
	ObjectiveKind currentObjectiveKind = ObjectiveKind.KILL;
	/*
	 * Triggers:
	 * Player touches trigger, which sends a trigger array to Brain.
	 * Brain gets the actionID from the trigger Array.
	 * Brain looks up actionID in a table and decides what to do.
	 */

	public Brain(Game game) {
		this.game = game;
		this.initImages();
		queuedActions = new ArrayDeque<>();
		 loadWarpDestinations();
		//this.loadActionRecordsThisLevel();
		this.loadActionTable();

	}
	
	public void killedEnemy() {
		if (currentObjectiveKind==ObjectiveKind.KILL) {
			objectivesComplete+=1;
			game.hud.updateObjectiveString();
		}
	}
	private void loadActionTable()  {
		String fullFilename = null;
		try {
			fullFilename = ACTION_TABLE_FILE  + ".csv";
			this.actionRecords = Utils.loadRecordsFromFile(fullFilename);
		} catch (FileNotFoundException e) {
			System.err.println("File not found, creating mock file " + fullFilename);
			Utils.saveMockRecordsToFile(fullFilename, FIELDS);
			this.actionRecords = new ArrayList<>();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private void loadActionRecordsThisLevel()  {
//		String fullFilename = null;
//		try {
//			fullFilename = ACTION_RECORDS_FILE + this.game.level + ".csv";
//			this.actionRecords = Utils.loadRecordsFromFile(fullFilename);
//		} catch (FileNotFoundException e) {
//			System.err.println("File not found, creating mock file " + fullFilename);
//			Utils.saveMockRecordsToFile(fullFilename, FIELDS);
//			this.actionRecords = new ArrayList<>();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	private void loadWarpDestinations() {
		
		try {
			
			this.warpDestinations = Utils.loadRecordsFromFile(WARP_DESTINATIONS_FILE);
		} catch (FileNotFoundException e) {
			System.err.println("File not found, creating mock file " + WARP_DESTINATIONS_FILE);
			Utils.saveMockRecordsToFile(WARP_DESTINATIONS_FILE, WD_FIELDS);
			this.warpDestinations = new ArrayList<>();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void enqueueAction(int actionID) {
		if (actionID == BLANK_ACTION_ID) {
			return;
		}
		if (this.queuedActions.size() < MAX_QUEUED_ACTIONS) {
			this.queuedActions.addLast(actionID);
		}
	}

	private void dequeueAction() {

		Integer actionID = this.queuedActions.poll();
		if (actionID == null || actionID == BLANK_ACTION_ID) {
			return;
		} else {
			runAction(actionID);
		}

	}

	private void runAction(int actionID) {
		int [] arecord = getActionRecord(actionID);
		if(arecord!=null) {
			int actionKind = arecord[AFKIND];
			int actionArg = arecord[AFARG];
			int actionNext = arecord[AFNEXT];
			if(actionNext!=BLANK_ACTION_ID) {
				enqueueAction(actionNext);
				//allow chaining actions
			}
			switch(actionKind) {
			case AKIND_WARP :
				int [] warpDestination = getWarpDestination(actionArg);
				if(null!=warpDestination) {
					System.out.println("Warp player to warpID "+actionArg);
					warpPlayerToLocation(warpDestination[WD_GRIDX], warpDestination[WD_GRIDY], warpDestination[WD_LEVEL]);
				}
				break;
			case AKIND_DIALOG :
				break;
			case AKIND_QUESTFLAG :
				break;
			case AKIND_ADDITEM :
				break;
			case AKIND_SUBITEM :
				break;
			case AKIND_HEAL :
				break;
				default:
					break;
			
			}
		}

	}
	
	private void warpPlayerToLocation(int gridX, int gridY, int level) {
		boolean invincibleOG = this.game.player.invincible;
		this.game.player.invincible = true;
		if (level != this.game.level) {
			this.changeLevel(level);
		}
		this.game.player.warpPlayer(gridX, gridY);
		this.game.player.invincible = invincibleOG;
	}

	private void initImages() {
		try {
			this.image = ImageIO.read(getClass().getResource("/images/goldbug.png")); // Image in
																						// resources/images/icon.png
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void changeLevel(int levelID) {
		System.out.println("Brain Change Level "+levelID);
		this.game.level = levelID;
		this.game.trigger.loadRecordsFromFile();
		this.game.tilegrid.loadTilegrid() ;
		this.game.decor.loadGridCurrentRoom();
		this.game.entity.loadCurrentData();
		this.game.collision.loadTilegrid();
		this.game.pickup.loadCurrentData();
		//loadActionRecordsThisLevel();
	}

	public void draw() {

	}

	public void update() {
		dequeueAction();
	}

	public void playerActivateNPC(EntityUnit entityUnit, boolean playerPressActivate) {
		// TODO Auto-generated method stub

	}
	
	public int[] getWarpDestination(int warpID) {
		for (int[] arecord : this.warpDestinations) {
			if (arecord[WD_ID] == warpID) {
				return arecord;

			}

		}
		return null;
	}

	public int[] getActionRecord(int actionID) {
		for (int[] arecord : this.actionRecords) {
			if (arecord[AFID] == actionID) {
				return arecord;

			}

		}
		return null;
	}

	public void handlePressTrigger(int[] triggerRecord) {
		// match trigger room ID to action ID
		// both trigger and action ID should be unique per room
		int actionID = triggerRecord[Trigger.TF_ROOM_ID];
		if (this.game.playerPressActivate) {
			System.out.println("Player press trigger activated actionID " + actionID);
			int[] arecord = getActionRecord(actionID);
			if (arecord != null) {
				System.out.println("Brain run actionID "+actionID);
				runAction(actionID);
			}
		}

	}
	enum ObjectiveKind{
		KILL,PICKUP,TRIGGER
	}
	public void endConversationNPC(int speakerNPC) {
		// TODO Auto-generated method stub
		
	}

	public void warpToLocation(int level, int gridX, int gridY) {
		if (level != this.game.level) {
			this.changeLevel(level);
		}
		this.game.player.warpPlayer(gridX, gridY);
		
	}

}
