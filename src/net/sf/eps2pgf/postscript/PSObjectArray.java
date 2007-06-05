/*
 * PSObjectArray.java
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

import java.io.*;
import java.util.*;

import net.sf.eps2pgf.postscript.errors.*;

/** PostScript object: array
 *
 * @author Paul Wagenaars
 */
public class PSObjectArray extends PSObject {
    List<PSObject> array;
    int offset;
    int count;
    
    /**
     * Create a new empty PostScript array
     */
    public PSObjectArray() {
        array = new ArrayList<PSObject>();
        offset = 0;
        count = Integer.MAX_VALUE;
    }

    /**
     * Creates a new instance of PSObjectArray
     * 
     * @param dblArray Array of doubles
     */
    public PSObjectArray(double[] dblArray) {
        array = new ArrayList<PSObject>(dblArray.length);
        for (int i = 0 ; i < dblArray.length ; i++) {
            array.add(new PSObjectReal(dblArray[i]));
        }
        
        // Use the entire array
        offset = 0;
        count = dblArray.length;
    }
    
    /**
     * Creates a new instance of PSObjectArray
     * @param objs Objects that will be stored in the new array.
     */
    public PSObjectArray(PSObject[] objs) {
        array = new ArrayList<PSObject>(objs.length);
        for (int i = 0 ; i < objs.length ; i++) {
            array.add(objs[i]);
        }
        
        // Use the entire array
        offset = 0;
        count = objs.length;
    }
    
    /**
     * Creates a new executable array object
     * @param str String representing a valid procedure (executable array)
     * @throws java.io.IOException Unable to read the string
     */
    public PSObjectArray(String str) throws IOException, PSErrorIOError {
        // quick check whether it is a literal or executable array
        if (str.charAt(0) == '{') {
            isLiteral = false;
        } else if (str.charAt(0) == '[') {
            isLiteral = true;
        }
        
        str = str.substring(1,str.length()-1);
        
        StringReader strReader = new StringReader(str);
        
        array = Parser.convertAll(strReader);
        count = array.size();
        offset = 0;
    }
    
    /**
     * Creates a new instance of PSObjectArray. The new array is a subset
     * of the supplied array. Thery share the data (changing a value is one
     * also changes the value in the other.
     * @param obj Complete array from which this new PSObjectArray is a subset.
     * @param index Index of the first element of the subarray in the obj array.
     * @param newCount Number of items in the subarray.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Indices out of range.
     */
    public PSObjectArray(PSObjectArray obj, int index, int newCount) throws PSErrorRangeCheck {
        int n = obj.size();
        if ( (newCount != 0) || (index != 0) ) {
            if (index >= n) {
                throw new PSErrorRangeCheck();
            }
            if ( (index + count - 1) >= n ) {
                throw new PSErrorRangeCheck();
            }
        }

        array = obj.array;
        offset = obj.offset+index;
        count = newCount;
        copyCommonAttributes(obj);
    }
    
    /**
     * Insert an element at the specified position in this array.
     * @param index Index at which the new element will be inserted.
     * @param value Value of the new element
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     */
    public void addAt(int index, PSObject value) throws PSErrorInvalidAccess {
        checkAccess(false, false, true);
        array.add(index+offset, value);
    }
    
    /**
     * Add an element to the end to this array.
     * @param value Value of the new element
     */
    public void addToEnd(PSObject value) throws PSErrorInvalidAccess {
        checkAccess(false, false, true);
        array.add(value);
    }
    
    /**
     * Replace executable name objects with their values
     * @param interp Interpreter to which the operators must be bound.
     * @return This array.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Something went wrong.
     */
    public PSObjectArray bind(Interpreter interp) throws PSErrorTypeCheck {
        try {
            for (int i = 0 ; i < size() ; i++) {
                try {
                    PSObject obj = get(i);
                    set(i, obj.bind(interp));
                    if (obj instanceof PSObjectArray) {
                        if ( !((PSObjectArray)obj).isLiteral ) {
                            obj.readonly();
                        }
                    }
                } catch (PSErrorInvalidAccess e) {
                    // no access, don't change anything
                }
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
        return this;
    }
    
    /**
     * Creates a deep copy of this array.
     * @throws java.lang.CloneNotSupportedException Unable to clone this object or one of its sub-objects
     * @return Deep copy of this array
     */
    public PSObjectArray clone() {
        PSObject[] objs = new PSObject[size()];
        int i = 0;
        for (PSObject obj : this) {
            objs[i] = obj.clone();
            i++;
        }
        return new PSObjectArray(objs);
    }

    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     */
    public PSObject copy(PSObject obj1) throws PSErrorRangeCheck, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        PSObjectArray array = obj1.toArray();
        putinterval(0, array);
        return getinterval(0, array.length());
    }
    
    /**
     * Copies values from another obj to this object.
     * @param obj Object from which the values must be copied
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to copy values from object.
     */
    public void copyValuesFrom(PSObject obj) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectArray array = obj.toArray();
        
        try {
            // First remove all current elements from the array
            for (int i = size()-1 ; i >= 0 ; i--) {
                remove(i);
            }
        
            // Copies the values
            for (int i = 0 ; i < array.size() ; i++) {
                addAt(i, array.get(i));
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
    }
    
    /**
     * Convert this object to a literal object
     * @return This object converted to a literal object
     */
    public PSObject cvlit() {
        isLiteral = true;
        return this;
    }

    /**
     * PostScript operator 'cvx'. Makes this object executable
     */
    public PSObject cvx() {
        isLiteral = false;
        return this;
    }

    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectArray dup() {
        try {
            return new PSObjectArray(this, 0, size());
        } catch (PSErrorRangeCheck e) {
            // this can never happen
            return null;
        }
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) throws PSErrorInvalidAccess {
        checkAccess(false, true, true);
        obj.checkAccess(false, true, true);
        try {
            PSObjectArray objArr = obj.toArray();
            if ( (count != objArr.count) || (offset != objArr.offset) ) {
                return false;
            }
            return (array == objArr.array);
        } catch (PSErrorTypeCheck e) {
            return false;
        }
    }
    
    /** Executes this object in the supplied interpreter */
    public void execute(Interpreter interp) throws Exception {
        if (isLiteral) {
            interp.opStack.push(dup());
        } else {
            List<PSObject> list = getItemList();
            list.remove(0);  // remove first item (= number of object per item)
            interp.processObjects(list);
        }
    }
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     */
    public void executeonly() throws PSErrorInvalidAccess {
        checkAccess(true, false, false);
        access = ACCESS_EXECUTEONLY;
    }
    
    /**
     * Returns an object from this array.
     * @param index Index of the element to return.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     * @return Value of the specifiec element.
     */
    public PSObject get(int index) throws PSErrorRangeCheck, PSErrorInvalidAccess {
        checkAccess(false, true, false);
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        return array.get(index+offset);
    }
    
    /**
     * PostScript operator: get
     * Gets a single element from this object.
     */
    public PSObject get(PSObject index) throws PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorUndefined, PSErrorInvalidAccess {
        return get(index.toInt());
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     */
    public List<PSObject> getItemList() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        List<PSObject> items = new LinkedList<PSObject>();
        items.add(new PSObjectInt(1));
        for (PSObject obj : this) {
            items.add(obj);
        }
        return items;
    }
    
    /**
     * Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object.
     * @param index Index of the first element of the subarray
     * @param count Number of items in the subarray
     * @return Subarray
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of bounds.
     */
    public PSObjectArray getinterval(int index, int count) throws PSErrorRangeCheck,
            PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return new PSObjectArray(this, index, count);
    }
    
    /**
     * Check whether a string is a procedure
     * @param str String to check.
     * @return Returns true when the string is a procedure. Returns false otherwise.
     */
    public static boolean isType(String str) {
        int len = str.length();
        if ( (str.charAt(0) == '{') && (str.charAt(len-1) == '}') ) {
            return true;
        } else if ( (str.charAt(0) == '[') && (str.charAt(len-1) == ']') ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return String representation of this object.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Something went wrong.
     */
    public String isis() {
        StringBuilder str = new StringBuilder();
        if (isLiteral) {
            str.append("[ ");
        } else {
            str.append("{ ");
        }
        for (PSObject obj : this) {
            str.append(obj.isis() + " ");
        }
        if (isLiteral) {
            str.append("]");
        } else {
            str.append("}");
        }
        return str.toString();
    }
    
    /**
     * Implements PostScript operate: length
     * @return Length of this object
     */
    public int length() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return size();
    }
    
    /**
     * PostScript operator: 'noaccess'
     */
    public void noaccess() {
        access = ACCESS_NONE;
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * @param index Index or key for new value
     * @param value New value
     */
    public void put(PSObject index, PSObject value) throws PSErrorRangeCheck,
            PSErrorTypeCheck, PSErrorInvalidAccess {
        put(index.toInt(), value);
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * @param index Index or key for new value
     * @param value New value
     */
    public void put(int index, PSObject value) throws PSErrorRangeCheck,
            PSErrorInvalidAccess {
        checkAccess(false, false, true);
        checkAccess(false, true, false);
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        array.set(index+offset, value);
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param obj Subsequence
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not 'putinterval' anything in this object type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Index or (index+length) out of bounds
     */
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorInvalidAccess {
        PSObjectArray array3 = obj.toArray();
        int N = array3.length();
        for (int i = 0 ; i < N ; i++) {
            set(index + i, array3.get(i));
        }
    }

    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     */
    public boolean rcheck() {
        if ( (access == ACCESS_UNLIMITED) || (access == ACCESS_READONLY) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * PostScript operator: 'readonly'
     */
    public void readonly() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        access = ACCESS_READONLY;
    }
    
    /**
     * Remove an element from this array.
     * @param index Index of the element to remove.
     * @return Removed element.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     */
    public PSObject remove(int index) throws PSErrorRangeCheck, PSErrorInvalidAccess {
        checkAccess(false, false, true);
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        return array.remove(index+offset);
    }
    
    /**
     * Replace the element with offset with value.
     * @param index Index of the element to replace.
     * @param value New value of the element.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     */
    public void set(int index, PSObject value) throws PSErrorRangeCheck,
            PSErrorInvalidAccess {
        checkAccess(false, false, true);
        value.checkAccess(false, true, false);
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        array.set(index+offset, value);
    }
    
    /**
     * Returns the number of elements in this array.
     * @return Number of items in this array.
     */
    public int size() {
        return Math.min(count, array.size()-offset);
    }
    
    /**
     * Convert this object to an array.
     * @return This array
     */
    public PSObjectArray toArray() {
        return this;
    }
    
    /**
     * Converts this PostScript array to a double[] array with the requested size.
     * @param k Required number of items in the array. If the actual number of
     * items is different a PSErrorRangeCheck is thrown.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck The number of items in this array is not the same as the required
     * number of items.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One or more items in this array can not be converted to a double.
     * @return Array with doubles
     */
    public double[] toDoubleArray(int k) throws PSErrorRangeCheck, 
            PSErrorTypeCheck, PSErrorInvalidAccess {
        if (k != size()) {
            throw new PSErrorRangeCheck();
        }
        return toDoubleArray();
    }
    
    /**
     * Convert this PostScript array to a double[] array.
     */
    public double[] toDoubleArray() throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        double newArray[] = new double[size()];
        try {
            for (int i = 0 ; i < size() ; i++) {
                newArray[i] = get(i).toReal();
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
        
        return newArray;
    }
    
    /**
     * Convert this object to a matrix, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Array is not a valid matrix
     * @return Matrix representation of this array
     */
    public PSObjectMatrix toMatrix() throws PSErrorRangeCheck, PSErrorTypeCheck,
            PSErrorInvalidAccess {
        if (this.size() != 6) {
            throw new PSErrorRangeCheck();
        }
        return new PSObjectMatrix(this.get(0).toReal(), this.get(1).toReal(),
                this.get(2).toReal(), this.get(3).toReal(),
                this.get(4).toReal(), this.get(5).toReal());
    }
    
    /**
     * Convert this object to an executable array (procedure), if possible.
     */
    public PSObjectArray toProc() throws PSErrorTypeCheck {
        if (isLiteral) {
            throw new PSErrorTypeCheck();
        }
        return this;
    }
    
    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "arraytype";
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     */
    public boolean wcheck() {
        return (access == ACCESS_UNLIMITED);
    }

}
