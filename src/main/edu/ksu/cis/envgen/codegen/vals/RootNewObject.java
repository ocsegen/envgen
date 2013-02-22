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
import soot.jimple.*;
import edu.ksu.cis.envgen.codegen.JavaPrinter;

public class RootNewObject extends Root{
	
	int creationIndex;

	Stmt allocationSite;

	boolean summaryNode;
	
	/** Enclosing object of the inner class */
	//boolean isInnerLoc;
	//boolean refEnclosingLoc;
	
	public RootNewObject(Value value, Type type) {
		//this.root = value;
		//this.rootType = type;
		super(value, type);	
	}

	/*--------------------------------------------------*/
	/*           Get and Set methods                    */
	/*--------------------------------------------------*/

	
	public int getCreationIndex() {
		return creationIndex;
	}

	/**
	 * also sets a name of this root
	 * @param index
	 */
	public void setCreationIndex(int index) {
		creationIndex = index;
		
		if(root instanceof NewExpr){
			Type refType = ((NewExpr) root).getBaseType();
			//trim the name
			String typeName = JavaPrinter.getShortName(refType.toString());
			name = typeName.toLowerCase() + creationIndex;
		}
		else if(root instanceof NewArrayExpr){
			Type elemType = ((NewArrayExpr) root).getBaseType();
				//trim the name
			String typeName = JavaPrinter.getShortName(elemType.toString());
			name = typeName.toLowerCase() + creationIndex;
		}
		if (summaryNode)
			name = name + "summary";
	}

	public Stmt getAllocationSite() {
		return allocationSite;
	}

	public void setAllocationSite(Stmt unit) {
		allocationSite = unit;
	}

	/*
	public SymLocPath getEnclosingLoc(){
		return enclosingLoc;
	}
	
	public void setEnclosingLoc(SymLocPath loc){
		enclosingLoc = loc;
	}
	
	public boolean refEnclosingLoc(){
		return refEnclosingLoc;
	}
	
	public void setRefEnclosing(boolean enclosing){
		this.refEnclosingLoc = enclosing;
	}
	*/
	
	public boolean getSummaryNode() {
		return summaryNode;
	}

	public void setSummaryNode(boolean summary) {
		summaryNode = summary;
	}
	
	public boolean isInnerLoc(){
		//System.out.println("inner");
		
		if (rootType.toString().indexOf('$') > 0)
			//root.setIsInnerLoc(true);
			return true;
		return false;
	}
	
	/*
	public void setIsInnerLoc(boolean inner){
		isInnerLoc = inner;
	}
	*/
	

	/*--------------------------------------------------*/
	/*      Methods to get a String representation      */
	/*--------------------------------------------------*/

	/**
	 * Generates code representation of symbolic
	 * location appropriate for code generation.
	 */
	public String getCode(JavaPrinter printer) {

		String rootStr = name;
		
		/*
		if(isInnerLoc())
			rootStr = rootStr +" inner";
		*/
		if (rootCastType != null)
			return (
				"(("
					+ printer.getPackageAdjustedType(rootCastType)
					+ ")"
					+ rootStr
					+ ")");
		else
			return rootStr;

	}
	
	public String toString() {

		String rootStr = "";
		if (root == null)
			return "no root";

		if (root instanceof NewExpr) {
			Type refType = ((NewExpr) root).getBaseType();
			//trim the name
			String typeName = JavaPrinter.getShortName(refType.toString());
			rootStr =
				typeName.toLowerCase() + "<" + allocationSite.toString() + ">";

		} else if (root instanceof NewArrayExpr) {
			Type elemType = ((NewArrayExpr) root).getBaseType();
			//trim the name
			String typeName = JavaPrinter.getShortName(elemType.toString());
			logger.fine("AllocationSite: " + allocationSite);

			rootStr =
				typeName.toLowerCase() + "<" + allocationSite.toString() + ">";
		} else if (root instanceof NewMultiArrayExpr) {
			logger.severe("Root, getCode: finish!");
		} else
			logger.severe("RootNewObject, getRootCode: unhandled val type: "+root);

		/*
		if(isInnerLoc)
			rootStr = "inner: "+rootStr;;
		*/
		
		if (rootCastType != null)
			return ("((" + rootCastType.toString() + ")" + rootStr + ")");
		else
			return rootStr;

	}

	/*
	public boolean equals(Root second)
	{
	System.out.println("Root, equals?");
	if(second.getRoot().toString().equals(root.toString()))
	    if(second.getRootType().toString().equals(rootType.toString()))
		if(second.getRootCastType().toString().equals(rootCastType.toString()))
		    //if(second.getCreationIndex() == creationIndex)
			if(second.getAllocationSite().equals(allocationSite))
			    //if(second.getSummaryNode() == summaryNode)
				return true;
	return false;
		 
	}
	*/
}
