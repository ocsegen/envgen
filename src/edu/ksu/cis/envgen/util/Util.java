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
package edu.ksu.cis.envgen.util;

import java.io.PrintWriter;
import java.util.*;

import soot.*;

public class Util {
	
	public static List getTokenList(String strList){
		List result = new ArrayList();
	
		StringTokenizer tokenizer = new StringTokenizer(strList);
		int length = tokenizer.countTokens();
		String str = null;
		for (int i = 0; i < length; i++) {
			str = tokenizer.nextToken();
			result.add(str);
		}
		return result;
	}

	/**
	 * Given a type returns a soot class.
	 */
	public static SootClass getClass(Type type) {

		if (type instanceof RefType)
			return ((RefType) type).getSootClass();

		if (type instanceof ArrayType) {
			Type bType = ((ArrayType) type).baseType;
			if (bType instanceof RefType)
				return ((RefType) bType).getSootClass();

		}
		return null;
	}
	
	/**
	 * Checks whether <code>target</code> belongs to a library class.
	 */
	public static boolean isLibraryMethod(SootMethod target) {
		String name = target.getDeclaringClass().getName();

		if (name.startsWith("java") || name.startsWith("sun.")
				|| name.startsWith("org") || name.startsWith("com.")
				|| name.startsWith("edu.") || name.startsWith("de.")
				|| name.startsWith("gov.") || name.startsWith("scale."))
			return true;
		return false;

	}

	public static SootMethod getConstructor(SootClass sc) {
		List methods = sc.getMethods();
		SootMethod sm;
		for (Iterator i = methods.iterator(); i.hasNext();) {
			sm = (SootMethod) i.next();
			if (sm.getName().equals("<init>"))
				return sm;
		}
		return null;
	}
	
	public static void printMethodBody(SootMethod sm){

		if(sm.hasActiveBody()){
			Body jb = sm.retrieveActiveBody();
			
				System.out.println(
					"\nPrinting jimple for: " + sm.toString());
				PrintWriter out = new PrintWriter(System.out, true);
				Printer.v().printTo(jb, out);
		}

	}
	
	public static void printMethodBody(SootMethod sm, Body jb){

				System.out.println(
					"\nPrinting jimple for: " + sm.toString());
				PrintWriter out = new PrintWriter(System.out, true);
				Printer.v().printTo(jb, out);

	}
	
	/** Prints the error message and halts the execution. */
	public static void error(String msg) {
		System.out.println("\n!!!!!!! Can't go further !!!!!!!");
		System.out.println(msg + "\n");
		System.exit(-1);
	}
	
	/** Prints the error message and halts the execution. */
	public static void condError(boolean condition, String msg) {
		if(condition)
			error(msg);
	}		
	
}
