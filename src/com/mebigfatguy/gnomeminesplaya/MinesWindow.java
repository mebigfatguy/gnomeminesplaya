package com.mebigfatguy.gnomeminesplaya;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MinesWindow {

	private static final int LARGE_COLUMNS = 30;
	private static final int LARGE_ROWS = 16;
	private static final int TOTAL_MINES = 99;

	private static final Point SETTINGS_MENU_OFFSET = new Point(90, 20);
	private static final Point PREFERENCES_OFFSET = new Point(90, 60);

	private static final double LARGE_X_FRAC = 0.42;
	private static final double LARGE_Y_FRAC = 0.47;

	private static final double CLOSE_X_FRAC = 0.60;
	private static final double CLOSE_Y_FRAC = 0.66;

	private static final int BORDER = 10;

	private static final double TOP_FRAC = 0.195;
	private static final double BOTTOM_FRAC = 0.847;

	private static final int GREY_EMPTY = 0;
	private static final int BLUE_ONE = 1;
	private static final int GREEN_TWO = 2;
	private static final int RED_THREE = 3;
	private static final int DKBLUE_FOUR = 4;
	private static final int BROWN_FIVE = 5;
	private static final int CYAN_SIX = 6;
	private static final int BRICK_FLAG = 7;
	private static final int YELLOW_BOMB = 8;
	private static final int BLACK = 9;
	private static final int DKGREY_UNKNOWN = 10;
	private static final int NUM_COLORS = 11;

	private Process minesProcess;
	private Point topLeft;
	private Rectangle boardBounds;
	private int tileSize;
	private final int[][] board = new int[30][16];
	private final byte[][] colorTable = new byte[3][NUM_COLORS];

	public MinesWindow() throws MinesException {
		launchMines();
		setupMines();
		for (int x = 0; x < LARGE_COLUMNS; x++) {
			Arrays.fill(board[x], DKGREY_UNKNOWN);
		}

		colorTable[0][GREY_EMPTY] = (byte)0xF0;
		colorTable[1][GREY_EMPTY] = (byte)0xEC;
		colorTable[2][GREY_EMPTY] = (byte)0xE3;

		colorTable[0][BLUE_ONE] = (byte)0x00;
		colorTable[1][BLUE_ONE] = (byte)0x00;
		colorTable[2][BLUE_ONE] = (byte)0xFF;

		colorTable[0][GREEN_TWO] = (byte)0x00;
		colorTable[1][GREEN_TWO] = (byte)0xA0;
		colorTable[2][GREEN_TWO] = (byte)0x00;

		colorTable[0][RED_THREE] = (byte)0xFF;
		colorTable[1][RED_THREE] = (byte)0x00;
		colorTable[2][RED_THREE] = (byte)0x00;

		colorTable[0][DKBLUE_FOUR] = (byte)0x00;
		colorTable[1][DKBLUE_FOUR] = (byte)0x00;
		colorTable[2][DKBLUE_FOUR] = (byte)0x7F;

		colorTable[0][DKBLUE_FOUR] = (byte)0x00;
		colorTable[1][DKBLUE_FOUR] = (byte)0x00;
		colorTable[2][DKBLUE_FOUR] = (byte)0x7F;

		colorTable[0][BROWN_FIVE] = (byte)0xA0;
		colorTable[1][BROWN_FIVE] = (byte)0x00;
		colorTable[2][BROWN_FIVE] = (byte)0x00;

		colorTable[0][CYAN_SIX] = (byte)0x00;
		colorTable[1][CYAN_SIX] = (byte)0xFF;
		colorTable[2][CYAN_SIX] = (byte)0xFF;

		colorTable[0][BRICK_FLAG] = (byte)0xB7;
		colorTable[1][BRICK_FLAG] = (byte)0x2C;
		colorTable[2][BRICK_FLAG] = (byte)0x2C;

		colorTable[0][BLACK] = (byte)0x00;
		colorTable[1][BLACK] = (byte)0x00;
		colorTable[2][BLACK] = (byte)0x00;

		colorTable[0][DKGREY_UNKNOWN] = (byte)0xC4;
		colorTable[1][DKGREY_UNKNOWN] = (byte)0xB7;
		colorTable[2][DKGREY_UNKNOWN] = (byte)0xA4;
	}

	public Point findMineLocation() {
		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == DKGREY_UNKNOWN) {
					Point loc = new Point(x, y);
					Iterator<Point> it = new NeighborIterator(loc, LARGE_COLUMNS, LARGE_ROWS);
					while (it.hasNext()) {
						Point neighbor = it.next();
						if (neighborDemandsFlag(neighbor)) {
							return loc;
						}
					}
				}
			}
		}

		return null;
	}

	public Point findSafeMove() {

		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == DKGREY_UNKNOWN) {
					Point loc = new Point(x, y);
					Iterator<Point> it = new NeighborIterator(loc, LARGE_COLUMNS, LARGE_ROWS);
					while (it.hasNext()) {
						Point neighbor = it.next();
						if (neighborIsSatisfied(neighbor)) {
							return loc;
						}
					}
				}
			}
		}

		return null;
	}

	public Point findSafestMove() {

		Point bestPoint = null;
		double bestScore = 0.0;

		List<Point> islandPoints = new ArrayList<Point>();

		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == DKGREY_UNKNOWN) {
					Point loc = new Point(x, y);

					Iterator<Point> it = new NeighborIterator(loc, LARGE_COLUMNS, LARGE_ROWS);
					double totalScore = 1.0;
					while (it.hasNext()) {
						Point neighbor = it.next();
						double score = neighborScore(neighbor);
						totalScore *= score;
					}

					if (bestScore == 1.0) {
						islandPoints.add(loc);
					} else if (totalScore > bestScore) {
						bestPoint = new Point(x, y);
						bestScore = totalScore;
					}
				}
			}
		}

		if (islandPoints.size() > 0) {
			double islandOdds = calcIslandOdds();
			if (islandOdds > bestScore ) {
				Collections.shuffle(islandPoints);
				return islandPoints.get(0);
			}
		}

		return bestPoint;
	}

	public boolean placeMine(int x, int y) throws MinesException {
		try {
			Robot r = new Robot();
			r.mouseMove(boardBounds.x + x * tileSize + tileSize / 2, boardBounds.y + y * tileSize + tileSize / 2);
			r.mousePress(InputEvent.BUTTON3_MASK);
			r.delay(100);
			r.mouseRelease(InputEvent.BUTTON3_MASK);

			return updateBoard(x, y);
		} catch (AWTException awte) {
			throw new MinesException("Failed to place mine flag (" + x + ", " + y + ")", awte);
		}
	}

	public boolean click(int x, int y) throws MinesException {
		try {
			Robot r = new Robot();
			r.mouseMove(boardBounds.x + x * tileSize + tileSize / 2, boardBounds.y + y * tileSize + tileSize / 2);
			r.mousePress(InputEvent.BUTTON1_MASK);
			r.delay(100);
			r.mouseRelease(InputEvent.BUTTON1_MASK);

			return updateBoard(x, y);
		} catch (AWTException awte) {
			throw new MinesException("Failed to click cell (" + x + ", " + y + ")", awte);
		}
	}

	public int[][] getBoard() {
		return board;
	}

	public boolean isFinished() {
		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == DKGREY_UNKNOWN) {
					return false;
				}
			}
		}

		return true;
	}

	private void launchMines() throws MinesException {
		try {
			Robot r = new Robot();
			Rectangle screenBounds = getScreenRect();
			BufferedImage origImage = createGrayscaleBitMap(r.createScreenCapture(screenBounds));
			r.delay(1000);
			minesProcess = Runtime.getRuntime().exec("gnomine");
			r.delay(2000);
			BufferedImage newImage = createGrayscaleBitMap(r.createScreenCapture(screenBounds));
			topLeft = calcFirstDifferencePt(origImage, newImage);

		} catch (Exception e) {
			throw new MinesException("Failed launching gnome mines", e);
		}
	}

	private void setupMines() throws MinesException {
		try {
			Robot robot = new Robot();

			if ((topLeft.x != 0) || (topLeft.y != 0)) {
				//Go FullScreen
				robot.keyPress(KeyEvent.VK_F11);
				robot.keyRelease(KeyEvent.VK_F11);
			}

			//Show the preference dialog
			robot.delay(1000);
			topLeft.x = 0;
			topLeft.y = 0;
			robot.mouseMove(SETTINGS_MENU_OFFSET.x, SETTINGS_MENU_OFFSET.y);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseMove(PREFERENCES_OFFSET.x, PREFERENCES_OFFSET.y);
			robot.delay(100);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);

			//Move mouse to Large radio button and click it
			robot.delay(1000);
			Rectangle bounds = getScreenRect();
			robot.mouseMove((int)(bounds.width * LARGE_X_FRAC), (int)(bounds.height * LARGE_Y_FRAC));
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);

			//Hit the close box
			robot.delay(1000);
			robot.mouseMove((int)(bounds.width * CLOSE_X_FRAC), (int)(bounds.height * CLOSE_Y_FRAC));
			robot.delay(2000);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);

			boardBounds = new Rectangle(BORDER, (int)(bounds.height * TOP_FRAC), bounds.width - 2 * BORDER, (int)(bounds.height * BOTTOM_FRAC) - (int)(bounds.height * TOP_FRAC));
			tileSize = boardBounds.width / 30;
			robot.delay(1000);
		} catch (AWTException awte) {
			throw new MinesException("Failed interacting with desktop", awte);
		}
	}

	private Rectangle getScreenRect() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		return gc.getBounds();
	}

	private Point calcFirstDifferencePt(BufferedImage origImage, BufferedImage newImage) {
		Point firstDiff = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

		int width = origImage.getWidth();
		int height = origImage.getHeight();

		DataBuffer origBuffer = origImage.getRaster().getDataBuffer();
		DataBuffer newBuffer = newImage.getRaster().getDataBuffer();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x > firstDiff.x) {
					break;
				}

				int pixelIndex = y * width + y;
				byte origPixel = (byte)origBuffer.getElem(pixelIndex);
				byte newPixel = (byte)newBuffer.getElem(pixelIndex);

				if (origPixel != newPixel) {
					if (x < firstDiff.x) {
						firstDiff.x = x;
					}
					if (y < firstDiff.y) {
						firstDiff.y = y;
					}
				}
			}

			if (y > firstDiff.y) {
				break;
			}
		}

		return firstDiff;
	}

	private BufferedImage createGrayscaleBitMap(BufferedImage srcImage) {
		BufferedImage dstImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		dstImage.getGraphics().drawImage(srcImage, 0, 0, srcImage.getWidth(), srcImage.getHeight(), Color.WHITE, null);

		return dstImage;
	}

	private boolean updateBoard(int clickX, int clickY) throws MinesException {
		try {
			Rectangle screenBounds = getScreenRect();
			Robot r = new Robot();
			BufferedImage screen = r.createScreenCapture(screenBounds);

			IndexColorModel colorModel = new IndexColorModel(8, colorTable[0].length, colorTable[0], colorTable[1], colorTable[2]);
			BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_BYTE_INDEXED, colorModel);

			for (int y = 0; y < LARGE_ROWS; y++) {
				for (int x = 0; x < LARGE_COLUMNS; x++) {
					if (board[x][y] == DKGREY_UNKNOWN) {
						int tX = boardBounds.x + x * tileSize;
						int tY = boardBounds.y + y * tileSize;

						image.getGraphics().drawImage(screen, 0, 0, tileSize, tileSize, tX, tY, tX + tileSize, tY + tileSize, null);
						DataBuffer buffer = image.getRaster().getDataBuffer();

						int[] colorCounts = new int[NUM_COLORS];
						Arrays.fill(colorCounts, 0);

						int color = DKGREY_UNKNOWN;
						int maxPixels = -1;

						for (int yy = 0; yy < tileSize; yy++) {
							for (int xx = 0; xx < tileSize; xx++) {

								int value = buffer.getElem(yy * tileSize + xx);
								colorCounts[value]++;

								for (int c = BLUE_ONE; c <= BLACK; c++) {
									if (colorCounts[c] > maxPixels) {
										maxPixels = colorCounts[c];
										color = c;
									}
								}
							}
						}

						if (maxPixels > 0) {
							if ((color == BLACK) || (color == YELLOW_BOMB)) {
								board[x][y] = Integer.MAX_VALUE;
								return true;
							} else {
								board[x][y] = color;
							}
						} else if (colorCounts[GREY_EMPTY] > colorCounts[DKGREY_UNKNOWN]) {
							board[x][y] = GREY_EMPTY;
						}

					}
				}
			}
			return false;
		} catch (AWTException awte) {
			throw new MinesException("Failed updating the board status", awte);
		}
	}

	private boolean neighborDemandsFlag(Point neighbor) {
		int neededBombs = board[neighbor.x][neighbor.y];
		if ((neededBombs == GREY_EMPTY) || (neededBombs == DKGREY_UNKNOWN) || (neededBombs == BRICK_FLAG)) {
			return false;
		}

		int freeSpaces = 0;
		int flags = 0;

		NeighborIterator it = new NeighborIterator(neighbor, LARGE_COLUMNS, LARGE_ROWS);

		while (it.hasNext()) {
			Point nn = it.next();
			int color = board[nn.x][nn.y];
			if (color == DKGREY_UNKNOWN) {
				freeSpaces++;
			} else if (color == BRICK_FLAG) {
				flags++;
			}
		}

		return (neededBombs - flags) == freeSpaces;
	}

	private boolean neighborIsSatisfied(Point neighbor) {
		int neededBombs = board[neighbor.x][neighbor.y];
		if ((neededBombs == GREY_EMPTY) || (neededBombs == DKGREY_UNKNOWN) || (neededBombs == BRICK_FLAG)) {
			return false;
		}

		int flags = 0;

		NeighborIterator it = new NeighborIterator(neighbor, LARGE_COLUMNS, LARGE_ROWS);

		while (it.hasNext()) {
			Point nn = it.next();
			int color = board[nn.x][nn.y];
			if (color == BRICK_FLAG) {
				flags++;
			}
		}

		return (neededBombs == flags);
	}

	private double neighborScore(Point neighbor) {
		int neededBombs = board[neighbor.x][neighbor.y];
		if ((neededBombs == GREY_EMPTY) || (neededBombs == DKGREY_UNKNOWN) || (neededBombs == BRICK_FLAG)) {
			return 1.0;
		}

		double score = 1.0;
		int flags = 0;
		int freeSpaces = 0;

		NeighborIterator it = new NeighborIterator(neighbor, LARGE_COLUMNS, LARGE_ROWS);

		while (it.hasNext()) {
			Point nn = it.next();
			int color = board[nn.x][nn.y];
			if (color == BRICK_FLAG) {
				flags++;
			} else if (color == DKGREY_UNKNOWN) {
				freeSpaces++;
			}
		}

		if (freeSpaces == 0) {
			return 1.0;
		}

		return 1.0 - ((neededBombs - flags) / (double)freeSpaces);
	}

	private double calcIslandOdds() {

		int freeSpaces = 0;
		int flags = 0;

		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == DKGREY_UNKNOWN) {
					freeSpaces++;
				} else if (board[x][y] == BRICK_FLAG) {
					flags++;
				}
			}
		}

		return 1.0 - (TOTAL_MINES - flags) / (double)freeSpaces;
	}
}
