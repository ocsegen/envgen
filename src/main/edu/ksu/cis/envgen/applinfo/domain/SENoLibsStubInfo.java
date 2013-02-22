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

public class SENoLibsStubInfo extends ApplInfo {
	
	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis.domain");
	
	public boolean isRelevantClass(SootClass sc){
		String className = sc.getName();
		//exclude libraries
		if(className.startsWith("java.") ||
				className.startsWith("sun.") ||
				className.startsWith("javax.") ||
				className.startsWith("com.sun.") ||
				className.startsWith("com.ibm.") ||
				className.startsWith("org.xml.") ||
				className.startsWith("org.w3c.") ||
				className.startsWith("org.apache."))
			return false;
						
		return true;
	}
	
	
	public boolean isRelevantType(Type type){
		logger.warning("finish implementation");
		return true;		
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
