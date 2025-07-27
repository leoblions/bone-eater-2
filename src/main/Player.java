package main;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Player {
	final int MOVE_DISTANCE = 2;
	final int SPEED_WALK = 2;
	final int SPEED_RUN=4;
	boolean run = false;
	final int STARTX = 400;
	final int STARTY = 400;
	final int WALK_TIMEOUT = 20;
	final int ATTACK_TIMEOUT = 20;

	Game game;
	Image image;
	Image[] images;
	public int worldX,worldY,screenX,screenY;
	public int width = 50;
	public int height = 50;
	public int spriteOffsetX =-0;
	public int spriteOffsetY =-25;
	public int spriteWidth =40;
	public int spriteHeight=80;
	public char state = 'w';// w=walk, a=attack, h=hit, d=dead, s=stand
	public char direction = 'd';
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
	
	
	
	public Player(Game game) {
		
		this.game = game;
		this.worldX = 300;
		this.worldY = 300;
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
		
		String SpriteSheet = "/images/zomPlayer.png";
		
		try {
			this.images = this.game.imageutils.characterSheetUDL4(SpriteSheet, 100, 200);
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
		case 'u':
			fy -= 1;
			break;
		case 'd':
			fy += 1;
			break;
		case 'l':
			fx -= 1;
			break;
		case 'r':
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
			this.direction='u';
		}
		else if(directions[1]) {
			velY = speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction='d';
		}else {
			velY = 0;
		}
		if(directions[2]) {
			velX =- speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction='l';
		}
		else if(directions[3]) {
			velX = speed;
			walkTimeout  = WALK_TIMEOUT;
			this.direction='r';
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
			this.state ='a';

		} 
		if(!playerMoved && !attacking && walkTimeout==0){
			this.state = 's';
		}
		
		if (playerMoved && walkTimeout>0 && !attacking ) {
			this.state = 'w';
		} 
	}
	
	
	
	public void cycleSprite() {

		if (state == 's') {
			int directionIndexpart = 0;
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
		} else if (state =='a') {
			int directionIndexpart = 16;
			switch (direction) {
			case 'u':
				directionIndexpart = 16;
				break;
			case 'd':
				directionIndexpart = 20;
				break;
			case 'l':
				directionIndexpart = 24;
				break;
			case 'r':
				directionIndexpart = 28;
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
			return;
		} else if (state == 'w') {

			int directionIndexpart = 0;

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

		} else {
			currentImageIndex = 0;
		}

	}
	
	
	public void takeDamageFromEnemy(int dEF_DAMAGE_FROM_PLAYER) {
		// TODO Auto-generated method stub
		
	}

}
