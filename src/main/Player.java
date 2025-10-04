package main;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Player extends Rectangle{
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
	
	public final int ATTACK_PERIOD = 10;
	private final int PLAYER_DAMAGE_TO_ENEMY = 100;
	
	final int MOVE_DISTANCE = 2;
	final int SPEED_WALK = 2;
	final int SPEED_RUN=4;
	boolean run = false;
	final static int STARTX = 200;
	final static int STARTY = 300;
	final static int STARTW = 40;
	final static int STARTH = 80;
	final int WALK_TIMEOUT = 20;
	final int ATTACK_TIMEOUT = 100;
	final int DAMAGE_COOLDOWN_MAX = 10;
	Pacer attackPacer;
	Game game;
	Image image;
	Image[] images;
	Image[] imagesAttack;
	Image[] imagesWalk;

	//public Rectangle collider;
	private int screenX,screenY;

	public int spriteOffsetX =-0;
	public int spriteOffsetY =-25;
	public int spritewidth =40;
	public int spriteheight=80;
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
	public boolean invincible = false;
	public int health = 100;
	public int damageCooldown = 0;
	BufferedImage shadow;
	
	
	
	public Player(Game game) {
		super(STARTX,STARTY,STARTW,STARTH);
		this.game = game;
		this.attackPacer = new Pacer(ATTACK_PERIOD);
		this.animationPacer = new Pacer(9);
		//maxY = game.getheight() - collider.height;
		//maxX = game.getwidth() - collider.width;
		
		this.initImages();
		
	}
	
	public int[] getGridPosition() {
		return new int [] {
				this.x / this.game.TILE_SIZE,
				this.y / this.game.TILE_SIZE
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
		
		String URL = "/images/shadow.png";
		try {
			this.shadow = this.game.imageutils.spriteSheetCutter(URL, 1, 1, 100, 100)[0];
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	public void draw() {
		if (image != null) {
			game.g.drawImage(shadow, screenX+spriteOffsetX,

					screenY+spriteOffsetY, spritewidth, spriteheight, null);
		
            this.game.g.drawImage(image, screenX+spriteOffsetX, screenY+spriteOffsetY, spritewidth, spriteheight, null);
        }
		
	}
	
	public void calculateTileForward() {
		int tx = this.x / game.tilegrid.tileSize;
		int ty = this.y / game.tilegrid.tileSize;
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
		tileForward[0] = Utils.clamp(0, game.COLS - 1, fx);
		tileForward[1] = Utils.clamp(0,game.ROWS - 1, fy);
		tilePlayer[0] = Utils.clamp(0, game.COLS , tx);
		tilePlayer[1] = Utils.clamp(0, game.ROWS , ty);
	}
	
	public void attack() {

		this.state=ATTACK;
		this.attackTimeout=ATTACK_TIMEOUT;
		this.game.sound.playSE(Sound.S_SLASH);
		
	}
	
	public void update() {
		this.image = this.images[this.currentImageIndex];
		
		setPlayerState();
		cycleSprite();
		motion();
		if (this.walkTimeout >0)walkTimeout  --;
		if (this.attackTimeout >0)attackTimeout  --;
		//this.updateCollider();
		if(this.state == ATTACK) {
			for(EntityUnit eunit: this.game.entity.entityUnits) {
				if(eunit.enemy&& eunit.intersects(this)) {
					eunit.takeDamageFromPlayer(PLAYER_DAMAGE_TO_ENEMY);
					this.game.sound.playSE(Sound.S_ROAR);
					this.state = WALK;
					break;
				}
			
			}}
		
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

		int testWX = this.x+velX;
		int testWY = this.y+velY;
		boolean collisions[] = this.game.collision.collideTilePlayerTestWXY( testWX, testWY);
		if(collisions[0]&&velY<0)velY=0;
		if(collisions[1]&&velY>0)velY=0;
		if(collisions[2]&&velX<0)velX=0;
		if(collisions[3]&&velX>0)velX=0;
		
		this.x+=velX;
		this.y+=velY;
		
		this.screenX=this.x-game.cameraX;
		this.screenY=this.y-game.cameraY;
		
	}
	
	
	public void warpPlayer(int gridX, int gridY) {

		velX = 0;
		velY = 0;

		this.x =gridX * this.game.tilegrid.tileSize;
		this.y =gridY * this.game.tilegrid.tileSize;


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
		this.game.decal.putDecalAtTile(this.x, this.y, Decal.DK_BLOOD);
		
	}

	public boolean pointCollidePlayer(int hitX, int hitY) {
		return (hitX >= this.x && hitX <= this.x+this.width && hitY >= this.y && hitY <= this.y+this.height);
		
	}

}
