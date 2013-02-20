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

public class RootParam extends Root{

	//parameter id
	int index;

	public RootParam(Value value, Type type) {
		//this.root = value;
		//this.rootType = type;
		super(value, type);
		index = ((ParameterRef) root).getIndex();
	}

	/*--------------------------------------------------*/
	/*           Get and Set methods                    */
	/*--------------------------------------------------*/

	/**
	 * Returns top level location: either 
	 * it's a static fields of the unit
	 * or a parameter.
	 */

	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	

	/*--------------------------------------------------*/
	/*      Methods to get a String representation      */
	/*--------------------------------------------------*/

	/**
	 * Generates code representation of symbolic
	 * location appropriate for code generation.
	 */
	public String getCode(JavaPrinter printer) {

		String rootStr = "param" + index;
		
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

			rootStr = "param" + index;
		

		if (rootCastType != null)
			return ("((" + rootCastType.toString() + ")" + rootStr + ")");
		else
			return rootStr;

	}
}
