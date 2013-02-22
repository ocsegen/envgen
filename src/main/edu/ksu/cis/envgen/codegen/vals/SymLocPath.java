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

import java.util.*;

import soot.*;
import edu.ksu.cis.envgen.codegen.JavaPrinter;

public class SymLocPath extends SymLoc {
	
	/** Used to identify unit locations. */
	public static final int CONCRETE_LOC = 0;

	/**
	 * Used to identify locations reachable from a unit location through a chain
	 * of references through the unit.
	 */
	public static final int REACHABLE_LOC = 1;

	/**
	 * Represents the very first base (b) of the symbolic location. For example
	 * the first base of an expression b.f1...fn is b.
	 */

	Root root;

	/**
	 * Represents the chain of the fields that need to be followed to reach the
	 * modified field.
	 */
	List accessors;

	int length;

	
	/**
	 * Used to create an environment or unknown location.
	 */
	public SymLocPath(int kind, Type type) {
		this.kind = kind;
		this.type = type;
		this.singular = false;
	}

	public SymLocPath(Root root, Type type) {
		this.root = root;
		this.type = type;
		this.singular = true;
	}

	/**
	 * Copy constructor.
	 */

	public SymLocPath(SymLocPath loc) {
		assert(loc!=null);
	
		this.kind = loc.getKind();
		this.root = loc.getRoot();

		if (loc.getAccessors() != null)
			this.accessors = new ArrayList(loc.getAccessors());
		this.type = loc.getType();

		this.modifiedAccessor = loc.getModifiedAccessor();
		this.modifiedType = loc.getModifiedType();
		this.singular = loc.getSingular();
	}

	/** Returns true if this is a concrete unit location. */
	public boolean isConcreteLoc() {
		return (kind == CONCRETE_LOC);
	}

	/** Returns true if this location is reachable from unit. */
	public boolean isReachableLoc() {
		return (kind == REACHABLE_LOC);
	}

	/**
	 * Appends <code>sf</code> to the end of the list of fields.
	 */
	public void addAccessor(Accessor a) {
		if (a == null)
			return;
		if (accessors == null)
			accessors = new ArrayList();
		
		/*
		if (a.getName().startsWith("this") && isInnerLoc()){
			((RootNewObject)root).setRefEnclosing(true);
			//return;
		}
		*/

		
		// reference from inner object to the enclosing object
		if (a.getName().startsWith("this") && !accessors.isEmpty()) {
			
			
			logger.fine("-------dropping this$0 field: "+a+ " in this loc: "+this);
			
			//removeLastAccessor();
			
			logger.fine("^^^^^^^^removing last from: "+accessors);
			
			accessors.remove(accessors.size() - 1);
			
			
		}
		else
			accessors.add(a);
	}

	/**
	 * Appends a list of fields to <code>fields</code>. Used when mapping
	 * called method summary into a summary of a callee.
	 */
	public void addAccessors(List a) {
		// ??should take care of the conversion to reachable

		if (a == null)
			return;
		if (accessors == null)
			accessors = new ArrayList();
		Accessor temp;
		for(Iterator  ai = a.iterator(); ai.hasNext();){
			temp = (Accessor)ai.next();
			addAccessor(temp);
		}
	}


	/*--------------------------------------------------*/
	/* Get and Set methods */
	/*--------------------------------------------------*/

	/**
	 * Returns top level location: either it's a static fields of the unit or a
	 * parameter.
	 */
	public Root getRoot() {
		return root;
	}

	public void setRoot(Root root) {
		this.root = root;
	}
	
	public boolean isInnerLoc(){
		//System.out.println("Query: "+this);
		if(accessors != null && !accessors.isEmpty()){
			Accessor last = (Accessor)accessors.get(accessors.size()-1);
			if(last.isInner()){
				//System.out.println("inner");
				return true;
			}
		}
		
		if(root instanceof RootNewObject){
			if(accessors == null || accessors.isEmpty())
				return ((RootNewObject)root).isInnerLoc();
			else{
				Accessor first = (Accessor)accessors.get(0);
				if(first.getName().startsWith("this")) {
					return false;
				}
				return true;
			}
				
		}
		//System.out.println("not inner");
		return false;
	}
	
	/*
	public boolean refEnclosingLoc(){ 
		
		if(accessors == null || accessors.isEmpty())
			return false;
		Accessor first = (Accessor)accessors.get(0);
		if(first.getName().startsWith("this")) {
			return true;
		}
		
		
		
		if(root instanceof RootNewObject){
			return ((RootNewObject)root).refEnclosingLoc();
		}
		else
		
			return false;
		
	}
	*/
	/*
	public void setEnclosingLoc(SymLocPath enclosing){
		if(root instanceof RootNewObject){
			((RootNewObject)root).setEnclosingLoc(enclosing);
		}
		else
			EnvPrinter.error("SymLocPath: setting enclosing for a wrong object: "+this);
	}
	
	public SymLocPath getEnclosingLoc(){
		if(root instanceof RootNewObject){
			return ((RootNewObject)root).getEnclosingLoc();
		}
		else
			EnvPrinter.error("SymLocPath: getting enclosing for a wrong object: "+this);
		return null;
	}
	*/

	public void setCastType(Type t) {
		if (kind == CONCRETE_LOC) {
			if (accessors != null && !accessors.isEmpty()) {
				// get the last accessor and set its cast
				Accessor a = (Accessor) accessors.get(accessors.size() - 1);
				a.setCastType(t);
			} else
				root.setRootCastType(t);
		} else
			type = t;
	}

	/**
	 * Returns a list of reachable fields for a location.
	 */
	public List getAccessors() {
		return accessors;
	}

	/** Returns the length of the <code>fields</code> chain. */
	public int getChainLength() {
		if (accessors == null)
			return 0;
		return accessors.size();
	}

	public void setChainLength(int i) {
		length = i;
	}

	public boolean equals(SymLocPath second) {
		logger.fine("SymLoc, checking equality between :" + this
					+ " and :" + second);
		if (this.toString().equals(second.toString())) {
			logger.fine("equals");
			return true;
		}
		logger.fine("not equals");
		return false;
	}

	/*--------------------------------------------------*/
	/* Methods to get a String representation */
	/*--------------------------------------------------*/

	/**
	 * Generates code representation of symbolic location appropriate for code
	 * generation.
	 */
	public String getCode(JavaPrinter printer) {

		logger.fine("getCode of: " + this);

		String rootStr = "";
		if (root != null)
			rootStr = root.getCode(printer);
		String rootAccessorsStr = getAccessorsCode(rootStr, printer);
		String modifiedAccessorStr = "";
		if (modifiedAccessor != null)
			modifiedAccessorStr = modifiedAccessor.getCode(printer);

		if (kind == CONCRETE_LOC) {
			return rootAccessorsStr + modifiedAccessorStr;
		}

		// String baseTypeStr= type.toString();

		if (kind == REACHABLE_LOC) {
			return printer.getRandomReachableCall(type, (rootAccessorsStr))
					+ modifiedAccessorStr;

		}

		logger.fine("SymLoc, getCode: unhandled kind");
		return "never get here";
	}

	public String toString() {

		//return getCode();
		
		String rootStr = "";
		if (root != null)
			rootStr = root.toString();

		String rootAccessorsStr = getAccessorsString(rootStr);

		String modifiedAccessorStr = "";
		if (modifiedAccessor != null)
			modifiedAccessorStr = "/mod: "
					+ modifiedAccessor.toString();

		if (kind == CONCRETE_LOC) {
			return rootAccessorsStr + modifiedAccessorStr;
		}

		// if (type == null)
		// EnvPrinter.error("SymLoc, getCode: null type");
		String baseTypeStr = type.toString();

		if (kind == REACHABLE_LOC) {
			return "reachable(type: " + baseTypeStr + ", from: "
					+ rootAccessorsStr + ")" + modifiedAccessorStr;

		} else
			logger.severe("SymLoc, toString: unknown kind of location");
		return ("never get here");
		
	}

	public String getAccessorsCode(String rootStr, JavaPrinter printer) {
		String rootAccessorsStr = rootStr;
		if (accessors != null) {
			Iterator fi = accessors.iterator();
			int i = 0;
			Accessor temp;
			Type tempCastType;

			while (fi.hasNext() && i < accessors.size()) {
				temp = (Accessor) fi.next();
				tempCastType = temp.getCastType();

				if (tempCastType == null)
					rootAccessorsStr = rootAccessorsStr.concat(temp.getCode(printer));
				else
					rootAccessorsStr = "((" + tempCastType.toString() + ")"
							+ rootAccessorsStr.concat(temp.getCode(printer)) + ")";
				i++;
			}
		}
		return rootAccessorsStr;
	}

	
	public String getAccessorsString(String rootStr) {
		String rootAccessorsStr = rootStr;
		if (accessors != null) {
			Iterator fi = accessors.iterator();
			int i = 0;
			Accessor temp;
			Type tempCastType;

			while (fi.hasNext() && i < accessors.size()) {
				temp = (Accessor) fi.next();
				//tempCastType = temp.getCastType();

				//if (tempCastType == null)
					rootAccessorsStr = rootAccessorsStr.concat(temp.toString());
				//else
				//	rootAccessorsStr = "((" + tempCastType.toString() + ")"
				//			+ rootAccessorsStr.concat(temp.toString()) + ")";
				i++;
			}
		}
		return rootAccessorsStr;
	}
	/*
	 * public boolean equals(SymLoc second) { System.out.println("SymLoc,
	 * checking equality between :"+this+" and :"+second);
	 * 
	 * if(second.getKind() == kind) if(second.getRoot().equals(root))
	 * if(second.getAccessors().equals(accessors))
	 * if(second.getType().toString().equals(type))
	 * if(second.getModifiedAccessor().equals(modifiedAccessor)) {
	 * System.out.println("equals"); return true; } System.out.println("not
	 * equals"); return false;
	 *  }
	 */

}
