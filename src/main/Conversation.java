package main;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;



public class Conversation {
	public static final String CONV_FILES_FOLDER = "conversations";
	public static final String DATA_FOLDER = "leveldata";
	public static final String CONV_FILES_PREFIX = "conv";
	public static final String CHAIN_FILES_PREFIX = "conversationChains";
	public static final String DATA_FILE_SUFFIX = ".cfg";
	public static final String CHAIN_FILE_SUFFIX = ".csv";
	public static final String CONV_FILES_SEP = "`";
	public static final int CONV_FILES_FIELDS = 3;
	public static final boolean LOAD_FIRST_ROOM = true;
	public static final boolean FREEZE_PLAYER_ON_CONVERSATION = true;
	
	//text box
	private final int TB_HEIGHT = 150;
	private final int TB_WIDTH = 300;
	private final int TB_PADDING = 30;
	
	// string subtitutions
	public static final String PC_NAME = "Mike";
	public static final String TOWN_NAME = "Milkington";
	public static final String CITY_NAME = "Garry";
	public static final String ACTOR = "ACTOR";
	public static final char DOUBLE_QUOTE = '"';
	public static final int ACTOR_NUMBER_STRING_LENGTH = 2;

	public int currentLineIter = 0;
	public int activeMessageIterator = 0;
	public int activeChainID = 0;
	public boolean displayTextBox = false;
	public boolean dialogTextChanged = false;
	public boolean chainActive = false;
	public int currentLine = 0;
	//public String currentDialogText = null;
	
	private String speakerString = "";
	private String textContent = "";
	private BufferedImage backgroundImage;
	
	//text boxx
	private Rectangle textBoxBackRect ;
	private Rectangle textBoxInnerRect ;
	
	// line wraping
	private final int LINE_LENGTH_CHARS = 25;
	private final int LINE_SPACING_Y = 15;
	//text rendering
	private final int Y_PADDING = 50;
	private final int X_PADDING = 38;
	
	private int changeMessageDelay = 0;
	private int changeMessageDelayMax = 50;
	

	Game game;
	ArrayList<String> convLines;
	String currentConvLine;
	ArrayList<String> boxLines; // each line in the box
	
	Integer[] activeChainArray = null;
	private int speakerNPC;
	private boolean showDialogBox=false;

	/*
	 * convXXX.cfg files: Just lines of dialog and no other data.
	 * 
	
		Dialog chains are loaded as needed by number.  They do not correspond to rooms or levels.
		Chain refers to the numbered file.
		Line refers to the line number within the file, top to bottom.
		boxLine is which line within the dialog box.
	 * 
	 * 
	 */

	public Conversation(Game game) {
		this.game = game;
		convLines = new ArrayList<>();
//		if (LOAD_FIRST_ROOM) {
//			loadDataFromFileConversationChains();
//			loadDataFromFileCurrentRoom();
//			printDialogChainsToConsole();
//			setupTextBox();
//		}
		
		try {
			backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/dialogBackground.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setupTextBox() {
		// needs to be called if game board is resized
		int screenX = (this.game.width / 2) - (TB_WIDTH/2);
		int screenY = this.game.height - (int)((float)TB_HEIGHT*1.5f);
		int innerH = TB_HEIGHT - (TB_PADDING*2);
		int innerW = TB_WIDTH - (TB_PADDING*2);
		textBoxBackRect = new Rectangle(screenX,screenY,TB_WIDTH,TB_HEIGHT);
		textBoxInnerRect = new Rectangle(screenX+TB_PADDING,screenY+TB_PADDING,innerW,innerH);
	}
	
	public boolean isDialogBoxVisible() {
		return this.showDialogBox;
	}

	public void draw() {
		// draw background
		
		if(showDialogBox) {
			game.g.drawImage(backgroundImage,textBoxBackRect.x , textBoxBackRect.y,
					textBoxBackRect.width, textBoxBackRect.height, null);
			int currY = Y_PADDING + textBoxBackRect.y;
			int currX = X_PADDING + textBoxBackRect.x;
			if (null!=this.boxLines &&this.boxLines.size()!=0) {
				for(String currLine: this.boxLines) {
					game.g.drawString(currLine, currX, currY);
					currY+=LINE_SPACING_Y;
				}
			}
			
		}
		
		
		

	}
	
	public void setSpeakerNPC(int entityKind){
		if (entityKind>0) {
			this.speakerNPC = entityKind;
		}
	}
	
	private void splitConvLinesToBoxLines() {
		LinkedList<String> words = new LinkedList<String>(List.of(this.currentConvLine.split(" ")));
		String currentBoxLine = "";
		LinkedList<String>boxLines = new LinkedList<>();
		for(String word:words) {
			System.out.println(word);
			
			
		}
		
		for(String word:words) {
			currentBoxLine += (word);
			currentBoxLine += (" ");
			if(currentBoxLine.length()>LINE_LENGTH_CHARS) {
				boxLines.add(currentBoxLine);
				currentBoxLine = "";
			}
			
			
		}if(currentBoxLine.length() !=0) {
			boxLines.add(currentBoxLine);
		}
		this.boxLines = new ArrayList<String>(boxLines);
	}

	public void update() {
		

		if (dialogTextChanged) {
			updateTextContent(currentConvLine);
			dialogTextChanged =false;
		}
		if(changeMessageDelay>0) {
			changeMessageDelay-=1;
		}
		

	}

	private void updateTextContent(String currentDialogText2) {
		// TODO Auto-generated method stub
		
	}

	public void startConversation(int chainID) {
		System.out.println("start conv");
		if (chainActive) {
			return;
			// don't allow this function to run if conversation already in progress
		}
		loadRecordsFromFile(chainID);
		this.boxLines = new ArrayList<>();
		this.boxLines.add(this.convLines.get(0));
		chainActive = true;
		
		activeChainID = chainID;
		chainActive = true;
		displayTextBox = true;
		dialogTextChanged = true;
		showDialogBox = true;
		
		currentConvLine = convLines.get(this.currentLineIter);
		for (String st:convLines) {
			System.out.println(st);
		}
		
		splitConvLinesToBoxLines() ;
		System.out.println(boxLines.get(0));
		changeMessageDelay = changeMessageDelayMax;
		
		try {
			
	
			
			if(FREEZE_PLAYER_ON_CONVERSATION) {
				game.frozen=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	


	public void advanceConversation() {
		if(chainActive && changeMessageDelay<=0) {
			activeMessageIterator +=1;
			System.out.println("advanceConversation "+currentLineIter);
			try {
				
				currentLineIter  +=1;
				if(currentLineIter < convLines.size()) {
					currentConvLine = convLines.get(this.currentLineIter);
					splitConvLinesToBoxLines() ;
					changeMessageDelay = changeMessageDelayMax;
				}else {
					conversationOver();
				}
				
				
			} catch (Exception e) {
				// no more lines
				conversationOver();
				return;
			
			}
		}
		

	}
	
	public void conversationOver() {
		System.out.println("conversationOver "+currentLineIter);
		this.boxLines = new ArrayList<>();
		splitConvLinesToBoxLines();
		chainActive = false;
		displayTextBox = false;
		this.showDialogBox=(false);
		game.frozen = false;
		game.brain.endConversationNPC(this.speakerNPC);
	}

	public void loadRecordsFromFile(int conversationID) {
		
		String filename = this.getConversationFileNameFromID(conversationID);
		String[] recordsAsStrings = Utils.getStringsFromFile(filename);
	
		List<String> convLinesList =  Arrays.asList(recordsAsStrings);
		this.convLines = new ArrayList< >(convLinesList);

	}

	private String getConversationFileNameFromID(int conversationID) {
		return CONV_FILES_PREFIX + String.format("%03d", conversationID) + DATA_FILE_SUFFIX;
	}

	private boolean folderExists(String folderName) {
		File file = new File(folderName);
		if (file.mkdir()) {
			System.out.println("folder created " + folderName);
			return true;
		}
		return false;

	}

	public String getDataFilename() {
		return Utils.getLevelresourceFilename(this.game.level, CONV_FILES_PREFIX, DATA_FILE_SUFFIX);
	}

	

	public void printRecordsToConsole() {
		for (String cr : this.convLines) {
			System.out.printf(" %s \n",cr);
		}
	}

	






	


}
