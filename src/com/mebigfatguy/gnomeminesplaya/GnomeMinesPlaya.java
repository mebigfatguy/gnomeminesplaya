package com.mebigfatguy.gnomeminesplaya;

import java.awt.Point;

import javax.swing.JOptionPane;

public class GnomeMinesPlaya {

	public static void main(String[] args) {

		try {
			MinesWindow mw = new MinesWindow();

			int x = 0;
			int y = 0;

			boolean bomb = false;

			while (!bomb && !mw.isFinished()) {

				Point mine = mw.findMineLocation();
				if (mine != null) {
					mw.placeMine(mine.x, mine.y);
				} else {
					Point move = mw.findSafeMove();
					if (move != null) {
						mw.click(move.x, move.y);
					} else {
						move = mw.findSafestMove();
						bomb = mw.click(move.x, move.y);
					}
				}
				Thread.sleep(4000);
			}
		} catch (MinesException me) {
			JOptionPane.showMessageDialog(null, me.getMessage());
		} catch (InterruptedException ie) {
		}
	}
}
