package main;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;


;

public class EntityUnit {

	final int PFWORLDX = 0;
	final int PFWORLDY = 1;
	final int PFWORLDW = 2;
	final int PFWORLDH = 3;
	final char DOWN = 'd';
	final int UP = 'u';
	final int LEFT = 'l';
	final int RIGHT = 'r';
	
	final boolean DRAW_COLLRECT = true;
	
	private final boolean DRAW_SENSE_BLOCKS = false;
	private final String IMAGE_URI_TEMPLATE_W = "/images/entity%dW.png";
	private final int SS_COLS = 4;
	private final int SS_ROWS = 4;
	private final int SS_CELL_W = 50;
	private final int SS_CELL_H = 50;
	private final int DEFAULT_SPEED = 5;
	private final int FULL_HEALTH = 100;
	private BufferedImage[] bufferedImages;
	private Pacer animationPacer;
	static HashMap<Integer, BufferedImage[]> entImageStoreW; // memoize image arrays
	static HashMap<Integer, BufferedImage[]> entImageStoreD; // dead
	public Rectangle wpSolidArea;
	public Rectangle wpProposedMove, testRect;
	int startGX, startGY, kind, UID;
	int spriteHitboxOffsetX, spriteHitboxOffsetY;
	int currentImageIndex = 0;
	int currentSpeed = 5;
	int spriteWidth = 25;
	int spriteHeight = 25;
	int velX = 0;
	int velY = 0;
	int health = 100;
	boolean alive = true;
	int frame = 0;
	char state = 'w'; // w=walk, s=stand, a=attack, d=dead, h=hit
	char direction = 'n';// n u d l r
	int ENEMY_DAMAGE = 1;
	final int DEF_DAMAGE_FROM_PLAYER = 10;
	int rightTurnDebounceWait = 0; // prevent making too many right turns in quick succession
	boolean foundWall = false;
	public boolean enemy = false;
	public boolean chasePlayer = false;
	public boolean playerPressToActivate = false;
	//int[] tileRight;
	int[] currTileYX; // x and y position in tile grid
	Game game;
	char currDirection = 'n';
	private boolean[] movesRequested;
	private int[] tileForward;
	public int[] position, testPosition;
	public Rectangle collider;
	
	
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
	 * 19 Jed
	 * 20 Mary
	 */

	/**
	 * 
	 * @param game
	 * @param startGX
	 * @param startGY
	 * @param kind
	 * @param UID
	 */
	public EntityUnit(Game game, int startGX, int startGY, int kind, int UID) {
		// TODO Auto-generated constructor stub
		this.game = game;
		this.startGX = startGX;
		this.startGY = startGY;
		this.kind = kind;
		this.UID = UID;
		this.currTileYX = new int[2];
		int[] dimensions = getEntityUnitWHFromKind(kind);
		entImageStoreW = new HashMap<>();
		animationPacer = new Pacer(10);
		int worldX = startGX * game.tilegrid.tileSize;
		int worldY = startGY * game.tilegrid.tileSize;
		position = new int[] {0, 0, dimensions[0], dimensions[1]}; //wx, wy, ww, wh
		testPosition = new int[] { 0, 0, dimensions[0], dimensions[1]};
		this.collider = new Rectangle(0, 0, dimensions[0], dimensions[1]);
		 setPositionToGridXY(startGX, startGY,position);
		wpSolidArea = new Rectangle();
		wpProposedMove = new Rectangle();
		testRect = new Rectangle();
		movesRequested = new boolean[4];
		tileForward = new int[2];
		spriteHitboxOffsetX = -30;
		spriteHitboxOffsetY = -30;
		alive = true;
		health = FULL_HEALTH;
		if (kind < 10) {
			//enemies
			enemy = true;
			chasePlayer = true;

		} else {
			//NPC
			currDirection = 'd';
			state = 's';
			direction = 'D';
			playerPressToActivate=true;
		}

		wpProposedMove.height = spriteHeight;
		wpProposedMove.width = spriteWidth;
		wpSolidArea.height = spriteHeight;
		wpSolidArea.width = spriteWidth;
		testRect.height = spriteHeight;
		testRect.width = spriteWidth;

		try {
			initImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.bufferedImages = entImageStoreW.get(kind);
		currDirection = 'd';
	}

	public int[] getEntityUnitWHFromKind(int kind) {
		return new int[] { 50, 50 };
	}
	
	public void setPositionToGridXY(int startGX, int startGY, int[] position) {
		position[0]=startGX*this.game.tilegrid.tileSize;
		position[1]=startGY*this.game.tilegrid.tileSize;
		
	}
	
	public void positionApplyVelocityXY(int velX, int velY, int[]position) {
		position[0]+=velX;
		position[1]+=velY;
	}
	public void setPositionToWorldXY(int startGX, int startGY, int[] position){
		position[0]=startGX;
		position[1]=startGY;
		
	}
	


	public void draw() {
		game.g.setColor(Color.orange);
		int worldX = position[PFWORLDX];
		int worldY = position[PFWORLDY];

		if (DRAW_COLLRECT)
			game.g.drawRect(worldX - game.cameraX, worldY - game.cameraY, wpSolidArea.width, wpSolidArea.height);
		game.g.drawImage(bufferedImages[currentImageIndex], (worldX - game.cameraX) + spriteHitboxOffsetX,
				(worldY - game.cameraY) + spriteHitboxOffsetY, 50, 50, null);

		

	}
	
	public void takeDamageFromPlayer(int damage) {
		int newHealth = health - damage;
		if (newHealth>0 ) {
			health = newHealth;
		}else {
			health = 0;
		}
		if(health <= 0) {
			alive = false;
			state = 'd';
		}
	}

//	public Direction4W translateDirectionLetterToEnum(char letter) {
//		switch (letter) {
//		case 'U':
//			return Direction4W.UP;
//		// break;
//		case 'D':
//			return Direction4W.DOWN;
//		// break;
//		case 'L':
//			return Direction4W.LEFT;
//		// break;
//		case 'R':
//			return Direction4W.RIGHT;
//		// break;
//		default:
//			return Direction4W.NONE;
//		// break;
//
//		}
//	}

	public void cycleSprite() {
		int directionIndexpart = 0;

		switch (currDirection) {
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
		if (state == 's' && animationPacer.check()) {

			if (frame==0) {
				frame=2;
			}else {
				frame=0;
			}

		} else if (state == 'w' && animationPacer.check()) {

			if (frame < 3) {
				frame++;
			} else {
				frame = 0;
			}

			

		} else if (state=='d'|| !alive) {
			frame = 0;
		}
		currentImageIndex = frame + directionIndexpart;

	}

	public void moveDirection() {
		if (state == 'w') {
			currentSpeed = DEFAULT_SPEED;
		} else {
			currentSpeed = 0;
		}
		switch (currDirection) {
		case UP:
			positionApplyVelocityXY(0, -currentSpeed,position);
			break;
		case DOWN:
			positionApplyVelocityXY(0, currentSpeed,position);
			break;
		case LEFT:
			positionApplyVelocityXY(-currentSpeed, 0,position);
			break;
		case RIGHT:
			positionApplyVelocityXY(currentSpeed, 0,position);
			break;
		default:
			positionApplyVelocityXY(0, 0,position);
			break;

		}
	}

	public boolean inbounds() {
		int worldX = position[PFWORLDX];
		int worldY = position[PFWORLDY];
		if (worldX >= 0 && worldX + spriteWidth < game.worldSizePxX && worldY >= 0
				&& worldY + spriteHeight < game.worldSizePxY) {
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
	public boolean borderBump() {
		int worldX =position[PFWORLDX];
		int worldY = position[PFWORLDY];
		int velXLocal = 0;
		int velYLocal = 0;

		if (worldX < 0) {
			velXLocal = currentSpeed;

		} else if (worldX + spriteWidth >= game.worldSizePxX) {
			velXLocal = -currentSpeed;

		} else if (worldY < 0) {
			velYLocal = currentSpeed;

		} else if (worldY + spriteHeight >= game.worldSizePxY) {
			velYLocal = -currentSpeed;

		} else {
			return false;
		}
		positionApplyVelocityXY(velXLocal, velYLocal,position);

		return true;
	}

	/**
	 * true if a tile is blocking the path ahead
	 * 
	 * @return
	 */
	public boolean tileAhead(Rectangle prop) {
		return game.collision.collideTileRectDirection(prop, currDirection);
	}

	private void setDirectionByPathFind() {
		int worldX = position[PFWORLDX];
		int worldY = position[PFWORLDY];
		Point worldPoint = new Point(worldX, worldY);
		this.direction = game.pathfind.getDirectionTowardsPlayer(worldPoint);
		//currDirection = translateDirectionLetterToEnum(direction);

	}

	public boolean moveOverlapsOtherEntityUnit() {
		// stop enemies and NPCs from overlapping
		setPositionToWorldXY(position[PFWORLDX], position[PFWORLDY],testPosition);
		switch (currDirection) {
		case UP:
			positionApplyVelocityXY(0, -currentSpeed,testPosition);
			break;
		case DOWN:
			positionApplyVelocityXY(0, currentSpeed,testPosition);
			break;
		case LEFT:
			positionApplyVelocityXY(-currentSpeed, 0,testPosition);
			break;
		case RIGHT:
			positionApplyVelocityXY(currentSpeed, 0,testPosition);
			break;
		default:
			positionApplyVelocityXY(0, 0,testPosition);
			break;

		}
		testRect.x = testPosition[PFWORLDX];
		testRect.y = testPosition[PFWORLDY];
		for (EntityUnit entity : game.entity.entityUnits) {
			if (this.testRect.intersects(entity.wpSolidArea) && !(this == entity)) {

				return true;
			}
		}
		return false;
	}

	public void update() {
		int worldX = position[PFWORLDX];
		int worldY = position[PFWORLDY];
		wpSolidArea.x = worldX;
		wpSolidArea.y = worldY;
		currTileYX[0] = worldY / game.tilegrid.tileSize;
		currTileYX[1] = worldX / game.tilegrid.tileSize;
		if(game.entity.frozen) {
			return;
		}
		if (alive && chasePlayer) {
			state = 'w';
			setDirectionByPathFind();

		}else if(!alive){
			bufferedImages=entImageStoreD.get(kind);
		}
		if (!moveOverlapsOtherEntityUnit()) {

			moveDirection();
		} else {
			state = 's';
		}
		cycleSprite();
		playerMeleeEnemy();
		entityCollidePlayer();

	}

	private void playerMeleeEnemy() {
		wpSolidArea.x = position[PFWORLDX];
		wpSolidArea.y = position[PFWORLDY];
		if(game.entity.playerMelee) {
			if(game.entity.playerHitbox.intersects(this.wpSolidArea)) {
				takeDamageFromPlayer(DEF_DAMAGE_FROM_PLAYER);
				game.particle.addParticle(wpSolidArea.x, wpSolidArea.y, 1);
			}
		}
		
	}
	
	public void drawSolidArea(Rectangle solidArea) {
		if(game.g==null)return;
		game.g.setColor(Color.black);
		int x = solidArea.x - game.cameraX;
		int y = solidArea.y - game.cameraY;
		int width =  solidArea.width;
		int height = solidArea.height;
		game.g.drawRect(x,y,width,height);
	}

	public void entityCollidePlayer() {
		//drawSolidArea(  game.player.wpActivateArea) ;
		if ( this.wpSolidArea.intersects(game.player.wpActivateArea)) {
			
			
			if(enemy && alive) {
				//game.player.health -= ENEMY_DAMAGE;
				game.player.takeDamageFromEnemy(DEF_DAMAGE_FROM_PLAYER);
			}else if( // player in range of NPC
					 game.entity.entityActivateDalay.delayExpired() &&
					!game.entity.entityTouchedList.contains(this)){

				game.hud.showActionPromptDelay = 60;
				if(game.entity.activateEntityUnitFlag) {
					//player pressed activate
					game.entity.entityActivateDalay.setDelay(Entity.ENTITY_ACTIVATE_DELAY_TICKS);
					game.brain.playerActivateNPC(this,game.playerPressActivate);
					game.entity.playerTouchedActorSincelastTick = true;
					game.entity.entityTouchedList.add(this);
				}
				//System.out.println("player touch ent");
				
			}
		}
	}

	public void initImages() throws IOException {
		if (null == entImageStoreW) {
			entImageStoreW = new HashMap<>();
		}
		if (null == entImageStoreD) {
			entImageStoreD = new HashMap<>();
		}
		// this.bufferedImages = new BufferedImage[20];
		String URLString = String.format(IMAGE_URI_TEMPLATE_W, this.kind);
		// bufferedImages[0] =
		// ImageIO.read(getClass().getResourceAsStream("/characters/orb4.png"));
		BufferedImage[] tempBI = null;
		try {
			tempBI = new Imageutils(game).spriteSheetCutter(URLString, SS_COLS, SS_ROWS, SS_CELL_W, SS_CELL_H);

		} catch (Exception e) {
			System.err.println("EntityUnit Failed to open the resource: " + URLString);
			e.printStackTrace();
		}
		if (null == tempBI) {
			tempBI = new Imageutils(game).spriteSheetCutterBlank(SS_COLS, SS_ROWS, SS_CELL_W, SS_CELL_H);
		}
		entImageStoreW.put(kind, tempBI);

		BufferedImage[] tempBI2 = null;
		tempBI2 = new BufferedImage[tempBI.length];
		for(int i = 0; i < tempBI.length;i++) {
			
			tempBI2[i]= Imageutils.convertBufferedImageBW(tempBI[i] );
		}
		entImageStoreD.put(kind, tempBI2);
	}

}
