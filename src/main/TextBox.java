package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class TextBox {
	private final String DEFAULT_TEXT = "A few times I've been around that track So it's not just goin' to happen like that 'Cause I ain't no hollaback girl I ain't no hollaback girl";
	private final int BORDER_WIDTH = 5;
	private final int DEF_LINE_LENGTH = 35;
	private final int DEF_LINE_AMOUNT = 5;
	private final int LINE_OFFSET_X = 5;
	private final int LINE_OFFSET_Y = 5;
	private final int LINE_HEIGHT = 15;
	private Color TEXT_BGC = new Color(0x0f, 0x0f, 0x0f, 0x11);
	private final boolean DEBUG_PRINT = false;
	int screenX, screenY, width, height;
	int lineLength, borderWidth,lineAmount,lineOffsetX,lineOffsetY,lineHeight;
	ArrayList<RasterString>rasterStrings;
	Game game;
	Rectangle backgroundRect;
	Color backgroundColor, backgroundColorBorder;
	String boxtext;
	Paragraph paragraph;
	
	private boolean visible;
	Stroke s1;

	public TextBox(Game game, int screenX, int screenY, int width, int height) {
		this.game = game;
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		this.visible = true;
		this.s1 = new BasicStroke(BORDER_WIDTH);
		this.paragraph = new Paragraph(DEF_LINE_AMOUNT,DEF_LINE_LENGTH,DEFAULT_TEXT);
		// background
		backgroundColor = new Color(100, 100, 100, 200);
		backgroundColorBorder = new Color(150, 100, 90, 200);
		backgroundRect = new Rectangle(screenX, screenY, width, height);
		updateRasterStrings();

	}

	public TextBox(Game game, int[] position) {
		//wx wy width, height
		this(  game,   position[0],   position[1],   position[2],   position[3]) ;

		boxtext = "THIS IS A TEST";

	}

	public void draw() {
		drawBackground();
		drawTextLines();

	}

	public void drawBackground() {

		game.g.setColor(backgroundColor);
		game.g.fillRect(backgroundRect.x, backgroundRect.y, backgroundRect.width, backgroundRect.height);
		//game.g.setStroke(s1);
		game.g.setColor(backgroundColorBorder);
		game.g.drawRect(backgroundRect.x, backgroundRect.y, backgroundRect.width, backgroundRect.height);
		drawTextLines();

	}
	
	private void drawTextLines() { 
		for(RasterString rs: this.rasterStrings) {
			rs.draw();
		}
	}
	
	private void updateRasterStrings() {
		this.rasterStrings = new ArrayList<RasterString>();
		int lineAmount = paragraph.size();
		int startX = this.screenX + LINE_OFFSET_X;
		int startY = this.screenY + LINE_OFFSET_Y;
		int currY = startY;
		for(String line: paragraph) {
			//RasterString currRS = new RasterString(game,line,startX,currY);

			RasterString currRS =  RasterString.RasterStringBGC(game,line,startX,currY,TEXT_BGC);
			this.rasterStrings.add(currRS);
			currY += LINE_HEIGHT;
		}
	}
	
	public void updateTextContent(String content) {
		paragraph.updateText(content);
		updateRasterStrings();
	}
	public void setTextContent(String content) {
		paragraph.updateText(content);
		updateRasterStrings();
	}

	public void update() {

	}

	public class Paragraph implements Iterable<String>{
		int linesAmount, lineLength;
		private String textContent;
		ArrayList<String> linesList;

		public Paragraph(int linesAmount, int lineLength, String textContent) {
			this.linesAmount = linesAmount;
			this.lineLength = lineLength;
			this.textContent = textContent;
			splitContentToList();
		}
		
		public void updateText(String newText) {
			this.textContent=newText;
			splitContentToList();
		}
		
		private String substringWithoutOOB(String instring,int start, int end) {
			int ssLen = instring.length();
			int newEnd = end;
			if(end > ssLen) {
				newEnd = ssLen;
			}
			return instring.substring(start,newEnd);
			
		}

		public void splitContentToList_0() {
			int contentLength = textContent.length();
			int startIndex = 0;
			String substring;
			ArrayList<String>stringsCollection = new ArrayList<>();
			while(startIndex < contentLength) {
				substring=substringWithoutOOB( textContent, startIndex, lineLength+startIndex);
				startIndex+=lineLength;
				stringsCollection.add(substring);
			}
			this.linesList = stringsCollection;

		}
		
		public void splitContentToList() {
			
			
			ArrayList<String>stringsCollectionL = Utils.splitStringIntoLinesAtWordEnding(textContent, DEF_LINE_LENGTH);
			this.linesList =  stringsCollectionL ;
			if(DEBUG_PRINT) {
				for(String line: stringsCollectionL) {
					System.out.println(line);
				}
			}
			

		}
		
		public int size() {
			return this.linesList.size();
		}
		public String get(int i) {
			return this.linesList.get(i);
		}

		public void cycleLines() {

		}

		public void cycleLinesWithNewLine() {

		}

		@Override
		public Iterator<String> iterator() {
			return linesList.iterator();
		}
	}

}
