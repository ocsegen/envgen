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

import java.util.List;

import soot.*;
import edu.ksu.cis.envgen.codegen.*;

public class InvokeExpr extends JavaExpr {

	String receiverStr;

	String methodName;
	
	SootMethod method;

	List args;

	public InvokeExpr(String receiver, String m, List args){
		this.receiverStr = receiver;
		this.methodName = m;
		this.args = args;
	}
	
	public String getReceiver(){
		return receiverStr;
	}
	
	public void setReceiver(String receiver){
		this.receiverStr = receiver;
	}
	
	public String getMethodName(){
		return methodName;
	}

	public List getArgs(){
		return args;
	}
	
	public void setArgs(List args){
		this.args = args;
	}
	
	public void setMethod(SootMethod sm){
		this.method = sm;
	}
	
	public SootMethod getMethod(){
		return method;
	}
	
	public boolean isNonObservable(){
		//System.out.println("checking method: "+methodName +", recever: "+receiverStr);
		if(methodName.equals("nonObservable")
			  && receiverStr.equals("environment0"))
			 return true;
		return false;
	}
	
	public boolean throwsExceptions(){
		if(method == null){
			logger.severe("MethodCall: null method in "+this);
			return false;
		}
		if(method.getExceptions() == null){
			logger.severe("MethodCall: null exceptions");
			return false;
		}
		return !(method.getExceptions().isEmpty());
	}
	
	public String toString(){
		if(isNonObservable())
			return "/* non-observable action */";
		return receiverStr + "." +methodName+args;
	}
	
	public String getCode(JavaPrinter printer){
		
		if(isNonObservable())
			return "/* non-observable action */";
		return receiverStr + "." +methodName+printer.printArgs(args);
		
		
	}
	
	//TODO: handle non-observable separately or put into a separate class
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift) {
		for (int i = 0; i < shift; i++)
			printf(file, " ");
		
		if(isNonObservable()){
			printf(file, "/* nonobservable */");
			return;
		}
		
		if (receiverStr != null) {
			printf(file, receiverStr);
			printf(file, ".");
		}
		printf(file, methodName);
		printf(file, printer.printArgs(args));
	}
}
