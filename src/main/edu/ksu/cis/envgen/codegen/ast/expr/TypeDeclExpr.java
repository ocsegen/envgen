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

import soot.*;

import edu.ksu.cis.envgen.codegen.*;

public class TypeDeclExpr extends JavaExpr{

	String typeStr;
	Type type;
	String name;
	JavaExpr valueExpr;
	
	
	public TypeDeclExpr(String type, String name, JavaExpr value){
		this.typeStr = type;
		this.name = name;
		this.valueExpr = value;
	}
	
	public Type getType(){
		return type;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public String getTypeStr(){
		return typeStr;
	}
	
	public void setTypeStr(String type){
		this.typeStr = type;
	}
	
	public JavaExpr getValueExpr(){
		return valueExpr;
	}
	
	public void setValueExpr(JavaExpr expr){
		this.valueExpr = expr;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean throwsExceptions(){
		if(valueExpr != null)
			return valueExpr.throwsExceptions();
		return false;
	}
	
	public String toString(){
		return typeStr + " "+ name + "="+ valueExpr;
	}
	
	public String getCode(JavaPrinter printer){
		return typeStr + " "+name + "=" + valueExpr.toString();
	}
	
	public void printToFile(FileWriter file,  int shift){
		printf(file, typeStr);
		printf(file, " "+ name);
		if(valueExpr != null){
			printf(file, " = ");
			printf(file, valueExpr.toString());
		}
	}
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift){
		String type = printer.getPackageAdjustedType(typeStr);
		printf(file, type);
		printf(file, " "+ name);
		if(valueExpr != null){
			printf(file, " = ");
			valueExpr.printToFile(file, printer, shift);
		}
	}
}
