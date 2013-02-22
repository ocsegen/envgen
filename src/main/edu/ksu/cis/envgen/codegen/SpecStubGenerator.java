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
import edu.ksu.cis.envgen.codegen.vals.TopValue;
//import edu.ksu.cis.envgen.spec.*;
//import edu.ksu.cis.envgen.spec.parser.*;

/**
 * Generates stubs for environment methods
 * based on  user specifications.
 * 
 */
public class SpecStubGenerator  extends JavaStubGenerator {

	/** Set to insert atomic steps into the generated environment. */
	//boolean atomicStepsMode;

	public SpecStubGenerator(){
		
	}

	/** StubGenerator constructor that sets fields flowing from the
	 *  EnvGenerator.
	 */
	public SpecStubGenerator(UnitInfo unit, EnvInfo env) {

		this.unit = unit;
		this.env = env;

	}

	public void genStubBody(
			SootClass markedClass,
			SootMethod markedMethod) {
		
		//TODO: finish code generation from LTL/RE
		//grab a spec of markedMethod from assumptions
		//generate code similar to driver's
		
		//empty stub generation
		
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
		markedMethod.setActiveBody(body);
		
	}


}
