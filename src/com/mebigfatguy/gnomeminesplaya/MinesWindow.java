/*
 * gnomeminesplaya - An app that plays the gnome mines game
 * Copyright 2011-2013 MeBigFatGuy.com
 * Copyright 2011-2013 Dave Brosius
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MinesWindow {

	private static final int LARGE_COLUMNS = 30;
	private static final int LARGE_ROWS = 16;
	private static final int TOTAL_MINES = 99;

	private static final Point SETTINGS_MENU_OFFSET = new Point(90, 20);
	private static final Point PREFERENCES_OFFSET = new Point(90, 60);

	private static final int LARGE_X_OFFSET = -100;
	private static final int LARGE_Y_OFFSET = -38;

	private static final int CLOSE_X_OFFSET = 100;
	private static final int CLOSE_Y_OFFSET = 170;

	private Process minesProcess;
	private Point topLeft;
	private Rectangle boardBounds;
	private int tileSize;
	private final int[][] board = new int[30][16];
	private byte[][] colorTable;
	private final SecureRandom random = new SecureRandom();

	public MinesWindow() throws MinesException {
		launchMines();
		setupMines();
		initializeBoard();
		
		loadColorTable();
	}

	public void terminate() {
		minesProcess.destroy();
	}

	public void restart() throws MinesException {
		try {
			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_CONTROL);
			r.keyPress(KeyEvent.VK_N);
			r.delay(100);
			r.keyRelease(KeyEvent.VK_N);
			r.keyRelease(KeyEvent.VK_CONTROL);
			initializeBoard();
			r.delay(1000);

		} catch (AWTException awte) {
			throw new MinesException("Failed restarting game", awte);
		}
	}

	public Point findMineLocation() {
	    Point loc = new Point();
		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == MinesColors.UNKNOWN.ordinal()) {
					loc.x = x;
					loc.y = y;
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

	    Point loc = new Point();
		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == MinesColors.UNKNOWN.ordinal()) {
					loc.x = x;
					loc.y = y;
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

		Point bestPoint = new Point();
		double bestScore = 0.0;

		List<Point> islandPoints = new ArrayList<Point>();

		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == MinesColors.UNKNOWN.ordinal()) {
			        Point loc = new Point(x, y);

					Iterator<Point> it = new NeighborIterator(loc, LARGE_COLUMNS, LARGE_ROWS);
					double totalScore = 1.0;
					while (it.hasNext()) {
						Point neighbor = it.next();
						double score = neighborScore(neighbor);
						totalScore *= score;
					}

					if (totalScore == 1.0) {
						islandPoints.add(loc);
					} else if (totalScore > bestScore) {
						bestPoint.x = x;
						bestPoint.y = y;
						bestScore = totalScore;
					}
				}
			}
		}

		if (islandPoints.size() > 0) {
			double islandOdds = calcIslandOdds();
			if (islandOdds > bestScore ) {
			    Point firstIsland = islandPoints.remove(random.nextInt(islandPoints.size()));
			    Point island = firstIsland;
			    while (!islandPoints.isEmpty()) {
			        if ((island.x > 0) && (island.x < (LARGE_COLUMNS-1)) && (island.y > 0) && (island.y < (LARGE_ROWS-1))) {
			            return island;
			        }
			        island = islandPoints.remove(random.nextInt(islandPoints.size()));
			    }
			    return firstIsland;
			}
		}

		return bestPoint;
	}

	public boolean placeMine(int x, int y) throws MinesException {
		try {
			Robot r = new Robot();
			r.mouseMove(boardBounds.x + x * tileSize + tileSize / 2, boardBounds.y + y * tileSize + tileSize / 2);
			click(r, InputEvent.BUTTON3_MASK);
			
			r.delay(500);

			return updateBoard();
		} catch (AWTException awte) {
			throw new MinesException("Failed to place mine flag (" + x + ", " + y + ")", awte);
		}
	}

	public boolean click(int x, int y) throws MinesException {
		try {
			Robot r = new Robot();
			r.mouseMove(boardBounds.x + x * tileSize + tileSize / 2, boardBounds.y + y * tileSize + tileSize / 2);
			click(r, InputEvent.BUTTON1_MASK);
			
			r.delay(500);

			return updateBoard();
		} catch (AWTException awte) {
			throw new MinesException("Failed to click cell (" + x + ", " + y + ")", awte);
		}
	}

	public boolean isFinished() {
		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == MinesColors.UNKNOWN.ordinal()) {
					return false;
				}
			}
		}

		return true;
	}
	
	public void win() throws MinesException {
	    try {
            Robot r = new Robot();
            r.delay(1000);
            type(r, KeyEvent.VK_G);
            type(r, KeyEvent.VK_N);
            type(r, KeyEvent.VK_O);
            type(r, KeyEvent.VK_M);
            type(r, KeyEvent.VK_E);
            type(r, KeyEvent.VK_M);
            type(r, KeyEvent.VK_I);
            type(r, KeyEvent.VK_N);
            type(r, KeyEvent.VK_E);
            type(r, KeyEvent.VK_S);
            type(r, KeyEvent.VK_P);
            type(r, KeyEvent.VK_L);
            type(r, KeyEvent.VK_A);
            type(r, KeyEvent.VK_Y);
            type(r, KeyEvent.VK_A);
            type(r, KeyEvent.VK_ENTER);
            r.delay(1000);
            type(r, KeyEvent.VK_ESCAPE);
	    } catch (AWTException awte) {
            throw new MinesException("Failed to enter high score name", awte);
        }
	}
	
	private void type(Robot r, int key) {
	    r.keyPress(key);
        r.delay(10);
        r.keyRelease(key);
        r.delay(10);
	}
	
	private void click(Robot r, int mask) {
        r.mousePress(mask);
        r.delay(100);
        r.mouseRelease(mask);
        r.delay(50);
	}
	
	private void loadColorTable() {
	    
	    InputStream is = null;
	    
	    try {
	        is = new BufferedInputStream(MinesWindow.class.getResourceAsStream("/com/mebigfatguy/gnomeminesplaya/colors.properties"));
	        MinesColors.loadColors(is);
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	    } finally {
	        Closer.close(is);
	        colorTable = MinesColors.getColorTable();
	    }
	}

	private void launchMines() throws MinesException {
		try {
			Robot r = new Robot();
			r.delay(1000);
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
			robot.delay(100);
            robot.mouseMove(PREFERENCES_OFFSET.x, PREFERENCES_OFFSET.y);
            robot.delay(100);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);

			//Move mouse to Large radio button and click it
			robot.delay(1000);
			Rectangle bounds = getScreenRect();
			robot.mouseMove(bounds.x + bounds.width/2 + LARGE_X_OFFSET, bounds.y + bounds.height/2 + LARGE_Y_OFFSET);
			click(robot, InputEvent.BUTTON1_MASK);

			//Hit the close box
			robot.delay(1000);
			robot.mouseMove(bounds.x + bounds.width/2 + CLOSE_X_OFFSET, bounds.y + bounds.height/2 + CLOSE_Y_OFFSET);
			robot.delay(2000);
			click(robot, InputEvent.BUTTON1_MASK);

			calculateBoardBounds();

			tileSize = boardBounds.width / 30;
			robot.delay(1000);
		} catch (AWTException awte) {
			throw new MinesException("Failed interacting with desktop", awte);
		}
	}

	private void initializeBoard() {
		for (int x = 0; x < LARGE_COLUMNS; x++) {
			Arrays.fill(board[x], MinesColors.UNKNOWN.ordinal());
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

	private boolean updateBoard() throws MinesException {
		try {
			Rectangle screenBounds = getScreenRect();
			Robot r = new Robot();
			BufferedImage screen = r.createScreenCapture(screenBounds);

			IndexColorModel colorModel = new IndexColorModel(8, colorTable[0].length, colorTable[0], colorTable[1], colorTable[2]);
			BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
            int[] colorCounts = new int[MinesColors.values().length];

			for (int y = 0; y < LARGE_ROWS; y++) {
				for (int x = 0; x < LARGE_COLUMNS; x++) {
					if (board[x][y] == MinesColors.UNKNOWN.ordinal()) {
						int tX = boardBounds.x + x * tileSize;
						int tY = boardBounds.y + y * tileSize;

						image.getGraphics().drawImage(screen, 0, 0, tileSize, tileSize, tX, tY, tX + tileSize, tY + tileSize, null);
						DataBuffer buffer = image.getRaster().getDataBuffer();

						Arrays.fill(colorCounts, 0);

						int color = MinesColors.UNKNOWN.ordinal();

						for (int yy = 3; yy < tileSize-3; yy++) {
							for (int xx = 3; xx < tileSize-3; xx++) {

								int value = buffer.getElem(yy * tileSize + xx);
								colorCounts[value]++;
							}
						}

                        int maxPixels = -1;
						for (int c = MinesColors.ONE.ordinal(); c <= MinesColors.BLACK.ordinal(); c++) {
							if (colorCounts[c] > maxPixels) {
								maxPixels = colorCounts[c];
								color = c;
							}
						}
						
						if (maxPixels > 0) {
							if ((color == MinesColors.BLACK.ordinal()) || (color == MinesColors.BOMB.ordinal())) {
								board[x][y] = Integer.MAX_VALUE;
								return true;
							} else {
								board[x][y] = color;
							}
						} else if (colorCounts[MinesColors.EMPTY.ordinal()] > colorCounts[MinesColors.UNKNOWN.ordinal()]) {
							board[x][y] = MinesColors.EMPTY.ordinal();
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
		if ((neededBombs == MinesColors.UNKNOWN.ordinal()) || (neededBombs == MinesColors.EMPTY.ordinal()) || (neededBombs == MinesColors.FLAG.ordinal())) {
			return false;
		}

		int unknownSpaces = 0;
		int flags = 0;

		NeighborIterator it = new NeighborIterator(neighbor, LARGE_COLUMNS, LARGE_ROWS);

		while (it.hasNext()) {
			Point nn = it.next();
			int color = board[nn.x][nn.y];
			if (color == MinesColors.UNKNOWN.ordinal()) {
				unknownSpaces++;
			} else if (color == MinesColors.FLAG.ordinal()) {
				flags++;
			}
		}

		return (neededBombs - flags) == unknownSpaces;
	}

	private boolean neighborIsSatisfied(Point neighbor) {
		int neededBombs = board[neighbor.x][neighbor.y];
		if ((neededBombs == MinesColors.UNKNOWN.ordinal()) || (neededBombs == MinesColors.EMPTY.ordinal()) || (neededBombs == MinesColors.FLAG.ordinal())) {
			return false;
		}

		int flags = 0;

		NeighborIterator it = new NeighborIterator(neighbor, LARGE_COLUMNS, LARGE_ROWS);

		while (it.hasNext()) {
			Point nn = it.next();
			int color = board[nn.x][nn.y];
			if (color == MinesColors.FLAG.ordinal()) {
				flags++;
			}
		}

		return (neededBombs == flags);
	}

	private double neighborScore(Point neighbor) {
		int neededBombs = board[neighbor.x][neighbor.y];
		if ((neededBombs == MinesColors.UNKNOWN.ordinal()) || (neededBombs == MinesColors.EMPTY.ordinal()) || (neededBombs == MinesColors.FLAG.ordinal())) {
			return 1.0;
		}

		int flags = 0;
		int unknownSpaces = 0;

		NeighborIterator it = new NeighborIterator(neighbor, LARGE_COLUMNS, LARGE_ROWS);

		while (it.hasNext()) {
			Point nn = it.next();
			int color = board[nn.x][nn.y];
			if (color == MinesColors.FLAG.ordinal()) {
				flags++;
			} else if (color == MinesColors.UNKNOWN.ordinal()) {
				unknownSpaces++;
			}
		}

		if (unknownSpaces == 0) {
			return 1.0;
		}

		return 1.0 - ((neededBombs - flags) / (double)unknownSpaces);
	}

	private double calcIslandOdds() {

		int unknownSpaces = 0;
		int flags = 0;

		for (int y = 0; y < LARGE_ROWS; y++) {
			for (int x = 0; x < LARGE_COLUMNS; x++) {
				if (board[x][y] == MinesColors.UNKNOWN.ordinal()) {
					unknownSpaces++;
				} else if (board[x][y] == MinesColors.FLAG.ordinal()) {
					flags++;
				}
			}
		}

		return 1.0 - (TOTAL_MINES - flags) / (double)unknownSpaces;
	}
	
	public boolean userWantsTermination() {
	    try {
	        minesProcess.exitValue();
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}

	private void calculateBoardBounds() throws MinesException {
		try {

			Rectangle screenBounds = getScreenRect();
			Robot r = new Robot();
			r.delay(500);
			BufferedImage screen = r.createScreenCapture(screenBounds);

			byte colorTable[][] = new byte[3][3];

			colorTable[0][0] = (byte)0xF0;
			colorTable[1][0] = (byte)0xEB;
			colorTable[2][0] = (byte)0xE2;

			colorTable[0][1] = (byte)0xCD;
			colorTable[1][1] = (byte)0xC2;
			colorTable[2][1] = (byte)0xAE;

			colorTable[0][2] = (byte)0xED;
			colorTable[1][2] = (byte)0x74;
			colorTable[2][2] = (byte)0x42;

			IndexColorModel colorModel = new IndexColorModel(8, colorTable[0].length, colorTable[0], colorTable[1], colorTable[2]);
			BufferedImage image = new BufferedImage(screenBounds.width, screenBounds.height, BufferedImage.TYPE_BYTE_INDEXED, colorModel);

			image.getGraphics().drawImage(screen, 0, 0, screenBounds.width, screenBounds.height, Color.WHITE, null);
            
			DataBuffer buffer = image.getRaster().getDataBuffer();

			int centerHBitsOffset = (screenBounds.height / 2) * screenBounds.width;

			int xOffset = 0;
			int color = buffer.getElem(centerHBitsOffset++);
			while (color == 0) {
				xOffset++;
				color = buffer.getElem(centerHBitsOffset++);
			}

			boardBounds = new Rectangle(xOffset, 0, screenBounds.width - 2 * xOffset, 0);

			int yOffset = 25;
			int vBitsOffset = yOffset * screenBounds.width + xOffset + 5;

			color = buffer.getElem(vBitsOffset);
			while (color == 0) {
				yOffset++;
				vBitsOffset += screenBounds.width;
				color = buffer.getElem(vBitsOffset);
			}
			/* skip the one pixel horizontal line */
			while (color != 0) {
	             yOffset++;
	                vBitsOffset += screenBounds.width;
	                color = buffer.getElem(vBitsOffset);
			}
			
            while (color == 0) {
                yOffset++;
                vBitsOffset += screenBounds.width;
                color = buffer.getElem(vBitsOffset);
            }

			int top = yOffset;

			yOffset = screenBounds.height - 5;
			vBitsOffset = yOffset * screenBounds.width + xOffset + 5;

			color = buffer.getElem(vBitsOffset);
			while (color == 0) {
				yOffset--;
				vBitsOffset -= screenBounds.width;
				color = buffer.getElem(vBitsOffset);
			}
			/* skip the one pixel horizontal line */
            while (color != 0) {
                yOffset--;
                vBitsOffset -= screenBounds.width;
                color = buffer.getElem(vBitsOffset);
            }
            
            while (color == 0) {
                yOffset--;
                vBitsOffset -= screenBounds.width;
                color = buffer.getElem(vBitsOffset);
            }           

			int bottom = yOffset;

			boardBounds.y = top;
			boardBounds.height = bottom - top;

		} catch (AWTException awte) {
			throw new MinesException("Failed determining board coordinates", awte);
		}
	}
}