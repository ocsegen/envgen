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

public class ContainmentDriverInfo extends ApplInfo {
	
	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis.domain");

	public boolean isRelevantClass(SootClass sc){
		//TODO: finish
		logger.warning("finish implementation");
		return true;
	}
	
	public boolean isRelevantType(Type type){
		//TODO: finish
		logger.warning("finish implementation");
		return true;		
	}
	public boolean isRelevantMethod(SootMethod sm){
		//TODO: finish
		logger.warning("finish implementation");
		return true;
	}
	public boolean isRelevantField(SootField sf){
		//TODO: finish
		logger.warning("finish implementation");
		return true;
	}
}
