package main;

public class Camera {
	//leash camera
	Game game;
	final int MOVE_BORDER = 150;
	int screenCenterWorldX, screenCenterWorldY;
	double playerCenterDistance;
	public final double MOVE_CAMERA_THRESHOLD_DISTANCE = 100;
	private final int SPEEDUP_THRESHOLD = 450;
	int thresholdX1, thresholdX2, thresholdY1, thresholdY2;
	public Camera(Game game) {
		/*
		 * The camera lazily tracks the player position.
		 * Changes screen to world offset as player moves
		 */
		this.game=game;
		System.out.println("camera created");
		initBounds();
		
		
	}
	
	public void initBounds() {
		screenCenterWorldX=game.cameraX+(game.WIDTH/2);
		screenCenterWorldY=game.cameraY+(game.HEIGHT/2);
		thresholdX1 = MOVE_BORDER;
		thresholdX2 = game.width - MOVE_BORDER;
		thresholdY1= MOVE_BORDER;
		thresholdY2=game.height-MOVE_BORDER;
	}
	
	public double distance(int x1, int y1, int x2, int y2) {
		double x1d = (double)x1;
		double y1d = (double)y1;
		double x2d = (double)x2;
		double y2d = (double)y2;
		double retval=0;
		//pythagorean theorem
		
		retval = Math.sqrt(Math.pow( (y2d - y1d ),2)+ Math.pow( (x2d-x1d),2)) ;
		return retval;
	}
	
	public void recenterCamera() {
		game.cameraY =  game.player.y - (Game.HEIGHT/2);
		game.cameraX =  game.player.x - (Game.WIDTH/2);
	}
	
	
	public void update() {
		//if(true)return;
		int screenX = game.player.x - game.cameraX;
		int screenY = game.player.y - game.cameraY;
		int cameraSpeed = this.game.player.speed;
		
		//move camera up
		if(screenY < thresholdY1) {
			game.cameraY -= cameraSpeed;
		}
		
		//move camera down
		if(screenY > thresholdY2) {
			game.cameraY += cameraSpeed;
		}
		//move cam left
		if(screenX < thresholdX1) {
			game.cameraX -= cameraSpeed;
		}
		
		//move camera down
		if(screenX > thresholdX2) {
			game.cameraX += cameraSpeed;
		}
//		this.game.cameraX = game.player.worldX -250;
//		this.game.cameraY = game.player.worldY -250;
		//System.out.println("cx "+game.cameraX);
		//game.player.screenX = screenX;
		//game.player.screenY = screenY;
		
		
	}

}
