package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Menu {
	final int BUTTONCOLUMN_SCREENX = 25;
	final int BUTTONCOLUMN_SCREENY = 25;
	final int BUTTONCOLUMN_WIDTH = 200;
	final int BUTTONCOLUMN_HEIGHT = 40;
	final int BUTTON_BGI_SIZE = 350;
	private Point mousePoint;
	
	
	final int STACKMENU_PADDING_Y = 30;
	final Color BUTTON_HIGHLIGHT_COLOR  = new Color(255,255,255,50);
	final Color BUTTON_FONT_COLOR  = new Color(255,255,255,255);
	final Font BUTTON_FONT = new Font("Serif", Font.BOLD, 24);
	final int FONTISPIECE_WIDTH = 350;
	final int FONTISPIECE_HEIGHT = 120;
			;
	final int FONTISPIECE_MIDDLE_OFFSET_X = 150;
	final int FONTISPIECE_Y = 100;
	
	final int BUTTON_AMOUNT_MENU = 4;
	final int BUTTON_AMOUNT_OPTIONS = 4;
	final int BUTTON_AMOUNT_PAUSED = 3;
	
	final int STACK_BG_WIDTH = 400;
	final int STACK_BG_HEIGHT = 400;
	final int STACK_BG_OFFSET = 40;
	final int STACK_BG_OFFSET_BOTTOM = 35;
	Game game;
	BufferedImage background;
	final int INTERACT_SCREENX = 470;
	final int INTERACT_SCREENY = 470;
	private int stackMenu = 150;
	private int stackMenuButtons = 4;
	BufferedImage interact;
	BufferedImage[] icons;
	
	Button[] mainButtons;
	Button[] optionButtons;
	Button[] ingameButtons;
	Button[] currentButtons;
	BufferedImage fontispieceImage;
	BufferedImage stackBackground,highlight;
	
	boolean showInteract = true;
	public int showActionPromptDelay = 0;
	
	private int middleX, startY, startX, paddingY, fontispieceX,fontispieceY;
	private int buttonBGIWidth, buttonBGIHeight, buttonBGIX, buttonBGIY;
	private int y1,y2,y3,y4;

	public Menu(Game game) {
		this.game = game;
		this.mousePoint = new Point(this.game.mouseX,this.game.mouseY);
		
		this.initPositions();
		this.initImages();
		this.initButtons();
		
	}
	
	private void initPositions() {
		this.paddingY = STACKMENU_PADDING_Y;
		this.middleX = game.getBounds().width/2;
		this.startY = paddingY *2 + FONTISPIECE_HEIGHT + STACK_BG_OFFSET;
		this.startX = middleX - (BUTTONCOLUMN_WIDTH/2);
		this.fontispieceX = this.middleX - FONTISPIECE_WIDTH/2;
		this.fontispieceY = paddingY;
		
		this.y1 = startY +10;
		this.y2 = startY + 75;
		this.y3 = startY + 175;
		this.y4 = startY + 225;
		
		this.buttonBGIHeight = BUTTON_BGI_SIZE;
		this.buttonBGIWidth = BUTTON_BGI_SIZE;
		this.buttonBGIX = (this.middleX) - (this.buttonBGIWidth/2);
		this.buttonBGIY = this.startY -paddingY;
	}
	
	private void initButtons() {
		this.mainButtons = new Button[BUTTON_AMOUNT_MENU];
		this.optionButtons = new Button[BUTTON_AMOUNT_OPTIONS];
		this.ingameButtons = new Button[BUTTON_AMOUNT_PAUSED];
		this.currentButtons = this.mainButtons;
		
		this.mainButtons[0] = new Button(startX,y1,BUTTONCOLUMN_WIDTH,BUTTONCOLUMN_HEIGHT,null,"New Game");
		this.mainButtons[1] = new Button(startX,y2,BUTTONCOLUMN_WIDTH,BUTTONCOLUMN_HEIGHT,null,"Continue");
		this.mainButtons[2] = new Button(startX,y3,BUTTONCOLUMN_WIDTH,25,null,"Options");
		this.mainButtons[3] = new Button(startX,y4,BUTTONCOLUMN_WIDTH,25,null,"Exit");
		this.mainButtons[0].paddingX = 30;
		this.mainButtons[1].paddingX = 40;
		this.mainButtons[2].paddingX = 46;
		this.mainButtons[3].paddingX = 70;
		this.mainButtons[2].paddingY = 25;
		this.mainButtons[3].paddingY = 25;
	}

	private void initImages() {
		BufferedImage spriteSheet;
		try {
			this.fontispieceImage = ImageIO.read(getClass().getResource("/images/BE2Metal.png")); 
			this.background = ImageIO.read(getClass().getResource("/images/moody.png")); 
			this.stackBackground = ImageIO.read(getClass().getResource("/images/gothicMenuButtons.png")); 
			this.highlight = ImageIO.read(getClass().getResource("/images/highlightButton.png")); 
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void draw() {
		switch (this.game.gameState ) {
			case MENU:
			case OPTIONS:
			case PAUSE:
				this.game.g.setFont(BUTTON_FONT);
				this.game.g.drawImage(background, 0, 0, this.game.getWidth(), this.game.getHeight(), null);
				this.game.g.drawImage(fontispieceImage, fontispieceX, fontispieceY, FONTISPIECE_WIDTH, FONTISPIECE_HEIGHT, null);
				this.game.g.drawImage(stackBackground, buttonBGIX, buttonBGIY, buttonBGIHeight, buttonBGIWidth, null);
				
				for(Button button : this.currentButtons) {
					this.game.g.setColor(BUTTON_HIGHLIGHT_COLOR);
					if(button.highlighted) {
						this.game.g.drawImage(highlight, button.x, button.y , button.width, button.height, null);
					}
					this.game.g.drawRect(button.x, button.y , button.width, button.height);
					this.game.g.setColor(BUTTON_FONT_COLOR);
					this.game.g.drawString(button.text, button.x+button.paddingX, button.y+button.paddingY);
					
				}
				
				break;
			default:
				return;
			
		}

	}

	public void update() {
		if (this.startX <= 0) {
			initPositions();
			initButtons();
		}
		mousePoint.x = game.mouseX;
		mousePoint.y=game.mouseY;
		for (Button button: this.currentButtons) {
			if (button.contains(mousePoint)) {
				button.highlighted = true;
			}else {
				button.highlighted = false;
			}
		}

		//showActionPromptDelay = showActionPromptDelay > 0 ? showActionPromptDelay - 1 : showActionPromptDelay;

	}
	
	private void activateButton(int buttonID) {
		switch(this.game.gameState) {
		case MENU:
			switch(buttonID) {
			case 0:
				this.game.switchState(Game.GState.PLAY);
				System.out.println("RESET GAME");
				return;
			case 1:
				this.game.switchState(Game.GState.PLAY);
				return;
			case 2:
				this.game.switchState(Game.GState.OPTIONS);
				break;
			case 3:
				System.exit(0);
			}
		}
	}
	
	public void handleClick(int button, int down) {
		switch (button) {
		case 1:// LC
			for (int i = 0;i<this.currentButtons.length;i++) {
				if (currentButtons[i].highlighted) {
					activateButton(i);
				}
			}
			
			break;
		case 2:
			

			break;
		case 3:
			

			break;
		default:
			break;

		}

	}

	

}
