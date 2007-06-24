/*
 * Curveto.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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

package net.sf.eps2pgf.postscript;

/**
 * cubic Bezier curve path section
 *
 * @author Paul Wagenaars
 */
public class Curveto extends PathSection implements Cloneable {
    
    /** Creates a new instance of Curveto */
    public Curveto() {
        for (int i = 0 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }
    
    /**
     * Creates a new instance of Curveto
     * @param controlCoor1 First Bezier control point
     * @param controlCoor2 Second Bezier control point
     * @param endCoor Endpoint
     */
    public Curveto(double[] controlCoor1, double[] controlCoor2, 
            double[] endCoor) {
        params[0] = controlCoor1[0];
        params[1] = controlCoor1[1];
        params[2] = controlCoor2[0];
        params[3] = controlCoor2[1];
        params[4] = endCoor[0];
        params[5] = endCoor[1];
    }
    
    /**
     * Get position in device space coordinates.
     * @return X- and Y-coordinate in device space. Returns {NaN, NaN} when
     *         this section has no coordinate.
     */
    public double[] deviceCoor() {
        double[] coor = new double[2];
        coor[0] = params[4];
        coor[1] = params[5];
        return coor;
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Curveto clone() {
        Curveto newSection = new Curveto();
        newSection.params = params.clone();
        return newSection;
    }
    
}
