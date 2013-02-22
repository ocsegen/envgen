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
import java.util.logging.Logger;

import edu.ksu.cis.envgen.codegen.*;

import soot.*;
//import soot.jimple.FieldRef;
//import soot.jimple.InvokeExpr;
//import soot.jimple.Stmt;
//import soot.jimple.AssignStmt;
//import soot.jimple.Constant;

public class EnvInfo extends ModuleInfo{
	/** Table of env classes */
	//Map classes = new HashMap();
	
	/** A list of concrete (analyzable) top-level environment methods */
	//List methods = new ArrayList();


	/** Used for insertion of environment classes */
	
	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.applinfo");
	
	public SootClass addClass(SootClass sc) {
		
		String className = sc.getName();

		if (!containsClass(className)) {
			//logger.fine("inserting into the envTable: " + sc);

			//create skeleton of the environment class
			//i.e., a class with the same name and modifiers 
			String prefixedName = JavaPrinter.getPrefixedName(className);
			SootClass markedClass =
				new SootClass(prefixedName, sc.getModifiers()
			
			& ~Modifier.ABSTRACT | Modifier.PUBLIC);

			//EnvClass markedClass =
			//	new EnvClass(prefixedName, sc.getModifiers()
			
			//& ~Modifier.ABSTRACT | Modifier.PUBLIC);

			logger.fine("adding to env: " + markedClass.getName());
			
			addClass(className, markedClass);
			
			if(!markedClass.isInterface()){
				SootMethod defaultConstructor = new SootMethod("<init>", new ArrayList(), VoidType.v(), Modifier.PUBLIC);
				//EnvMethod defaultConstructor = new EnvMethod("<init>", new ArrayList(), VoidType.v(), Modifier.PUBLIC);
				markedClass.addMethod(defaultConstructor);
				//defaultConstructor.setModifiers(Modifier.PUBLIC);
			}
			return markedClass;
		}
		return sc;
	}
	
	/**
	 * Creates an image of <code>externalMethod</code> not including 
	 * the body, and adds it to the <code>externalClass</code>.
	 */
	public void addMethodToClass(
		SootClass markedClass,
		SootMethod externalMethod) {
		
		String methodName = externalMethod.getName();
		List parameterTypes = externalMethod.getParameterTypes();
		Type returnType = externalMethod.getReturnType();
		if (!markedClass
			.declaresMethod(methodName, parameterTypes, returnType)) {
			
			SootMethod markedMethod =
				new SootMethod(
					methodName,
					parameterTypes,
					returnType,
					externalMethod.getModifiers()
			, externalMethod.getExceptions());
			
			//no abstract methods, except for interfaces
			if(!markedClass.isInterface()){
				markedMethod.setModifiers(externalMethod.getModifiers() & (~Modifier.ABSTRACT));
			}
			//TODO: volatile causes a problem?
			String mod = Modifier.toString(markedMethod.getModifiers());
			if(mod.contains("volatile")){
				//System.out.println("volatile method: " +markedMethod.getName());
				markedMethod.setModifiers(externalMethod.getModifiers() & (~Modifier.VOLATILE));
			
			}
			//TODO: check the usage of native
			if(mod.contains("native")){
				//System.out.println("native method: " +markedMethod.getName());
				markedMethod.setModifiers(externalMethod.getModifiers() & (~Modifier.NATIVE));
			
			}
		
			//logger.info("adding to env: " + markedMethod.getName());
			markedClass.addMethod(markedMethod);

			//fill the envMethods list
			if (externalMethod.isPhantom()
				|| externalMethod.isNative()
				|| externalMethod.isAbstract())
				return;
			if (!methods.contains(externalMethod))
				methods.add(externalMethod);
		}
	}

	public void addFieldToClass(
			SootClass markedClass,
			SootField externalField) {

		String fieldName = externalField.getName();
		Type fieldType = externalField.getType();
		if (!markedClass
				.declaresField(fieldName, fieldType)) {
				

			SootField markedField =
			new SootField(
				fieldName,
				fieldType,
				externalField.getModifiers());
			//logger.fine("adding field to class "+ markedClass);
			markedClass.addField(markedField);
			
			//TODO: might want to initialize this field if it is
			//initialized in the original class
			
			//init constants
			
			if(externalField.isFinal()){
				
				logger.info("checking final field: "+externalField);
				addConstant(externalField);
			}
			
			/*
			if(externalField.isFinal()){
				
				logger.info("checking final field: "+externalField);
				//get its value and put into the constant pool
				
				SootClass externalClass = externalField.getDeclaringClass();
				
				logger.info("checking final field of: "+markedClass);
				
				//System.out.println("methods: "+externalClass.getMethods());
				
				if(!externalClass.declaresMethodByName("<clinit>"))
					return;
				SootMethod clinitMethod = externalClass.getMethodByName("<clinit>");
				//TODO: check that there is one only
				
				//step through the statements
				
				Body jb = null;
				try{
				jb = clinitMethod.retrieveActiveBody();
				}catch (Exception e){
					e.printStackTrace(); 
					logger.severe("can't analyze method: "+clinitMethod);
					return;
				}
				if (logger.isLoggable(Level.FINE)) 
					Util.printMethodBody(clinitMethod, jb);
				
				PatchingChain stmtList = jb.getUnits();
				for (Iterator si = stmtList.iterator(); si.hasNext();) {
					Stmt s = (Stmt) si.next();
					logger.finer("statement: "+s+" is "+s.getClass().getName());

					if (s instanceof AssignStmt) {
						Value lhs = ((AssignStmt)s).getLeftOp();
						if (lhs instanceof FieldRef){
							SootField refField = ((FieldRef)lhs).getField();
							if(refField.getSignature().equals(externalField.getSignature())){
								//grab the rhs, this is the value
								Value rhs = ((AssignStmt)s).getRightOp();
								if(rhs instanceof Constant)
									addConstantValue(markedField, rhs);
								//else
								//	logger.warning("Didn't find const val/source for: "+externalField);			
							}
							
						}
							
					}
					
				}
				
			}
			*/
			
		}
	}
	
}
