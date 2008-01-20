/*
 * Indexed.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.postscript.colors;

import net.sf.eps2pgf.postscript.PSObject;
import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.PSObjectInt;
import net.sf.eps2pgf.postscript.PSObjectName;
import net.sf.eps2pgf.postscript.PSObjectString;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Implements Indexed color space.
 */
public class Indexed extends PSColor {
    
    /** Integer specifying the maximum valid index value. */
    private int hival;
    
    /**
     * If a string is used to specify the color, this table specifies the color
     * components for each index. If a procedure is as lookup table this value
     * is <code>null</code>.
     */
    private double[][] levels;
    
    /** Currently selected color. */
    private PSColor currentColor;
    
    /**
     * Create a new indexed color.
     * 
     * @param obj Object describing this color
     * 
     * @throws PSError A PostScript error occurred.
     */
    public Indexed(final PSObject obj) throws PSError {
        // Indexed color spaces must be defined using an array
        if (!(obj instanceof PSObjectArray)) {
            throw new PSErrorTypeCheck();
        }
        
        // Save the array specifying this color space
        PSObjectArray colorArray = (PSObjectArray) obj;
        
        // Extract base color space
        currentColor = ColorUtils.autoSetColorSpace(colorArray.get(1));
        
        // Extract the hival.
        hival = colorArray.get(2).toInt();
        if (hival > 4095) {
            throw new PSErrorRangeCheck();
        }
        
        // Extract lookup table
        PSObject lookup = colorArray.get(3);
        if (lookup instanceof PSObjectString) {
            //
            // Convert the lookup string to a more convenient format
            //
            PSObjectString str = colorArray.get(3).toPSString();
            int m = currentColor.getNrInputValues();
            if (m * (hival + 1) != str.length()) {
                throw new PSErrorRangeCheck();
            }
            levels = new double[hival + 1][m];
            for (int i = 0; i <= hival; i++) {
                for (int j = 0; j < m; j++) {
                    double value = (double) str.get(i * m + j) / 255.0;
                    levels[i][j] = value;
                }
            }
        } else {
            throw new PSErrorUnimplemented("Indexed color space with non-string"
                    + " lookup table.");
        }
        
    }

    /**
     * Creates an exact deep copy of this object.
     * 
     * @return an exact deep copy of this object.
     */
    @Override
    public Indexed clone() {
        Indexed copy;
        try {
            copy = (Indexed) super.clone();
            copy.currentColor = currentColor.clone();
            copy.levels = levels.clone();
        } catch (CloneNotSupportedException e) {
            copy = null;
        }
        
        return copy;
    }

    /**
     * Gets a single color level.
     * 
     * @param i Index of color component to get.
     * 
     * @return The color level.
     */
    @Override
    public double getLevel(final int i) {
        return currentColor.getLevel(i);
    }
    
    /**
     * Gets the equivalent CMYK levels of this color.
     * 
     * @return the CMYK
     */
    @Override
    public double[] getCMYK() {
        return currentColor.getCMYK();
    }

    /**
     * Gets a PostScript array describing the color space of this color.
     * 
     * @return array describing color space.
     */
    @Override
    public PSObjectArray getColorSpace() {
        PSObjectArray array = new PSObjectArray();
        array.addToEnd(new PSObjectName("Indexed", true));
        array.addToEnd(currentColor.getColorSpace());
        array.addToEnd(new PSObjectInt(hival));

        try {
            int m = currentColor.getNrComponents();
            PSObjectString str = new PSObjectString(m * (hival + 1));
            for (int i = 0; i <= hival; i++) {
                for (int j = 0; j < m; j++) {
                    char chr = (char) Math.round(levels[i][j] * 255.0);
                    str.set(i * m + j, chr);
                }
            }
            array.addToEnd(str);
        } catch (PSErrorRangeCheck e) {
            // this can never happen
        }
        
        return array;
    }

    /**
     * Gets the gray level equivalent of this color.
     * 
     * @return the gray level
     */
    @Override
    public double getGray() {
        return currentColor.getGray();
    }

    /**
     * Gets the equivalent HSB levels of this color.
     * 
     * @return the HSB
     */
    @Override
    public double[] getHSB() {
        return currentColor.getHSB();
    }

    /**
     * Gets the number of color components required to specify this color.
     * E.g. RGB has three and CMYK has four components.
     * 
     * @return the number of components for this color
     */
    @Override
    public int getNrComponents() {
        return currentColor.getNrComponents();
    }

    /**
     * Gets the number of values required to specify this color. For an RGB,
     * CMYK, ... this is the same as getNrComponents(), but for an indexed
     * color space the number of input values is only 1, while the number of
     * components is 3 (in case the indexed colors were specified as RGB
     * values).
     * 
     * @return The number of input values required to specify this color. 
     */
    @Override
    public int getNrInputValues() {
        return 1;
    }

    /**
     * Gets the equivalent RGB levels of this color.
     * 
     * @return the RGB
     */
    @Override
    public double[] getRGB() {
        return currentColor.getRGB();
    }
    
    /**
     * Changes the current color to another color in the same color space.
     * 
     * @param components the new color
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public void setColor(final double[] components)
            throws PSErrorRangeCheck {
        
        if (components.length != 1) {
            throw new PSErrorRangeCheck();
        }
        
        int index = (int) components[0];
        currentColor.setColor(levels[index]);
    }

}
