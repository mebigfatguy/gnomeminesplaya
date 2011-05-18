/*
 * gnomeminesplaya - An app that plays the gnome mines game
 * Copyright 2011 MeBigFatGuy.com
 * Copyright 2011 Dave Brosius
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NeighborIterator implements Iterator<Point> {

	private final Iterator<Point> it;

	public NeighborIterator(Point p, int columns, int rows) {

		List<Point> neighbors = new ArrayList<Point>();

		if (p.y > 0) {
			for (int n = -1; n < 2; n++) {
				int x = p.x + n;
				if ((x >= 0) && (x < columns)) {
					neighbors.add(new Point(x, p.y - 1));
				}
			}
		}

		for (int n = -1; n < 2; n+=2) {
			int x = p.x + n;
			if ((x >= 0) && (x < columns)) {
				neighbors.add(new Point(x, p.y));
			}
		}
		if (p.y < rows - 1) {
			for (int n = -1; n < 2; n++) {
				int x = p.x + n;
				if ((x >= 0) && (x < columns)) {
					neighbors.add(new Point(x, p.y + 1));
				}
			}
		}

		it = neighbors.iterator();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Point next() {
		return it.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
