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

import soot.*;
import soot.util.*;

import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.vals.*;

/**
 * Generates most general stubs for environment methods.
 */
//TODO: finish, right now empty stubs are generated.
public class UniversalStubGenerator extends JavaStubGenerator {

	/** StubGenerator constructor that sets fields flowing from the
	 *  EnvGenerator.
	 */
	public UniversalStubGenerator(UnitInfo unit, EnvInfo env) {

		this.unit = unit;
		this.env = env;

	}

	public void genStubBody(SootClass sc, SootMethod sm) {
		logger.warning("StubGenerator, buildUniversalStubBody: not implemented yet");
		buildEmptyStubBody(sm);
	}

	public void buildEmptyStubBody(SootMethod markedMethod) {
		Type returnType = markedMethod.getReturnType();

		Body body = new JavaBody();
		body.setMethod(markedMethod);
		if (returnType instanceof PrimType
			|| returnType instanceof RefLikeType) {
			Chain units = body.getUnits();
			units.add(
				JavaGr.newReturnStmt(
					JavaGr.newValueExpr(new TopValue(returnType))));
		}
		markedMethod.setActiveBody(body);

	}

}
