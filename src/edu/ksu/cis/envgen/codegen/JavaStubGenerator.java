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

import soot.*;
import soot.util.*;

import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.ast.stmt.JavaStmt;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.Util;

/**
 * Generates stubs for environment methods.
 * 
 */
public abstract class JavaStubGenerator  extends AbstractStubGenerator {
	
	boolean atomicStepsMode = false; 
	
	String packageName = "";
	
	Logger logger = Logger.getLogger("envgen.codegen");
	
	public void setOptions(Properties properties){
		//TODO: recheck/finish options
		
		String atomicStr = properties.getProperty("atomicStepsMode");
		atomicStepsMode = Boolean.valueOf(atomicStr);
		
		String packageNameStr = properties.getProperty("packageName");
		if(packageNameStr != null)
			packageName = packageNameStr;
	}

	/**
	 * If sym is of the form this.f and markedClass doesn't
	 * declare f, such field is included. 
	 */
	public void genMissingFields(SymLocValue symVal, SootClass markedClass) {
		if(!(symVal instanceof SymLoc))
			return;
		SymLoc sym = (SymLoc)symVal;
		
		//TODO: check
		//String symCode = sym.getCode();
		String symCode = sym.toString();
		if (symCode.startsWith("this")) {

			List accessors = ((SymLocPath)sym).getAccessors();
			//the first field is the one
			//that needs to be inserted into the marked class
			Accessor a;
			SootField sf = null;
			if (accessors != null && !accessors.isEmpty()) {
				a = (Accessor) accessors.get(0);
				sf = a.getField();
			} else {
				a = sym.getModifiedAccessor();
				if (a != null)
					sf = a.getField();
			}
			if (sf != null) {
				if (!markedClass.declaresField(sf.getName(), sf.getType())) {
					//check the type of this field and make sure environment has it
					Type fieldType = sf.getType();
					SootClass typeClass = Util.getClass(fieldType);
					
					//TODO: check this error
					if(typeClass == null){
						logger.severe("null class for type: "+fieldType);
					}
					else{
					
						//TODO: check unitAnalysis
						if(!unit.containsClass(typeClass.getName())){
							env.addClass(typeClass);
						}
					}

					SootField markedField =
						new SootField(
							sf.getName(),
							fieldType,
							sf.getModifiers());
					markedClass.addField(markedField);
				}
			}
		}
		
		//recheck the type of the location
		Type type = sym.getType();
		SootClass locTypeClass = Util.getClass(type);
		//TODO: check unitAnalysis
		if(locTypeClass == null)
			logger.severe("null class for type: "+type);
		else
		  if(!unit.containsClass(locTypeClass.getName())){
			env.addClass(locTypeClass);
		}
		
		//check the cast type
		
		//check the static field
		
	}

	/**
	 * This method adds top fields and special constructors.
	 */
	public void genTopFields() {
		SootClass markedClass;
		SootClass realClass;

		Type classType;
		String className, typeName, name;
		
		for (Iterator it = env.getClasses().iterator(); it.hasNext();) {
			markedClass = (SootClass) it.next();
			if (markedClass.isInterface())
				continue;
			if(markedClass.isAbstract())
				continue;
			
			className = markedClass.getName();
			
			//TODO: check if we can add classes into libs in Scene
			
			if(className.startsWith("$")){
				//get rid of a prefix
				typeName = JavaPrinter.getActualName(markedClass.getName());
			
			
				realClass = Scene.v().getSootClass(typeName);
				classType = realClass.getType();
		
			}
			else{
				//the counterpart of this class is not in Scene
				classType = markedClass.getType();
			}
			name = JavaPrinter.getShortName(className);
			
			//add the top field
			SootField topField =
				new SootField(
					"TOP",
					classType,
					Modifier.PUBLIC | Modifier.STATIC);
			markedClass.addField(topField);

			//add a special constructor
			/*
			SootMethod markedMethod =
				new SootMethod(
					"new" + name,
					new MultiSet(),
					classType,
					Modifier.PUBLIC);
			markedClass.addMethod(markedMethod);
			Body body = new JavaBody();
			body.setMethod(markedMethod);

			Chain units = body.getUnits();
			units.add(JavaGr.newExprStmt(JavaGr.newStrExpr("return TOP")));
			

			markedMethod.setActiveBody(body);
			*/
			
			//add a default constructor 
			//if(!markedClass.declaresMethod("void <init>()"))
			//{
			//System.out.println("*******adding a default constructor to: "+markedClass);
			//SootMethod defaultConstructor = new SootMethod("<init>",  
			//					       new MultiSet(), 
			//					       VoidType.v(), 
			//					       Modifier.PUBLIC);  
			//markedClass.addMethod(defaultConstructor);
			//}
		}
	}

	/** Adds a class with a special definition of "equals" method.
	 */
	public void genEnvEqualsClass() {
		//build a class that defines a special "equals" method
		SootClass markedClass;
		//String name;
		String typeName;

		SootClass eqClass = new SootClass("EnvEquals", Modifier.PUBLIC);

		SootMethod eqMethod =
			new SootMethod(
				"equals",
				Arrays.asList(
					new Type[] {
						RefType.v("java.lang.Object"),
						RefType.v("java.lang.Object")}),
				BooleanType.v(),
				Modifier.PUBLIC | Modifier.STATIC);

		eqClass.addMethod(eqMethod);
		Body body = new JavaBody();
		body.setMethod(eqMethod);

		Chain units = body.getUnits();
		// construct the body

		for (Iterator it = env.getClasses().iterator(); it.hasNext();) {
			markedClass = (SootClass) it.next();
			//get rid of a prefix
			typeName = JavaPrinter.getActualName(markedClass.getName());
			
			//TODO: fix
			//typeName = vgen.getPackageAdjustedName(typeName);
			
			//filter out inner and anonymous classes for now (?)		
			//if(typeName.indexOf('$')!= -1){
			//	continue;
			//}

			JavaStmt returnChoose =
				JavaGr.newReturnStmt(
					JavaGr.newStrExpr(JavaPrinter.getRandomBoolCall()));
			units.add(
				JavaGr.newIfStmt(
					JavaGr.newStrExpr(
						"param0 instanceof "
							+ typeName
							+ " || param1 instanceof "
							+ typeName),
					returnChoose));

		}
		units.add(
			JavaGr.newExprStmt(
				JavaGr.newStrExpr("else return param0 == param1")));
		eqMethod.setActiveBody(body);
		
		env.addClass("EnvEquals", eqClass);
		//return eqClass;
	}
}
