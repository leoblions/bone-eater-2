package main;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Game.GState;

public class Input {
	
	Game game;
	Image image;
	Image[] images;
	

	public Input(Game game) {
		this.game = game;
		this.initImages();
		Image image;
	}

	private void initImages() {
		try {
			this.image = ImageIO.read(getClass().getResource("/images/goldbug.png")); // Image in
																						// resources/images/icon.png
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void handleKeyEvent(KeyEvent e, boolean keyIsDown) {
		int keyCode = e.getKeyCode();
		if(e.isShiftDown()) {
			this.game.player.run=true;
		}
		
		if(  this.game.gameState==GState.CONSOLE) {
			this.game.console.sendKeyEvent(e);
			return;
		}
		
		switch (keyCode) {
		
		case KeyEvent.VK_SPACE:
			//System.out.println("space");
			this.game.player.run=true;
			break;

		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			this.game.player.directions[0] = keyIsDown;

			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			this.game.player.directions[1] = keyIsDown;

			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			this.game.player.directions[2] = keyIsDown;

			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			this.game.player.directions[3] = keyIsDown;

			break;
		case KeyEvent.VK_T:
			this.game.editor.editMode = 't';
			this.game.editor.updateStrings();
			System.out.println("edit tile");

			break;
		case KeyEvent.VK_F:
			this.game.player.attack();
			

			break;
		case KeyEvent.VK_O:
			this.game.editor.editMode = 'o';
			this.game.editor.updateStrings();
			System.out.println("edit trigger");

			break;
		case KeyEvent.VK_E:
			
			this.game.playerPressActivate = true;
			//System.out.println("player pressed activate");

			break;
		case KeyEvent.VK_BACK_QUOTE:
			//this.game.toggleConsole();
			
			this.game.toggleState(Game.GState.CONSOLE, Game.GState.PLAY);
			System.out.println(game.gameState.toString());
			break;
			
		case KeyEvent.VK_ESCAPE:
			this.game.toggleState(GState.MENU,GState.PLAY);
			System.out.println(this.game.gameState.toString());
			

			break;
		case KeyEvent.VK_N:
			this.game.editor.editMode = 'n';
			this.game.editor.updateStrings();
			

			break;
		case KeyEvent.VK_L:
			if(!keyIsDown)this.game.editor.toggleLatch();
			break;
		case KeyEvent.VK_CLOSE_BRACKET:
			//save
			if(!keyIsDown)this.game.editor.saveOrLoadData(true);
			break;
		case KeyEvent.VK_OPEN_BRACKET:
			//load
			if(!keyIsDown)this.game.editor.saveOrLoadData(false);
			break;
		}
		

	}

	public void draw() {

	}

	public void update() {
	
	
			
			
		
	}

}
