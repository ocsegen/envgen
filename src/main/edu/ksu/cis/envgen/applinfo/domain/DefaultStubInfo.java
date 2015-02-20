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
package edu.ksu.cis.envgen.applinfo.domain;

import java.util.logging.Logger;

import soot.*;

import edu.ksu.cis.envgen.*;

/**
 * Interested in unit type objects.
 *
 */
public class DefaultStubInfo extends ApplInfo {
	
	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis.domain");
	
	public boolean isRelevantClass(SootClass sc){	
		if (unit.isModuleType(sc)) {
			return true;
		}
		logger.fine("Not relevant: "+sc);
		return false;
	}
	
	
	public boolean isRelevantType(Type type){
		if (type instanceof RefType) {
			SootClass typeClass = ((RefType) type).getSootClass();
			return isRelevantClass(typeClass);
		}
		if (type instanceof ArrayType) {
			// figure out the element type
			Type elemType = ((ArrayType) type).baseType;
			return isRelevantType(elemType);

		}
		return false;
	}
	
	public boolean isRelevantMethod(SootMethod sm){
		SootClass declClass = sm.getDeclaringClass();
		return isRelevantClass(declClass);
		
	}
	
	public boolean isRelevantField(SootField sf){
		SootClass declClass = sf.getDeclaringClass();
		return isRelevantClass(declClass);
	}
}
