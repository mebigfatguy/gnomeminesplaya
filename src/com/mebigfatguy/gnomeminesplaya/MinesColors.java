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

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum MinesColors {

    UNKNOWN("#F0ECE3"),
    ONE("#0000FF"),
    TWO("#00A000"),
    THREE("#FF0000"),
    FOUR("#00007F"),
    FIVE("#A00000"),
    SIX("#00FFFF"),
    SEVEN("#A000A0"),
    BRICK("#B72C2C"),
    BLACK("#000000"),
    BOMB("#FFFC00"),
    EMPTY("#C4B7A4");
    
    Color color;
    
    private MinesColors(String defaultColor) {
        color = Color.decode(defaultColor);
    }
    
    public Color getColor() {
        return color;
    }
    
    private void setColor(Color c) {
        color = c;
    }
    
    public static void loadColors(InputStream is) throws IOException {
        
        Properties colorProps = new Properties();
        colorProps.load(is);
        
        for (MinesColors c : MinesColors.values()) {
            c.setColor(Color.decode(colorProps.getProperty(c.name())));
        }    
    }
    
    public static byte[][] getColorTable() {
        
        int numColors = MinesColors.values().length;
        byte[][] table = new byte[3][numColors];
        
        MinesColors[] colors = MinesColors.values();
        for (int i = 0; i < numColors; ++i) {
            Color c = colors[i].getColor();
            table[0][i] = (byte)c.getRed();
            table[1][i] = (byte)c.getGreen();
            table[2][i] = (byte)c.getBlue(); 
        }
        
        return table;
    }
}
