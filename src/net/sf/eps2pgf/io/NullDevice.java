/*
 * NullDevice.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.io;

import net.sf.eps2pgf.postscript.GraphicsState;
import net.sf.eps2pgf.postscript.PSObjectDict;
import net.sf.eps2pgf.postscript.PSObjectMatrix;
import net.sf.eps2pgf.postscript.Path;
import net.sf.eps2pgf.postscript.colors.PSColor;

/**
 * Discards all output written to this device
 *
 * @author Paul Wagenaars
 */
public class NullDevice implements OutputDevice {
    
    /**
     * Implements PostScript clip operator
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     */
    public void clip(Path clipPath) {
        
    }
    
    /**
     * Returns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     */
    public PSObjectMatrix defaultCTM() {
        return new PSObjectMatrix(1, 0 ,0, 1, 0, 0);
    }
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     */
    public void init(GraphicsState gstate) {
        
    }
    
    /**
     * Finilize writing. Normally, this method writes a footer.
     */
    public void finish() {
        
    }
    
    /**
     * Fills a path using the non-zero rule
     * See the PostScript manual (fill operator) for more info.
     */
    public void fill(Path path) {
        
    }
    
    public void eoclip(Path clipPath) {
        
    }

    /**
     * Fills a path using the even-odd rule
     * See the PostScript manual (fill operator) for more info.
     */
    public void eofill(Path path) {
        
    }
    
    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     */
    public double[] eps2pgfGetMetrics() {
    	double[] dummyData = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    	return dummyData;
    }
    
    /**
     * Shading fill (shfill PostScript operator)
     */
    public void shfill(PSObjectDict dict, GraphicsState gstate) {
        
    }

    /**
     * Implements PostScript stroke operator
     */
    public void stroke(GraphicsState gstate) {
        
    }
    
    /**
     * Implements PostScript operator setlinecap
     */
    public void setlinecap(int cap) {
        
    }
    
    /**
     * Implements PostScript operator setlinejoin
     */
    public void setlinejoin(int join) {
        
    }
    
    /**
     * Sets the current color in gray, rgb or cmyk
     */
    public void setColor(PSColor color) {
        
    }
    
    /**
     * Sets the miter limit
     */
    public void setmiterlimit(double num) {
        
    }
    
    /**
     * Starts a new scope
     */
    public void startScope() {
        
    }
    
    /**
     * Ends the current scope scope
     */
    public void endScope() {
        
    }
    
    /**
     * Draws text
     */
    public void show(String text, double[] position, double angle,
            double fontsize, String anchor) {
        
    }
    
    /**
     * Draws a red dot (usefull for debugging, don't use otherwise)
     */
    public void drawDot(double x, double y) {
        
    }
    
    /**
     * Draws a blue rectangle (usefull for debugging, don't use otherwise)
     */
    public void drawRect(double[] lowerLeft, double[] upperRight) {
        
    }
    
}
