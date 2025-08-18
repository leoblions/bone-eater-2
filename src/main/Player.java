package main;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Player {
	public static final char ATTACK = 'a';
	public static final char WALK = 'w';
	public static final char STAND = 's';
	public static final char DEAD = 'd';
	public static final char HIT = 'h';
	
	public static final char UP = 'u';
	public static final char DOWN = 'd';
	public static final char LEFT = 'l';
	public static final char RIGHT = 'r';
	public static final char NONE = 'n';
	
	final int MOVE_DISTANCE = 2;
	final int SPEED_WALK = 2;
	final int SPEED_RUN=4;
	boolean run = false;
	final int STARTX = 200;
	final int STARTY = 300;
	final int WALK_TIMEOUT = 20;
	final int ATTACK_TIMEOUT = 100;
	final int DAMAGE_COOLDOWN_MAX = 10;
	Game game;
	Image image;
	Image[] images;
	Image[] imagesAttack;
	Image[] imagesWalk;
	
	public int worldX,worldY,screenX,screenY;
	public int width = 50;
	public int height = 50;
	public int spriteOffsetX =-0;
	public int spriteOffsetY =-25;
	public int spriteWidth =40;
	public int spriteHeight=80;
	public char state = Player.WALK;// w=walk, a=attack, h=hit, d=dead, s=stand
	public char direction = DOWN;
	public int frame = 0;
	public int currentImageIndex = 0;
	public int walkTimeout =0;
	public int attackTimeout =0;
	public boolean attack = false;
	public int[] tileForward, tilePlayer;
	private Pacer animationPacer, healPacer, staminaPacer;
	int maxX,maxY;
	int velX,velY;
	int speed =SPEED_WALK;
	public boolean[] directions = {false,false,false,false};
	public Rectangle wpActivateArea;
	public Rectangle collider; //wp cooridinates
	public boolean invincible = false;
	public int health = 100;
	public int damageCooldown = 0;
	
	
	
	public Player(Game game) {
		
		this.game = game;
		this.worldX = STARTX;
		this.worldY = STARTY;
		this.animationPacer = new Pacer(9);
		maxY =game.getHeight() - height;
		maxX = game.getWidth() - width;
		this.collider = new Rectangle(this.worldX,this.worldY,this.width,this.height);
		this.initImages();
		
	}
	
	public int[] getGridPosition() {
		return new int [] {
				this.worldX / this.game.TILE_SIZE,
				this.worldY / this.game.TILE_SIZE
		};
	}
	private void initImages() {
		try {
            this.image = ImageIO.read(getClass().getResource("/images/goldbug.png")); // Image in resources/images/icon.png
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		String walkImagesURL = "/images/zomPlayer.png";
		String attackImagesURL = "/images/zombieAttack.png";
		
		try {
			this.images = this.game.imageutils.characterSheetUDL4(walkImagesURL, 100, 200);
			this.imagesWalk = this.game.imageutils.characterSheetUDL4(walkImagesURL, 100, 200);
			this.imagesAttack = this.game.imageutils.characterSheetUDL4(attackImagesURL, 100, 200);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void draw() {
		if (image != null) {
		
            this.game.g.drawImage(image, screenX+spriteOffsetX, screenY+spriteOffsetY, spriteWidth, spriteHeight, null);
        }
		
	}
	
	public void calculateTileForward() {
		int tx = this.worldX / game.tilegrid.tileSize;
		int ty = this.worldY / game.tilegrid.tileSize;
		int fx = tx;
		int fy = ty;
		switch (direction) {
		case UP:
			fy -= 1;
			break;
		case DOWN:
			fy += 1;
			break;
		case LEFT:
			fx -= 1;
			break;
		case RIGHT:
			fx += 1;
			break;
		default:
			break;
		}
		tileForward[0] = Utils.clamp(0, game.tilegrid.COLS - 1, fx);
		tileForward[1] = Utils.clamp(0,game.tilegrid.ROWS - 1, fy);
		tilePlayer[0] = Utils.clamp(0, game.tilegrid.COLS , tx);
		tilePlayer[1] = Utils.clamp(0, game.tilegrid.ROWS , ty);
	}
	
	public void attack() {

		this.state=ATTACK;
		this.attackTimeout=ATTACK_TIMEOUT;
		
	}
	
	public void update() {
		this.image = this.images[this.currentImageIndex];
		
		setPlayerState();
		cycleSprite();
		motion();
		if (this.walkTimeout >0)walkTimeout  --;
		if (this.attackTimeout >0)attackTimeout  --;
		this.updateCollider();
		
	}
	
	private void updateCollider() {
		this.collider.x = this.worldX;
		this.collider.y = this.worldY;
		this.collider.width = this.width;
		this.collider.height = 
				this.height;
		
	}
	
	
	
	private void motion() {

		if (this.run) {
			speed = SPEED_RUN;
		}else {
			speed=SPEED_WALK;
		}
		this.run=false;
		
		if(directions[0]) {
			velY =- speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction=UP;
		}
		else if(directions[1]) {
			velY = speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction=DOWN;
		}else {
			velY = 0;
		}
		if(directions[2]) {
			velX =- speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction=LEFT;
		}
		else if(directions[3]) {
			velX = speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction=RIGHT;
		}else {
			velX = 0;
		}

		int testWX = worldX+velX;
		int testWY = worldY+velY;
		boolean collisions[] = this.game.collision.collideTilePlayerTestWXY( testWX, testWY);
		if(collisions[0]&&velY<0)velY=0;
		if(collisions[1]&&velY>0)velY=0;
		if(collisions[2]&&velX<0)velX=0;
		if(collisions[3]&&velX>0)velX=0;
		
		this.worldX+=velX;
		this.worldY+=velY;
		
		this.screenX=worldX-game.cameraX;
		this.screenY=worldY-game.cameraY;
		
	}
	
	
	public void warpPlayer(int gridX, int gridY) {

		velX = 0;
		velY = 0;

		this.worldX =gridX * this.game.tilegrid.tileSize;
		this.worldY =gridY * this.game.tilegrid.tileSize;


	}
	
	public void setPlayerState() {
		// change state to walking if recent movement
		
		boolean playerMoved = false;
		boolean attacking = false;
		
		if (velX != 0 || velY != 0) {
			playerMoved = true;

		} 
		if (attack || attackTimeout>0) {
			attacking = true;
		}
		
		if (!attack && playerMoved) {
			walkTimeout=WALK_TIMEOUT;
		} 
		
		if (attack  ) {
			walkTimeout=WALK_TIMEOUT;
			attackTimeout=ATTACK_TIMEOUT;
			this.state =ATTACK;

		} 
		if(!playerMoved && !attacking && walkTimeout==0){
			this.state = STAND;
		}
		
		if (playerMoved && walkTimeout>0 && !attacking ) {
			this.state = WALK;
		} 
	}
	
	
	
	public void cycleSprite() {
		int directionIndexpart = 0;
		switch(this.state) {
		
		case STAND:
			this.images=this.imagesWalk;
		
			switch (direction) {
			case 'u':
				directionIndexpart = 0;
				break;
			case 'd':
				directionIndexpart = 4;
				break;
			case 'l':
				directionIndexpart = 8;
				break;
			case 'r':
				directionIndexpart = 12;
				break;
			}
			currentImageIndex = directionIndexpart;
			return;
			
		case WALK:
		case ATTACK:
			
			if(state==ATTACK) {
				this.images=this.imagesAttack;
			}else {
				this.images=this.imagesWalk;
			}
			switch (direction) {
			case 'u':
				directionIndexpart = 0;
				break;
			case 'd':
				directionIndexpart = 4;
				break;
			case 'l':
				directionIndexpart = 8;
				break;
			case 'r':
				directionIndexpart = 12;
				break;
			}

			if (animationPacer.check()) {
				if (frame < 3) {
					frame++;
				} else {
					frame = 0;
				}

			}

			currentImageIndex = frame + directionIndexpart;
			break;
			
		}



	}
	
	
	public void takeDamageFromEnemy(int damageFromEnemy) {
		if (damageCooldown > 0) {
			damageCooldown -= 1;
			return;
		}else {
			damageCooldown = DAMAGE_COOLDOWN_MAX;
		}
		int newHealth = this.health - damageFromEnemy;
		if (newHealth > 0) {
			this.health = newHealth;
		}else {
			this.health = 0;
		}
		this.game.hud.updateHealthbar(this.health);
		this.game.decal.putDecalAtTile(this.worldX, this.worldY, Decal.DK_BLOOD);
		
	}

	public boolean pointCollidePlayer(int hitX, int hitY) {
		return (hitX >= this.worldX && hitX <= this.worldX+this.width && hitY >= this.worldY && hitY <= this.worldY+this.height);
		
	}

}
