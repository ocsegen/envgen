/* OCSEGen: Open Components and Systems Environment Generator
 * Copyright (c) <2002-2008> Oksana Tkachuk, Kansas State University.
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * For questions about the license, copyright, and software, contact 
 * Oksana Tkachuk at oksana.tkachuk@gmail.com
 */ 
package edu.ksu.cis.envgen.codegen.vals;

import java.util.List;
import java.util.logging.Logger;

import soot.*;
import soot.util.Switch;

import edu.ksu.cis.envgen.codegen.*;

/**
 * Representation of a value that a symbolic location
 * can hold. 
 * 
 */
public class SymLocValue implements Value{
	/** Type of the object whose field is being modified.
	  *  Used in chooseReachable("type", unit object).field
	  * or chooseClass("type").field.
	  */
	Type type;
	
	Logger logger = Logger.getLogger("envgen.analysis.codegen.vals");
	
	/** Returns the type of the object whose field is being modified,
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Setd <code>typeOfFieldBase</code> to <code>t</code>,
	 * needed for reachable locations and unknown locations,
	 * where the type of the base of the field reference is needed.
	 */
	public void setType(Type t) {
		type = t;
	}

	
	public String toString() {
		logger.severe("SymLocVal: toString should be overriden");
		return "should be overridden";
	}
	

	public String getCode(JavaPrinter printer) {
		logger.severe("SymLocVal: getCode should be overriden");
		return "should be overridden";
	}
	
	public boolean equals(SymLocValue second) {
		//String code = this.getCode();
		//String secCode = second.getCode();
		String code = this.toString();
		String secCode = second.toString();

		if (code.equals(secCode)) {
			logger.fine(
					"SymLocValue, this: " + code + " equals :" + secCode);
			return true;

		} else {
			logger.fine(
					"SymLocValue, this: " + code + " NOT equals :" + secCode);
			return false;

		}
	}
	
	/*--------------------------------------------------*/
	/*      Dummy methods to match "implements"         */
	/*--------------------------------------------------*/

	public List getUseBoxes() {
		logger.severe("SymLoc, getUseBoxes: dummy method");
		return null;
	}

	public boolean equivTo(Object obj) {
		logger.severe("SymLoc, equivTo: dummy method");
		return false;
	}

	public void apply(Switch sw) {
		logger.severe("SymLoc, apply: dummy method");
	}
	public int equivHashCode() {
		logger.severe("SymLoc, equivHashCode: dummy method");
		return 0;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (Exception e) {
		}
		return null;
	}

	public void toString(UnitPrinter up) {
		logger.severe("SymLoc, toString: dummy method");
	}
}
