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
import java.util.Arrays;

public class MinesWindow {

	private static final int MENUBAR_HEIGHT = 20;
	private static final Point SETTINGS_MENU_OFFSET = new Point(90, 20);
	private static final Point FULLSCREEN_OFFSET = new Point(90, 40);
	private static final Point PREFERENCES_OFFSET = new Point(90, 60);

	private static final double LARGE_X_FRAC = 0.42;
	private static final double LARGE_Y_FRAC = 0.47;

	private static final double CLOSE_X_FRAC = 0.60;
	private static final double CLOSE_Y_FRAC = 0.66;

	private static final int LEFT_BORDER = 10;
	private static final int RIGHT_BORDER = 10;

	private static final double TOP_FRAC = 0.195;
	private static final double BOTTOM_FRAC = 0.847;

	private static final int LARGE_COLUMNS = 30;
	private static final int LARGE_ROWS = 16;

	private Process minesProcess;
	private Point topLeft;
	private Rectangle boardBounds;
	private int tileSize;
	private final int[][] board = new int[30][16];

	public MinesWindow() throws MinesException {
		launchMines();
		setupMines();
		for (int x = 0; x < LARGE_COLUMNS; x++) {
			Arrays.fill(board[x], -1);
		}
	}

	public void click(int x, int y) throws MinesException {
		try {
			Robot r = new Robot();
			r.mouseMove(boardBounds.x + x * tileSize + tileSize / 2, boardBounds.y + y * tileSize + tileSize / 2);
			r.mousePress(InputEvent.BUTTON1_MASK);
			r.delay(100);
			r.mouseRelease(InputEvent.BUTTON1_MASK);
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
				if (board[x][y] == -1) {
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

			boardBounds = new Rectangle(LEFT_BORDER, (int)(bounds.height * TOP_FRAC), bounds.width - 2 * LEFT_BORDER, (int)(bounds.height * BOTTOM_FRAC) - (int)(bounds.height * TOP_FRAC));
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
}
