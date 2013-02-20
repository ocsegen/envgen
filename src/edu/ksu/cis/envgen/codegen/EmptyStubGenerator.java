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
package edu.ksu.cis.envgen.codegen;

//import java.util.*;

import soot.*;
import soot.util.*;

import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.ast.*;
//import edu.ksu.cis.envgen.codegen.ast.expr.*;
//import edu.ksu.cis.envgen.codegen.ast.stmt.*;
import edu.ksu.cis.envgen.codegen.vals.*;

/**
 * Generates empty stubs for environment methods, 
 * useful for producing a skeleton of the environment.
 * 
 */
public class EmptyStubGenerator extends JavaStubGenerator {

	public EmptyStubGenerator(){
		
	}
	
	/** StubGenerator constructor that sets fields flowing from the
	 *  EnvGenerator.
	 */
	public EmptyStubGenerator(UnitInfo unit, EnvInfo env) {

		this.unit = unit;
		this.env = env;
		//this.callGraph = callGraph;
		//this.envInterface = envInterface;

	}

	public void genStubBody(SootClass markedClass, SootMethod markedMethod) {
		//assert(markedMethod!=null)
		Type returnType = markedMethod.getReturnType();
		String methodName = markedMethod.getName();
		Body body = new JavaBody();
		body.setMethod(markedMethod);
		Chain units = body.getUnits();
		if (returnType instanceof PrimType
			|| returnType instanceof RefLikeType) {
			
			units.add(
				JavaGr.newReturnStmt(
					JavaGr.newValueExpr(new TopValue(returnType))));
		}
		
		//build a constructor
		/*
		else if (methodName.equals("<init>")) {
			List paramTypes = markedMethod.getParameterTypes();
			//if not empty, call super on the same args
			String params = "";
			int numParams = markedMethod.getParameterCount();
			for (int i = 0; i < numParams; i++) {
				if (i != 0)
					params = params+ ", ";
				
				params = params+" param" + i;
			}	
			if(params!= "")
				units.add(new ExprStmt(new StrExpr("super("+params+")")));
			
		}
		*/
		markedMethod.setActiveBody(body);
	}
}
