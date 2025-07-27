package main;

import java.awt.Rectangle;

public class Collision {
	/*
	 * handles a variety of collision tasks for player, projectiles, enemies, attacks
	 */

	Game gp;
	int BUFFER_ZONE = 2;
	double playerCenterDistance;

	public Collision(Game gp) {
		this.gp = gp;

	}

	public static enum Direction4W {
		UP, DOWN, LEFT, RIGHT, NONE
	}

	public int tileAtWorldCoord(int wx, int wy) {
		return gp.tilegrid.getTileYX(wy / gp.tilegrid.tileSize, wx / gp.tilegrid.tileSize);

	}
	
	public static int tileAtWorldCoord(Game gp, int wx, int wy) {
		return gp.tilegrid.getTileYX(wy / gp.tilegrid.tileSize, wx / gp.tilegrid.tileSize);

	}

	public static boolean tileKindIsSolid(int kind) {
		if (kind >15 ) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean pointCollideWithSolidTile(Game gp, int worldX, int worldY) {
		try {

			return tileKindIsSolid(tileAtWorldCoord(gp,worldX, worldY));
		}catch(ArrayIndexOutOfBoundsException e) {
			return true;
		}
	}
	
public boolean[] collideTileTestWXY(int testX,int testY, int width, int height) {
		

		boolean[] collisions = new boolean[] { false, false, false, false };
		if (gp.tilegrid.grid==null) {
			return collisions;
		}
		

		// up coll
		int tmp = tileAtWorldCoord(testX + (width / 2), testY - BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide up");
			collisions[0] = true;
		}
		// down coll
		tmp = tileAtWorldCoord(testX + (width / 2), testY + (height) + BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide dn");
			collisions[1] = true;
		}
		// left coll
		tmp = tileAtWorldCoord(testX - BUFFER_ZONE, testY + height / 2);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide lt");
			collisions[2] = true;
		}
		// right coll
		tmp = tileAtWorldCoord(testX + width + BUFFER_ZONE, testY + height / 2);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide rt");
			collisions[3] = true;

		}
		return collisions;

	}
	
public boolean[] collideTilePlayerTestWXY(int testX,int testY) {
		

		boolean[] collisions = new boolean[] { false, false, false, false };
		if (gp.tilegrid.grid==null) {
			return collisions;
		}
		Player p = gp.player;

		// up coll
		int tmp = tileAtWorldCoord(testX + (p.width / 2), testY - BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide up");
			collisions[0] = true;
		}
		// down coll
		tmp = tileAtWorldCoord(testX + (p.width / 2), testY + (p.height) + BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide dn");
			collisions[1] = true;
		}
		// left coll
		tmp = tileAtWorldCoord(testX - BUFFER_ZONE, testY + p.height / 2);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide lt");
			collisions[2] = true;
		}
		// right coll
		tmp = tileAtWorldCoord(testX + p.width + BUFFER_ZONE, testY + p.height / 2);
		if (tileKindIsSolid(tmp)) {
			//System.out.println("collide rt");
			collisions[3] = true;

		}
		return collisions;

	}

	public boolean[] collideTilePlayer() {
		

		boolean[] collisions = new boolean[] { false, false, false, false };
		if (gp.tilegrid.grid==null) {
			return collisions;
		}
		Player p = gp.player;

		// up coll
		int tmp = tileAtWorldCoord(p.worldX + (p.width / 2), p.worldY - BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide up");
			collisions[0] = true;
		}
		// down coll
		tmp = tileAtWorldCoord(p.worldX + (p.width / 2), p.worldY + (p.height) + BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide dn");
			collisions[1] = true;
		}
		// left coll
		tmp = tileAtWorldCoord(p.worldX - BUFFER_ZONE, p.worldY + p.height / 2);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide lt");
			collisions[2] = true;
		}
		// right coll
		tmp = tileAtWorldCoord(p.worldX + p.width + BUFFER_ZONE, p.worldY + p.height / 2);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide rt");
			collisions[3] = true;

		}
		return collisions;

	}

	/**
	 * returns array of bools, any of which will be true if the rectangles collide
	 * in that direction with a tile
	 * 
	 * @param r
	 * @return
	 */
	public boolean[] collideTileRect(Rectangle r) {

		boolean[] collisions = new boolean[] { false, false, false, false };
		// Player p = gp.player;

		// up coll
		int tmp = tileAtWorldCoord(r.x + (r.width / 2), r.y - BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			// System.out.println("collide up");
			collisions[0] = true;
		}
		// down coll
		tmp = tileAtWorldCoord(r.x + (r.width / 2), r.y + (r.height) + BUFFER_ZONE);
		if (tileKindIsSolid(tmp)) {
			// System.out.println("collide dn");
			collisions[1] = true;
		}
		// left coll
		tmp = tileAtWorldCoord(r.x - BUFFER_ZONE, r.y + r.height / 2);
		if (tileKindIsSolid(tmp)) {
			// System.out.println("collide lt");
			collisions[2] = true;
		}
		// right coll
		tmp = tileAtWorldCoord(r.x + r.width + BUFFER_ZONE, r.y + r.height / 2);
		if (tileKindIsSolid(tmp)) {
			// System.out.println("collide rt");
			collisions[3] = true;

		}
		return collisions;

	}

	public boolean collideTileRectDirection(Rectangle r, char direction) {

		// up coll
		int tmp = tileAtWorldCoord(r.x + (r.width / 2), r.y - BUFFER_ZONE);
		if (tileKindIsSolid(tmp) && direction == 'u') {
			return true;
		}
		// down coll
		tmp = tileAtWorldCoord(r.x + (r.width / 2), r.y + (r.height) + BUFFER_ZONE);
		if (tileKindIsSolid(tmp) && direction == 'd') {
			return true;
		}
		// left coll
		tmp = tileAtWorldCoord(r.x - BUFFER_ZONE, r.y + r.height / 2);
		if (tileKindIsSolid(tmp) && direction == 'l') {
			return true;
		}
		// right coll
		tmp = tileAtWorldCoord(r.x + r.width + BUFFER_ZONE, r.y + r.height / 2);
		if (tileKindIsSolid(tmp) && direction == 'r') {
			return true;

		}
		return false;

	}

	public boolean collideTileRectDirection(Rectangle r, Direction4W direction) {

		// up coll
		int tmp = tileAtWorldCoord(r.x + (r.width / 2), r.y - BUFFER_ZONE);
		if (tileKindIsSolid(tmp) && direction == Direction4W.UP) {
			// System.out.println("collide up");
			return true;
		}
		// down coll
		tmp = tileAtWorldCoord(r.x + (r.width / 2), r.y + (r.height) + BUFFER_ZONE);
		if (tileKindIsSolid(tmp) && direction == Direction4W.DOWN) {
			// System.out.println("collide dn");
			return true;
		}
		// left coll
		tmp = tileAtWorldCoord(r.x - BUFFER_ZONE, r.y + r.height / 2);
		if (tileKindIsSolid(tmp) && direction == Direction4W.LEFT) {
			// System.out.println("collide lt");
			return true;
		}
		// right coll
		tmp = tileAtWorldCoord(r.x + r.width + BUFFER_ZONE, r.y + r.height / 2);
		if (tileKindIsSolid(tmp) && direction == Direction4W.RIGHT) {
			// System.out.println("collide rt");
			return true;

		}
		return false;

	}

	public boolean collideTileRectAny(Rectangle r) {

		// boolean[] collisions = new boolean[] {false,false,false,false};
		// Player p = gp.player;

		// up coll
		int tmp = tileAtWorldCoord(r.x, r.y);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide up");
			return true;
		}
		// down coll
		tmp = tileAtWorldCoord(r.x, r.y);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide dn");
			return true;
		}
		// left coll
		tmp = tileAtWorldCoord(r.x, r.y);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide lt");
			return true;
		}
		// right coll
		tmp = tileAtWorldCoord(r.x, r.y);
		if (tileKindIsSolid(tmp)) {
			System.out.println("collide rt");
			return true;

		}
		return false;

	}

}
