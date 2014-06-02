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

import java.util.*;
import java.util.logging.*;

import soot.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.util.*;

/**
 * Discovers unit's interface that consists of visible methods and fields of the
 * unit.
 * 
 */
public class UnitInterfaceFinder extends InterfaceFinder {
	/** Table of unit (internal) classes. */
	ModuleInfo unit;

	/**
	 * Table of environment components, in the case of the driver, components
	 * that make a driver.
	 */
	ModuleInfo env;

	ApplInfo applInfo;

	Logger logger = Logger.getLogger("envgen.applinfo");

	public UnitInterfaceFinder() {

	}

	public UnitInterfaceFinder(ModuleInfo unit, ModuleInfo env) {
		this.unit = unit;
		this.env = env;
	}

	public void setOptions(Properties properties) {
		//TODO: finish
		logger.fine("finish implementation");
	}

	/**
	 * 
	 * Discovers the interface of the unit.
	 * 
	 */
	public ApplInfo findInterface(ApplInfo applInfo) {
		assert(applInfo!=null);
		this.applInfo = applInfo;

		unit = applInfo.getUnit();
		env = applInfo.getEnv();
		
		SootClass internalClass;
		SootMethod internalMethod;

		ModuleInfo refinedUnit = new UnitInfo();
		Map attributes = refinedUnit.getAttributes();
		
		List<SootClass> implementors = new ArrayList<SootClass>();
		
		for (Iterator<SootClass> it = unit.getClasses().iterator(); it.hasNext();) {
			internalClass = it.next();
			if(!applInfo.isRelevantClass(internalClass))
				continue;
			
			//TODO: check if implementation is already provided
			if (Modifier.isAbstract(internalClass.getModifiers())) {
				SootClass concreteClass = EnvInterfaceFinder
						.buildImplementation(internalClass);

				//unit.addClass(concreteClass);
				implementors.add(concreteClass);
				refinedUnit.addClass(internalClass.getName(), internalClass);
				internalClass = concreteClass;
			}
			
			refinedUnit.addClass(internalClass);
			//TODO: handle inherited methods (?)

			List<SootMethod> methodsList = internalClass.getMethods();
			String methodName;
			for (Iterator<SootMethod> mi = methodsList.iterator(); mi.hasNext();) {
				internalMethod = mi.next();
				System.out.println("***MethodSig: "+internalMethod.getSignature());
				if (logger.isLoggable(Level.FINE)) 
					Util.printMethodBody(internalMethod);
				methodName = internalMethod.getName();

				// we can choose to pull in protected methods as well
				if (applInfo.isRelevantMethod(internalMethod)){
					refinedUnit.addMethod(internalMethod);
					Set methodAttributes = findAttributes(internalClass, internalMethod);
					attributes.put(internalMethod, methodAttributes);
				}
				
			}
		}

		//add implementors here to avoid ConcurrentModificationException
		//for(Iterator ii=implementors.iterator(); ii.hasNext();){
		//	internalClass  = (SootClass)ii.next();
		//	unit.addClass(internalClass);
		//}
		
		//TODO: add a non-observable method  (?)
		SootClass envClass = new SootClass("Environment");
		SootMethod nonObservableMethod = new SootMethod("nonObservable",
				new java.util.ArrayList(), VoidType.v());
		envClass.addMethod(nonObservableMethod);
		refinedUnit.addMethod(nonObservableMethod);
		
		applInfo.setUnit(refinedUnit);
		return applInfo;
		
	}
	
	protected Set findAttributes(SootClass sc, SootMethod sm){
		return null;
	}
	
}
