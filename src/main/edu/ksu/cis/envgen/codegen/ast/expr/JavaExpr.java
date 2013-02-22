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
package edu.ksu.cis.envgen.codegen.ast.expr;

import java.util.*;
import java.util.logging.Logger;
import java.io.*;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import edu.ksu.cis.envgen.codegen.*;

/**
 * Representation of the Java expression.
 */
public abstract class JavaExpr  /* implements Expr */{
	
	Logger logger = Logger.getLogger("envgen.codegen.javagrammar");

	/**
	 * Kinds of expression: 0 - string of code (used as a shortcut for names,
	 * etc) 1 - method call, 2 - assignment, 3 - new : type name = new
	 * methodcall.
	 * 
	 *  
	 */
	//int kind;

	//String type;

	//String name;

	//String receiver;

	//String method;

	//String args;

	//JavaExpr val;
	
	//TODO: refactor to contain soot Value

	/*
	public JavaExpr(int k, String t, String n, String r, String m, String a,
			JavaExpr v) {
		kind = k;
		type = t;
		name = n;
		receiver = r;
		method = m;
		args = a;
		val = v;
	}

	public void printToFile(FileWriter file, int shift) {
		for (int i = 0; i < shift; i++)
			printf(file, " ");
		switch (kind) {
		case 0:
			printf(file, name);
			break;

		case 1:
			if (val != null) {
				val.printToFile(file, 0);
				printf(file, ".");
			}
			printf(file, method);
			printf(file, args);
			break;

		case 2:
			printf(file, name + " = ");
			val.printToFile(file, 0);
			break;

		case 3:

			printf(file, "new ");
			printf(file, type);
			printf(file, args);

			break;

		default:
			break;

		}
	}
	*/
	
	public abstract boolean throwsExceptions();
	
	public abstract String getCode(JavaPrinter printer);
	
	//public abstract void printToFile(FileWriter file,  int shift);
	
	public abstract void printToFile(FileWriter file, JavaPrinter printer, int shift); 

	public void printf(FileWriter outStream, String string) {

		if(string == null)
			string = "null";
		
		for (int i = 0; i < string.length(); i++) {
			try {
				outStream.write(string.charAt(i));
			} catch (Exception e) {
			}
		}
	}
	
	/** 
	  * Generates a list of top values for a list of <code>types</code>. 
	  */
	//TODO: may want to have ArgList as a separate class
	/*
	public String printArgs(List args) {
		
		assert(args != null);
	
		String result = "";
		int count = 0;
		JavaExpr val;
		for (Iterator ai = args.iterator(); ai.hasNext();) {
			val = (JavaExpr)ai.next();
			if (count != 0)
				result = result + ", ";
			result = result + val.toString();
			count++;
		}
		return "(" + result + ")";
	}
	*/
}