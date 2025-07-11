package main;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Entity records 0=gx,1=gy,2=kind,3=uid Each room has its own records csv file,
 * that's loaded on entry. CSV data is converted into LOIA, which is used to
 * instantiate EntityUnits Edits apply to LOIA and entityUnits data is saved
 * from LOIA
 * 
 */
public class Entity {
	Pacer animationPacer;
	final int PFWORLDX = 0;
	final int PFWORLDY = 1;
	final int PFWORLDW = 2;
	final int PFWORLDH = 3;
	
	final char DOWN = 'd';
	final int UP = 'u';
	final int LEFT = 'l';
	final int RIGHT = 'r';
	
	final char ES_FOLLOW = 'f';
	final char ES_STAND = 's';
	final char ES_WANDER = 'w';
	final char ES_DEAD = 'd';
	final char ES_ATTACK = 'a';
	
	final int VISUAL_RANGE = 5; // how far entities can see

	final int ENTITY_PLAYER_COLLIDE_RANGE = 40;

	final int EK_GUARD = 0;

	final boolean DRAW_COLLRECT = true;

	final int SPEED_WALK = 2;
	final int SPEED_CHASE = 5;

	public static final int NEW_ENTITY_DEFAULT_UID = 0;
	public static final int ENTITY_ACTIVATE_DELAY_TICKS = 120;
	private static final String DATA_FILE_PREFIX = "entity";
	private static final String DATA_FILE_SUFFIX = ".csv";
	final int ATTACK_FRAMES=4;
	private final int DEF_DAMAGE_FROM_PLAYER = 5;
	final int IMAGES_PER_ENTITY = 16;
	private BufferedImage[][] entityImages;
	private BufferedImage[][] attackImages;
	Game game;
	final int ENTITY_KINDS = 10;
	final int FIELDS = 4;
	final int EFGRIDX = 0;
	final int EFGRIDY = 1;
	final int EFKIND = 2;
	final int EFID = 3;
	ArrayList<int[]> entityUnitData; // stores entity init data only, 0=GX, 1=GY, 2=KIND, 3=UID
	ArrayList<EntityUnit> entityUnits; // stores refs to Entity objects
	ArrayList<EntityUnit> entityTouchedList;
	private boolean modified = false;
	public boolean playerTouchedActorSincelastTick = false;
	public boolean activateEntityFlag = false;
	public Delay entityActivateDalay;
	public Rectangle playerCollider;
	public final int HITBOX_SIZE_INC = 15;
	public final int HITBOX_OFFSET = -25;
	public boolean playerMelee = false;

	public boolean frozen = false;
	public boolean drawHitbox = true;
	public boolean activateEntityUnitFlag;
	

	public Entity(Game game) {
		this.game = game;
		entityUnits = new ArrayList<>();
		entityTouchedList = new ArrayList<>();

		// this.addEntity(5, 5, 0, 0);
		entityActivateDalay = new Delay();
		animationPacer = new Pacer(10);
		this.initImages();

		playerCollider = new Rectangle();
		this.addEntity(5, 5, 0, 1);
	}

	public void initImages() {
		this.entityImages = new BufferedImage[ENTITY_KINDS][IMAGES_PER_ENTITY];
		this.attackImages = new BufferedImage[ENTITY_KINDS][IMAGES_PER_ENTITY];
		BufferedImage[] tempBI, tempBIL, tempBIR = null;
		String URL = null;
		try {
			URL = "/images/guard_100_200w.png";
			// tempBI = new Imageutils(game).spriteSheetCutter(URL, 4, 4, 100, 200);
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 100, 200);
			this.entityImages[EK_GUARD] = tempBI;
			
			URL = "/images/guard_100_200a.png";
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 200, 200);
		
			this.attackImages[EK_GUARD] = tempBI;

		} catch (Exception e) {
			System.err.println("EntityUnit Failed to open the resource: " + URL);
			e.printStackTrace();
		}

	}
	
	private boolean entityCheckChangedTile(EntityUnit eunit) {
		if(eunit.worldX==eunit.lastTile[0]&&eunit.worldY==eunit.lastTile[1]) {
			eunit.tileChanged=true;
			eunit.lastTile[0]=eunit.worldX;
			eunit.lastTile[1]=eunit.worldY;
			return true;
			
		}else {
			eunit.tileChanged = false;
			return false;
		}
	}
	
	public int entityPlayerDistance(EntityUnit eunit) {
		double deltaX = this.game.player.worldX - eunit.worldX;
		double deltaY = this.game.player.worldY - eunit.worldY;
		double Asquared = Math.pow(deltaX,2);
		double Bsquared = Math.pow(deltaY,2);
		double hypotenuse = Math.sqrt(Asquared+Bsquared);
		return (int)hypotenuse;
	}

	public void playerAttackEntityMelee() {
		// set hitbox

		// grid coord
		this.game.player.calculateTileForward();
		int pgfx = game.player.tileForward[0];
		int pgfy = game.player.tileForward[1];
		int pgx = game.player.tilePlayer[0];
		int pgy = game.player.tilePlayer[1];

		int pgx2 = pgx + 1;
		int pgy2 = pgy + 1;
		int pgfx2 = pgfx + 1;
		int pgfy2 = pgfy + 1;

		int hbx1, hbx2, hby1, hby2;

		hbx1 = (pgfx < pgx) ? pgfx : pgx;
		hby1 = (pgfy < pgy) ? pgfy : pgy;
		hbx2 = (pgfx2 > pgx2) ? pgfx2 : pgx2;
		hby2 = (pgfy2 > pgy2) ? pgfy2 : pgy2;

		playerCollider.x = hbx1 * game.tilegrid.tileSize;
		playerCollider.y = hby1 * game.tilegrid.tileSize;
		playerCollider.width = (hbx2 - hbx1) * game.tilegrid.tileSize + HITBOX_SIZE_INC;
		playerCollider.height = (hby2 - hby1) * game.tilegrid.tileSize + HITBOX_SIZE_INC;

		playerMelee = true;

	}

	public void addEntity(int startGX, int startGY, int kind, int UID) {
		EntityUnit eunit = new EntityUnit(this.game, startGX, startGY, kind, UID);
		this.entityUnits.add(eunit);
	}

	public void draw() {
		if (drawHitbox) {

			game.g.setColor(Color.red);
			game.g.drawRect(playerCollider.x - game.cameraX, playerCollider.y - game.cameraY, playerCollider.width,
					playerCollider.height);
		}
		// int[] visible = game.visibleArea;

		for (int i = 0; i < entityUnits.size(); i++) {
			EntityUnit eunit = entityUnits.get(i);
			if (null != eunit) {
				drawUnit(eunit);

			}

		}

	}

	public void drawUnit(EntityUnit eunit) {
		game.g.setColor(Color.orange);

		if (DRAW_COLLRECT)
			game.g.drawRect(eunit.worldX - game.cameraX, eunit.worldY - game.cameraY, eunit.width, eunit.height);
		eunit.screenX = (eunit.worldX - game.cameraX);
		eunit.screenY = (eunit.worldY - game.cameraY);
		BufferedImage image = null;
		if(eunit.state=='a') {
			image=this.attackImages[eunit.kind][eunit.currentImageIndex];
		}else {
			image=this.entityImages[eunit.kind][eunit.currentImageIndex];
		}
		
		game.g.drawImage(image, eunit.screenX + eunit.offsetX,

				eunit.screenY + eunit.offsetY, eunit.width, eunit.height, null);

	}

	private void updateFrozenState() {

		if (game.gameState != 'p') {
			frozen = true;
		} else {
			frozen = false;
		}
	}

	public void update() {
		updateFrozenState();
		if (frozen) {
			return;
		}
		playerTouchedActorSincelastTick = false;
		entityActivateDalay.reduce();
		for (int i = 0; i < entityUnits.size(); i++) {
			EntityUnit eunit = entityUnits.get(i);
			if (null != eunit) {
				entityCheckChangedTile(eunit);
				updateState(eunit);
				updatePosition(eunit);
				updateColliderUnit(eunit);

			}

		}
		if (playerTouchedActorSincelastTick == false) {
			entityTouchedList.clear();
			activateEntityFlag = false;
		}
		playerMelee = false;

	}
	/**
	 * 
	 * @param startGridX grid position of entity X
	 * @param startGridY grid position of entity Y
	 * @param deltaX change of X with each iteration
	 * @param deltaY change of Y with each iteration
	 * @param iterations distance of entity's vision
	 * @return
	 */
	public boolean senseBeam(int startGridX, int startGridY, int deltaX, int deltaY, int iterations) {
		// allows entity to see player in a given direction
		// true if player spotted
		int currGridX = startGridX;
		int currGridY = startGridY;
		boolean isWall=false;
		boolean isPlayer=false;
		for (int i = 0; i< iterations; i++) {
			try {
				isWall = this.game.pathfind.wallgrid[startGridY][startGridX];
			}catch(Exception e) {
				isWall = true;
			}
			int playerPos[]=  this.game.player.getGridPosition();
			isPlayer = (playerPos[0]==currGridX&& playerPos[1]==currGridY);
			if(isPlayer&& !isWall) {
				return true;
			}else if(!isPlayer && isWall) {
				return false;
			}
			  currGridX += deltaX;
			  currGridY += deltaY;
			
			
			
		}
		return false;
		
	}
	
	public void updateState(EntityUnit eunit) {
		// for sensing player
		if (eunit.state==ES_WANDER||eunit.state==ES_STAND) {
			int deltaX, deltaY;
			 deltaY = (eunit.direction=='d')?1:0;
			 deltaY = (eunit.direction=='u')?-1:0;
			 deltaX = (eunit.direction=='l')?-1:0;
			 deltaX = (eunit.direction=='r')?1:0;
			 boolean playerSpotted = senseBeam(eunit.tileX, eunit.tileY, deltaX, deltaY, VISUAL_RANGE);
			 //System.out.println("playerSpotted "+playerSpotted);
			 if(playerSpotted) {
				 eunit.state=ES_FOLLOW;
			 }
			
		}else if(eunit.state==ES_FOLLOW){
			int playerPos[] = this.game.player.getGridPosition();
			if(eunit.tileX==playerPos[0]&& eunit.tileY==playerPos[1]) {
				eunit.direction=ES_ATTACK;
			}
			
		}
		
		 
		
		
	}

	public void updatePosition(EntityUnit eunit) {

		eunit.tileY = eunit.worldY / Game.TILE_SIZE;
		eunit.tileX = eunit.worldX / Game.TILE_SIZE;
		if (game.entity.frozen) {
			return;
		}
		if (eunit.alive && (eunit.chasePlayer||eunit.state=='f')) {
			eunit.state = 'f';
			eunit.entityPlayerDistance = this.entityPlayerDistance(eunit);
			if(eunit.entityPlayerDistance>110) {
				this.setDirectionByPathFind(eunit);
			}else {
				//eunit.direction = 'n';
			}
			
			//eunit.direction=UP;

		} else if (!eunit.alive) {
			// update image
		}
		if (!moveOverlapsOtherEntityUnit(eunit)) {
			if(eunit.entityPlayerDistance>110) {
				moveDirection(eunit);
			}

			
		} else {
			eunit.state = 's';
		}
		cycleSprite(eunit);
		playerMeleeEnemy(eunit);
		entityCollidePlayer(eunit);

	}

	public void setGridData(int[][] data) {
		// update records list
		convertDataGridToRecordsList(data);
		// init eunit objects
		instantiateEntityObjectsFromRecordList();

	}

	public void initBlank() {
		// this.barrierRecords = new ArrayList<>();
		ArrayList<EntityUnit> outerList = new ArrayList<>();

		this.entityUnits = outerList;
		ArrayList<EntityUnit> el = new ArrayList<>();

		this.entityUnits = el;

	}

	public boolean moveOverlapsOtherEntityUnit(EntityUnit eunitA) {
		// stop enemies and NPCs from overlapping
		// eunitA is unit current unit being moved, eunitB is other unit
		eunitA.colliderTest.x = eunitA.worldX;
		eunitA.colliderTest.y = eunitA.worldY;

		switch (eunitA.direction) {
		case UP:

			eunitA.colliderTest.y -= eunitA.currentSpeed;
			break;
		case DOWN:

			eunitA.colliderTest.y += eunitA.currentSpeed;
			break;
		case LEFT:

			eunitA.colliderTest.x += -eunitA.currentSpeed;

			break;
		case RIGHT:

			eunitA.colliderTest.x += -eunitA.currentSpeed;

			break;
		default:
			eunitA.colliderTest.x = 0;
			eunitA.colliderTest.y = 0;
			break;

		}
		eunitA.colliderTest.x = eunitA.worldX;
		eunitA.colliderTest.y = eunitA.worldY;
		for (EntityUnit eunitB : game.entity.entityUnits) {
			if (eunitA.colliderTest.intersects(eunitB.collider) && !(eunitB == eunitA)) {

				return true;
			}
		}
		return false;
	}

	public int[] getEntityUnitWHFromKind(int kind) {
		return new int[] { 50, 50 };
	}

	public void setPositionToGridXY(int startGX, int startGY, int[] position) {
		position[0] = startGX * this.game.tilegrid.tileSize;
		position[1] = startGY * this.game.tilegrid.tileSize;

	}

	public void positionApplyVelocityXY(int velX, int velY, int[] position) {
		position[0] += velX;
		position[1] += velY;
	}

	public void setPositionToWorldXY(int startGX, int startGY, int[] position) {
		position[0] = startGX;
		position[1] = startGY;

	}

	public void paintAsset(int gridX, int gridY, int kind) {
		// create eunit object
		// check if coords in use
		// replace eunit in coords if it does, append if does not
		if (game.editor.delete) {

			int matchingIndex = getIndexEntityRecordWithMatchingGridCoord(gridX, gridY);
			if (matchingIndex == -1) {
				System.out.println("No eunit there");
			} else {
				System.out.println("Delete eunit type: " + entityUnits.get(matchingIndex));
				entityUnits.set(matchingIndex, null);
				entityUnits.set(matchingIndex, null);
			}
		}
		modified = true;
		int UID = getNewUID();
		EntityUnit eunit = new EntityUnit(this.game, gridX, gridY, kind, UID);
		EntityUnit eRecord = new EntityUnit(game, gridX, gridY, kind, UID);
		int matchingIndex = getIndexEntityRecordWithMatchingGridCoord(gridX, gridY);
		if (matchingIndex == -1) {
			entityUnits.add(eRecord);
			entityUnits.add(eunit);
		} else {
			entityUnits.set(matchingIndex, eRecord);
			entityUnits.set(matchingIndex, eunit);
		}

	}

	private boolean checkEntityRecordExistsAtGridCoord(int gridX, int gridY) {
		for (EntityUnit e : this.entityUnits) {
			if (e.startGX == gridX && e.startGY == gridY) {
				return true;
			}
		}
		return false;
	}

	private int getIndexEntityRecordWithMatchingGridCoord(int gridX, int gridY) {
		int Length = this.entityUnits.size();
		for (int i = 0; i < Length; i++) {
			EntityUnit e = this.entityUnits.get(i);
			if (e.startGX == gridX && e.startGY == gridY) {
				return i;
			}
		}
		// not found: return -1
		return -1;
	}

	private int getNewUID() {
		return NEW_ENTITY_DEFAULT_UID;
	}

	private int[][] getDataGridFromEntityRecordList() {
		LinkedList<int[]> outerList = new LinkedList<>();
		int length = 0;
		for (EntityUnit eunit : this.entityUnits) {
			if (null != eunit) {
				int[] recordAsArray = new int[] { eunit.startGX, eunit.startGY, eunit.kind, eunit.UID };
				outerList.add(recordAsArray);
				length += 1;
			}

		}
		int[][] output = new int[length][4];
		for (int i = 0; i < length; i++) {
			output[i] = outerList.get(i);
		}

		return output;

	}

	private void convertDataGridToRecordsList(int[][] dataGrid) {
		ArrayList<EntityUnit> outerList = new ArrayList<>();

		for (int[] arr : dataGrid) {
			if (null != arr) {
				EntityUnit eunit = new EntityUnit(game, arr[0], arr[1], arr[2], arr[3]);
				outerList.add(eunit);

			}

		}
		this.entityUnits = outerList;

	}

	private void instantiateEntityObjectsFromRecordList() {
		ArrayList<EntityUnit> outerList = new ArrayList<>();

		for (int[] eunitd : this.entityUnitData) {
			if (null != eunitd) {
				EntityUnit eunit = new EntityUnit(game, eunitd[EFGRIDX], eunitd[EFGRIDY], eunitd[EFKIND], eunitd[EFID]);
				outerList.add(eunit);

			}

		}
		this.entityUnits = outerList;

	}

	private void playerMeleeEnemy(EntityUnit eunit) {

		if (game.entity.playerMelee) {
			if (game.entity.playerCollider.intersects(eunit.collider)) {
				takeDamageFromPlayer(eunit, DEF_DAMAGE_FROM_PLAYER);
				game.particle.addParticle(eunit.worldX, eunit.worldY, 1);
			}
		}

	}

	public void drawSolidArea(Rectangle solidArea) {
		if (game.g == null)
			return;
		game.g.setColor(Color.black);
		int x = solidArea.x - game.cameraX;
		int y = solidArea.y - game.cameraY;
		int width = solidArea.width;
		int height = solidArea.height;
		game.g.drawRect(x, y, width, height);
	}

	public void entityCollidePlayer(EntityUnit eunit) {
		// drawSolidArea( game.player.wpActivateArea) ;
		if (eunit.collider.intersects(game.player.collider)) {

			if (eunit.enemy && eunit.alive) {
				// game.player.health -= ENEMY_DAMAGE;
				game.player.takeDamageFromEnemy(DEF_DAMAGE_FROM_PLAYER);
			} else if ( // player in range of NPC
			game.entity.entityActivateDalay.delayExpired() && !game.entity.entityTouchedList.contains(eunit)) {

				game.hud.showActionPromptDelay = 60;
				if (game.entity.activateEntityUnitFlag) {
					// player pressed activate
					game.entity.entityActivateDalay.setDelay(Entity.ENTITY_ACTIVATE_DELAY_TICKS);
					game.brain.playerActivateNPC(eunit, game.playerPressActivate);
					game.entity.playerTouchedActorSincelastTick = true;
					game.entity.entityTouchedList.add(eunit);
				}
				// System.out.println("player touch ent");

			}
		}
	}

	public void takeDamageFromPlayer(EntityUnit eunit, int damage) {
		int newHealth = eunit.health - damage;
		if (newHealth > 0) {
			eunit.health = newHealth;
		} else {
			eunit.health = 0;
		}
		if (eunit.health <= 0) {
			eunit.alive = false;
			eunit.state = 'd';
		}
	}
	
	public void cycleSpriteAttack(EntityUnit eunit) {
		int directionIndexpart = 0;
		switch (eunit.direction) {
		
		case UP:
			directionIndexpart = 0;
			break;
		case DOWN:
			directionIndexpart = 4;
			break;
		case LEFT:
			directionIndexpart = 8;
			break;
		case RIGHT:
			directionIndexpart = 12;
			break;
		default:
			directionIndexpart = 4;
		}
	}

	public void cycleSprite(EntityUnit eunit) {
//		if (eunit.state=='a') {
//			cycleSpriteAttack(eunit);
//			return;
//		}
		int directionIndexpart = 0;

		switch (eunit.direction) {
		case UP:
			directionIndexpart = 0;
			break;
		case DOWN:
			directionIndexpart = 4;
			break;
		case LEFT:
			directionIndexpart = 8;
			break;
		case RIGHT:
			directionIndexpart = 12;
			break;
		default:
			directionIndexpart = 4;
		}
		var changeFrame = animationPacer.check(); 
		if (eunit.state == 's' && changeFrame) {

			if (eunit.frame == 0) {
				eunit.frame = 2;
			} else {
				eunit.frame = 0;
			}

		} else if ((eunit.state == 'w' || eunit.state == 'f'||eunit.state == 'a') && changeFrame) {

			if (eunit.frame < 3) {
				eunit.frame++;
			} else {
				eunit.frame = 0;
			}

		} else if (eunit.state == 'd' || !eunit.alive) {
			eunit.frame = 0;
		}
		eunit.currentImageIndex = eunit.frame + directionIndexpart;

	}

	public void moveDirection(EntityUnit eunit) {
		
		if (eunit.state == 'w') {
			eunit.currentSpeed = SPEED_WALK;
		} else if(eunit.state == 'f') {
			eunit.currentSpeed = SPEED_CHASE;
		}else {
			eunit.currentSpeed = 0;
		}
		//eunit.worldX = 0;
		//eunit.worldY = 0;
		switch (eunit.direction) {
		case 'n':
			break;
		case UP:
			eunit.worldY += -eunit.currentSpeed;

			break;
		case DOWN:
			eunit.worldY += eunit.currentSpeed;
			break;
		case LEFT:
			eunit.worldX += -eunit.currentSpeed;
			break;
		case RIGHT:
			eunit.worldX += eunit.currentSpeed;
			break;
		default:
//			eunit.worldY = 0;
//			eunit.worldY = 0;
			break;

		}
	}

	public boolean inbounds(EntityUnit eunit) {

		if (eunit.worldX >= 0 && eunit.worldX + eunit.width < game.worldSizePxX && eunit.worldY >= 0
				&& eunit.worldY + eunit.height < game.worldSizePxY) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * returns true if character bumped the main border
	 * 
	 * @return
	 */
	public boolean borderBump(EntityUnit eunit) {

		int velXLocal = 0;
		int velYLocal = 0;

		if (eunit.worldX < 0) {
			velXLocal = eunit.currentSpeed;

		} else if (eunit.worldX + eunit.width >= game.worldSizePxX) {
			velXLocal = -eunit.currentSpeed;

		} else if (eunit.worldY < 0) {
			velYLocal = eunit.currentSpeed;

		} else if (eunit.worldY + eunit.height >= game.worldSizePxY) {
			velYLocal = -eunit.currentSpeed;

		} else {
			return false;
		}

		eunit.worldX += velXLocal;
		eunit.worldY += velYLocal;

		return true;
	}

	/**
	 * true if a tile is blocking the path ahead
	 * 
	 * @return
	 */
	public boolean tileAhead(EntityUnit eunit) {
		return game.collision.collideTileRectDirection(eunit.colliderTest, eunit.direction);
	}

	private void setDirectionByPathFind(EntityUnit eunit) {

		//Point worldPoint = new Point(eunit.worldX, eunit.worldY);
		eunit.direction = game.pathfind.getDirectionTowardsPlayer(eunit.worldX, eunit.worldY);
		int[] playerLoc = this.game.player.getGridPosition();
		
		if(playerLoc[1]==eunit.worldY-1) {
			eunit.direction='u';
		}else if(playerLoc[1]==eunit.worldY+1){
			eunit.direction='d';
		}else if(playerLoc[0]==eunit.worldX-1){
			eunit.direction='l';
		}else if(playerLoc[0]==eunit.worldY+1){
			eunit.direction='r';
		}else if(playerLoc[0]==eunit.worldX && playerLoc[1]==eunit.worldY ) {
			eunit.direction='n';
		}

	}

	private void updateColliderUnit(EntityUnit eunit) {
		eunit.collider.x = eunit.worldX;
		eunit.collider.y = eunit.worldY;
		eunit.collider.width = eunit.width;
		eunit.collider.height = eunit.height;

	}

}
