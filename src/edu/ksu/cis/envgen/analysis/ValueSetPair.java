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
package edu.ksu.cis.envgen.analysis;

import soot.Value;

import edu.ksu.cis.envgen.util.MultiSet;

/**
 * Representation of data flow information as a single
 * entry in the data flow set. Represents 
 * a mapping from an expression to a set of symbolic
 * locations it might refer to in alias analysis, and
 * a mapping from a symbolic location to a set of values it
 * might hold in side-effects analysis.
 * 
 */
public class ValueSetPair {
	/** Left hand side of the mapping. */
	Value left;

	/** Right hand side of the mapping. */
	MultiSet right;

	public ValueSetPair(Value left, MultiSet right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the left hand side of the mapping. 
	 */
	Value getLeft() {
		return left;
	}

	/**
	 * Returns the right hand side of the mapping.
	 */
	MultiSet getRight() {
		return right;
	}

	public String toString() {
		return left.toString() + " --> " + right.toString();
	}
}
