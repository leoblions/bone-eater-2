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
	/**
	 * EntityUnit class stores position and state data for entities.
	 */

	

	final boolean DRAW_COLLRECT = true;
	private final int FULL_HEALTH = 100;

	

	public Rectangle collider, colliderTest;
	
	int startGX, startGY, kind, UID;
	int tileX,tileY;
	
	int currentImageIndex = 0;
	int currentSpeed = 5;
	
	int velX = 0;
	int velY = 0;
	int worldX = 0;
	int worldY = 0;
	int width = 100;
	int height = 100;
	int health = 100;
	int offsetX = 0;
	int offsetY = 0;
	
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
	public boolean chasePlayer = false;
	public boolean playerPressToActivate = false;
	Game game;
	char currDirection = 'n';
	
	public int[] gridXY, testGridXY, forwardGridXY;




	/**
	 * 
	 * @param game
	 * @param startGX
	 * @param startGY
	 * @param kind
	 * @param UID
	 */
	public EntityUnit(Game game, int startGX, int startGY, int kind, int UID) {
	 
		this.game = game;
		this.startGX = startGX;
		this.startGY = startGY;
		this.kind = kind;
		this.UID = UID;
		
		this.worldX = startGX * Game.TILE_SIZE;
		this.worldY = startGY * Game.TILE_SIZE;
		
	

		this.collider = new Rectangle(0, 0, width, height); //WP coordinates
		this.colliderTest = new Rectangle(0, 0, width, height); //WP coordinates
	
	
		alive = true;
		health = FULL_HEALTH;
		
		currDirection = 'd';
	}

	

	

	

	

	

	

	

	

}
