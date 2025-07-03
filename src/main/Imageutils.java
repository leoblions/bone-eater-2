package main;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;

public class Imageutils {

	public final static String COL_SEPARATOR = ",";
	public final static String ROW_SEPARATOR = "\n";
	public final static String IMAGE_PLACEHOLDER = "/images/ph.png";
	public final static boolean MOCK_WRITE_FILE = false;
	public final static boolean WRITE_NEW_FILE = false;
	Game game;

	public Imageutils(Game game) {
		this.game=game;

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

	/**
	 * 
	 * @param fileURL
	 * @param cols
	 * @param rows
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	public BufferedImage[] spriteSheetCutter(String fileURL, int cols, int rows, int width, int height)
			throws IOException {
		BufferedImage[] images = new BufferedImage[rows * cols];
		BufferedImage spriteSheet = null;
		try {
			spriteSheet = ImageIO.read(getClass().getResourceAsStream(fileURL));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int subscript = 0;
		BufferedImage tempImage = null;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int x = c * width;
				int y = r * height;
				tempImage = spriteSheet.getSubimage(x, y, width, height);
				images[subscript] = tempImage;
				subscript++;
			}
		}
		return images;
	}

	public BufferedImage[] spriteSheetCutterBW(String fileURL, int cols, int rows, int width, int height)
			throws IOException {
		BufferedImage[] images = new BufferedImage[rows * cols];
		BufferedImage spriteSheet = null;
		try {
			spriteSheet = ImageIO.read(getClass().getResourceAsStream(fileURL));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int subscript = 0;
		BufferedImage tempImage = null;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int x = c * width;
				int y = r * height;
				tempImage = convertBufferedImageBW(spriteSheet.getSubimage(x, y, width, height));
				images[subscript] = tempImage;
				subscript++;
			}
		}
		return images;
	}

	public static BufferedImage convertBufferedImageBW(BufferedImage input) throws IOException {
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
				int alpha = (clr & 0xff000000) >> 24;
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

	public BufferedImage[] spriteSheetCutterBlank(int cols, int rows, int width, int height) throws IOException {
		String fileURL = IMAGE_PLACEHOLDER;
		BufferedImage[] images = new BufferedImage[rows * cols];
		BufferedImage img = null;
		try {
			img = ImageIO.read(getClass().getResourceAsStream(fileURL));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int arrayLength = cols * rows;
		Graphics2D g2 = img.createGraphics();
		g2.drawImage(img, 0, 0, width, height, null);
		for (int i = 0; i < arrayLength; i++) {
			images[i] = img;
		}

		return images;
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

	public static BufferedImage[] appendArray(BufferedImage[] arr1, BufferedImage[] arr2) {
		int index = 0;
		int len1 = arr1.length;
		int len2 = arr2.length;
		int len3 = len1 + len2;
		BufferedImage[] arr3 = new BufferedImage[len3];
		for (BufferedImage b : arr1) {
			arr3[index] = b;
			index++;
		}
		for (BufferedImage b : arr2) {
			arr3[index] = b;
			index++;
		}
		return arr3;

	}

	public static BufferedImage flipBufferedImage(BufferedImage original, boolean flipHorizontally,
			boolean flipVertically) {
		// get dimensions
		int width = original.getWidth();
		int height = original.getHeight();
		// create blank image
		BufferedImage flipped = new BufferedImage(width, height, original.getType());
		// get context
		Graphics2D g2d = flipped.createGraphics();
		// create affine transform matrix, scaling image up or left
		AffineTransform transform = AffineTransform.getScaleInstance(flipHorizontally ? -1 : 1,
				flipVertically ? -1 : 1);
		// shift image in opposite direction than scale moved the origin
		transform.translate(flipHorizontally ? -width : 0, flipVertically ? -height : 0);
		g2d.drawImage(original, transform, null);
		g2d.dispose();
		return flipped;
	}
	
	public BufferedImage[] scaleImages(BufferedImage[] bufferedImages, int width, int height) {
		Image tmp_image;
		BufferedImage tmp_bimage;
		BufferedImage[] outputBufferedImage = new BufferedImage[bufferedImages.length];
		for (int i = 0; i < bufferedImages.length; i++) {
			if (bufferedImages[i] == null)
				continue;
			tmp_image = bufferedImages[i].getScaledInstance(width, height, Image.SCALE_SMOOTH);
			tmp_bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			tmp_bimage.getGraphics().drawImage(tmp_image, 0, 0, null);
			outputBufferedImage[i] = tmp_bimage;
		}
		return outputBufferedImage;
	}

	public BufferedImage[] characterSheetUDL4(String url, int width, int height) throws IOException {
		int rows = 3;
		int cols = 4;
		BufferedImage[] UDL = this.spriteSheetCutter(url, cols, rows, width, height);
		int lastIndex = UDL.length - 1;

		BufferedImage[] slice = Arrays.copyOfRange(UDL, cols *2, cols *3 +1);
		BufferedImage[] sliceReversed = new BufferedImage[cols];
		for (int i=0;i< slice.length-1;i++) {
			sliceReversed[i] = flipBufferedImage(slice[i],true,false);
			
		}
		BufferedImage[] UDLR = Utils.concatenate(UDL,sliceReversed);
		return UDLR;

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
