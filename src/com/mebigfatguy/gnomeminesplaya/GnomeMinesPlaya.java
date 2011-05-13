package com.mebigfatguy.gnomeminesplaya;

import javax.swing.JOptionPane;

public class GnomeMinesPlaya {

	public static void main(String[] args) {

		try {
			MinesWindow mw = new MinesWindow();

			while (!mw.isFinished()) {
				int x = (int)(Math.random() * 30);
				int y = (int)(Math.random() * 16);

				mw.click(x, y);
				Thread.sleep(4000);
			}
		} catch (MinesException me) {
			JOptionPane.showMessageDialog(null, me.getMessage());
		} catch (InterruptedException ie) {
		}
	}
}
