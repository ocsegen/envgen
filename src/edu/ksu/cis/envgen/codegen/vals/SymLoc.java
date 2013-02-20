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

/** 
 * Implementation of symbolic location that represents
 * a  memory location.
 * There are four different symbolic locations that can be identified
 * by the analysis: unit locations, locations reachable
 * from the unit through a chain of references,
 * locations in the environment, and locations that the analysis
 * can't identify (unknown locations).
 * 
 */
public class SymLoc extends SymLocValue {

	
	/**
	 * One of the different locations:
	 * unit,
	 * reachable from unit,
	 * environment,
	 * unknown, and dummy.
	 */
	int kind;

	/** Represents the modified field/element of the object/array. */
	Accessor modifiedAccessor;

	Type modifiedType;

	boolean singular;

	public SymLoc(){}
	
	/**
	 * Used to create a dummy location.
	 */
	
	public SymLoc(int kind) {
		this.kind = kind;
		this.singular = false;
	}
	

	/**
	 * Used to create an environment or unknown location.
	 */
	public SymLoc(int kind, Type type) {
		this.kind = kind;
		this.type = type;
		this.singular = false;
	}


	/**
	 * Copy constructor.
	 */

	public SymLoc(SymLoc loc) {
		assert(loc!=null);
		
		this.kind = loc.getKind();
		this.type = loc.getType();

		this.modifiedAccessor = loc.getModifiedAccessor();
		this.modifiedType = loc.getModifiedType();
		this.singular = loc.getSingular();
	}

	public boolean isSingular() {
		return singular;
	}
	
	
	/*--------------------------------------------------*/
	/*           Get and Set methods                    */
	/*--------------------------------------------------*/

	public int getKind() {
		return kind;
	}

	/** Sets the <code>kind</code> of the location. */
	public void setKind(int k) {
		this.kind = k;
	}

	public boolean getSingular() {
		return singular;
	}

	public void setSingular(boolean s) {
		singular = s;
	}

	public void setCastType(Type t){
		logger.severe("setCastType: should be overriden");
	}

	public Accessor getModifiedAccessor() {
		return modifiedAccessor;
	}

	/**
	 * Sets <code>modifiedField</code> to <code>sf</code>, 
	 * used in side-effects analysis, where modified fields
	 * are identified.
	 */
	public void setModifiedAccessor(Accessor a) {
		if (a == null)
			return;
		modifiedAccessor = a;
	}

	public Type getModifiedType() {
		return modifiedType;
	}

	public void setModifiedType(Type t) {
		modifiedType = t;
	}

	public boolean equals(SymLoc second) {
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

}
