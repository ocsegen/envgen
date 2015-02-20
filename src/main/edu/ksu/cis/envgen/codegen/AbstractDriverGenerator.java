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

import java.util.*;
import java.util.logging.Logger;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.spec.*;

/**
 * Generates drivers for unit under analysis.
 * 
 */
public abstract class AbstractDriverGenerator extends CodeGenerator {
//	/** Table of unit (internal) classes. */
//	public ModuleInfo unit;
//
//	/**
//	 * Table of environment classes. */
//	public ModuleInfo env;	
	
	public List instantiations;
	public Map definitions;
	public List threads;
	public ThreadSpec mainThread;
	
	Logger logger = Logger.getLogger("envgen.codegen");
	
	protected void initialize(ApplInfo info){

		assert(info!=null);

		this.unit = info.getUnit();
		this.env = info.getEnv();
			
		assert(unit!=null);
	}
		
	public void genCode(ApplInfo info, Assumptions assumptions) {
		
		initialize(info);
		
		//TODO: change to DriverAssumptions
		if(assumptions instanceof UserSpec){
		  //this.userSpec = (UserSpec) assumptions;
		  instantiations = ((UserSpec)assumptions).getInitProps();
		  threads = ((UserSpec)assumptions).getThreads();
		  definitions = ((UserSpec)assumptions).getDefinitions();
		  mainThread = ((UserSpec)assumptions).getMainThread();
		  genThreads();
		  genMainThread();
		}
		else if(assumptions == null){
			logger.info("generating a universal driver");
			//instantiations = defaultInstantiations;
			threads = new ArrayList();
			genThreads();
			genMainThread();
		}
		else
			logger.severe("AbstractDriverGenerator: Expected assumptions of UserSpec type");
		
	}

	public abstract void genMainThread();

	public abstract void genThreads();

}