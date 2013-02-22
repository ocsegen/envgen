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

import java.io.FileWriter;

import java.util.*;

import soot.*;

import edu.ksu.cis.envgen.codegen.JavaPrinter;

public class NewExpr extends JavaExpr{
	
	Type type;
	String typeStr;
	List args;
	
	//public NewExpr(Type type, List args){
	//	this.args = args;
	//	this.type = type;
	//}

	public NewExpr(String type, List args){
		this.args = args;
		this.typeStr = type;
	}
	
	public String getTypeStr(){
		return typeStr;
	}
	
	public void setTypeStr(String type){
		this.typeStr = type;
	}
	
	public Type getType(){
		return type;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public List getArgs(){
		return args;
	}
	
	public void setArgs(List args){
		this.args = args;
	}
	
	public boolean throwsExceptions(){
		return false;
	}
	
	public String toString(){
		return "new "+typeStr+args;
	}
	
	public String getCode(JavaPrinter printer){
		return "new "+typeStr+printer. printArgs(args);
	}
	
	public void printToFile(FileWriter file,  int shift){
		printf(file, "new ");
		printf(file, typeStr);
		printf(file, "(");
		logger.warning("refine print args");
		printf(file, args.toString());
		printf(file, ")");
	}
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift){
		String type = printer.getPackageAdjustedType(typeStr);
		printf(file, "new ");
		printf(file, type);
		printf(file, printer.printArgs(args));
	}
}
