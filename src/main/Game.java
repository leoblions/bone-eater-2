package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Game extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final long frameIntervalMilliseconds = 17;
	static int HEIGHT;
	static int WIDTH;
	static final int TILE_SIZE = 100;
	static final int COLL_GRID_SIZE = 50;
	static final int ROWS = 25;
	static final int COLS = 25;
	 
	int worldSizePxX = TILE_SIZE * COLS;
	int worldSizePxY = TILE_SIZE * ROWS;
	final boolean LOAD_FIRST_LEVEL = true;
	private boolean DRAW_WALL = true;
	public GState gameState = GState.PLAY;
	private BufferedImage image;
	public Graphics g;
	public Utils utils;
	public Trigger trigger;
	public Sound sound;
	public Hud hud;
	public Player player;
	public Editor editor;
	public Input input;
	public Tilegrid tilegrid;
	public Collision collision;
	public Background background;
	public Imageutils imageutils;
	public Camera camera;
	public Brain brain;
	public Entity entity;
	public Particle particle;
	public Pathfind pathfind;
	public Console console;
	public Decor decor;
	public Decal decal;
	public Pickup pickup;
	public Menu menu;

	public   int width = 600;
	public   int height = 600;
	private Thread gameThread;
	private long lastTimeDraw;
	private long lastTimeUpdate;
	private boolean run = true;
	public int cameraX, cameraY;
	public int mouseX, mouseY;
	public int level = 0;
	// public char gameState = 'p'; // p=play, m=menu, c=console, a=paused,
	// b=toolbar, i=inventory, g=gameover
	public int gameStateDelay = 0;
	public static final String LEVEL_DATA_SUBDIR = "leveldata";
	public final String SETTINGS_FILE = "settings.ini";
	public boolean playerPressActivate;
	public int[] visibleArea;

	public Game() {
		level = 0;
		lastTimeDraw = System.currentTimeMillis();
		lastTimeUpdate = System.currentTimeMillis();
		setPreferredSize(new Dimension(width, height));

		this.width = getWidth();
		this.height = getHeight();
		WIDTH = width;
		HEIGHT = height;
		init();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.g = g;

		// Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

//		// White rectangle
//		g.setColor(Color.WHITE);
//		g.fillRect(100, 100, 300, 300);
//
//		// Draw the image (20x20) overlapping the rectangle
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		draw();
	}

	void init() {
		this.setBackground(Color.BLACK);
		this.menu = new Menu(this);
		this.imageutils = new Imageutils(this);
		this.player = new Player(this);
		this.entity = new Entity(this);
		this.editor = new Editor(this);
		this.input = new Input(this);
		this.tilegrid = new Tilegrid(this);
		this.sound = new Sound(this);
		this.collision = new Collision(this);
		this.pathfind = new Pathfind(this);
		this.background = new Background(this);
		this.trigger = new Trigger(this);
		this.hud = new Hud(this);
		this.console = new Console(this);
		this.brain = new Brain(this);
		this.camera = new Camera(this);
		this.decor = new Decor(this);
		this.decal = new Decal(this);
		this.pickup = new Pickup(this);

		setFocusable(true);
		requestFocusInWindow();

		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				input.handleKeyEvent(e, true);

			}

			@Override
			public void keyReleased(KeyEvent e) {
				input.handleKeyEvent(e, false);

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// input.handleKeyEvent(e,false);

			}

		});
		
		
		
		
		this.setFocusTraversalKeysEnabled(false); // enable use of TAB key in game
		myAddMouseListener();

		gameThread = new Thread(this);

		gameThread.start();
		System.out.println(gameThread.isAlive());

		if (LOAD_FIRST_LEVEL) {
			loadLevel(level);;
		}

	}
	
	public void loadLevel(int level) {
		this.tilegrid.loadTilegrid();
		// this.wall.loadTilegrid();
		this.entity.startLevel(level);
		this.collision.loadTilegrid();
		this.pickup.loadCurrentData();
	}

	void myAddMouseListener() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();

				int kind = e.getButton();
				editor.handleClick(kind, 1);
				if (gameState != GState.PLAY) {
					menu.handleClick(kind, 1);
				}

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int kind = e.getButton();
				editor.handleClick(kind, 0);
				System.out.printf("Mouse up %d %d %d\n", mouseX, mouseY, kind);
				if (gameState != GState.PLAY) {
					menu.handleClick(kind, 0);
				}
			}

		});
		// mouse motion

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				// System.out.println("Mouse moved to X: " + x + ", Y: " + y); // Uncomment for
				// continuous output

			}
		});
	}

	void draw() {

		if (this.gameState == GState.PLAY) {
			this.tilegrid.draw();
			// this.wall.draw();
			this.collision.draw();
			this.decor.draw('b');
			this.decal.draw();
			this.player.draw();
			this.decor.draw('f');
			this.entity.draw();
			this.trigger.draw();
			this.pickup.draw();
			this.hud.draw();
			this.pathfind.draw();
			this.editor.draw();
			this.console.draw();
		} else {
			this.menu.draw();
		}
		// this.background.draw();

	}

	void update() {
		// System.out.println("test");
		this.sound.update();
		if (this.gameState == GState.CONSOLE) {
			this.console.update();
		} else if (this.gameState == GState.MENU) {
			this.menu.update();
		} else {
			this.tilegrid.update();
			// this.wall.update();
			this.collision.update();
			this.entity.update();
			this.decor.update();
			this.decal.draw();
			this.background.update();
			this.editor.update();
			this.player.update();
			this.trigger.update();
			this.pathfind.update();
			this.hud.update();
			this.camera.update();
			this.pickup.update();

		}

		this.input.update();

		if (gameStateDelay > 0)
			gameStateDelay--;
		this.playerPressActivate = false;
		// System.out.println(gameStateDelay);

	}

	void switchState(GState newState) {
		if (this.gameStateDelay <= 0) {
			gameStateDelay = 60;
			this.gameState = newState;
		}
	}

	void toggleConsole() {
		if (this.gameStateDelay <= 0) {
			System.out.println("toggle console");
			gameStateDelay = 60;
			if (this.gameState != GState.CONSOLE) {
				this.gameState = GState.CONSOLE;
			} else {
				this.gameState = GState.PAUSE;
			}

		}
	}

	void toggleState(GState newState) {
		// used for pause, console, inventory, etc
		if (this.gameStateDelay <= 0) {
			gameStateDelay = 60;
			GState oldState = this.gameState;
			if (newState == oldState) {
				this.gameState = GState.PLAY; // play
			} else {

				this.gameState = newState;
			}
		}
	}

	void toggleState(GState newState, GState returnState) {
		// used for pause, console, inventory, etc
		if (this.gameStateDelay <= 0) {
			gameStateDelay = 60;
			// GState oldState = this.gameState;
			if (newState == this.gameState) {
				this.gameState = returnState; // play
			} else {

				this.gameState = newState;
			}
		}
	}

	@Override
	public void run() {
		while (run) {
			long nowMillis = System.currentTimeMillis();
			long differenceU = nowMillis - lastTimeUpdate;
			long differenceD = nowMillis - lastTimeDraw;
			// repaint();

			if (differenceD > frameIntervalMilliseconds) {
				lastTimeDraw = nowMillis;
				repaint();
			}
			if (differenceU > frameIntervalMilliseconds) {
				lastTimeUpdate = nowMillis;
				update();
			}

		}

	}

	public enum GState {
		PLAY, PAUSE, MENU, OPTIONS, GAMEOVER, CONSOLE, TOOLABAR, INVENTORY
	}

	public void resizeWindow() {
		this.camera.initBounds();
		this.decor.setDrawDistance();
		
	}
}
