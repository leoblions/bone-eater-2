package main;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;

public class Utils {

	public final static String COL_SEPARATOR = ",";
	public final static String ROW_SEPARATOR = "\n";
	public final static String IMAGE_PLACEHOLDER = "/images/ph.png";
	public final static boolean MOCK_WRITE_FILE = false;
	public final static boolean WRITE_NEW_FILE = false;

	public Utils() {

	}

	public static int clamp(int min, int max, int test) {
		return (test > max) ? max : (test < min) ? min : test;
	}
	
	/**
	 * 
	 * @param origList LinkedList of integer arrays
	 * @return 2d integer array
	 */
	public static int[][] LOIAtoint2DA(LinkedList<int[]> origList){
		try {
			int cols = origList.get(0).length;
			int rows = origList.size();
			int[][] outputArray = new int[rows][cols];
			for(int y = 0;y<rows;y++) {
				for(int x = 0;x<cols;x++) {
					outputArray[y][x]=origList.get(y)[x];
				}
			}
			return outputArray;
		}catch(Exception e){
			System.out.println("LOIA has invalid dimensions");
			return null;
			
		}
		
	}
	
	public static int[][] initGrid(int rows , int cols, int fill) {
		int[][] grid=new int[rows][cols];
		for(int y = 0;y< rows;y++) {
			for(int x = 0;x< cols;x++) {
				grid[y][x]=fill;
				
			}
		}
		return grid;
		
	}
	
	/**
	 * 
	 * @param origList LinkedList of integer arrays
	 * @return 2d integer array
	 */
	public static int[][] ALOItoint2DA(ArrayList<int[]> origList){
		try {
			int cols = origList.get(0).length;
			int rows = origList.size();
			int[][] outputArray = new int[rows][cols];
			for(int y = 0;y<rows;y++) {
				for(int x = 0;x<cols;x++) {
					outputArray[y][x]=origList.get(y)[x];
				}
			}
			return outputArray;
		}catch(Exception e){
			System.out.println("ALOI has invalid dimensions");
			return null;
			
		}
		
	}

	
	public static ArrayList<int[]> int2DAtoALOI(int int2DA[][]){
		if(int2DA ==null) {
			System.err.println("2DAtoALOI array is null");
			return null;
		}
		int rows = int2DA.length;
		if(rows ==0) {
			return null;
		}
		int cols=int2DA[0].length;
		ArrayList<int[]> output = new ArrayList<>();
		for (int[] row : int2DA) {
			output.add(row);
		}
		return output;
	}
	
	
	
	public static String[][] openCSVto2DA(String filePath) throws FileNotFoundException {
		File dataFile = new File(filePath);
		if (!dataFile.exists()) {
			System.out.println("file not found " + filePath);
		}
		String[][] outerArray = null;
		Scanner scanner;
		LinkedList<String[]> allLines = new LinkedList<>();
		int lineLength = -1;
		scanner = new Scanner(dataFile);
		while (scanner.hasNextLine()) {
			String[] currentLineStrings;

			String data = scanner.nextLine();
			currentLineStrings = data.split(COL_SEPARATOR);
			if (lineLength == -1) {
				lineLength = currentLineStrings.length;
			}
			allLines.add(currentLineStrings);
			// System.out.println(currentLineStrings);
		}
		int outerArrayLength = allLines.size();
		outerArray = new String[outerArrayLength][lineLength];
		int rows = outerArrayLength;
		int cols = lineLength;
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				outerArray[y][x] = allLines.get(y)[x];
			}
		}
//		catch (FileNotFoundException e) {
//			System.out.printf("File %s does not exist. \n",filePath);
//			e.printStackTrace();
//		}

		return outerArray;

	}

	public static String[] getStringsFromFile(String filePath) {
		File dataFile = new File(filePath);
		if (!dataFile.exists()) {
			System.out.println("file not found " + filePath);
		}
		String[] outputArray = null;
		Scanner scanner;
		LinkedList<String> allLines = new LinkedList<>();

		try {
			scanner = new Scanner(dataFile);
			while (scanner.hasNextLine()) {
				String currentLineStrings;

				String data = scanner.nextLine();
				currentLineStrings = data;
				allLines.add(currentLineStrings);

			}
			int outerArrayLength = allLines.size();
			outputArray = new String[outerArrayLength];
			int rows = outerArrayLength;
			for (int y = 0; y < rows; y++) {
				outputArray[y] = allLines.get(y);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputArray;

	}

	public static int[][] openCSVto2DAInt(String filePath) throws Exception {

		String[][] strings2DA = openCSVto2DA(filePath);
		if (null == strings2DA) {
			throw new Exception("openCSVto2DAInt: no valid data");
		}
		int rows = strings2DA.length;
		int cols = strings2DA[0].length;
		int[][] int2DA = new int[rows][cols];

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				int2DA[y][x] = Integer.parseInt(strings2DA[y][x]);
			}
		}

		return int2DA;

	}

	public static ArrayList<int[]> openCSVto2DAIntListJagged(String filePath) throws Exception {

		String[] stringsArray = getStringsFromFile(filePath);
		if (null == stringsArray) {
			throw new Exception("openCSVto2DAInt: no valid data");
		}
		int rows = stringsArray.length;

		ArrayList<int[]> intListOfArrays = new ArrayList<>();

		String[] rowSplitToStrings = null;

		int[] rowSplitToInts = null;

		for (int y = 0; y < rows; y++) {
			rowSplitToStrings = stringsArray[y].split(COL_SEPARATOR);

			int cols = rowSplitToStrings.length;
			rowSplitToInts = new int[cols];

			for (int i = 0; i < cols; i++) {
				rowSplitToInts[i] = Integer.parseInt(rowSplitToStrings[i]);
			}
			intListOfArrays.add(rowSplitToInts);
		}

		return intListOfArrays;

	}

	public static int[][] initBlankGrid(int cols, int rows, int fill) {
		int[][] grid = new int[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				grid[r][c] = fill;

			}
		}
		return grid;
	}

	public int[][] initBlankGridD(int cols, int rows, int fill) {
		int[][] grid = new int[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				grid[r][c] = fill;

			}
		}
		return grid;
	}

	public static boolean[][] initBlankGrid(int cols, int rows, boolean fill) {
		boolean[][] grid = new boolean[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				grid[r][c] = fill;

			}
		}
		return grid;
	}

	public static void print2DAofStrings(String[][] str2DA) {

		int rows = str2DA.length;
		int cols = str2DA[0].length;
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {

				System.out.printf("%s,", str2DA[y][x]);
			}
			System.out.println();
		}

	}

	public static int[][] convert2DAstringTo2DAint(String[][] str2DA) {

		int rows = str2DA.length;
		int cols = str2DA[0].length;
		int[][] outputIntArray = new int[rows][cols];
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {

				// System.out.printf("%s,",str2DA[y][x]);
				outputIntArray[y][x] = Integer.parseInt(str2DA[y][x]);
			}
			// System.out.println();
		}
		return outputIntArray;

	}

	public static void writeInt2DAToCSV_0(int[][] int2DA, String filePath) throws IOException {
		File fhandle = null;
		FileWriter writer = null;
		if (MOCK_WRITE_FILE) {
			filePath = filePath + "_mock.csv";
		}
		try {
			fhandle = new File(filePath);
			writer = new FileWriter(filePath);
			if (fhandle.createNewFile()) {
				System.out.println("File created: " + fhandle.getName());

			} else {
				System.out.println("File already exists. deleting old one");
				fhandle.delete();
				fhandle.createNewFile();

			}
			if (!fhandle.canWrite()) {
				System.err.println("Can't write the file " + filePath);
			}
			int rows = int2DA.length;
			int cols = int2DA[0].length;
			StringBuilder sb = new StringBuilder();
			String currentPart;
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < cols; x++) {

					currentPart = Integer.toString(int2DA[y][x]);
					sb.append(currentPart);
					if (x != cols - 1) {
						sb.append(COL_SEPARATOR);
					} else {
						sb.append(ROW_SEPARATOR);
					}

				}
				// System.out.println("print to file" +sb.toString());
				writer.write(sb.toString());
				writer.flush();
				sb = new StringBuilder();
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

	public void setPermissionsWindows(String myPath, String permissionsStr) {

		Path dir = Paths.get(myPath);
		try {
			// Get the ACL view
			AclFileAttributeView view = Files.getFileAttributeView(dir, AclFileAttributeView.class);
			// Get the current ACL entries
			List<AclEntry> acl = view.getAcl();
			// Modify the ACL to remove write permissions
			for (int i = 0; i < acl.size(); i++) {
				AclEntry entry = acl.get(i);
				if (entry.principal().getName().equals("Everyone")) {
					Set<AclEntryPermission> permissions = entry.permissions();
					permissions.remove(PosixFilePermission.OTHERS_WRITE);
					AclEntry newEntry = AclEntry.newBuilder().setType(entry.type()).setPrincipal(entry.principal())
							.setPermissions(permissions).build();
					acl.set(i, newEntry);
				}
			}
			// Set the updated ACL
			view.setAcl(acl);
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public static void setPermissions(String myPath, String permissionsStr) {
		if (permissionsStr == null) {
			permissionsStr = "rwxrwxrwx";
		}
		Path dir = Paths.get(myPath);
		try {
			// Set read-only permission
			Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(permissionsStr);
			Files.setPosixFilePermissions(dir, permissions);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeInt2DAToCSV(int[][] int2DA, String filePath) throws IOException {
		File fhandle = null;
		FileWriter writer = null;
		if (MOCK_WRITE_FILE) {
			filePath = filePath + "_mock.csv";
		}
		try {
			fhandle = new File(filePath);
			writer = new FileWriter(filePath);
			if (WRITE_NEW_FILE) {
				if (fhandle.createNewFile()) {
					System.out.println("File created: " + fhandle.getName());

				} else {
					System.out.println("File already exists. deleting old one");
					fhandle.delete();
					fhandle.createNewFile();

				}
				if (!fhandle.canWrite()) {
					System.err.println("Can't write the file " + filePath);
				}
			}
			int rows = int2DA.length;
			int cols = int2DA[0].length;
			StringBuilder sb = new StringBuilder();
			String currentPart;
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < cols; x++) {

					currentPart = Integer.toString(int2DA[y][x]);
					sb.append(currentPart);
					if (x != cols - 1) {
						sb.append(COL_SEPARATOR);
					} else {
						sb.append(ROW_SEPARATOR);
					}

				}
				// System.out.println("print to file" +sb.toString());
				writer.write(sb.toString());
				writer.flush();
				sb = new StringBuilder();
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

	public static String getLevelresourceFilename(int level, String prefix, String suffix) {
		return prefix + String.format("%03d", level) + suffix;
	}

	public static boolean checkFolderExists(String dirpath) {
		Path path = Paths.get(dirpath);

		return Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * 
	 * @param dirpath
	 * @return true if created a directory. False if failed, or already exists
	 */
	public static boolean createDirectoryIfNotExist(String dirpath) {
		if (!checkFolderExists(dirpath)) {
			Path path = Paths.get(dirpath);
			new File(dirpath).mkdirs();
			System.out.println("created folder " + dirpath);
			return true;

		} else {
			System.out.println("did not create folder " + dirpath);
		}

		return false;
	}

	public static ArrayList<String> splitStringIntoLinesAtWordEnding(String inputString, int lengthLimit) {
		String[] words = inputString.split(" ");
		int currWord = 0;
		int lastWord = words.length - 1;
		String currentLine = "";
		String testLine = "";

		ArrayList<String> lines = new ArrayList<String>();
		while (currWord <= lastWord) {

			testLine += words[currWord] + " ";
			int tlLength = testLine.length();
			// System.out.println(tlLength);
			// System.out.println(currentLine);
			if (tlLength > lengthLimit) {
				// make new line
				lines.add(currentLine);
				currentLine = "";
				testLine = "";
			} else {
				// append to current line
				currentLine = testLine;

				currWord++;
			}
		}
		if (!testLine.equals("")) {
			// make new line
			lines.add(testLine);
			currentLine = "";
			testLine = "";
		}
		return lines;
	}

	/**
	 * return visible screen area in world tiles X,Y,X,Y
	 * 
	 * @return
	 */
	public static int[] getVisibleArea(Game gp) {
		int ULCX = gp.cameraX / gp.tilegrid.tileSize;
		int ULCY = gp.cameraY / gp.tilegrid.tileSize;
		int BRCX = (gp.cameraX + gp.WIDTH) / gp.tilegrid.tileSize;
		int BRCY = (gp.cameraY + gp.HEIGHT) / gp.tilegrid.tileSize;
		return new int[] { ULCX, ULCY, BRCX, BRCY };

	}

	

	public static <T> T[] concatenate(T[] a, T[] b) {
	    int aLen = a.length;
	    int bLen = b.length;

	    @SuppressWarnings("unchecked")
	    T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
	    System.arraycopy(a, 0, c, 0, aLen);
	    System.arraycopy(b, 0, c, aLen, bLen);

	    return c;
	}

	

	public static boolean createFileIfNotExist(String subdir, String filename) {
		Path filePath = Paths.get(subdir, filename);
		File file = new File(filePath.toString());
		try {
			return file.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create file: " + filePath);
			e.printStackTrace();
			return false;
		}

	}

	


	public static <T> boolean contains(T[] collection, T query) {
		// equivalent to Python's "in" keyword using generics
		// test if a primative variable is in an array
		for (T test : collection) {
			if (test == query) {
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<int[]> loadRecordsFromFile(String fileName) throws FileNotFoundException {
		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		ArrayList<int[]> recordsNew =null;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), fileName);
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setReadable(true);
			int [][] allRecordsTmp = Utils.openCSVto2DAInt(completePath.toString());
			recordsNew = new ArrayList<>(Arrays.asList(allRecordsTmp));
			
		}catch(FileNotFoundException e) {
			// File error
			throw e;
		} catch (Exception e) {
			// Misc error
			e.printStackTrace();
		}
		
		System.out.println("Load file data " + dataPath.toString());
		return recordsNew;
		
	}
	
	
	
	public static void saveRecordsToFile(String fileName, ArrayList<int[]>dataALOI) {
		dataALOI.removeIf(Objects::isNull);
		int tg[][] = Utils.ALOItoint2DA(dataALOI);
		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), fileName);
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setWritable(true);
			Utils.writeInt2DAToCSV(tg, completePath.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Save file data " + dataPath.toString());
	}
	
	public static void saveMockRecordsToFile(String fileName, int fieldsAmount) {
		//dataALOI.removeIf(Objects::isNull);
		int tg[][] = new int[1][fieldsAmount];
		String dataFolderName = Game.LEVEL_DATA_SUBDIR;
		Utils.createDirectoryIfNotExist(dataFolderName);
		String currentWorkingDirectory = System.getProperty("user.dir");
		Path dataPath = Paths.get(currentWorkingDirectory, dataFolderName);
		Path completePath = Paths.get(dataPath.toString(), fileName);
		try {
			File parentDirAsFile = new File(dataPath.toString());
			parentDirAsFile.setWritable(true);
			Utils.writeInt2DAToCSV(tg, completePath.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Save file data " + dataPath.toString());
	}

	public static BufferedImage imageSetAlpha(BufferedImage input, int alpha) {
		int rows = input.getHeight();
		int cols = input.getWidth();
		// System.out.println("dims cols"+cols+"rows"+rows);
		BufferedImage output = new BufferedImage(cols, rows, input.getType());
		Graphics2D outputG = output.createGraphics();
		int r1, g1, b1, a1, r2, g2, b2, a2;
		int subscript = 0;
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				int clr = input.getRGB(x, y);
				// int alpha = (clr & 0xff000000) >> 24;
				int red = (clr & 0x00ff0000) >> 16;
				int green = (clr & 0x0000ff00) >> 8;
				int blue = clr & 0x000000ff;
				// System.out.println("color red"+red+"green"+green);
				int average = (red + green + blue) / 3;

				int newAlpha = alpha << 24;
				int redNew = average << 16;
				int greenNew = average << 8;
				int blueNew = average;
				int clrNew = redNew + greenNew + blueNew + newAlpha;

				output.setRGB(x, y, clrNew);
			}
		}
		return output;
	}

}
