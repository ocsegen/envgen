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
package edu.ksu.cis.envgen.spec;

import soot.*;

public class StubSpec {
	String className;
	String methodName;
	
	SootMethod sm;
	
	//TODO: change into LTL/RE spec + optional return stmt
	Body body;
	
	public StubSpec(String className, String methodName){
		this.className = className;
		this.methodName = methodName;
	}
	

	public String getClassName(){
		return className;
	}
	
	public String getMethodName(){
		return methodName;
	}
	
	public SootMethod getMethod(){
		return sm;
	}
	
	public Body getBody(){
		return body;
	}
	
	public void setBody(Body body){
		this.body = body;
	}
	
	public void setMethod(SootMethod sm){
		this.sm = sm;
	}
}
