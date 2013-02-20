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

public class GUIStubInfo extends ApplInfo {
	
	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis.domain");
	
	public boolean isRelevantClass(SootClass sc){
		String className = sc.getName();
		//not tracking inner classes
		if(className.contains("$"))
			return false;
		//TODO: add support to specify these in a config
		if( className.equals("java.awt.Container") ||
				 className.equals("java.awt.Component")  /* ||
				 className.startsWith("javax.swing.JComponent")  ||
				 className.startsWith("java.util.AbstractList") ||
				 className.startsWith("java.util.AbstractCollection") ||
				 className.equals("java.util.HashMap") ||
				 className.startsWith("java.util.HashMap.Entry") ||
				 className.startsWith("java.util.Map") ||
				 className.startsWith("AbstractMap") ||
				 className.startsWith("java.util.Set") */ ){
					 logger.fine(className+ ": gui target");
					 return true; 
				 }
				 else{
					 logger.fine(className +": not gui target");
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
			return isRelevantType(elemType);

		}
		return false;
	}
	
	public boolean isRelevantMethod(SootMethod sm){
		SootClass sc = sm.getDeclaringClass();
		return isRelevantClass(sc);
	}
	public boolean isRelevantField(SootField sf){
		//TODO: add support to specify these in a config
		String name = sf.getName();
		if(name.equals("visible"))
			return true;
		if(name.equals("enabled"))
			return true;
		if(name.equals("component"))
			return true;
		if(name.equals("componentListener"))
				return true;
		return false;
		
	}
}
