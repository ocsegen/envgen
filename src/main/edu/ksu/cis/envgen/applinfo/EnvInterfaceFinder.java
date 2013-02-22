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
import java.io.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.spark.SparkTransformer;
import soot.util.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.analysis.cg.*;
import edu.ksu.cis.envgen.codegen.JavaPrinter;
import edu.ksu.cis.envgen.util.Util;

/**
 * Discovers environment's interface that consists of 
 * environment classes, methods, and fields referenced in 
 * the unit.
 *
 */
public class EnvInterfaceFinder extends InterfaceFinder {
	
	ApplInfo applInfo;
	
	/** Keeps track of unit classes. */
	ModuleInfo unit;

	/** 
	 * Keeps track of environment components, 
	 * including only those that get called inside the unit. 
	 */
	ModuleInfo env;
	
	EnvCallGraph callGraph;
	
	/** Hierarchy for original classes */
	Hierarchy hierarchy;
	
	/** Hierarchy for modeled classes */
	EnvHierarchy envHierarchy;

	protected Properties properties;
	
	//TODO: should be moved to EnvCallGraph
	boolean unitAnalysis = false; 
	
	//TODO: replace this by extensible architecture
	String callGraphType = "cha";

	List ignoreModelingList;

	Logger logger = Logger.getLogger("envgen.applinfo");
	
	public EnvInterfaceFinder(){
		
	}
	
	/*
	public EnvInterfaceFinder(
		UnitInfo unit,
		EnvInfo env) {
		
		this.unit = unit;
		this.env = env;
	}
 */
	/** Set options using a config file */
	public void setOptions(Properties properties){
		
		this.properties = properties;
		
		String unitAnalysisStr = properties.getProperty("unitAnalysis");
		if(unitAnalysisStr != null)
			unitAnalysis = Boolean.valueOf(unitAnalysisStr);
		
		/*
		String packageAnalysisStr = properties.getProperty("packageAnalysis");
		if(packageAnalysisStr != null)
			packageAnalysis = Boolean.valueOf(packageAnalysisStr);
		
		String packageNameStr = properties.getProperty("analysisPackageName");
		if(packageNameStr != null)
			analysisPackageName = packageNameStr;
		*/
		
		String callGraphTypeStr = properties.getProperty("callGraph");
		if(callGraphTypeStr!=null){
			if(callGraphTypeStr.equals("spark"))
				callGraphType = "spark";
			else if(callGraphTypeStr.equals("ofa"))
				callGraphType = "ofa";
			
		}

		String ignoreModelingStr = properties.getProperty("ignoreModeling");
		if(ignoreModelingStr!=null){
			ignoreModelingList = Util.getTokenList(ignoreModelingStr);
		}
			
		//TODO: move to SPARK call graph
		String mainClassName = properties.getProperty("mainClass");
		SootClass mainClass = null;
		if (mainClassName != null) {
			mainClass = Scene.v().getSootClass(mainClassName);
			Scene.v().setMainClass(mainClass);
		}
		//TODO: finish 
		logger.fine("finish implementation");
	}
	
	
	public ApplInfo findInterface(ApplInfo applInfo){

		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.env = applInfo.getEnv();
		
		//try{
		//hierarchy = new Hierarchy();
		//}catch(Exception e){e.printStackTrace();}
		
	    if(callGraphType.equals("spark"))
	    	callGraph = new EnvCallGraphSpark();
	    else if(callGraphType.equals("ofa"))
	    	callGraph = new EnvCallGraphIndus();
	    else
	        callGraph = new EnvCallGraphCHA(); 
	         
	        
		callGraph.setOptions(properties);
		
		callGraph.buildCallGraph(applInfo);
		env.setMethods(callGraph.getEnvMethods());
		
		//try{
		//hierarchy = new Hierarchy();
		//}catch(Exception e){e.printStackTrace();}
		findMissingComponents();
		
		logger.info("unit components: "+unit );
		logger.info("discovered environment components: "+env);
		
		//Set the hierarchy for env classes
		//Pull in abstract methods 
		//TODO: may do make another pass to pull in final fields 
		setHierarchy();
		logger.fine("-----Implementors map: "+envHierarchy.getDirImplementorsMap());
		
		logger.fine("-----SubinterfacesMap map: "+envHierarchy.getDirSubinterfacesMap());
		
		initConstants();
		
		buildImplementors();

		//ApplInfo info = new ApplInfo(unit, env, domainInfo, callGraph, envHierarchy);
		applInfo.setEnvCallGraph(callGraph);
		applInfo.setEnvHierarchy(envHierarchy);
		return applInfo;
	}
	/** 
	 * Processes unit classes and identifies all
	 * external references, recording them in the
	 * <code>envTable</code>.
	 */
	public void findMissingComponents() {

		SootClass internalClass;
		logger.info("----------discovering environment components");
		

		//walk over all internal classes statement by statement and identify
		//all statements with external references
		//put all external classes into envTable

		for (Iterator it = unit.getClasses().iterator(); it.hasNext();) {
			internalClass = (SootClass) it.next();
			String className = internalClass.getName();
			logger.fine("Unit class: " + className);
			
			

			if (unitAnalysis) {
				SootClass markedClass = env.addClass(internalClass);
				List methods = internalClass.getMethods();
				logger.fine("unit methods: "+methods);
				SootMethod internalMethod;
				for (Iterator mi = methods.iterator(); mi.hasNext();) {
					internalMethod = (SootMethod) mi.next();
					env.addMethodToClass(markedClass, internalMethod);
				}

			} else {

				// whatever this class extends or implements needs to be in the environment
				//put the parent of this class in the environment
				envCheckParent(internalClass);

				//put interfaces of this class in the environment
				envCheckInterfaces(internalClass);

				envCheckFields(internalClass);

				envCheckMethods(internalClass);
			}
		}
		//System.out.println("----------discovered environment components: ");
		//EnvPrinter.printTable(envTable);
		//System.out.println("----------discovered environment constants: "+env.constants);
	}

	/**
	 * If a parent of the unit class <code>internalClass</code>
	 * is external to the unit, 
	 * records it in the <code>envTable</code>.
	 */
	protected void envCheckParent(SootClass internalClass) {
		if (internalClass.hasSuperclass()) {
			SootClass parent = internalClass.getSuperclass();
			envCheck(parent);
		}
	}

	/**
	 * If any of the interfaces of the unit class <code>internalClass</code>
	 * are external,
	 * records them in the <code>envTable</code>.
	 *
	 */
	protected void envCheckInterfaces(SootClass internalClass) {
		if (internalClass.getInterfaceCount() > 0) {
			Chain interfaces = internalClass.getInterfaces();
			SootClass interfc;
			for (Iterator ii = interfaces.iterator(); ii.hasNext();) {
				interfc = (SootClass) ii.next();
				envCheck(interfc);
			}
		}
	}

	protected void envCheckFields(SootClass internalClass) {
		Chain fields = internalClass.getFields();
		SootField internalField;
		for (Iterator fi = fields.iterator(); fi.hasNext();) {
			internalField = (SootField) fi.next();
			envCheckField(internalField);
		}
	}

	protected void envCheckMethods(SootClass internalClass) {
		SootMethod internalMethod;
		List methods = internalClass.getMethods();
		//System.out.println("chainList: "+methodsList);

		//check the concurrent modification exception here ??
		//Iterator mi = methodsList.snapshotIterator();
		//System.out.println("methodsList: "+mi);
		for (Iterator mi = methods.iterator(); mi.hasNext();) {
			internalMethod = (SootMethod) mi.next();

			envCheckMethod(internalMethod);
		}
	}

	protected void envCheckMethod(SootMethod internalMethod) {

		
		logger.fine("\nEnvInterface, envCheckMethod: checking Soot Method: "
					+ internalMethod);

		// check the return type class and if needed add to the environment
		envCheckReturnType(internalMethod);

		// check the parameter types classes and if needed add to the environment
		envCheckParams(internalMethod);

		//check the exptions thrown by the method and add into the env if needed
		envCheckExceptions(internalMethod);

		//check the implementation of the method

		if (internalMethod.isPhantom()
			|| internalMethod.isNative()
			|| internalMethod.isAbstract()) {
			//EnvPrinter.error("phantom method: "+internalMethod);
			return;

		}

		Body jb = null;
		try{
		jb = internalMethod.retrieveActiveBody();
		}catch (Exception e){
			e.printStackTrace(); 
			logger.severe("can't analyze method: "+internalMethod);
			return;
		}
		//if (logger.isLoggable(Level.FINE)) 
			Util.printMethodBody(internalMethod, jb);
		
		envCheckLocals(internalMethod, jb);

		envCheckStmts(internalMethod, jb);

	}

	/**
	 * If any of the locals of the  <code>internalMethod</code>
	 * are external,
	 * records them in the <code>envTable<code>.
	 */
	protected void envCheckLocals(SootMethod sm, Body jb) {
		//Body jb = internalMethod.retrieveActiveBody();
		//Look at the declarations
		
		logger.fine("checking locals of: " + sm);
		
		Chain locals = jb.getLocals();
		Local l = null;
		Type localType;
		SootClass localClass;

		for (Iterator li = locals.iterator(); li.hasNext();) {
			l = (Local) li.next();
			localType = l.getType();
			localClass = Util.getClass(localType);
			if (localClass != null)
				envCheck(localClass);

		}
	}

	/**
	 * Records any of the external references found in the statement
	 * list of the <code>internalMethod</code>
	 * in the <code>envTable<code>.
	 */
	protected void envCheckStmts(SootMethod internalMethod, Body jb) {

		//Body jb = internalMethod.retrieveActiveBody();
		
		logger.fine("checking statements of: " + internalMethod);
		
		PatchingChain stmtList = jb.getUnits();
		for (Iterator si = stmtList.iterator(); si.hasNext();) {
			Stmt s = (Stmt) si.next();
			logger.finer("statement: "+s+" is "+s.getClass().getName());
				
			//identify all possible statements that might have 
			//external references

			if (s.containsInvokeExpr()) {

				InvokeExpr expr = (InvokeExpr) s.getInvokeExpr();
				
				SootMethod invokeMethod;
				
				invokeMethod = expr.getMethod();
				
				envCheckMethodSignature(invokeMethod);

				envCheckInvokeExpr(s, expr, internalMethod);

			} else if (s.containsFieldRef()) {
				FieldRef fieldRef = (FieldRef) s.getFieldRef();
				SootField refField = ((FieldRef) fieldRef).getField();
				envCheckFieldRef(refField);
			}
		}

	}

	/**
	 * Resolves invoke expression <code>expr</code> and
	 * calls <code>envCheckMethod</code> for all
	 * possible methods at this site.
	 */
	protected void envCheckInvokeExpr(
		Stmt site,
		InvokeExpr expr,
		SootMethod internalMethod) {
		logger.fine("checking invoke expr: " + expr);

		SootMethod invokeMethod = expr.getMethod();
		//String methodName = invokeMethod.getName();

		SootClass declClass = invokeMethod.getDeclaringClass();
		envCheckMethodSignature(invokeMethod);

		logger.fine("decl class: " + declClass);

		SootMethod sm;

		if (expr instanceof InstanceInvokeExpr) {
			Type receiverType = ((InstanceInvokeExpr) expr).getBase().getType();

			if (receiverType instanceof RefType) {
				// since Type might be Null
				Collection targets = null;
				targets =
					callGraph.getTargetsOf(
						site,
						expr,
						receiverType,
						internalMethod);

				logger.fine("Invoke expr <" + expr + "> has targets:");

				for (Iterator it = targets.iterator(); it.hasNext();) {
					sm = (SootMethod) it.next();
					logger.fine("target: " + sm);
					envCheckMethodSignature(sm);
				}
			}
		} else if (expr instanceof StaticInvokeExpr) {
			sm = expr.getMethod();
			envCheckMethodSignature(sm);
		}

		/*
		else if (expr instanceof SpecialInvokeExpr)
		{
		    sm = hierarchy.resolveSpecialDispatch((SpecialInvokeExpr)expr, internalMethod);
		    //sc = sm.getDeclaringClass();
		    envCheckMethodSignature(sm);
		}
		*/
	}

	/**
	 * If any of the components of the 
	 * <code>externalMethod</code> signature are in the 
	 * environment, records them
	 * in the <code>/envTable</code>.
	 *
	 */
	protected void envCheckMethodSignature(SootMethod externalMethod) {

		SootClass methodDeclClass = externalMethod.getDeclaringClass();
		//String methodName = externalMethod.getName();
		logger.fine(
				"checking signature of method: " + externalMethod+
		" with declaring class: " + methodDeclClass);
		

		if (envCheck(methodDeclClass)) {

			// check the return type class and if needed add to the environment
			envCheckReturnType(externalMethod);

			// check the parameter types classes and if needed add to the environment
			envCheckParams(externalMethod);

			//check the exceptions thrown by the method and add into the env if needed
			envCheckExceptions(externalMethod);

			//give the same method to the marked Class
			//if it doesn't contain it yet

			//env.addMethodToClass(methodDeclClass, externalMethod);
			
			String markedClassName = methodDeclClass.getName();
			SootClass markedClass = (SootClass) env.getClass(markedClassName);
			env.addMethodToClass(markedClass, externalMethod);
		}
	}

	/**
	 * If the return type
	 * of <code>externalMethod</code> is in the
	 * environment, records it in the <code>envTable</code>.
	 */
	protected void envCheckReturnType(SootMethod externalMethod) {
		Type returnType = externalMethod.getReturnType();
		SootClass returnTypeClass = Util.getClass(returnType);
		if (returnTypeClass != null)
			envCheck(returnTypeClass);
	}

	/**
	 * If any of the parameter
	 * declared types of <code>external</code>
	 * method are in the environment, records
	 * them in the <codeenvTable<code>.
	 */
	protected void envCheckParams(SootMethod externalMethod) {
		List paramTypes = externalMethod.getParameterTypes();
		Type paramType = null;
		SootClass paramTypeClass;
		for (Iterator pi = paramTypes.iterator(); pi.hasNext();) {
			paramType = (Type) pi.next();
			paramTypeClass = Util.getClass(paramType);
			if (paramTypeClass != null)
				envCheck(paramTypeClass);
		}
	}

	/**
	 * If any of the exceptions
	 * of the <code>externalMethod</code>
	 * are in the environment,
	 * records them in the <code>envTable</code>.
	 */
	protected void envCheckExceptions(SootMethod externalMethod) {
		List exceptions = externalMethod.getExceptions();
		SootClass except = null;
		for (Iterator ei = exceptions.iterator(); ei.hasNext();) {
			except = (SootClass) ei.next();
			envCheck(except);
		}
	}

	/**
	 * If field reference <code>fieldRef</code>
	 * is a part of the environment,
	 * records it in the <code>envTable</code>.
	 */
	protected void envCheckFieldRef(SootField refField) {
		SootClass refClass = refField.getDeclaringClass();
		logger.fine(
				"External reference to field: "
					+ refField.getName()+" Declaring class: " + refClass);
		

		if (envCheck(refClass)) {
			//need to add this field to the class
			
			SootClass markedClass =
				env.getClass(refClass.getName());
			

				//check the type of this field and make sure environment
				//has it

				
				Type fieldType = refField.getType();
				SootClass typeClass = Util.getClass(fieldType);
				envCheck(typeClass);
				
				env.addFieldToClass(markedClass, refField);
		}
		
	}

	/**
	 * If field  <code>field</code>
	 * is a part of the environment,
	 * records its type class in the <code>envTable</code>.
	 */
	protected void envCheckField(SootField field) {

		Type fieldType = field.getType();
		SootClass typeClass = Util.getClass(fieldType);
		if (typeClass != null)
			envCheck(typeClass);

	}

	/**
	 * If <code>sc</code> is in the environment
	 * records it in the <code>envTable</code> if it's not there, and returns
	 * true.
	 */
	public boolean envCheck(SootClass sc) {
		//logger.info("envCheck " + sc);
		if (sc == null)
			return false;

		String typeName = sc.getPackageName();
		String className = sc.getName();

		
		//ignore generation of stubs for java.lang package
		if (ignoreModeling(typeName))
			return false;

		//filter out inner and anonymous classes 		
		if(className.indexOf('$')!= -1){
			//logger.info("inner class: "+className);
			//TODO: if the inner class is part of the unit class
			//add it to the unit (separate table?) and traverse it
			return false;
		}

		if (unit.containsClass(className))
			return false;

			
		env.addClass(sc);
		
		return true;
	}



	/**
	 * Sets the hierarchy among the environment classes.  
	 * This allows to leave out some classes from the environment.
	 */
	protected void setHierarchy() {
		
		envHierarchy = new EnvHierarchy(unit, env);
		
		logger.fine(
				"-------------Setting hierarchy for environment (marked) classes");
		SootClass externalClass, markedClass, parent, interfc;
		String realName, modelName;
		for (Iterator it = env.getClasses().iterator(); it.hasNext();) {
			markedClass = (SootClass) it.next();
			
			realName = JavaPrinter.getActualName(markedClass.getName());

			//System.out.println("set hierarchy for: "+realName);
			externalClass = Scene.v().getSootClass(realName);

			if(externalClass.isInterface()){
				envHierarchy.addInterface(realName);
			}
			
			//set the parent
			if (externalClass.hasSuperclass()) {
				parent = getParent(externalClass);
				markedClass.setSuperclass(parent);

			}

			//set interfaces
			if (externalClass.getInterfaceCount() > 0) {
				//need to identify those intefaces that get used 
				//in the system and set interfaces
				//of the marked class to those 
				
				//List interfaces = envHierarchy.getSuperInterfaceClasses(externalClass);

				Chain interfaces = externalClass.getInterfaces();
				logger.fine(
						"External class corresponding to: "
							+ markedClass.getName()
							+ " has interfaces: "
							+ interfaces);
				Iterator ii = interfaces.iterator();
				while (ii.hasNext()) {
					interfc = (SootClass) ii.next();

					//add interface only if it's already in the environment
					//if not, don't bother
					
					String interfcName = interfc.getName();
					
					if (!ignoreReference(interfcName)){
						
						markedClass.addInterface(interfc);
						
						//markedClass can be an interface 
						//a subinterface is treated as an implementation
						
						if(markedClass.isInterface()){
							envHierarchy.addDirSubinterface(interfcName, markedClass);
						}
						else{
							envHierarchy.addDirImplementor(interfcName, markedClass);
						}
		
						addAbstractMethods(markedClass, interfcName);					
					}
				}
			}
		}
		setHierarchyUnitEnv();
	}

	/**
	 * Sets the hierarchy among the environment classes.  
	 * This allows to leave out some classes from the environment.
	 */
	protected void setHierarchyUnitEnv() {
		
		SootClass markedClass, interfc;
		String realName;
		
		//add implementations that are in the unit
		for (Iterator it = unit.getClasses().iterator(); it.hasNext();) {
			markedClass = (SootClass) it.next();
			
			realName = markedClass.getName();

			if(markedClass.isInterface()){
				envHierarchy.addInterface(realName);
			}

			//set interfaces
			if (markedClass.getInterfaceCount() > 0) {
				//need to identify those intefaces that get used 
				//in the system and set interfaces
				//of the marked class to those 
				//Chain interfaces = getInterfaces(externalClass);
				//List interfaces = envHierarchy.getSuperInterfaceClasses(markedClass);

				Chain interfaces = markedClass.getInterfaces();
				logger.fine(
						"External class corresponding to: "
							+ markedClass.getName()
							+ " has interfaces: "
							+ interfaces);
				Iterator ii = interfaces.iterator();
				while (ii.hasNext()) {
					interfc = (SootClass) ii.next();

					//add interface only if it's already in the environment
					//if not, don't bother
					
					String interfcName = interfc.getName();
					
					if (!ignoreReference(interfcName)){
						
						if(markedClass.isInterface()){
							envHierarchy.addDirSubinterface(interfcName, markedClass);
						}
						else{
							envHierarchy.addDirImplementor(interfcName, markedClass);
						}		
					}
				}
			}
		}
	}

	protected void initConstants(){
		Set constants = env.getConstants();
		SootField sf;
		for (Iterator fi = constants.iterator(); fi.hasNext();){
			sf = (SootField)fi.next();
			
			SootClass externalClass = sf.getDeclaringClass();
			
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
						if(refField.getSignature().equals(sf.getSignature())){
							//grab the rhs, this is the value
							Value rhs = ((AssignStmt)s).getRightOp();
							if(rhs instanceof Constant){
								env.addConstantValue(sf, rhs);
								logger.info("found const values for:"+sf);
							}
							else
								logger.warning("Didn't find const val/source for: "+sf);			
						}
					}
				}
			}
		}
	}
	
	/**
	 * Sets the hierarchy among the environment classes.  
	 * This allows to leave out some classes from the environment.
	 */
	protected void buildImplementors() {
		logger.fine(
				"-------------Building implementations for env interfaces");
		Collection interfaces = envHierarchy.getInterfaces();
		
		
		for (Iterator it = interfaces.iterator(); it.hasNext();) {
			String interfcName = (String)it.next();
			String modelName = interfcName;
			if(env.containsClass(interfcName))
				modelName = JavaPrinter.getPrefixedName(interfcName);
			List implementors = envHierarchy.getDirImplementorsOf(interfcName);
			List subinterfaces = envHierarchy.getDirSubinterfacesOf(interfcName);
			
			
			//TODO: an interface that has subinterfaces should not
			//get a separate implementation
			
			if(implementors.isEmpty() && subinterfaces.isEmpty()){
				//create an implementation
				
				SootClass interfcClass = Scene.v().getSootClass(modelName);
				SootClass implClass = buildImplementation(interfcClass);
				env.addClass(implClass.getName(), implClass);
				implementors.add(implClass);
			}
		}
	}

	
	
	
	/**
	 * Uses Soot Hiearchy.
	 */
	protected void buildImplementors2() {
		logger.fine(
				"-------------Building implementations for env interfaces");
		//Map implementorsMap = env.getImplementorsMap();
		
		SootClass markedClass, externalClass;
		String modelName, realName;
		
		//a trick to avoid concurrent modification exception
		List classNames = new ArrayList(env.getClassNames());
		
		for (Iterator it = classNames.iterator(); it.hasNext();) {
			
			modelName = (String)it.next();
			
			markedClass = env.getClass(modelName);
			
			
			if(markedClass.isInterface()){
				
				realName = JavaPrinter.getActualName(markedClass.getName());

				//System.out.println("set hierarchy for: "+realName);
				externalClass = Scene.v().getSootClass(realName);
				
				
				List subclasses = hierarchy.getDirectImplementersOf(externalClass);
				List subInterfaces = hierarchy.getDirectSubinterfacesOf(externalClass);
				
				if(subclasses.isEmpty() && subInterfaces.isEmpty()){
				//create an implementation
				
					
					SootClass implClass = buildImplementation(markedClass);
					env.addClass(implClass.getName(), implClass);
					//implementors.add(implClass);
				}
			}
		}
	}


	/**
	 * Gives <code>markedClass</code> all of the final
	 * fields of the <code>externalClass</code>
	 * to be able to resolve final field references
	 * that are present in the source code
	 * but might be absent in the class files.
	 */
	protected void addFinalFields(
		SootClass externalClass,
		SootClass markedClass) {
		//pulling final variables in

		if (externalClass.getFieldCount() > 0) {
			Chain fields = externalClass.getFields();
			Iterator fi = fields.iterator();
			while (fi.hasNext()) {
				SootField finalField = (SootField) fi.next();
				if (Modifier.isFinal(finalField.getModifiers())) {
					SootField markedField =
						new SootField(
							finalField.getName(),
							finalField.getType(),
							finalField.getModifiers());
					//make sure the type of the field is in the env (?)

					markedClass.addField(markedField);
				}
			}
		}
	}

	
	protected void addAbstractMethods(
			SootClass markedClass,
			String interfcName) {

			//get the env representative of the interfcClass
			SootClass interfc;
			if(env.containsClass(interfcName)){
				interfc = env.getClass(interfcName);
			}
			else if(unit.containsClass(interfcName)){
				interfc = unit.getClass(interfcName);
			}
			else{
				logger.severe("No interface for: "+interfcName);
				return;
			}
			
			SootMethod sm;
			for( Iterator it = interfc.getMethods().iterator(); it.hasNext();) {
				sm = (SootMethod)it.next();
				//check if this method is implemented in markedClass
				if(!markedClass.declaresMethod(sm.getName(),  sm.getParameterTypes(), sm.getReturnType())){
					//add this method to the class
					env.addMethodToClass(markedClass, sm);
				}
				
			}

		}
	
	
	/**
	 * If <code>externalClass</code> extends an abstract
	 * class, <code>markedClass</code> might need to have
	 * all of the methods that implement abstract methods
	 * of the parent.  This method should be used after the
	 * hierarchy has been established among the environment
	 * classes.
	 *
	 */
	protected void addAbstractMethods(
		SootClass externalClass,
		SootClass markedClass) {

		if (externalClass.hasSuperclass()) {
			SootClass superClass = externalClass.getSuperclass();
			markedClass.setSuperclass(superClass);

			//this is for the case when Superclass is abstract
			//need to pull in more methods (all at this point but
			//might refine in the future)

			if (Modifier.isAbstract(superClass.getModifiers())) {
				List externalMethods = externalClass.getMethods();
				Iterator ei = externalMethods.iterator();
				while (ei.hasNext()) {
					SootMethod extMethod = (SootMethod) ei.next();
					//env.addMethodToClass(markedClass, extMethod);
				}
			}
		}

	}
	
	/**
	 *
	 * Gets a parent class from the system.
	 */
	protected SootClass getParent(SootClass sc) {
		if (sc.isPhantom())
			logger.severe("class " + sc + " is a phantom class");

		//String className = sc.getName();

		//these are needed, if java.lang is not being generated

		//if(omitModeling(className))
		//	return sc;
		if (!sc.hasSuperclass())
			return sc;
		
		
		logger.fine("Looking for parent of " + sc.getName());

		SootClass parentClass = sc.getSuperclass();
		String className = parentClass.getName();
		if(ignoreReference(className)){
			return getParent(parentClass);
		}
		return parentClass;
		
		//if (unit.contains(parentClass.getName())
		//	|| env.contains(parentClass.getName()))
		//	return parentClass;
		//else
		//	return getParent(parentClass);
		
	}
	
	
	protected boolean ignoreModeling(String type){
		if(ignoreModelingList == null)
			return false;
		if(ignoreModelingList.contains(type))
			return true;
		return false;	
	}
	
	protected boolean ignoreReference(String type){
		
		if (env.containsClass(type)
				|| unit.containsClass(type)){
			return false;
		}
		//TODO refine, take into account the omitted table
		
		if (type.equals("java.lang.RuntimeException"))
			return false;
		
		if (type.equals("java.lang.Exception"))
			return false;

		if (type.equals("java.lang.Throwable"))
			return false;

		if (type.equals("java.lang.Thread"))
			return false;
		
		if (type.equals("java.lang.Error"))
			return false;
		
		return true;
		
	}
	
	
	public static SootClass buildImplementation(SootClass interfc){
		//special care needs to be taken
		//create a subtype of this class
		
		String concreteName = JavaPrinter.getActualName(interfc.getName())+"Impl";

		SootClass concreteClass =
			new SootClass(concreteName, Modifier.PUBLIC);
		if (interfc.isInterface())
			concreteClass.addInterface(interfc);
		else
			concreteClass.setSuperclass(interfc);
		
		implementMethods(concreteClass, interfc);
		return concreteClass;

	}
	
	
	public static void implementMethods(SootClass concreteClass, SootClass interfc){
		
		SootMethod internalMethod;
		//implement the abstract methods of the internalClass
		List internalMethods = interfc.getMethods();
		Iterator ii = internalMethods.iterator();
		while (ii.hasNext()) {
			internalMethod = (SootMethod) ii.next();
			Type returnType = internalMethod.getReturnType();
			//implement abstract methods and grab constructors
			if (Modifier.isAbstract(internalMethod.getModifiers())
				|| internalMethod.getName().compareTo("<init>") == 0) {
				//add this method (without the abstract modifier to the concrete class
				SootMethod concreteMethod =
					new SootMethod(
						internalMethod.getName(),
						internalMethod.getParameterTypes(),
						returnType,
						internalMethod.getModifiers()
							& (~Modifier.ABSTRACT));

				concreteClass.addMethod(concreteMethod);
				
				/*
				//create a stub body for concreteMethod
				Body body = new JavaBody();
				body.setMethod(concreteMethod);
				Chain units = body.getUnits();
				if (!(returnType instanceof VoidType))
					units.add(
						JavaGr.newReturnStmt(
							JavaGr.newStrExpr(
								JavaPrinter.getTopValName(returnType))));

				concreteMethod.setActiveBody(body);
				//String methodName = concreteMethod.getName();
				*/
				
			}
		}

	}
}
