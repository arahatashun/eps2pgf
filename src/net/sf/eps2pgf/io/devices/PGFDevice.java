/*
 * PGFExport.java
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

package net.sf.eps2pgf.io.devices;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import net.sf.eps2pgf.postscript.Closepath;
import net.sf.eps2pgf.postscript.Curveto;
import net.sf.eps2pgf.postscript.GraphicsState;
import net.sf.eps2pgf.postscript.Lineto;
import net.sf.eps2pgf.postscript.Moveto;
import net.sf.eps2pgf.postscript.PSObjectArray;
import net.sf.eps2pgf.postscript.PSObjectDict;
import net.sf.eps2pgf.postscript.PSObjectMatrix;
import net.sf.eps2pgf.postscript.PSObjectReal;
import net.sf.eps2pgf.postscript.Path;
import net.sf.eps2pgf.postscript.PathSection;
import net.sf.eps2pgf.postscript.RadialShading;
import net.sf.eps2pgf.postscript.Shading;
import net.sf.eps2pgf.postscript.colors.PSColor;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Writes PGF files.
 * @author Paul Wagenaars
 */
public class PGFDevice implements OutputDevice {
    /** Coordinate format (used to format X- and Y-coordinates). */
    static final DecimalFormat COOR_FORMAT = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    /** Length format (used to format linewidth, dash, etc...). */
    static final DecimalFormat LENGTH_FORMAT = new DecimalFormat("#.###", 
            new DecimalFormatSymbols(Locale.US));
    
    /** Font size format (used to set fontsize in pt). */
    static final DecimalFormat FONTSIZE_FORMAT = new DecimalFormat("#.##",
            new DecimalFormatSymbols(Locale.US));
    
    /**
     * Colors (in range from 0.0 to 1.0) have at least 16-bit per channel
     * accuracy.
     */
    static final DecimalFormat COLOR_FORMAT = new DecimalFormat("#.######",
            new DecimalFormatSymbols(Locale.US));
    
    /** Recursion depth of \begin{pgfscope}...\end{pgfscope} commands. */
    private static int scopeDepth = 0;
    
    /** Output file. */
    private Writer out;
    
    /**
     * Creates a new instance of PGFExport.
     * @param wOut Writer to where the PGF code will be written.
     */
    public PGFDevice(final Writer wOut) {
        out = wOut;
    }
    
    /**
     * Returns a <b>copy</b> default transformation matrix (converts user space
     * coordinates to device space).
     * 
     * @return Default transformation matrix.
     */
    public PSObjectMatrix defaultCTM() {
        return new PSObjectMatrix(25.4 * 1000.0 / 72.0, 0.0, 0.0,
                25.4 * 1000.0 / 72.0, 0.0, 0.0);
    }
    
    /**
     * Internal Eps2pgf command: eps2pgfgetmetrics
     * It is meant for the cache device. When this command is issued, it will
     * return metrics information about the drawn glyph.
     * 
     * @return Metrics information about glyph.
     */
    public double[] eps2pgfGetMetrics() {
        double[] dummyData = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        return dummyData;
    }
    
    /**
     * Initialize before any other methods are called. Normally, this method
     * writes a header.
     * 
     * @param gstate the gstate
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void init(final GraphicsState gstate) throws PSError, IOException {
        // Force setting the line width by setting the last width to an
        // impossible/negative value.
        gstate.deviceData.setKey("pgf_last_linewidth", new PSObjectReal(-1.0));
        gstate.deviceData.setKey("pgf_last_dashpattern", new PSObjectArray());
        gstate.deviceData.setKey("pgf_last_dashoffset", new PSObjectReal(0.0));
        
        out.write("% Created by " + net.sf.eps2pgf.Main.getNameVersion() + " ");
        Date now = new Date();
        out.write("on " + now  + "\n");
        out.write("\\begin{pgfpicture}\n");
    }
    
    /**
     * Finalize writing. Normally, this method writes a footer.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void finish()  throws IOException {
        for (int i = 0; i < scopeDepth; i++) {
            out.write("\\end{pgfscope}\n");
        }
        out.write("\\end{pgfpicture}\n");
    }
    
    /**
     * Convert a Path to pgf code and write in to the output.
     * 
     * @param path The path.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    void writePath(final Path path) throws IOException, PSErrorUnimplemented {
        for (int i = 0; i < path.getSections().size(); i++) {
            PathSection section = path.getSections().get(i);
            if (section instanceof Moveto) {
                // If the path ends with a moveto, the moveto is ignored.
                if (i < (path.getSections().size() - 1)) {
                    String x = COOR_FORMAT.format(1e-4 * section.getParam(0));
                    String y = COOR_FORMAT.format(1e-4 * section.getParam(1));
                    out.write("\\pgfpathmoveto{\\pgfpoint{" + x + "cm}{" + y
                            + "cm}}\n");
                }
            } else if (section instanceof Lineto) {
                String x = COOR_FORMAT.format(1e-4 * section.getParam(0));
                String y = COOR_FORMAT.format(1e-4 * section.getParam(1));
                out.write("\\pgfpathlineto{\\pgfpoint{" + x + "cm}{" + y
                        + "cm}}\n");
            } else if (section instanceof Curveto) {
                String x1 = COOR_FORMAT.format(1e-4 * section.getParam(0));
                String y1 = COOR_FORMAT.format(1e-4 * section.getParam(1));
                String x2 = COOR_FORMAT.format(1e-4 * section.getParam(2));
                String y2 = COOR_FORMAT.format(1e-4 * section.getParam(3));
                String x3 = COOR_FORMAT.format(1e-4 * section.getParam(4));
                String y3 = COOR_FORMAT.format(1e-4 * section.getParam(5));
                out.write("\\pgfpathcurveto");
                out.write("{\\pgfpoint{" + x1 + "cm}{" + y1 + "cm}}");
                out.write("{\\pgfpoint{" + x2 + "cm}{" + y2 + "cm}}");
                out.write("{\\pgfpoint{" + x3 + "cm}{" + y3 + "cm}}\n");
            } else if (section instanceof Closepath) {
                out.write("\\pgfpathclose\n");
            } else {
                throw new PSErrorUnimplemented("Can't handle "
                        + section.getClass().getName());
            }
        }
    }
    
    /**
     * Implements PostScript stroke operator.
     * 
     * @param gstate Current graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    public void stroke(final GraphicsState gstate) throws IOException, PSError {
        updateDash(gstate);
        updateLinewidth(gstate);
        writePath(gstate.path);
        out.write("\\pgfusepath{stroke}\n");
    }
    
    /**
     * Implements PostScript clip operator.
     * Intersects the area inside the current clipping path with the area
     * inside the current path to produce a new, smaller clipping path.
     * 
     * @param clipPath the clip path
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     *                              (yet) implemented.
     */
    public void clip(final Path clipPath)
            throws IOException, PSErrorUnimplemented {
        writePath(clipPath);
        out.write("\\pgfusepath{clip}\n");
    }
    
    /**
     * Fills a path using the non-zero rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param path the path
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     *                              (yet) implemented.
     */
    public void fill(final Path path) throws IOException, PSErrorUnimplemented {
        writePath(path);
        out.write("\\pgfusepath{fill}\n");
    }
        
    /**
     * Set the current clipping path in the graphics state as clipping path in
     * the output document. The even-odd rule is used to determine which point
     * are inside the path.
     * 
     * @param clipPath Path to use for clipping
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     *                              (yet) implemented.
     */
    public void eoclip(final Path clipPath)
            throws IOException, PSErrorUnimplemented {
        writePath(clipPath);
        out.write("\\pgfseteorule\\pgfusepath{clip}\\pgfsetnonzerorule\n");
    }
    
    /**
     * Fills a path using the even-odd rule.
     * See the PostScript manual (fill operator) for more info.
     * 
     * @param path the path
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public void eofill(final Path path)
            throws IOException, PSErrorUnimplemented {
        writePath(path);
        out.write("\\pgfseteorule\\pgfusepath{fill}\\pgfsetnonzerorule\n");
    }
    
    /**
     * Shading fill (shfill PostScript operator).
     * 
     * @param dict Shading to use.
     * @param gstate Current graphics state.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void shfill(final PSObjectDict dict, final GraphicsState gstate)
            throws PSError, IOException {
        Shading shading = Shading.newShading(dict);
        if (shading instanceof RadialShading) {
            radialShading((RadialShading) shading, gstate);
        } else {
            throw new PSErrorUnimplemented("Shading of this type " + shading);
        }
    }
    
    /**
     * Create a radial shading.
     * 
     * @param shading The shading.
     * @param gstate The graphics state.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    void radialShading(final RadialShading shading, final GraphicsState gstate)
            throws IOException, PSError {
        // Convert coordinates and radii from user space to coordinate space
        // PGF does not support the Extend parameters for shadings. So we
        // try to emulate the effect.
        double scaling = gstate.ctm.getMeanScaling();
        double xScale = gstate.ctm.getXScaling() / scaling;
        double yScale = gstate.ctm.getYScaling() / scaling;
        double angle = gstate.ctm.getRotation();
        double[] coor0 = gstate.ctm.transform(shading.getCoord(0.0));
        double[] coor1 = gstate.ctm.transform(shading.getCoord(1.0));

        double maxS = 1.0;
        if (shading.getExtend1()) {
            // Find the s value for which the radius is big (0.3 meters ~
            // a4 paper).
            maxS = shading.getSForDistance(0.3 * 1e6 / scaling, 1,
                    Double.POSITIVE_INFINITY);
            coor1 = gstate.ctm.transform(shading.getCoord(maxS));
        }
        
        startScope();
        out.write("\\pgfdeclareradialshading{eps2pgfshading}{\\pgfpoint{");
        out.write(COOR_FORMAT.format(1e-4 * (coor0[0] - coor1[0]) / xScale)
                + "cm}{");
        out.write(COOR_FORMAT.format(1e-4 * (coor0[1] - coor1[1]) / yScale)
                + "cm}}{");
        double[] sFit = shading.fitLinearSegmentsOnColor(0.01);        
        for (int i = 0; i < sFit.length; i++) {
            if (i > 0) {
                out.write(";");
            }
            double r = scaling * shading.getRadius(sFit[i]);
            double[] color = shading.getColor(sFit[i]);
            out.write("rgb(" + LENGTH_FORMAT.format(1e-4 * r) + "cm)=");
            out.write("(" + COLOR_FORMAT.format(color[0]));
            out.write("," + COLOR_FORMAT.format(color[1]));
            out.write("," + COLOR_FORMAT.format(color[2]) + ")");
        }
        if (maxS > 1.0) {
            double r = scaling * shading.getRadius(maxS);
            double[] color = shading.getColor(1.0);
            out.write(";rgb(" + LENGTH_FORMAT.format(1e-4 * r) + "cm)=");
            out.write("(" + COLOR_FORMAT.format(color[0]));
            out.write("," + COLOR_FORMAT.format(color[1]));
            out.write("," + COLOR_FORMAT.format(color[2]) + ")");
        }
        out.write("}");
        out.write("\\pgflowlevelobj{");
        out.write("\\pgftransformshift{\\pgfpoint{");
        out.write(LENGTH_FORMAT.format(1e-4 * coor1[0]) + "cm}{");
        out.write(LENGTH_FORMAT.format(1e-4 * coor1[1]) + "cm}}");
        if (Math.abs(angle) > 1e-10) {
            out.write("\\pgftransformrotate{" + COOR_FORMAT.format(angle)
                    + "}");
        }
        if (Math.abs(xScale - 1.0) > 1e-10) {
            out.write("\\pgftransformxscale{" + xScale + "}");
        }
        if (Math.abs(yScale - 1.0) > 1e-10) {
            out.write("\\pgftransformyscale{" + yScale + "}");
        }
        out.write("}{\\pgfuseshading{eps2pgfshading}}");
        endScope();
    }
    
    /**
     * Implements PostScript operator setlinecap.
     * 
     * @param cap Cap type (see PostScript manual for info)
     * 
     * @throws IOException Unable to write output
     * @throws PSErrorRangeCheck Invalid cap type
     */
    public void setlinecap(final int cap)
            throws IOException, PSErrorRangeCheck {
        switch (cap) {
            case 0:
                out.write("\\pgfsetbuttcap\n");
                break;
            case 1:
                out.write("\\pgfsetroundcap\n");
                break;
            case 2:
                out.write("\\pgfsetrectcap\n");
                break;
            default:
                throw new PSErrorRangeCheck();
        }
    }
    
    /**
     * Implements PostScript operator setlinejoin.
     * 
     * @param join Join type (see PostScript manual)
     * 
     * @throws IOException Unable to write output
     * @throws PSErrorRangeCheck Invalid join type
     */
    public void setlinejoin(final int join)
            throws IOException, PSErrorRangeCheck {
        switch (join) {
            case 0:
                out.write("\\pgfsetmiterjoin\n");
                break;
            case 1:
                out.write("\\pgfsetroundjoin\n");
                break;
            case 2:
                out.write("\\pgfsetbeveljoin\n");
                break;
            default:
                throw new PSErrorRangeCheck();
        }        
    }
    
    /**
     * Implements PostScript operator setdash.
     * 
     * @param gstate The gstate.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     */
    void updateDash(final GraphicsState gstate) throws IOException, PSError {
        String lastPattern = gstate.deviceData.get("pgf_last_dashpattern")
                                .isis();
        double lastOffset = gstate.deviceData.get("pgf_last_dashoffset")
                                .toReal();
        
        double scaling = gstate.ctm.getMeanScaling();
        PSObjectArray currentArray = new PSObjectArray();
        for (int i = 0; i < gstate.dashPattern.size(); i++) {
            currentArray.addToEnd(new PSObjectReal(gstate.dashPattern.get(i)
                    .toReal() * scaling));
        }
        String currentPattern = currentArray.isis();
        double currentOffset = gstate.dashOffset * scaling;
        
        if (!currentPattern.equals(lastPattern)
                || (Math.abs(lastOffset - currentOffset) > 1e-10)) {
            out.write("\\pgfsetdash{");
            try {
                int i = 0;
                while (true) {
                    out.write("{" + LENGTH_FORMAT.format(1e-4
                            * currentArray.get(i++).toReal()) + "cm}");
                }
            } catch (PSErrorRangeCheck e) {
                
            } finally {
                out.write("}{" + LENGTH_FORMAT.format(1e-4 * currentOffset)
                        + "cm}\n");
            }
            gstate.deviceData.setKey("pgf_last_dashpattern", currentArray);
            gstate.deviceData.setKey("pgf_last_dashoffset",
                    new PSObjectReal(currentOffset));
        }
    }
    
    /**
     * Compares the current line width with the last-used line width. If they
     * are different the new line width is set in the output.
     * 
     * @param gstate The graphics state.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void updateLinewidth(final GraphicsState gstate)
            throws PSError, IOException {
        double lastWidth = gstate.deviceData.get("pgf_last_linewidth").toReal();
        double currentWidth = gstate.lineWidth * gstate.ctm.getMeanScaling();
        if (Math.abs(currentWidth - lastWidth) > 1e-10) {
            out.write("\\pgfsetlinewidth{"
                    + LENGTH_FORMAT.format(1e-3 * currentWidth) + "mm}\n");
            gstate.deviceData.setKey("pgf_last_linewidth",
                    new PSObjectReal(currentWidth));
        }
    }
    
    /**
     * Sets the miter limit.
     * 
     * @param miterLimit The miter limit.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void setmiterlimit(final double miterLimit) throws IOException {
        out.write("\\pgfsetmiterlimit{" + miterLimit + "}\n");
    }
    
   /**
     * Starts a new scope.
     * 
     * @throws IOException Unable to write output
     */
    public void startScope() throws IOException {
        out.write("\\begin{pgfscope}\n");
        scopeDepth++;
    }
    
    /**
     * Ends the current scope scope.
     * 
     * @throws IOException There was an error write to the output
     */
    public void endScope() throws IOException {
        if (scopeDepth > 0) {
            out.write("\\end{pgfscope}\n");
            scopeDepth--;
        }
    }

    /**
     * Sets the current color in gray, RGB or CMYK.
     * 
     * @param color Depending on the length of the array. If it has one
     * parameter it is a gray value, if it has three parameters it are RGB
     * values and if it has four parameters it are CMYK values.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void setColor(final PSColor color) throws IOException {
        if (color.getNrComponents() >= 4) {
            out.write("\\definecolor{eps2pgf_color}{cmyk}{"
                    + COLOR_FORMAT.format(color.getLevel(0))
                    + "," + COLOR_FORMAT.format(color.getLevel(1))
                    + "," + COLOR_FORMAT.format(color.getLevel(2))
                    + "," + COLOR_FORMAT.format(color.getLevel(3)) + "}");
        } else if (color.getNrComponents() == 3) {
            out.write("\\definecolor{eps2pgf_color}{rgb}{"
                    + COLOR_FORMAT.format(color.getLevel(0))
                    + "," + COLOR_FORMAT.format(color.getLevel(1))
                    + "," + COLOR_FORMAT.format(color.getLevel(2)) + "}");
        } else {
            out.write("\\definecolor{eps2pgf_color}{gray}{"
                    + COLOR_FORMAT.format(color.getLevel(0)) + "}");
        }
        
        out.write("\\pgfsetstrokecolor{eps2pgf_color}");
        out.write("\\pgfsetfillcolor{eps2pgf_color}\n");
    }

    /**
     * Draws text.
     * 
     * @param text Exact text to draw
     * @param position Text anchor point in [micrometer, micrometer]
     * @param angle Text angle in degrees
     * @param pFontsize in PostScript pt (= 1/72 inch). If fontsize is NaN, the
     *        font size is not set and completely determined by LaTeX.
     * @param anchor String with two characters:
     *               t - top, c - center, B - baseline b - bottom
     *               l - left, c - center, r - right
     *               e.g. Br = baseline,right
     *               
     * @throws IOException Unable to write output
     */
    public void show(final String text, final double[] position,
            final double angle, final double pFontsize, final String anchor)
            throws IOException {
        String x = COOR_FORMAT.format(1e-4 * position[0]);
        String y = COOR_FORMAT.format(1e-4 * position[1]);
        
        // Process anchor
        String posOpts = "";
        // Vertical alignment
        if (anchor.contains("t")) {
            posOpts = "top,";
        } else if (anchor.contains("B")) {
            posOpts = "base,";
        } else if (anchor.contains("b")) {
            posOpts = "bottom,";
        }
        
        // Horizontal alignment
        if (anchor.contains("l")) {
            posOpts += "left,";
        } else if (anchor.contains("r")) {
            posOpts += "right,";
        }
        
        // Convert fontsize in PostScript pt to TeX pt
        double fontsize = pFontsize / 72.0 * 72.27;
        
        String angStr = LENGTH_FORMAT.format(angle);
        
        String texText = "";
        if (!Double.isNaN(fontsize)) {
            texText += "\\fontsize{" + FONTSIZE_FORMAT.format(fontsize) + "}{" 
            + FONTSIZE_FORMAT.format(1.2 * fontsize) + "}\\selectfont{";
        }
        texText += text;
        if (!Double.isNaN(fontsize)) {
            texText += "}";
        }
        out.write(String.format("\\pgftext[%sx=%scm,y=%scm,rotate=%s]{%s}\n",
                posOpts, x, y, angStr, texText));
    }
    
    /**
     * Draws a red dot (useful for debugging, don't use otherwise).
     * 
     * @param x X-coordinate (micrometer)
     * @param y Y-coordinate (micrometer)
     * 
     * @throws IOException Unable to write output
     */
    public void drawDot(final double x, final double y) throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetfillcolor{red}\\pgfpathcircle{"
                + "\\pgfpoint{" + 1e-4 * x + "cm}{" + 1e-4 * y
                + "cm}}{0.5pt}\\pgfusepath{fill}\\end{pgfscope}\n");
    }
    
    /**
     * Draws a blue rectangle (useful for debugging, don't use otherwise).
     * 
     * @param lowerLeft X- and Y-coordinate (in micrometer) of lower left
     * corner.
     * @param upperRight X- and Y-coordinate (in micrometer) of upper right
     * corner.
     * 
     * @throws IOException Unable to write output
     */
    public void drawRect(final double[] lowerLeft, final double[] upperRight)
            throws IOException {
        out.write("\\begin{pgfscope}\\pgfsetstrokecolor{blue}"
                + "\\pgfsetlinewidth{0.1pt}\\pgfpathrectangle{\\pgfpoint{"
                + 1e-4 * lowerLeft[0] + "cm}{" + 1e-4 * lowerLeft[1]
                + "cm}}{\\pgfpoint{" + 1e-4 * (upperRight[0] - lowerLeft[0])
                + "cm}{" + 1e-4 * (upperRight[1] - lowerLeft[1])
                + "cm}}\\pgfusepath{stroke}\\end{pgfscope}\n");
    }

}
