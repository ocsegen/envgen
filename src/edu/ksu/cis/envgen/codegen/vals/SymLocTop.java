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

import soot.*;
import edu.ksu.cis.envgen.codegen.JavaPrinter;

public class SymLocTop extends SymLoc  {
	
	/** Used to identify location in the environment. */
	public static final int ENVIRONMENT_LOC = 2;

	/** Used to identify locations that the analysis can't produce
	 * more precise information. */
	public static final int UNKNOWN_LOC = 3;

	public static final int DUMMY_LOC = 4;
	
	public static final int READ_LOC = 5;
	
	public static final int WRITE_LOC = 6;


	/**
	 * Used to create a dummy location.
	 */
	public SymLocTop(int kind) {
		this.kind = kind;
		this.singular = false;
	}
	

	/**
	 * Used to create an environment or unknown location.
	 */
	public SymLocTop(int kind, Type type) {
		this.kind = kind;
		this.type = type;
		this.singular = false;
	}


	/**
	 * Copy constructor.
	 */

	public SymLocTop(SymLocTop loc) {
		assert(loc!=null);
		
		this.kind = loc.getKind();
		this.type = loc.getType();

		this.modifiedAccessor = loc.getModifiedAccessor();
		this.modifiedType = loc.getModifiedType();
		this.singular = false;
	}
	
	/** Returns true is this is an environment location. */
	public boolean isEnvironmentLoc() {
		return (kind == ENVIRONMENT_LOC);
	}

	/** Returns true if this is an unknown location. */
	public boolean isUnknownLoc() {
		return (kind == UNKNOWN_LOC);
	}

	public boolean isDummyLoc() {
		return (kind == DUMMY_LOC);
	}
	
	public boolean isReadLoc() {
		return (kind == READ_LOC);
	}
	
	public boolean isWriteLoc() {
		return (kind == WRITE_LOC);
	}
	
	
	/*--------------------------------------------------*/
	/*           Get and Set methods                    */
	/*--------------------------------------------------*/


	public void setCastType(Type t) {
			type = t;
	}

	public boolean equals(SymLocTop second) {
		logger.fine(
				"SymLoc, checking equality between :"
					+ this
					+ " and :"
					+ second);
		if (this.toString().equals(second.toString())) {
			logger.fine("equals");
			return true;
		}
		logger.fine("not equals");
		return false;
	}

	/*--------------------------------------------------*/
	/*      Methods to get a String representation      */
	/*--------------------------------------------------*/

	/**
	 * Generates code representation of symbolic
	 * location appropriate for code generation.
	 */
	public String getCode(JavaPrinter printer) {

		if (kind == ENVIRONMENT_LOC || kind == UNKNOWN_LOC){
			
			String modifiedAccessorStr = "";
			if (modifiedAccessor != null)
				modifiedAccessorStr = modifiedAccessor.getCode(printer);
			
			return printer.getRandomObjectCall(type) + modifiedAccessorStr;
		}

		if (kind == DUMMY_LOC)
			return "dummy";
		
		if(kind == READ_LOC)
			return "read";
		
		if(kind == WRITE_LOC)
			return "write";
		
		return "never get here";
	}

	public String toString() {
		if (kind == DUMMY_LOC)
			return "dummy";

		if(kind == READ_LOC)
			return "read";
		
		if(kind == WRITE_LOC)
			return "write";

		String modifiedAccessorStr = "";
		if (modifiedAccessor != null)
			modifiedAccessorStr =
				"/mod: " + modifiedAccessor.toString();


		String baseTypeStr = type.toString();
		
		if (kind == ENVIRONMENT_LOC) {
			return "environment(" + baseTypeStr +")"+ modifiedAccessorStr;
		} else if (kind == UNKNOWN_LOC) {
			return "unknown(" + baseTypeStr + ")" + modifiedAccessorStr;
		} 	
		return ("never get here");
	}
}
