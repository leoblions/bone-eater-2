package main;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import main.Game.GState;

/**
 * Entity records 0=gx,1=gy,2=kind,3=uid Each room has its own records csv file,
 * that's loaded on entry. CSV data is converted into LOIA, which is used to
 * instantiate EntityUnits Edits apply to LOIA and entityUnits data is saved
 * from LOIA
 * 
 */
public class Entity {

	Pacer animationPacer, attackPacer, updateEntityTilePacer;
	Rectangle hitbox;
	Rectangle senseRectangle;
	final int PFWORLDX = 0;
	final int PFWORLDY = 1;
	final int PFWORLDW = 2;
	final int PFWORLDH = 3;
	final int WALL_COLLIDE_WIGGLEROOM = 10;
	final int ENTITY_FOLLOW_PLAYER_MIN_DISTANCE = 40;

	final char DOWN = 'd';
	final char UP = 'u';
	final char LEFT = 'l';
	final char RIGHT = 'r';
	final char NONE = 'n';
	
	final int m = 0;
	final int u = 1;
	final int d = 2;
	final int l = 3;
	final int r = 4;

	public static final char ES_FOLLOW = 'f';
	public static final char ES_FLEE = 'l';
	public static final char ES_STAND = 's';
	public static final char ES_WANDER = 'w';
	public static final char ES_WALK = 'w';
	public static final char ES_DEAD = 'd';
	public static final char ES_ATTACK = 'a';

	final int VISUAL_RANGE = 5; // how far entities can see
	final int[][] blockOffset = {
			{0,0},//middle
			{0,-1},//up x,y
			{0,1},//down
			{-1,0},//left
			{1,0}//right
	};
	
	final int[][] blockOffsetLeft = {
			{0,0},//middle
			{-1,-1},//up x,y
			{1,1},//down
			{-1,-1},//left
			{1,1}//right
	};
	
	final int[][] blockOffsetRight = {
			{0,0},//middle
			{1,-1},//up x,y
			{-1,1},//down
			{-1,1},//left
			{1,-1}//right
	};

	final int ENTITY_PLAYER_COLLIDE_RANGE = 40;
	final boolean CHECK_COLLISIONS = false;
	
	
	final int EK_GUARD = 0;
	final int EK_PEASANT = 1;
	final int EK_WOMAN = 2;
	final int EK_ZOMBIEF = 3;

	final boolean DRAW_COLLRECT = true;

	final int SPEED_WALK = 1;
	final int SPEED_CHASE = 5;
	final int TICKS_PER_FRAME = 10;
	final int ATTACK_HIT_TICK_PERIOD = 10;
	private final int HITBOX_SIZE = 70;
	private final int HITBOX_SIZE_INC = 50;
	private final int HITBOX_OFFSET = -50;

	public static final int NEW_ENTITY_DEFAULT_UID = 0;
	public static final int ENTITY_ACTIVATE_DELAY_TICKS = 120;
	public static final int ENTITY_ATTACK_DISTANCE = 5; // tiles
	public static final int EP_DISTANCE_START_PATHFIND = 500;
	public static final int EP_DISTANCE_START_BEELINE = 100;
	public static final int ENTITY_GIVEUP_DISTANCE = 25; // tiles
	private static final String DATA_FILE_PREFIX = "entity";
	private static final String DATA_FILE_SUFFIX = ".csv";
	final int ATTACK_FRAMES = 4;
	private final int DEF_DAMAGE_FROM_PLAYER = 5;
	final int IMAGES_PER_ENTITY = 16;
	private BufferedImage[][] entityImages;
	private BufferedImage[][] attackImages;
	private BufferedImage[][] deathImages;
	private BufferedImage[] shadows;
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
	public boolean playerMelee = false;
	private boolean attackOnThisTick = false;
	public boolean frozen = false;
	public boolean drawHitbox = true;
	public boolean activateEntityUnitFlag;
	

	public Entity(Game game) {
		this.game = game;
		entityUnits = new ArrayList<>();
		entityTouchedList = new ArrayList<>();
		this.hitbox = new Rectangle(10, 10,HITBOX_SIZE, HITBOX_SIZE);

		// this.addEntity(5, 5, 0, 0);
		entityActivateDalay = new Delay();
		animationPacer = new Pacer(TICKS_PER_FRAME);
		attackPacer = new Pacer(ATTACK_HIT_TICK_PERIOD);
		updateEntityTilePacer = new Pacer(ATTACK_HIT_TICK_PERIOD);
		this.initImages();

		playerCollider = new Rectangle();
		senseRectangle = new Rectangle();
		
		//startLevel(this.game.level);

		
	}
	
	public void startLevel(int level) {
		this.entityUnits.clear();
		this.addEntityGrid(5, 5, 1, 's', 1);
		this.addEntityGrid(10, 4, 2, 'w', 1);
		this.addEntityGrid(24, 8, 0, 'w', 1);
		this.addEntityWorld(1800, 810, 0, 'w', 1);
		this.addEntityWorld(240, 1200, 0, 'w', 1);
		this.addEntityWorld(2100, 1700, 0, 'w', 1);
		this.addEntityWorld(1400, 2300, 0, 'w', 1);
	}

	private void initImages() {
		this.entityImages = new BufferedImage[ENTITY_KINDS][IMAGES_PER_ENTITY];
		this.attackImages = new BufferedImage[ENTITY_KINDS][IMAGES_PER_ENTITY];
		this.deathImages = new BufferedImage[ENTITY_KINDS][IMAGES_PER_ENTITY];
		this.shadows = new BufferedImage[2];
		BufferedImage[] tempBI, tempBIL, tempBIR = null;
		String URL = null;
		try {
			// guard
			
			URL = "/images/guard_100_200w.png";
			// tempBI = new Imageutils(game).spriteSheetCutter(URL, 4, 4, 100, 200);
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 100, 200);
			this.entityImages[EK_GUARD] = tempBI;

			URL = "/images/guard_100_200a.png";
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 200, 200);

			this.attackImages[EK_GUARD] = tempBI;

			URL = "/images/guard_gr_fall.png";
			tempBI = this.game.imageutils.spriteSheetCutter(URL, 2, 4, 300, 200);

			this.deathImages[Kind.GUARD] = tempBI;
			
			// peasant
			
			URL = "/images/peasant_100_200w.png";
			// tempBI = new Imageutils(game).spriteSheetCutter(URL, 4, 4, 100, 200);
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 100, 200);
			this.entityImages[Kind.PEASANT] = tempBI;

			URL = "/images/peasant_100_200a.png";
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 100, 200);

			this.attackImages[Kind.PEASANT] = tempBI;

			URL = "/images/guard_gr_fall.png";
			tempBI = this.game.imageutils.spriteSheetCutter(URL, 2, 4, 300, 200);

			this.deathImages[Kind.PEASANT] = tempBI;
			
			// woman
			
			URL = "/images/woman_100_200w.png";
			// tempBI = new Imageutils(game).spriteSheetCutter(URL, 4, 4, 100, 200);
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 100, 200);
			this.entityImages[Kind.WOMAN] = tempBI;

			URL = "/images/woman_100_200a.png";
			tempBI = this.game.imageutils.characterSheetUDL4(URL, 100, 200);

			this.attackImages[Kind.WOMAN] = tempBI;

			URL = "/images/guard_gr_fall.png";
			tempBI = this.game.imageutils.spriteSheetCutter(URL, 2, 4, 300, 200);

			this.deathImages[Kind.WOMAN] = tempBI;
			
			
			
			
			
			// shadows

			URL = "/images/shadow.png";
			tempBI = this.game.imageutils.spriteSheetCutter(URL, 1, 1, 100, 100);

			this.shadows = tempBI;

		} catch (Exception e) {
			System.err.println("EntityUnit Failed to open the resource: " + URL);
			e.printStackTrace();
		}

	}

	private boolean entityCheckChangedTile(EntityUnit eunit) {
		if (eunit.x == eunit.lastTile[0] && eunit.y == eunit.lastTile[1]) {
			eunit.tileChanged = true;
			eunit.lastTile[0] = eunit.x;
			eunit.lastTile[1] = eunit.y;
			return true;

		} else {
			eunit.tileChanged = false;
			return false;
		}
	}

	public int entityPlayerDistance(EntityUnit eunit) {
		double deltaX = this.game.player.x - eunit.x;
		double deltaY = this.game.player.y - eunit.y;
		double Asquared = Math.pow(deltaX, 2);
		double Bsquared = Math.pow(deltaY, 2);
		double hypotenuse = Math.sqrt(Asquared + Bsquared);
		return (int) hypotenuse;
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

	public void addEntityGrid(int startGX, int startGY, int kind, char state, int UID) {
		EntityUnit eunit = new EntityUnit(this.game, startGX, startGY, kind, UID);
		eunit.state = state;
		this.entityUnits.add(eunit);
	}
	
	public void addEntityWorld(int startX, int startY, int kind, char state, int UID) {
		
		EntityUnit eunit = new EntityUnit(this.game, startX / Game.TILE_SIZE, startY / Game.TILE_SIZE, kind, UID);
		eunit.x = startX;
		eunit.y = startY;
		eunit.state = state;
		this.entityUnits.add(eunit);
	}

	public void draw() {
		if (drawHitbox) {

			game.g.setColor(Color.red);
//			game.g.drawRect(playerCollider.x - game.cameraX, playerCollider.y - game.cameraY, playerCollider.width,
//					playerCollider.height);

			game.g.drawRect(hitbox.x - game.cameraX, hitbox.y - game.cameraY, hitbox.width, hitbox.height);
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
			game.g.drawRect(eunit.x - game.cameraX, eunit.y - game.cameraY, eunit.width, eunit.height);
		eunit.screenX = (eunit.x - game.cameraX);
		eunit.screenY = (eunit.y - game.cameraY);

		game.g.drawImage(shadows[0], eunit.screenX + eunit.offsetX,

				eunit.screenY + eunit.offsetY, eunit.width, eunit.height, null);

		game.g.drawImage(eunit.image, eunit.screenX + eunit.offsetX,

				eunit.screenY + eunit.offsetY, eunit.width, eunit.height, null);

	}

	private void updateFrozenState() {

		if (game.gameState != GState.PLAY) {
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
		attackOnThisTick = attackPacer.check();
		boolean updateEunitTile = updateEntityTilePacer.check();
		for (int i = 0; i < entityUnits.size(); i++) {
			EntityUnit eunit = entityUnits.get(i);
			if (null != eunit) {
				if(updateEunitTile) {
					eunit.tileX = eunit.x / Game.TILE_SIZE;
					eunit.tileY = eunit.y / Game.TILE_SIZE;
				}
				entityCheckChangedTile(eunit);
				updateState(eunit);
				updatePosition(eunit);
				selectUnitImage(eunit);
				// updateColliderUnit(eunit);
				if (attackOnThisTick && eunit.state == ES_ATTACK) {
					if (checkEnemyAttackHitPlayer(eunit)) {

						this.game.player.takeDamageFromEnemy(eunit.damageToPlayer);
						eunit.damagePlayerOnTouch = true;
						attackOnThisTick = false;
					}
				} else {
					eunit.damagePlayerOnTouch = false;
				}
				// System.out.println("Entity state "+eunit.state);

			}

		}
		if (playerTouchedActorSincelastTick == false) {
			entityTouchedList.clear();
			activateEntityFlag = false;
		}
		playerMelee = false;

	}

	private void selectUnitImage(EntityUnit eunit) {
		// assigns image to entity unit
		switch (eunit.state) {
		case ES_ATTACK:

			eunit.image = this.attackImages[eunit.kind][eunit.currentImageIndex];
			break;
		case ES_WALK:
			eunit.image = this.entityImages[eunit.kind][eunit.currentImageIndex];
			break;
		case ES_DEAD:
			if (eunit.currentImageIndex >= 8) {
				eunit.currentImageIndex = 0;
			}
			eunit.image = this.deathImages[eunit.kind][eunit.currentImageIndex];
			break;
		default:

			eunit.image = this.entityImages[eunit.kind][eunit.currentImageIndex];
		}

	}

	/**
	 * 
	 * @param startGridX grid position of entity X
	 * @param startGridY grid position of entity Y
	 * @param deltaX     change of X with each iteration
	 * @param deltaY     change of Y with each iteration
	 * @param iterations distance of entity's vision
	 * @return
	 */
	private boolean detectPlayer(EntityUnit eunit, int deltaX, int deltaY, int iterations) {
		// allows entity to see player in a given direction
		// true if player spotted
		int currGridX = eunit.tileX ;
		int currGridY = eunit.tileY ;
		int playerPos[] = this.game.player.getGridPosition();
		boolean isWall, isWallL, isWallR = false;
		boolean isPlayer, isPlayerR, isPlayerL = false;
		int tileLX,tileLY,tileRX,tileRY;
		//String dbgString = "";
		//System.out.println("Player "+playerPos[0] + " "+playerPos[1]);
		FWD_SENSE:for (int i = 0; i < iterations; i++) {
			//dbgString += String.format("x:%dy:%d  ", currGridX,currGridY);
			try {
				isWall = this.game.pathfind.wallgrid[currGridY][currGridX];
			} catch (Exception e) {
				isWall = true;
			}
			try {
				this.game.g.setColor(Color.cyan);
				this.game.g.fillRect(currGridX*this.game.TILE_SIZE-this.game.cameraX, currGridY*this.game.TILE_SIZE-this.game.cameraY, 3, 3);
			}catch (Exception e) {};
			
			
			isPlayer = (playerPos[0] == currGridX && playerPos[1] == currGridY);
			
			if (isPlayer && !isWall) {
				//System.out.println(dbgString);
				return true;
			} else if (!isPlayer && isWall) {
				//System.out.println(dbgString);
				break FWD_SENSE;
			}
			currGridX += deltaX;
			currGridY += deltaY;

		}
		switch(eunit.direction) {
		case 'u':
			tileLX = eunit.tileX -1;
			tileLY = eunit.tileY -1;
			tileRX = eunit.tileX +1;
			tileRY = eunit.tileY -1;
			break;
		case 'd':
			tileLX = eunit.tileX +1;
			tileLY = eunit.tileY +1;
			tileRX = eunit.tileX -1;
			tileRY = eunit.tileY +1;
			break;
		case 'l':
			tileLX = eunit.tileX -1;
			tileLY = eunit.tileY +1;
			tileRX = eunit.tileX -1;
			tileRY = eunit.tileY -1;
			break;
		case 'r':
			tileLX = eunit.tileX +1;
			tileLY = eunit.tileY -1;
			tileRX = eunit.tileX +1;
			tileRY = eunit.tileY +1;
			break;
		case 'n':
		default:
			return false;
		}
		try {
			isWallL = this.game.pathfind.wallgrid[currGridY][currGridX];
			isWallR = this.game.pathfind.wallgrid[currGridY][currGridX];
			isPlayerL = (playerPos[0] == tileLX && playerPos[1] == tileLY);
			isPlayerR = (playerPos[0] == tileRX && playerPos[1] == tileRY);
			
		}catch(Exception e) {
			isWallL = true;
			isWallR = true;
			isPlayerL = false;
			isPlayerR = false;
			
			
		}
		if((!isWallL&&isPlayerL)||(!isWallR&&isPlayerR)) {
			return true;
		}
		//System.out.println(dbgString);
		return false;

	}
	
	private boolean detectPlayerBeam(int startX, int startY, int range) {
		
		// true if unobstructed line between point and player.
		int iterations = 10;
		int deltaPEWX = ( this.game.player.x-startX);
		int deltaPEWY = ( this.game.player.y-startY);
		int deltaWX,deltaWY;
		if (deltaPEWX==0) {
			deltaPEWX = -1;
		}
		if (deltaPEWY==0) {
			deltaPEWY = -1;
		}
		if(deltaPEWX>deltaPEWY) {
			deltaWX = this.game.COLL_GRID_SIZE;
			deltaWY = deltaPEWY / deltaPEWX;
		}else {
			deltaWY = this.game.COLL_GRID_SIZE;
			deltaWX = deltaPEWX / deltaPEWY;
		}
		
		int currGridX = startX/ Game.COLL_GRID_SIZE;
		int currGridY = startY/ Game.COLL_GRID_SIZE;
		int currWorldX = startX + deltaWX;
		int currWorldY = startY + deltaWY;
		boolean isWall = false;
		boolean isPlayer = false;
		int startGridX = startX / Game.COLL_GRID_SIZE;
		int startGridY = startY / Game.COLL_GRID_SIZE;
		
		for (int i = 0; i < iterations; i++) {
			currGridX = currWorldX/ Game.COLL_GRID_SIZE;
			currGridY = currWorldY/ Game.COLL_GRID_SIZE;
			if(null!=this.game.g) {
				this.game.g.fillRect(currGridX, currGridY, 3, 3);
			}
			
			try {
				isWall = this.game.pathfind.wallgrid[currGridY][currGridX];
			} catch (Exception e) {
				isWall = true;
			}
			
			int playerGridPos[] = this.game.player.getGridPosition();
			
			isPlayer = (playerGridPos[0] == currGridX && playerGridPos[1] == currGridY);
			
			if (isPlayer && !isWall) {
				return true;
			} else if (!isPlayer && isWall) {
				return false;
			}
			currWorldX += deltaWX;
			currWorldY += deltaWY;

		}
		return false;

	}

	private void updateState(EntityUnit eunit) {
		int deltaX = 0;
		int deltaY = 0;
		int playerPos[] = null;
		int dX, dY;
		senseRectangle.width = eunit.width;
		senseRectangle.height= eunit.height;
		senseRectangle.x = eunit.x;
		senseRectangle.y=eunit.y;
		int SENSE_RECT_OFFSET = 25;
		
		if (eunit.health <= 0 && eunit.state != ES_DEAD) {
			eunit.state = ES_DEAD;
			eunit.currentImageIndex = 0;
			game.sound.playSE(Sound.S_DIE);
			game.brain.killedEnemy();
		}
		switch (eunit.state) {
		case ES_WANDER:
		case ES_STAND:
			
			// pick direction to use for beam and animation
			switch(eunit.direction) {
			case 'u':
				deltaX = 0;
				deltaY = -1;
				senseRectangle.x -= 0;
				senseRectangle.y -= SENSE_RECT_OFFSET;
				break;
			case 'd':
				deltaX = 0;
				deltaY = 1;
				senseRectangle.x -= 0;
				senseRectangle.y += SENSE_RECT_OFFSET;
				break;
			case 'l':
				deltaX = -1;
				deltaY = 0;
				senseRectangle.x -= SENSE_RECT_OFFSET;
				senseRectangle.y -= 0;
				break;
			case 'r':
				deltaX = 1;
				deltaY = 0;
				senseRectangle.x += SENSE_RECT_OFFSET;
				senseRectangle.y -= 0;
				break;
			}
//			deltaY = (eunit.direction == 'd') ? 1 : 0;
//			deltaY = (eunit.direction == 'u') ? -1 : 0;
//			deltaX = (eunit.direction == 'l') ? -1 : 0;
//			deltaX = (eunit.direction == 'r') ? 1 : 0;
			if(eunit.epDistance<EP_DISTANCE_START_PATHFIND) {
				//boolean playerSpotted = detectPlayerBeam(eunit.x, eunit.y, VISUAL_RANGE);
				
				boolean touchPlayer = senseRectangle.intersects(this.game.player);
				boolean playerSpotted = touchPlayer || detectPlayer(eunit, deltaX, deltaY, VISUAL_RANGE);
				if (playerSpotted) {
					//System.out.println("playerSpotted "+playerSpotted);
					eunit.state = ES_FOLLOW;
					//System.out.println("playerSpotted "+playerSpotted);
					this.game.sound.playSE(Sound.S_HEY);
				}
			}else {
				//System.out.println("too far");
			}
			
			
			// System.out.println("playerSpotted "+playerSpotted);
			
			break;
		case ES_FOLLOW:

			eunit.width = eunit.widthS;
			playerPos = this.game.player.getGridPosition();
			dX = Math.abs(eunit.tileX - playerPos[0]);
			dY = Math.abs(eunit.tileY - playerPos[1]);

			if (dX < ENTITY_ATTACK_DISTANCE && dY < ENTITY_ATTACK_DISTANCE) {
				eunit.state = ES_ATTACK;
				this.game.sound.playSE(Sound.S_SLASH);
			} else if (dX > ENTITY_GIVEUP_DISTANCE || dY > ENTITY_GIVEUP_DISTANCE) {
				eunit.state = ES_WANDER;
				this.game.sound.playSE(Sound.S_SLASH);
				// System.out.printf("player %d, %d entity: %d, %d
				// \n",playerPos[0],playerPos[1],eunit.tileX,eunit.tileY);
			}
			break;
		case ES_ATTACK:
			eunit.width = eunit.widthA;
			playerPos = this.game.player.getGridPosition();
			dX = Math.abs(eunit.tileX - playerPos[0]);
			dY = Math.abs(eunit.tileY - playerPos[1]);
			// String st = "attack "+dX+ " "+ dY;
			// System.out.println(st);
			if (dX > ENTITY_ATTACK_DISTANCE || dY > ENTITY_ATTACK_DISTANCE) {
				eunit.state = ES_FOLLOW;
				// System.out.printf("player %d, %d entity: %d, %d
				// \n",playerPos[0],playerPos[1],eunit.tileX,eunit.tileY);
			}

			break;
		case ES_DEAD:
			eunit.width = eunit.widthA;
			break;

		}

	}



	private int[] attackMotion(EntityUnit eunit) {
		int[] deltaXY4 = { 0, 0 };
		int BOUNCE = 0;
		eunit.entityPlayerDistance = this.entityPlayerDistance(eunit);
		if (eunit.entityPlayerDistance >= EP_DISTANCE_START_PATHFIND) {
			// don't move
		} else if (eunit.entityPlayerDistance >= EP_DISTANCE_START_BEELINE) {
			this.setDirectionByPathFind(eunit);
			deltaXY4 = calculateMoveFromDirection4W(eunit);
			int testX = eunit.x + deltaXY4[0];
			int testY = eunit.y + deltaXY4[1];
			// boolean moveCollideWall = moveEntityCollideWall(eunit, testX, testY);

			if (CHECK_COLLISIONS) {
				boolean[] collisions = this.game.collision.collideTileTestWXY(testX, testY, eunit.width, eunit.height);
				if (collisions[0] && deltaXY4[1] < 0) {
					deltaXY4[1] = BOUNCE;

				}
				if (collisions[1] && deltaXY4[1] > 0) {
					deltaXY4[1] = -BOUNCE;

				}
				if (collisions[2] && deltaXY4[0] < 0) {
					deltaXY4[0] = BOUNCE;

				}
				if (collisions[3] && deltaXY4[0] > 0) {
					deltaXY4[0] = -BOUNCE;

				}
			}
		}
		return deltaXY4;

	}

	private int[] wanderMotion(EntityUnit eunit) {
		int[] deltaXY4 = { 0, 0 };
		int BOUNCE = 0;
		setDirectionByCollision(eunit);

		deltaXY4 = calculateMoveFromDirection4W(eunit);
		int testX = eunit.x + deltaXY4[0];
		int testY = eunit.y + deltaXY4[1];

		if (CHECK_COLLISIONS) {
			boolean[] collisions = this.game.collision.collideTileTestWXY(testX, testY, eunit.width, eunit.height);
			if (collisions[0] && deltaXY4[1] < 0) {
				deltaXY4[1] = BOUNCE;

			}
			if (collisions[1] && deltaXY4[1] > 0) {
				deltaXY4[1] = -BOUNCE;

			}
			if (collisions[2] && deltaXY4[0] < 0) {
				deltaXY4[0] = BOUNCE;

			}
			if (collisions[3] && deltaXY4[0] > 0) {
				deltaXY4[0] = -BOUNCE;

			}
		}
		return deltaXY4;

	}

	public void updatePosition(EntityUnit eunit) {
		int[] deltaXY4 = { 0, 0 };
		eunit.epDistance = entityPlayerDistance(eunit);

		eunit.tileY = eunit.y / Game.TILE_SIZE;
		eunit.tileX = eunit.x / Game.TILE_SIZE;
		if (game.entity.frozen) {
			return;
		}
		if (eunit.alive == false) {
			eunit.state = ES_DEAD;
		}
		switch (eunit.state) {
		case ES_ATTACK:
			deltaXY4 = attackMotion(eunit);
			break;
		case ES_WANDER:
			deltaXY4 = wanderMotion(eunit);
			break;
		case ES_FOLLOW:
			deltaXY4 = wanderMotion(eunit);
			break;

		}

		if (eunit.alive) {

		} else if (eunit.entityPlayerDistance < EP_DISTANCE_START_BEELINE) {
			int[] dir2W = this.setDirectionByBeeline(eunit);
			deltaXY4 = this.calculateMoveBeeline(eunit, dir2W);
			// eunit.direction = 'n';
		}

		// eunit.direction=UP;

		// int[] deltaXY8 = calculateMoveFromDirection8W(eunit);

		boolean collideOtherUnit = moveOverlapsOtherEntityUnit(eunit);

		if (!collideOtherUnit) {
			if (eunit.entityPlayerDistance > ENTITY_FOLLOW_PLAYER_MIN_DISTANCE) {
				// stop entity from pacing if too close to player
				// System.out.println("moving "+deltaXY[0]+ " " + deltaXY[1]);
				moveEntityFromDeltas(eunit, deltaXY4[0], deltaXY4[1]);

			}

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

	private boolean moveOverlapsOtherEntityUnit(EntityUnit eunitA) {
		// stop enemies and NPCs from overlapping
		// eunitA is unit current unit being moved, eunitB is other unit
		eunitA.colliderTest.x = eunitA.x;
		eunitA.colliderTest.y = eunitA.y;
		

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
		eunitA.colliderTest.x = eunitA.x;
		eunitA.colliderTest.y = eunitA.y;
		for (EntityUnit eunitB : game.entity.entityUnits) {
			if (eunitB.state!=ES_DEAD&&eunitA.colliderTest.intersects(eunitB) && !(eunitB == eunitA) ) {

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
			if (game.entity.playerCollider.intersects(eunit)) {
				takeDamageFromPlayer(eunit, DEF_DAMAGE_FROM_PLAYER);
				game.particle.addParticle(eunit.x, eunit.y, 1);
			}
		}

	}

	private void drawSolidArea(Rectangle solidArea) {
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
		if (eunit.intersects(game.player)) {

			if (eunit.enemy && eunit.alive && eunit.damagePlayerOnTouch) {
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
			eunit.frame = 0;
		}
	}


	private void cycleSprite(EntityUnit eunit) {
 
		int directionIndexpart = 0;

		switch (eunit.direction) {
		case 'n':
			directionIndexpart = 4;
			break;
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
		if (!eunit.alive && eunit.state != ES_DEAD) {
			eunit.state = ES_DEAD;
			eunit.currentImageIndex = 0;
		}
		if (changeFrame) {
			switch (eunit.state) {
			case ES_STAND:
				if (eunit.frame == 0) {
					eunit.frame = 2;
				} else {
					eunit.frame = 0;
				}
				eunit.currentImageIndex = eunit.frame + directionIndexpart;
				break;
			case ES_WALK:
			case ES_FOLLOW:
			case ES_ATTACK:
				if (eunit.frame < 3) {
					eunit.frame++;
				} else {
					eunit.frame = 0;
				}
				eunit.currentImageIndex = eunit.frame + directionIndexpart;
				break;
			case ES_DEAD:
				if (eunit.frame < eunit.frameMax) {
					eunit.frame++;
					eunit.currentImageIndex = eunit.frame;
				}
				break;
			default:
				eunit.frame = 0;
				break;

			}
		}

	}

	private boolean checkEnemyAttackHitPlayer(EntityUnit eunit) {
		int hitX = eunit.x + eunit.width / 2;
		int hitY = eunit.y + eunit.height / 2;

		switch (eunit.direction) {

		case UP:
			hitY -= eunit.height;
			break;
		case DOWN:
			hitY += eunit.height;
			break;
		case LEFT:
			hitX -= eunit.width;
			break;
		case RIGHT:
			hitX += eunit.width;
			break;
		default:
		}
		this.hitbox.x = hitX +HITBOX_OFFSET;
		this.hitbox.y = hitY +HITBOX_OFFSET;
		if (this.game.player.pointCollidePlayer(hitX, hitY)) {
			return true;

		} else {
			return false;
		}
	}

	private void moveEntityFromDeltas(EntityUnit eunit, int deltaX, int deltaY) {
		eunit.x += deltaX;
		eunit.y += deltaY;
	}



	private int[] calculateMoveFromDirection4W(EntityUnit eunit) {
		int[] deltaXY = { 0, 0 };
		switch (eunit.state) {
		case ES_WANDER:

			eunit.currentSpeed = SPEED_WALK;
			break;
		case ES_ATTACK:
		case ES_FOLLOW:
			eunit.currentSpeed = SPEED_CHASE;
			break;
		default:
			eunit.currentSpeed = 0;
		}

		switch (eunit.direction) {
		case 'n':
			break;
		case UP:
			deltaXY[1] += -eunit.currentSpeed;

			break;
		case DOWN:
			deltaXY[1] += eunit.currentSpeed;
			break;
		case LEFT:
			deltaXY[0] += -eunit.currentSpeed;
			break;
		case RIGHT:
			deltaXY[0] += eunit.currentSpeed;
			break;
		default:
			break;

		}
		return deltaXY;
	}

	private int[] calculateMoveBeeline(EntityUnit eunit, int[] direction2W) {
		int[] deltaXY = { 0, 0 };
		switch (eunit.state) {
		case ES_WANDER:
			eunit.currentSpeed = SPEED_WALK;
			break;
		case ES_ATTACK:
		case ES_FOLLOW:
			eunit.currentSpeed = SPEED_CHASE;
			break;
		default:
			eunit.currentSpeed = 0;
		}

		int halfSpeed = eunit.currentSpeed / 2;
		if (direction2W[0] == 0) {
			deltaXY[1] = direction2W[1] * halfSpeed;
		} else if (direction2W[1] == 0) {
			deltaXY[0] = direction2W[0] * halfSpeed;
		}
		deltaXY[0] += direction2W[0] * halfSpeed;
		deltaXY[1] += direction2W[1] * halfSpeed;

		return deltaXY;
	}

//	private int[] calculateMoveFromDirection8W(EntityUnit eunit) {
//		// returns change in X and change in Y for entity in World Coords
//		// does not move the entity
//
//		/**
//		 * 0 top 1 trc 2 right 3 brc 4 bottom 5 blc 6 left 7 tlc 8 neutral
//		 */
//		int[] deltaXY = { 0, 0 };
//
//		if (eunit.state == 'w') {
//			eunit.currentSpeed = SPEED_WALK;
//		} else if (eunit.state == 'f') {
//			eunit.currentSpeed = SPEED_CHASE;
//		} else {
//			eunit.currentSpeed = 0;
//		}
//		// eunit.worldX = 0;
//		// eunit.worldY = 0;
//		// System.out.println("speed "+ eunit.currentSpeed);
//		switch (eunit.direction8w) {
//		case 8:
//			break;
//		case 0:
//			deltaXY[0] += -eunit.currentSpeed;
//
//			break;
//		case 1:
//			deltaXY[0] += eunit.currentSpeed;
//			deltaXY[1] += -eunit.currentSpeed;
//			break;
//		case 2:
//			deltaXY[0] += eunit.currentSpeed;
//			break;
//		case 3:
//			deltaXY[0] += eunit.currentSpeed;
//			deltaXY[1] += eunit.currentSpeed;
//			break;
//		case 4:
//			deltaXY[1] += eunit.currentSpeed;
//
//			break;
//		case 5:
//			deltaXY[1] += eunit.currentSpeed;
//			deltaXY[0] += -eunit.currentSpeed;
//			break;
//		case 6:
//			deltaXY[0] += -eunit.currentSpeed;
//			break;
//		case 7:
//			deltaXY[0] += -eunit.currentSpeed;
//			deltaXY[1] += -eunit.currentSpeed;
//			break;
//		default:
////			eunit.worldY = 0;
////			eunit.worldY = 0;
//			break;
//
//		}
//		// System.out.println("d8w "+eunit.direction8w);
//		return deltaXY;
//	}
//
//	private boolean inbounds(EntityUnit eunit) {
//
//		if (eunit.x >= 0 && eunit.x + eunit.width < game.worldSizePxX && eunit.y >= 0
//				&& eunit.y + eunit.height < game.worldSizePxY) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	/**
	 * returns true if character bumped the main border
	 * 
	 * @return
	 */
//	private boolean borderBump(EntityUnit eunit) {
//
//		int velXLocal = 0;
//		int velYLocal = 0;
//
//		if (eunit.x < 0) {
//			velXLocal = eunit.currentSpeed;
//
//		} else if (eunit.x + eunit.width >= game.worldSizePxX) {
//			velXLocal = -eunit.currentSpeed;
//
//		} else if (eunit.y < 0) {
//			velYLocal = eunit.currentSpeed;
//
//		} else if (eunit.y + eunit.height >= game.worldSizePxY) {
//			velYLocal = -eunit.currentSpeed;
//
//		} else {
//			return false;
//		}
//
//		eunit.x += velXLocal;
//		eunit.y += velYLocal;
//
//		return true;
//	}

	private void setDirectionByPathFind(EntityUnit eunit) {

		eunit.direction = game.pathfind.getDirectionTowardsPlayer(eunit);

	}

	private void setDirectionByCollision(EntityUnit eunit) {
		int centerX = eunit.x + eunit.halfWidth;
		int centerY = eunit.y + eunit.halfHeight;

		int collGridValue = game.collision.getGridValueXY(centerX, centerY);
		switch (collGridValue) {
		case 0:
			// do nothing, continue same direction
			break;
		case -1:
		case -2:
			// stop
			eunit.direction = NONE;
		case 1:
			switch (eunit.direction) {
			case UP:
				eunit.direction = DOWN;
				break;
			case DOWN:
				eunit.direction = UP;
				break;
			case LEFT:
				eunit.direction = RIGHT;
				break;
			case RIGHT:
				eunit.direction = LEFT;
				break;

			}
			break;
		case 2:
			eunit.direction = UP;
			break;
		case 3:
			eunit.direction = DOWN;
			break;
		case 4:
			eunit.direction = LEFT;
			break;
		case 5:
			eunit.direction = RIGHT;
			break;
		default:
			break;
		}

	}

	private int[] setDirectionByBeeline(EntityUnit eunit) {

		int[] playerLoc = this.game.player.getGridPosition();
		int dX = playerLoc[0] - eunit.tileX;
		int dY = playerLoc[1] - eunit.tileY; // negative means move up
		int dir2W[] = { 0, 0 }; // dx, dy
		// direction is used for animation

		if (dX < 0) {
			eunit.direction = 'l';
			dir2W[0] = -1;
		} else if (dX > 0) {
			eunit.direction = 'r';
			dir2W[0] = 1;
		} else {
			dir2W[0] = 0;
		}

		if (dY < 0) {
			eunit.direction = 'u';
			dir2W[1] = -1;
		} else if (dY > 0) {
			eunit.direction = 'd';
			dir2W[1] = 1;
		} else {
			dir2W[1] = 0;
		}
		// used for motion
		return dir2W;

	}

//	private void updateColliderUnit(EntityUnit eunit) {
//		eunit.x = eunit.x + WALL_COLLIDE_WIGGLEROOM;
//		eunit.y = eunit.y + WALL_COLLIDE_WIGGLEROOM;
//		eunit.width = eunit.width - WALL_COLLIDE_WIGGLEROOM;
//		eunit.height = eunit.height - WALL_COLLIDE_WIGGLEROOM;
//
//	}

}
