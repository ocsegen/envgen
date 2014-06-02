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
	
	public static List<String> getTokenList(String strList){
		List<String> result = new ArrayList<String>();
	
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
		List<SootMethod> methods = sc.getMethods();
		SootMethod sm;
		for (Iterator<SootMethod> i = methods.iterator(); i.hasNext();) {
			sm = i.next();
			if (sm.getName().equals("<init>"))
				return sm;
		}
		return null;
	}
	
	/* find a public constructor with the least number of args */
	//TODO: should preference be given to primitive type args? 
	public static SootMethod getConstructorMin(SootClass sc) {
		List<SootMethod> methods = sc.getMethods();
		SootMethod sm = null;
		SootMethod result = null;
		int numArgs;
		int resultNumArgs = 1000;
		for (Iterator<SootMethod> i = methods.iterator(); i.hasNext();) {
			sm = i.next();
			if(!sm.isPublic())
				continue;
			if (sm.getName().equals("<init>")){
				List<Type> types = sm.getParameterTypes();
				numArgs = types.size();
				if(numArgs < resultNumArgs){
					resultNumArgs = numArgs;
					result = sm;
				}
					
			}
				
		}
		return result;
	}
	
	
	/* find a public constructor with primitive args */
	//TODO: should the preference be given to constructors that setup the most data?
	public static SootMethod getConstructorPrimitiveArgs(SootClass sc) {
		List<SootMethod> methods = sc.getMethods();
		SootMethod sm = null;
		Type type;
		SootMethod result = null;
		//int numArgs;
		//int resultNumArgs = 1000;
		for (Iterator<SootMethod> i = methods.iterator(); i.hasNext();) {
			sm = i.next();
			if(!sm.isPublic())
				continue;
			if (sm.getName().equals("<init>")){
				List<Type> types = sm.getParameterTypes();
				boolean allPrimitiveArgs = true;
				for(Iterator<Type> ti = types.iterator(); ti.hasNext();){
					type = ti.next();
					if(type instanceof RefType ||
					   type instanceof ArrayType)
						allPrimitiveArgs = false;
				}
				if(allPrimitiveArgs)
					return sm;
					
			}
				
		}
		return result;
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
