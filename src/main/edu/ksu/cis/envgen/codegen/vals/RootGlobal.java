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


public class RootGlobal extends Root{

	public RootGlobal(Value value, Type type) {
		//this.root = value;
		//this.rootType = type;
		super(value, type);
	}
	
	/*--------------------------------------------------*/
	/* Methods to get a String representation */
	/*--------------------------------------------------*/

	/**
	 * Generates code representation of symbolic location appropriate for code
	 * generation.
	 */
	public String getCode(JavaPrinter printer) {

		String rootStr = "";

		SootField f = ((StaticFieldRef) root).getField();
		String s = f.getDeclaringClass().getName();
		// adjust the name of the class
		rootStr = (printer.getPackageAdjustedType(s) + "." + f.getName());

		if (rootCastType != null)
			return ("((" + printer.getPackageAdjustedType(rootCastType)
					+ ")" + rootStr + ")");
		else
			return rootStr;

	}

	public String toString() {

		String rootStr = "";

		SootField f = ((StaticFieldRef) root).getField();
		String s = f.getDeclaringClass().getName();
		rootStr = (s + "." + f.getName());

		if (rootCastType != null)
			return ("((" + rootCastType.toString() + ")" + rootStr + ")");
		else
			return rootStr;

	}

	/*
	 * public boolean equals(Root second) { System.out.println("Root, equals?");
	 * if(second.getRoot().toString().equals(root.toString()))
	 * if(second.getRootType().toString().equals(rootType.toString()))
	 * if(second.getRootCastType().toString().equals(rootCastType.toString()))
	 * //if(second.getCreationIndex() == creationIndex)
	 * if(second.getAllocationSite().equals(allocationSite))
	 * //if(second.getSummaryNode() == summaryNode) return true; return false;
	 *  }
	 */
}
