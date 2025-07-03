package main;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Entity records
 * 0=gx,1=gy,2=kind,3=uid
 * Each room has its own records csv file, that's loaded on entry.
 * CSV data is converted into LOIA, which is used to instantiate EntityUnits
 * Edits apply to LOIA and entityUnits
 * data is saved from LOIA
 * 
 */
public class Entity {
	public static final int NEW_ENTITY_DEFAULT_UID = 0;
	public static final int ENTITY_ACTIVATE_DELAY_TICKS = 120;
	private static final String DATA_FILE_PREFIX = "entity";
	private static final String DATA_FILE_SUFFIX = ".csv";
	Game game;
	final int FIELDS = 4;
	final int EFGRIDX = 0;
	final int EFGRIDY = 1;
	final int EFKIND = 2;
	final int EFID=3;
	ArrayList<int[]>entityUnitData; //stores entity init data only, 0=GX, 1=GY, 2=KIND, 3=UID
	ArrayList<EntityUnit>entityUnits; //stores refs to Entity objects
	ArrayList<EntityUnit>entityTouchedList;
	private boolean modified=false;
	public boolean playerTouchedActorSincelastTick = false;
	public boolean activateEntityFlag = false;
	public Delay entityActivateDalay;
	public Rectangle playerHitbox;
	public final int HITBOX_SIZE_INC = 15;
	public final int HITBOX_OFFSET = -25;
	public boolean playerMelee = false;


	public String[] entityNames= {
			"Meatberry",
			"Bat",
			"Centipede",
			"Spider",
			"Maggot",
			"Earwig",
			"Groundhog",
			"Zombie",
			"Mercenary",
			"Mercenary Leader",
			"Rick",
			"Lilly",
			"Rodney",
			"Nicole",
			"Ed",
			"Melissa",
			"John",
			"Terry",
			"Sue",
			"Jason",
			"Mary",
			
	};
	public boolean frozen = false;
	public boolean drawHitbox = false;
	public boolean activateEntityUnitFlag;
	
	/*
	 * Types: 
	 * 0 Meatberry 
	 * 1 Bat 
	 * 2 Centipede 
	 * 3 Spider 
	 * 4 Maggot 
	 * 5 Earwig 
	 * 6 Groundhog 
	 * 7 Zombie 
	 * 8 Mercenary
	 * 9 Mercenary Leader 
	 * 
	 * 10 Rick 
	 * 11 Lilly 
	 * 12 Rodney 
	 * 13 Nicole
	 * 14 Ed
	 * 15 Melissa
	 * 16 John
	 * 17 Terry
	 * 18 Sue
	 * 19 Jason
	 * 20 Mary
	 */
	
	
	
	public Entity(Game game) {
		this.game=game;
		entityUnits = new ArrayList<>();
		entityTouchedList = new ArrayList<>();
		entityUnits = new ArrayList<>();
		//this.addEntity(5, 5, 0, 0);
		entityActivateDalay = new Delay();
		

		playerHitbox=new Rectangle();
	}
	
	public void playerAttackEntityMelee() {
		// set hitbox
		
		//grid coord
		this.game.player.calculateTileForward();
		int pgfx = game.player.tileForward[0]  ;
		int pgfy = game.player.tileForward[1]  ;
		int pgx = game.player.tilePlayer[0]  ;
		int pgy = game.player.tilePlayer[1]  ;
		
		int pgx2 = pgx + 1 ;
		int pgy2 = pgy + 1  ;
		int pgfx2 = pgfx + 1   ;
		int pgfy2 = pgfy + 1  ;
		
		int hbx1,hbx2,hby1,hby2 ;
		
		hbx1 = (pgfx<pgx)?pgfx:pgx;
		hby1 = (pgfy<pgy)?pgfy:pgy;
		hbx2 = (pgfx2>pgx2)?pgfx2:pgx2;
		hby2 = (pgfy2>pgy2)?pgfy2:pgy2;
		
		playerHitbox.x = hbx1* game.tilegrid.tileSize;
		playerHitbox.y = hby1* game.tilegrid.tileSize;
		playerHitbox.width = (hbx2-hbx1)* game.tilegrid.tileSize+HITBOX_SIZE_INC;
		playerHitbox.height= (hby2-hby1)* game.tilegrid.tileSize+HITBOX_SIZE_INC;


		playerMelee=true;
		
		
		
	}
	
	public void addEntity(int startGX, int startGY, int kind, int UID) {
		EntityUnit eunit = new EntityUnit(this.game,  startGX,   startGY,   kind,   UID);
		this.entityUnits.add(eunit);
	}
	
	public void draw() {
		if (drawHitbox) {

			game.g.setColor(Color.red);
			game.g.drawRect(playerHitbox.x -game.cameraX , playerHitbox.y-game.cameraY, playerHitbox.width , playerHitbox.height);
		}
		//int[] visible = game.visibleArea;
		

		
		for (int i = 0; i < entityUnits.size(); i++) {
			 EntityUnit eunit =entityUnits.get(i) ;
				if (null!= eunit) {
					eunit.draw();
					
				} 
				
			}
			
		
	}
	
	
	private void updateFrozenState() {

		if (game.gameState=='g'||game.gameState=='i'||
				game.gameState=='a'||game.gameState=='b') {
			frozen=true;
		}else {
			frozen=false;
		}
	}
	
	public void update() {
		updateFrozenState();
		if(frozen) {
			return;
		}
		playerTouchedActorSincelastTick = false;
		entityActivateDalay.reduce();
		for (int i = 0; i < entityUnits.size(); i++) {
			 EntityUnit eunit =entityUnits.get(i) ;
				if (null!= eunit) {
					
					eunit.update();
					
				} 
				
			}
		if(playerTouchedActorSincelastTick == false) {
			entityTouchedList.clear();
			activateEntityFlag = false;
		}
		playerMelee = false;
	}

	

	


	public void setGridData(int[][] data) {
		// update records list
		convertDataGridToRecordsList(data);
		// init eunit objects
		instantiateEntityObjectsFromRecordList();
		
	}

	public void initBlank( ) {
		//this.barrierRecords = new ArrayList<>();
		ArrayList<EntityUnit> outerList = new ArrayList<>();
		
		this.entityUnits = outerList;
		ArrayList<EntityUnit> el = new ArrayList<>();
		 
		
		this.entityUnits = el;


	}


	public void paintAsset(int gridX, int gridY, int kind) {
		// create eunit object
		// check if coords in use
		// replace eunit in coords if it does, append if does not
		if(game.editor.delete) {

			int matchingIndex = getIndexEntityRecordWithMatchingGridCoord(  gridX,   gridY);
			if(matchingIndex==-1) { 
				System.out.println("No eunit there");
			}else {
				System.out.println("Delete eunit type: "+entityUnits.get(matchingIndex));
				entityUnits.set(matchingIndex, null);
				entityUnits.set(matchingIndex, null);
			}
		}
		modified=true;
		int UID = getNewUID();
		EntityUnit eunit = new EntityUnit(this.game,  gridX,   gridY,   kind, UID);
		EntityUnit eRecord = new EntityUnit( game, gridX,   gridY,   kind,   UID);
		int matchingIndex = getIndexEntityRecordWithMatchingGridCoord(  gridX,   gridY);
		if(matchingIndex==-1) {
			entityUnits.add(eRecord);
			entityUnits.add(eunit);
		}else {
			entityUnits.set(matchingIndex, eRecord);
			entityUnits.set(matchingIndex, eunit);
		}
		
	}
	
	private boolean checkEntityRecordExistsAtGridCoord(int gridX, int gridY) {
		for(EntityUnit e : this.entityUnits) {
			if(e.startGX==gridX&& e.startGY==gridY) {
				return true;
			}
		}
		return false;
	}
	private int getIndexEntityRecordWithMatchingGridCoord(int gridX, int gridY) {
		int Length = this.entityUnits.size();
		for(int i = 0; i< Length; i++) {
			EntityUnit e = this.entityUnits.get(i);
			if(e.startGX==gridX&& e.startGY==gridY) {
				return i;
			}
		}
		//not found: return -1
		return -1;
	}
	
	private int getNewUID( ) {
		return NEW_ENTITY_DEFAULT_UID;
	}
	
	private int[][] getDataGridFromEntityRecordList(){
		LinkedList<int[]> outerList = new LinkedList<>();
		int length = 0;
		for(EntityUnit eunit: this.entityUnits) {
			if(null!=eunit) {
				int[] recordAsArray = new int[] {eunit.startGX,eunit.startGY,eunit.kind,eunit.UID};
				outerList.add(recordAsArray);
				length +=1;
			}
			
		}
		int[][] output = new  int[length][4];
		for(int i=0;i<length;i++) {
			output[i]=outerList.get(i);
		}
		
		return  output;
		
	}
	
	private void convertDataGridToRecordsList(int[][] dataGrid){
		ArrayList<EntityUnit> outerList = new ArrayList<>();
	 
		for(int[] arr: dataGrid) {
			if(null!=arr) {
				EntityUnit eunit = new EntityUnit( game, arr[0],   arr[1],   arr[2],   arr[3]) ;
				outerList.add(eunit);
				 
			}
			
		}
		this.entityUnits = outerList;
		
	}
	
	private void instantiateEntityObjectsFromRecordList(){
		ArrayList<EntityUnit> outerList = new ArrayList<>();
	 
		for(int[] eunitd: this.entityUnitData) {
			if(null!=eunitd) {
				EntityUnit eunit = new EntityUnit(game, eunitd[EFGRIDX],  eunitd[EFGRIDY],  eunitd[EFKIND],  eunitd[EFID]);
				outerList.add(eunit);
			
			}
			
		}
		this.entityUnits = outerList;
		
	}

	

	
	
	

}

