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

import soot.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.vals.SymLocValue;
import edu.ksu.cis.envgen.spec.UserSpec;

/**
 * Generates stubs for environment methods.
 * 
 */
public abstract class AbstractStubGenerator  extends CodeGenerator {
	/** Unit classes. */
	ModuleInfo unit;

	/** 
	 * Keeps track of environment components, 
	 * including only those that get called inside the unit. 
	 */
	ModuleInfo env;
	
	Assumptions assumptions;
	
	List stubs;
	
	protected void initialize (ApplInfo info){
		
		assert(info != null);
		
		this.unit = info.getUnit();
		this.env = info.getEnv();

		assert(env != null);	
	}
	

	/**
	 * If <code>seAnalysis</code>
	 * is set, performs side-effects analysis and cretes bodies from summary
	 * information for each of the environment methods.  
	 * If specification is provided reads it
	 * and generates bodies from user-provided descriptions.  If no specification
	 * of any kind is provided, assumes no data modifications. 
	 */
	public void genCode(ApplInfo info, Assumptions assumptions){
		
		initialize(info);
		
		this.assumptions = assumptions;
		
		//TODO: change to StubAssumptions
		if(assumptions instanceof UserSpec){
		  stubs  = ((UserSpec)assumptions).getStubs();
		}
			
		SootClass markedClass;
		SootMethod markedMethod;
		String className;
		//a hack to avoid ConcurrentModificationException
		Collection keys = env.getClassNamesCopy();

		for (Iterator it = keys.iterator(); it.hasNext();) {
			className = (String)it.next();
		
			markedClass = env.getClass(className);
			
			List methodsList = markedClass.getMethods();
			for (Iterator mi = methodsList.iterator(); mi.hasNext();) {
				markedMethod = (SootMethod) mi.next();
				if (markedMethod.isPhantom()
					|| markedMethod.isAbstract()
					|| markedMethod.isNative()) {
					
					continue;
				}
				
				if (!markedMethod.hasActiveBody()){
					
						genStubBody(markedClass, markedMethod);
	
				}		
			}
		}
		genTopFields();
	    //genEnvEqualsClass();
	}


	public abstract void genStubBody(SootClass markedClass, SootMethod markedMethod);

	public abstract void genMissingFields(SymLocValue symVal, SootClass markedClass);
	
	//public abstract void genMissingMethods(SootClass markedClass);
	
	//TODO: move into EnvInterfaceFinder
	public abstract void genTopFields();
	
	public abstract void genEnvEqualsClass();
}
