/*
 * PSObjectReal.java
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

import net.sf.eps2pgf.postscript.errors.*;

/** PostScript object: real
 *
 * @author Paul Wagenaars
 */
public class PSObjectReal extends PSObject {
    double value;
    
    /**
     * Creates a new instance of PSObjectReal
     * @param str String with valid real.
     */
    public PSObjectReal(String str) {
        value = Double.parseDouble(str);
    }
    
    /** Create a new real object. */
    public PSObjectReal(double dbl) {
        value = dbl;
    }
    
    /**
     * Creates a new real object.
     */
    public PSObjectReal(PSObjectReal obj) {
        value = obj.value;
        copyCommonAttributes(obj);
    }
    
    /**
     * Check whether a string is a real
     * @param str String to check.
     * @return Returns true when str is a valid real. Returns false otherwise.
     */
    public static boolean isType(String str) {
        try {
            Double.parseDouble(str);            
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return String.valueOf(value);
    }

    /**
     * Returns the absolute value of this integer
     * @return Absolute value of this object
     */
    public PSObjectReal abs() {
        return new PSObjectReal(Math.abs(value));
    }

    /**
     * Returns the sum of this object and the passed object, if both are numeric
     * @param obj Object that will be added to this object
     * @return Sum of this object and passed object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     */
    public PSObject add(PSObject obj) throws PSErrorTypeCheck {
        double num2 = obj.toReal();
        return new PSObjectReal(value + num2);
    }

    /**
     * Return this value rounded upwards
     * @return Value of this object rounded upwards
     */
    public PSObject ceiling() {
        return new PSObjectReal(Math.ceil(value));
    }
    
    /**
     * PostScript operator 'cvi'. Convert this object to an integer
     */
    public int cvi() {
        try {
            return (int)truncate().toReal();
        } catch (PSErrorTypeCheck e) {
            // this can never happen, because this object is a real
            return 0;
        }
    }
    
    /**
     * PostScript operator 'cvr'. Convert this object to a real
     */
    public double cvr() throws PSErrorTypeCheck {
        return value;
    }
    
    /**
     * PostScript operator 'cvrs'
     */
    public String cvrs(int radix) throws PSErrorTypeCheck, PSErrorRangeCheck {
        if (radix < 2) {
            throw new PSErrorRangeCheck();
        }
        if (radix == 10) {
            return Double.toString(value);
        } else {
            PSObjectInt valueInt = new PSObjectInt(cvi());
            return valueInt.cvrs(radix);
        }
    }

    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
     * @return Text representation
     */
    public String cvs() {
        return String.valueOf(value);
    }

    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectReal dup() {
        return new PSObjectReal(this);
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        try {
            if ((obj instanceof PSObjectReal) || (obj instanceof PSObjectInt)) {
                return (value == obj.toReal());
            }
        } catch (PSErrorTypeCheck e) {
            // This can never happen because of the typecheck with instanceof
        }
        return false;
    }

    /**
     * Return this value rounded downwards
     * @return Value of this object rounded downwards
     */
    public PSObject floor() {
        return new PSObjectReal(Math.floor(value));
    }

    /**
     * PostScript operator 'gt'
     * @param obj2 Object to compare this object to
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to compare the type of this object and/or obj2
     * @return Returns true when this object is greater than obj2, return false
     * otherwise.
     */
    public boolean gt(PSObject obj2) throws PSErrorTypeCheck {
        return (toReal() > obj2.toReal());
    }
    
    /**
     * Multiply this object with another object
     * @param obj Multiplication of this object and passed object
     * @return Multiplication object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object(s) are not numeric
     */
    public PSObject mul(PSObject obj) throws PSErrorTypeCheck {
        double num2 = obj.toReal();
        return new PSObjectReal(value * num2);
    }

    /**
     * Returns the negative value of this double
     * @return Absolute value of this double
     */
    public PSObjectReal neg() {
        return new PSObjectReal(-value);
    }

    /**
     * Return this value rounded to the nearest integer
     * @return Value of this object rounded to the nearest integer
     */
    public PSObject round() {
        return new PSObjectReal(Math.round(value));
    }

    /**
     * Subtract an object from this object
     * @param obj Object that will be subtracted from this object
     * @return Passed object subtracted from this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     */
    public PSObject sub(PSObject obj) throws PSErrorTypeCheck {
        double num2 = obj.toReal();
        return new PSObjectReal(value - num2);
    }

    /**
     * Convert this object to a human readable string.
     * @return Human readable string.
     */
    public String toString() {
        return "Real: " + value;
    }
    
    /** Convert this object to a real number, if possible. */
    public double toReal() {
        return value;
    }
    
    /**
     * Return this value rounded towards zero
     * @return Value of this object rounded towards zero
     */
    public PSObject truncate() {
        if (value > 0) {
            return floor();
        } else {
            return ceiling();
        }
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "realtype";
    }
}
