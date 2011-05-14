package com.mebigfatguy.gnomeminesplaya;

import java.security.SecureRandom;

import javax.swing.JOptionPane;

public class GnomeMinesPlaya {

	public static void main(String[] args) {

		try {
			MinesWindow mw = new MinesWindow();

			SecureRandom sr = new SecureRandom();
			int x = 0;
			int y = 0;

			boolean bomb = false;

			while (!bomb && !mw.isFinished()) {

				boolean isUnknown = false;
				while (!isUnknown) {
					x = sr.nextInt(30);
					y = sr.nextInt(16);
					isUnknown = mw.getBoard()[x][y] == -1;
				}

				bomb = mw.click(x, y);
				Thread.sleep(4000);
			}
		} catch (MinesException me) {
			JOptionPane.showMessageDialog(null, me.getMessage());
		} catch (InterruptedException ie) {
		}
	}
}
