/*
 * GraphicsState.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import java.util.logging.*;
import net.sf.eps2pgf.postscript.errors.PSErrorNoCurrentPoint;

/**
 * Structure that holds the graphics state (graphic control parameter).
 * See PostScript manual table 4.1, p. 179 for more info.
 *
 * @author Paul Wagenaars
 */
public class GraphicsState implements Cloneable {
    Logger log = Logger.getLogger("global");
    
    /**
     * Current Transformation Matrix (CTM). All coordinates will be transformed by
     * this matrix.
     * TeX points are 1/72.27 inch, while PostScript points are 1/72 inch.
     * That is very annoying. The default CTM converts PostScript pt used
     * in eps files to centimeters.
     * [a b c d tx ty] -> x' = a*x + b*y + tx ; y' = c*x + d*y * ty
     */
    public PSObjectMatrix CTM = new PSObjectMatrix(2.54/72.0, 0 ,0, 2.54/72.0, 0, 0);
    
    /**
     * Current position in pt (before CTM is applied).
     */
    public double[] position = new double[2];
    
    /**
     * Current path
     */
    public Path path;
    
    /**
     * Current clipping path
     */
    public Path clippingPath;
    
    /**
     * Current font
     */
    public PSObjectFont font;
    
    /**
     * Creates a new default graphics state.
     */
    public GraphicsState() {
        path = new Path();
        clippingPath = new Path();
        font = new PSObjectFont();
    }
    
    
    /**
     * Move the current position to a new location (PostScript moveto operator)
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void moveto(double x, double y) {
        position[0] = x;
        position[1] = y;
        double[] transformed = CTM.apply(x, y);
        path.moveto(transformed[0], transformed[1], x, y);
    }
    
    /**
     * Draw a line to a point.
     * @param x X-coordinate (before CTM is applied)
     * @param y Y-coordinate (before CTM is applied)
     */
    public void lineto(double x, double y) {
        position[0] = x;
        position[1] = y;
        double[] transformed = CTM.apply(x, y);
        path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Add a curveto section to the current path
     * @param x1 X-coordinate of first control point
     * @param y1 Y-coordinate of first control point
     * @param x2 X-coordinate of second control point
     * @param y2 Y-coordinate of second control point
     * @param x3 X-coordinate of end point
     * @param y3 Y-coordinate of end point
     */
    public void curveto(double x1, double y1, double x2, double y2, 
            double x3, double y3) {
        position[0] = x3;
        position[1] = y3;
        double[] coor1 = CTM.apply(x1, y1);
        double[] coor2 = CTM.apply(x2, y2);
        double[] coor3 = CTM.apply(x3, y3);
        path.curveto(coor1, coor2, coor3);
    }
    
    /**
     * Draw a line to a relative point.
     * @param dx delta X-coordinate (before CTM is applied)
     * @param dy delta Y-coordinate (before CTM is applied)
     */
    public void rlineto(double dx, double dy) {
        position[0] = position[0] + dx;
        position[1] = position[1] + dy;
        double[] transformed = CTM.apply(position);
        path.lineto(transformed[0], transformed[1]);
    }
    
    /**
     * Retrieves the current position in device space. 
     * @return X- and Y-coordinate in device space (centimeters)
     */
    public double[] getCurrentPosInDeviceSpace() throws PSErrorNoCurrentPoint {
        if (path.sections.size() == 0) {
            throw new PSErrorNoCurrentPoint();
        }
        return path.sections.get(path.sections.size() - 1).deviceCoor();
    }
    
    /**
     * Intersects the area inside the current clipping path with the area
     * inside the current path.
     */
    public void clip() {
        if (clippingPath.sections.size() > 0) {
            log.info("Clip operator is not fully implemented. This might have an effect on the result.");
        }
        clippingPath = path.clone();
    }
    
    /**
     * Creates a deep copy of this object.
     * @throws java.lang.CloneNotSupportedException Indicates that clone is not (fully) supported. This should not happen.
     * @return Returns the deep copy.
     */
    public GraphicsState clone() throws CloneNotSupportedException {
        GraphicsState newState = new GraphicsState();
        newState.CTM = CTM.clone();
        newState.position = position.clone();
        newState.path = path.clone();
        newState.clippingPath = clippingPath.clone();
        newState.font = font.clone();
        return newState;
    }
}
