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
package edu.ksu.cis.envgen.applinfo;

import java.util.logging.Logger;

import soot.*;

public class UnitInfo extends ModuleInfo{

	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.applinfo");
	
	public SootClass addClass(SootClass sc){
		classes.put(sc.getName(), sc);
		return sc;
	}
	
	public void addMethodToClass(
			SootClass markedClass,
			SootMethod externalMethod) {
		
		//not used yet
		logger.info("finish implementation");
	}
	
	public void addFieldToClass(SootClass sc, SootField sf){
		//not used yet
		logger.info("finish implementation");
	}
	

}
