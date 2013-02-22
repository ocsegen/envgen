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

import edu.ksu.cis.envgen.codegen.*;

public class AssignExpr extends JavaExpr{

	JavaExpr leftExpr;
	JavaExpr rightExpr;
	String op;
	
	public AssignExpr(JavaExpr left, String op, JavaExpr right){
		this.leftExpr = left;
		this.op = op;
		this.rightExpr = right;
	}
	
	public JavaExpr getLeft(){
		return leftExpr;
	}
	
	public JavaExpr getRight(){
		return rightExpr;
	}
	
	public boolean throwsExceptions(){
		return leftExpr.throwsExceptions() || rightExpr.throwsExceptions();
	}
	
	public String toString(){
		return leftExpr + op + rightExpr;
		
	}
	
	public String getCode(JavaPrinter printer){
		return leftExpr.getCode(printer)+op+rightExpr.getCode(printer);
	}
	
	public void printToFile(FileWriter file, int shift) {
		
		printf(file, leftExpr.toString());
		printf(file, op);
		printf(file, rightExpr.toString());
	}
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift) {
		printf(file, leftExpr.getCode(printer));
		printf(file, op);
		printf(file, rightExpr.getCode(printer));
	}
}
