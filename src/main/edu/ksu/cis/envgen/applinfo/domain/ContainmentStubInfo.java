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

public class ContainmentStubInfo extends ApplInfo {

	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis.domain");

	public boolean isRelevantClass(SootClass sc) {
		if (unit.isModuleType(sc)) {
			return true;
		}
		//TODO: add support to specify this in a config
		String className = sc.getName();
		if (className.startsWith(" java.util.Iterator")
				 /*|| className.startsWith("java.util.ArrayList")
				|| className.startsWith("java.util.List")
				|| className.startsWith("java.util.AbstractList")
				|| className.startsWith("java.util.AbstractCollection") */
				|| className.startsWith("java.util.HashMap")
				|| className.startsWith("java.util.HashMap.Entry")
				|| className.startsWith("java.util.Map")
				|| className.startsWith("AbstractMap")
				 /*  || className.startsWith("java.util.Set") */
				|| className.startsWith("Vector")
				|| className.startsWith("Buffer") ) {
			logger.info(className + ": containment target");
			return true;
		} else {
			//logger.info(className + ": not containment target");
			return false;
		}
		
	}

	public boolean isRelevantType(Type type){
		if (type instanceof RefType) {
			SootClass typeClass = ((RefType) type).getSootClass();
			return isRelevantClass(typeClass);
		}
		if (type instanceof ArrayType) {
			// figure out the element type
			Type elemType = ((ArrayType) type).baseType;
			
			logger.info("********Checking array with element type: "+elemType);
			return isRelevantType(elemType);

		}
		return false;
				
	}
	
	public boolean isRelevantMethod(SootMethod sm) {
		SootClass sc = sm.getDeclaringClass();
		return isRelevantClass(sc);
	}

	public boolean isRelevantField(SootField sf) {
		//TODO: add support to specify this info in config
		String name = sf.getName();
		if(name.equals("table"))
			return true;
		if (name.equals("size") 
				|| name.equals("elementCount")
				|| name.equals("elementData"))
			return true;
		
		return false;
	}
}
