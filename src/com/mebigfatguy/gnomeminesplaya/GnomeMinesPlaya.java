/*
 * gnomeminesplaya - An app that plays the gnome mines game
 * Copyright 2011-2019 MeBigFatGuy.com
 * Copyright 2011-2019 Dave Brosius
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

import java.awt.Point;

import javax.swing.JOptionPane;

public class GnomeMinesPlaya {

	public static void main(String[] args) {

		try {
			MinesWindow mw = new MinesWindow();

			while (true) {

				boolean bomb = false;

				while (!bomb && !mw.isFinished() && !mw.userWantsTermination()) {

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
				}
				
				if (mw.isFinished()) {
				    mw.win();
				} else if (mw.userWantsTermination()) {
				    mw.terminate();
				    System.exit(0);
				}

				try { Thread.sleep(10000); } catch (InterruptedException ie) {}
				mw.restart();
			}
		} catch (MinesException me) {
			JOptionPane.showMessageDialog(null, me.getMessage());
		}
	}
}
