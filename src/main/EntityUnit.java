package main;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

;

public class EntityUnit extends Rectangle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * EntityUnit class stores position and state data for entities.
	 * Superclass Rectangle is used for world position and collision.
	 */

	

	final boolean DRAW_COLLRECT = true;
	private final int FULL_HEALTH = 100;
	
	public static final char ATTACK = 'a';
	public static final char WALK = 'w';
	public static final char STAND = 's';
	public static final char DEAD = 'd';
	public static final char HIT = 'h';
	
	
	public static final int EK_GUARD = 0;
	public static final int EK_KNIGHT = 1;
	public static final int EK_SPIDER = 2;
	public static final int EK_LIZARD = 3;
	public static final int EK_BEAR = 4;

	

	public Rectangle  colliderTest;
	private Rectangle collider;
	public BufferedImage image;
	int startGX, startGY, kind, UID;
	int tileX,tileY;
	int widthS,heightS,widthA,heightA;
	
	public int damageToPlayer = 15;
	
	int currentImageIndex = 0;
	int currentSpeed = 5;
	int velX = 0;
	int velY = 0;
	int deathCount = 0;
	int deathCountMax = 100;
	int frameMax = 7;
	//int worldX = 0;
	//int worldY = 0;
	int width = 100;
	int height = 100;
	public int health = 100;
	int offsetX = 0;
	int offsetY = 0;
	int gridX,gridY;
	int locationOffsetX = 25;
	int locationOffsetY  =35;
	
	boolean alive = true;
	int screenX,screenY;
	int frame = 0;
	char state = 'w'; // w=walk, s=stand, a=attack, d=dead, h=hit
	char direction = 'n';// n u d l r
	int ENEMY_DAMAGE = 1;
	final int DEF_DAMAGE_FROM_PLAYER = 10;
	int rightTurnDebounceWait = 0; // prevent making too many right turns in quick succession
	boolean foundWall = false;
	public boolean enemy = false;
	//public boolean chasePlayer = false;
	public boolean playerPressToActivate = false;
	public int[] lastTile;
	public boolean tileChanged;
	int entityPlayerDistance;
	int direction8w;
	Game game;
	//char currDirection = 'n';
	
	public int[] gridXY, testGridXY, forwardGridXY;
	public boolean damagePlayerOnTouch;




	/**
	 * 
	 * @param game
	 * @param startGX
	 * @param startGY
	 * @param kind
	 * @param UID
	 */
	public EntityUnit(Game game, int startGX, int startGY, int kind, int UID) {
		super(0,0,40,80);
		this.game = game;
		this.startGX = startGX;
		this.startGY = startGY;
		this.kind = kind;
		this.UID = UID;
		this.entityPlayerDistance = 500;
		this.x = startGX * Game.TILE_SIZE;
		this.y = startGY * Game.TILE_SIZE;
		this.gridX = x / Game.TILE_SIZE;
		this.gridY = y / Game.TILE_SIZE;
		
		this.lastTile = new int[] {this.gridX,this.gridY};
		this.tileChanged=false;
	

		//this.collider = new Rectangle(0, 0, width, height); //WP coordinates
		this.colliderTest = new Rectangle(0, 0, width, height); //WP coordinates
	
	
		alive = true;
		health = FULL_HEALTH;
		
		direction = 'n';
		direction8w = 8;
		switch(kind) {
		case EK_GUARD:// basic guard
			this.width = 40;
			this.height = 80;
			this.widthS=40;
			this.heightS=80;
			this.widthA=80;
			this.heightA=90;
			this.enemy=true;
			this.state = 's';
			if (UID==1) {
				this.direction='u';
			}
		}
	}
	
	public void takeDamage(int damageAmount) {
		
		
		int newHealth = this.health - damageAmount;
		if (newHealth > 0) {
			this.health = newHealth;
		}else {
			this.health = 0;
		}
		//this.game.hud.updateHealthbar(this.health);
		this.game.decal.putDecalAtTile(this.x, this.y, Decal.DK_BLOOD);
		
	}
	

	

	

	

	

	

	

	

}
