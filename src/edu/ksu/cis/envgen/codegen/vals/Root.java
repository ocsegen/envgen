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

import java.util.logging.Logger;

import soot.*;
import soot.jimple.*;
import edu.ksu.cis.envgen.codegen.JavaPrinter;

/** 
 * Implementation of a root of symbolic location that represents
 * a  memory location.
 * There are four different symbolic locations that can be identified
 * by the analysis: unit locations, locations reachable
 * from the unit through a chain of references,
 * locations in the environment, and locations that the analysis
 * can't identify (unknown locations).
 * 
 */
public abstract class Root {

	/** 
	 * Represents the very first base (b) of the symbolic location. For example
	 * the first base of an expression b.f1...fn is b.
	 */
	Value root;

	Type rootType;

	Type rootCastType;
	
	String name;

	//int creationIndex;

	//Stmt allocationSite;

	//boolean summaryNode;
	
	//String code;

	Logger logger = Logger.getLogger("envgen.codegen.vals");

	public Root(Value value, Type type) {
		this.root = value;
		this.rootType = type;
	}

	/*--------------------------------------------------*/
	/*           Get and Set methods                    */
	/*--------------------------------------------------*/

	/**
	 * Returns top level location: either 
	 * it's a static fields of the unit
	 * or a parameter.
	 */
	public Value getRoot() {
		return root;
	}

	public void setRoot(Value v) {
		root = v;
	}

	public Type getRootType() {
		return rootType;

	}

	public void setRootType(Type t) {
		rootType = t;
	}

	public Type getRootCastType() {
		return rootCastType;
	}

	public void setRootCastType(Type t) {

		rootCastType = t;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isInnerLoc(){
		return false;
	}

	/**
	 * Generates code representation of symbolic
	 * location appropriate for code generation.
	 */
	public abstract String getCode(JavaPrinter printer);

}
