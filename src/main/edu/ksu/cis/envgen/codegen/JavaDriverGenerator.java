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
import soot.util.*;

import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;
import edu.ksu.cis.envgen.codegen.ast.stmt.*;
import edu.ksu.cis.envgen.spec.*;

/** Generates java drivers for unit under analysis. */
public abstract class JavaDriverGenerator extends AbstractDriverGenerator {

	/** Set to insert atomic steps into the generated environment. */
	public boolean atomicSteps; 
	
	public boolean printActions;
	
	public JavaDriverGenerator(){
		
	}
	
	public void setOptions(Properties properties){
		String atomicStr = properties.getProperty("atomicStepsMode");
		atomicSteps = Boolean.valueOf(atomicStr);
		
		String printActionsStr = properties.getProperty("printActions");
		printActions = Boolean.valueOf(printActionsStr);
		
		logger.fine("printActionsStr: "+printActionsStr);
		
		//TODO: add other fields
	}
	
	/**
	 * Constructs <code>EnvDriver</code> soot class, the main driver that
	 * starts up threads.
	 */
	public void genMainThread() {
		SootClass driver = new SootClass("EnvDriver", Modifier.PUBLIC);
		//TODO: take care of the possible exception in soot?
		driver.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		env.addClass("EnvDriver", driver);
		SootMethod mainMethod = new SootMethod("main", Arrays
				.asList(new Type[] { ArrayType.v(RefType.v("java.lang.String"),
						1) }), VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		driver.addMethod(mainMethod);
		Body mainBody = genMainRunBody();
		mainBody.setMethod(mainMethod);
		mainMethod.setActiveBody(mainBody);
		//return driver;
	}

	public Body genMainRunBody() {
		//TODO: assert(userSpec!=null)
		//List threads = userSpec.getThreads();

		Body mainBody = new JavaBody();

		PatchingChain mainUnits = mainBody.getUnits();
		//Chain mainLocals = mainBody.getLocals();
		List threadParms = new ArrayList();
		JavaExpr arg;

		int count = 0;
		Proposition prop;
		JavaExpr expr;
		
		if(mainThread != null){
			genMainFromSpec(mainUnits);
			//TODO: fill threadParms
			/*
			List mainProps = mainThread.getPropositions();
			
			for(Iterator ii = mainProps.iterator(); ii.hasNext(); ){
				prop = (Proposition)ii.next();
				expr = prop.getExpression();
				
				if(expr instanceof TypeDeclExpr){
					String varName = ((TypeDeclExpr)expr).getName();
					arg = new StrExpr(varName);
					threadParms.add(arg);
				}
			}
			*/
		 
		}
		
			for (Iterator ii = instantiations.iterator(); ii.hasNext(); count++) {
			//in defaultInstantiations concrete classes might appear
				prop = (Proposition)ii.next();
				expr = prop.getExpression();
			
				if(mainThread == null)
					mainUnits.add(JavaGr.newExprStmt(expr));

				if(expr instanceof TypeDeclExpr){
					String varName = ((TypeDeclExpr)expr).getName();
					arg = new StrExpr(varName);
					threadParms.add(arg);
				}
			}

		//SootClass threadClass;
		String startMethod = "start";

		ThreadSpec threadSpec;
		int numThreads;
		String threadName;
		Type threadType;
		//String threadTypeStr = threadType.toString();

		for (Iterator ti = threads.iterator(); ti.hasNext();) {
			threadSpec = (ThreadSpec) ti.next();
			numThreads = threadSpec.getNum();
			threadName = threadSpec.getName();
			//threadType = threadSpec.getType();
			
			mainUnits.add(JavaGr.newExprStmt(new TypeDeclExpr(threadName, threadName+0, new NewExpr(threadName, threadParms))));
			
			mainUnits.add(JavaGr.newExprStmt(JavaGr.newMethodCallExpr(threadName+0,
					startMethod, new ArrayList())));

			if (numThreads > 0) {
				for (int i = 1; i < numThreads; i++) {
					
					mainUnits.add(JavaGr.newExprStmt(new TypeDeclExpr(threadName, threadName+i, new NewExpr(threadName, threadParms))));
					
					mainUnits.add(JavaGr.newExprStmt(JavaGr.newMethodCallExpr(
							 threadName + i, startMethod, new ArrayList())));
				}
			}

		}
		return mainBody;
	}

	public abstract void genMainFromSpec(Chain units);

	/**
	 * Creates a soot class <code>threadName</code> with a constructor as a
	 * base for synthesized thread construction methods.
	 */
	public SootClass genThreadBasics(String threadName) {
		//TODO: assert(threadName!=null) && threadName!=""
		SootClass driverThread = new SootClass(threadName, Modifier.PUBLIC);
		
		//TODO: check for possible exception by soot?
		try{
		SootClass javaLangThread = Scene.v().getSootClass("java.lang.Thread");
		driverThread.setSuperclass(javaLangThread);
		}catch(Exception e){}
		
		env.addClass(threadName, driverThread);
		
		//put stub for java.lang.Thread into the environment
		//might want to omit that for simplicity
		//if(!envTable.containsKey(javaLangThread.getName()))
		//envTable.put(javaLangThread.getName(), javaLangThread);

		List paramTypes = new ArrayList();

		SootMethod constructor = new SootMethod("<init>", paramTypes, VoidType
				.v(), Modifier.PUBLIC);

		Body constrBody = genThreadConstructorBody(driverThread, constructor);

		driverThread.addMethod(constructor);
		constrBody.setMethod(constructor);
		constructor.setActiveBody(constrBody);
		return driverThread;
	}

	public Body genThreadConstructorBody(SootClass driverThread,
			SootMethod method) {

		Body constrBody = new JavaBody();
		Chain constrUnits = constrBody.getUnits();
		
		if(instantiations == null || instantiations.isEmpty())
			instantiations = genDefaultInstantiations();

		int count = 0;
		Proposition prop;
		JavaExpr expr;
		for (Iterator ii = instantiations.iterator(); ii.hasNext();) {
			
			prop = (Proposition)ii.next();
			expr = prop.getExpression();

			if(expr instanceof TypeDeclExpr){
				TypeDeclExpr declExpr = (TypeDeclExpr)expr;
				Type declType = declExpr.getType();
				assert declType != null;

				String fieldName = ((TypeDeclExpr)expr).getName();
				
				SootField instField = new SootField(fieldName, declType,
					Modifier.PUBLIC);
				driverThread.addField(instField);
				constrUnits.add(JavaGr.newExprStmt(JavaGr.newAssignExpr(new StrExpr(fieldName), "=",
					new StrExpr("param" + count))));
				count++;
				//get Type of this class and put it into the paramTypes List
				List paramTypes = method.getParameterTypes();
				paramTypes.add(declType);
			}
			else{ 
				//ignore
			}
		}

		return constrBody;
	}

	public JavaStmt genPropositionStmt(Proposition prop) {
		JavaExpr expr = prop.getExpression();
		assert expr != null;
		if (expr instanceof InvokeExpr) {
			return genMethodStmt((InvokeExpr) expr);
		} else if (expr instanceof AssignExpr) {
			return genAssignmentStmt(expr);
		}
		else if (expr instanceof TypeDeclExpr) {
			return genAssignmentStmt(expr);
		}
		else if (expr instanceof StrExpr){
			return genDefProposition((StrExpr) expr);
		}
		logger.severe("unhandled proposition:"+expr.getClass());
		return null;
	}

	/**
	 * Constructs a Java statement for a method call, including atomic steps
	 * around the call, print out of the action label, and try-catch clause if
	 * the method invoked throws exceptions.
	 */
	public JavaStmt genMethodStmt(InvokeExpr expr) {

		JavaStmt methodCall;
		String label = expr.toString(); // expr.getLabel();

		methodCall = JavaGr.newExprStmt(expr);
		
		methodCall = genAtomicPropStmt(methodCall);
		
		methodCall = genPrintAction(methodCall, label);

		if (expr.throwsExceptions())
			methodCall = genTryCatchStmt(methodCall);

		return methodCall;
	}

	/**
	 * Constructs a Java statement for a method call, including atomic steps
	 * around the call, print out of the action label, and try-catch clause if
	 * the method invoked throws exceptions.
	 */
	public JavaStmt genAssignmentStmt(JavaExpr expr) {

		//check if this is a legal method call
		JavaStmt assignment;
		String label;

		//String methodCode = a.getCode();

		assignment = JavaGr.newExprStmt(expr);

		if (atomicSteps)
			assignment = genAtomicPropStmt(assignment);

		//identify mapping
		label = "";//a.getLabel();

		//if (label == null) {
			//since methodCode itself might contain "" we need
			//to replace these with something that doesn't conflict
		//	if (methodCode.indexOf('"') != -1) {
		//		label = methodCode.replace('"', '`');
		//	} else {
		//		label = methodCode;
		//	}
		//}
		assignment = JavaGr.newSequenceStmt(JavaGr.newExprStmt(JavaGr
				.newStrExpr("System.out.println(\"" + "@EnvDriverThread: "
						+ label + "\")")), assignment);
		
		if (expr.throwsExceptions())
			assignment = genTryCatchStmt(assignment);

		return assignment;
	}

	/**
	 * Generates a stmt from a string that should have a definition.
	 * @param expr
	 * @return
	 */
	public JavaStmt genDefProposition(StrExpr expr){
		logger.severe("finish impl");
		return null;
	}
	
	/**
	 * Creates a method call statement surrounded by endAtomic/beginAtomic
	 * method invocations.
	 */
	public JavaStmt genAtomicPropStmt(JavaStmt stmt) {
		
		if(atomicSteps)
			return JavaGr.newSequenceStmt(JavaGr.newExprStmt(JavaGr
				.newStrExpr(JavaPrinter.getEndAtomicCall())), JavaGr
				.newSequenceStmt(stmt, JavaGr.newExprStmt(JavaGr
						.newStrExpr(JavaPrinter.getBeginAtomicCall()))));
		return stmt;
	}

	/**
	 * 
	 * Incloses <code>methodCall</code> by a try-catch clause.
	 *  
	 */
	//TODO: refine try-catch to catch method-specific exceptions
	public JavaStmt genTryCatchStmt(JavaStmt methodCall) {
	
		
		return JavaGr.newSequenceStmt(JavaGr.newTryStmt(methodCall), JavaGr
				.newCatchStmt(JavaGr.newVDeclStmt(JavaGr
						.newStrExpr("Exception"), JavaGr.newExprStmt(JavaGr
						.newStrExpr("e"))), JavaGr.newExprStmt(JavaGr
						.newStrExpr("e.printStackTrace()"))));
	}
	
	public JavaStmt genPrintAction(JavaStmt stmt, String label){
		if(printActions)
			return JavaGr.newSequenceStmt(JavaGr.newExprStmt(JavaGr
			.newStrExpr("System.out.println(\"" + "@EnvDriverThread: "
					+ label + "\")")), stmt);
		return stmt;
	}
	

	/**
	 * 
	 * Gathers classes of the unit that need to be instantiated in the driver.
	 * Extends abstract classes with concrete classes in order to exercise
	 * nonabstract public methods of abstract classes of the unit.
	 *
	 */
	public List genDefaultInstantiations() {
		SootClass internalClass;
		Proposition prop;
		TypeDeclExpr objAlloc;
		String internalClassName;

		List defaultInstantiations = new ArrayList();
		
		for (Iterator it = unit.getClasses().iterator(); it.hasNext();) {
			internalClass = (SootClass) it.next();
			internalClassName = internalClass.getName();

			objAlloc = new TypeDeclExpr(
						internalClassName,
						JavaPrinter
							.getShortName(internalClassName)
							.toLowerCase()
							+ "0",
						new NewExpr(internalClass.getName(), new ArrayList()));
			//objAlloc.setDeclClass(internalClass);
			objAlloc.setType(internalClass.getType());
			
			prop = new Proposition(objAlloc);
			defaultInstantiations.add(prop);

		}
		return defaultInstantiations;
	}

	/** List that holds Proposition objects defining public nonabstract methods that
	 * are used for generation of universal drivers. 
	 */
	public List genDefaultPropositions() {
		
		SootClass internalClass;
		SootMethod internalMethod;
		Proposition defaultAction;
		List defaultPropositions = new ArrayList();
		List unitMethods = unit.getMethods();
		

		for (Iterator it = unitMethods.iterator(); it.hasNext();) {
			internalMethod = (SootMethod) it.next();
			//need to add hierarchy check
			//some methods can be inherited and invoked
			//by the environment
			//can be achieved by adding parent classes into unit
			//and specifying receiver objects for child classes
			//should be taken care by soot framework (?)
			//String className = internalClass.getName();
		
			internalClass = internalMethod.getDeclaringClass();
					defaultAction =
						genDefaultInvoke(internalClass, internalMethod);
					defaultPropositions.add(defaultAction);
				
			//}
		}
		
		//TODO: iterate over fields and create their updates
		return defaultPropositions;
	}
	
	/**
	 * Creates a default proposition for invocation of <code> internalMethod</code>,
	 * used for creation of universal drivers and for creation
	 * of propositions universe for an LTL specification. 
	 */
	public Proposition genDefaultInvoke(
		SootClass internalClass,
		SootMethod internalMethod) {

		String methodName = internalMethod.getName();
		List types = internalMethod.getParameterTypes();
		List args = CodeGenUtil.genDefaultArgs(types);
		
		//logger.info("default args: "+args+" for type: "+types);

		//figure out the receiver object for the default invocation
		String receiver;
		String declClassName = internalClass.getName();

		String varName = JavaPrinter.getShortName(declClassName);

		//if (Modifier.isAbstract(internalClass.getModifiers())) {

		//	if (!internalMethod.isStatic())
		//		receiver = "concrete" + varName.toLowerCase() + "0";
		//	else
		//		receiver = "Concrete" + varName;
		//} else {
			if (!internalMethod.isStatic())
				receiver = varName.toLowerCase() + "0";
			else
				receiver = varName;
		//}
		
		InvokeExpr invoke = new InvokeExpr(receiver, methodName, args);
		invoke.setMethod(internalMethod);
		Proposition result = new Proposition(invoke);

		return result;
	}
	
	public Proposition genDefaultAssignment(
			SootClass internalClass, SootField sf){
		logger.warning("finish implementation of c.f = TOP");
		return null;
	}
}