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
import soot.util.Chain;

import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.ast.stmt.*;
import edu.ksu.cis.envgen.spec.*;

/** Generates universal drivers. */
public class UniversalDriverGenerator extends JavaDriverGenerator{
	
	int numThreads = 2; //default value

	boolean ifElseCodeCond = false;

	public UniversalDriverGenerator(){
		
	}

	public UniversalDriverGenerator( ModuleInfo unit, ModuleInfo env ) {
		this.unit = unit;
		this.env = env;
	}
	
	public void setOptions(Properties properties){
		
		String numThreadsStr  = properties.getProperty("numThreads");
		if(numThreadsStr != null){
		try{
			
			numThreads = Integer.parseInt(numThreadsStr);
			
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		}
		
		//TODO: check/finish
	}
	
	public void genThreads() {
		SootClass universalThread = null;
		if (ifElseCodeCond) //generate code using if-else construct
			universalThread = genIfElseThread();
		else
			universalThread = genThread();

		//userSpec.addThread(universalThread, numThreads);

		threads.add(new ThreadSpec(universalThread, numThreads));
		
		//threadSpec.setImplClass(universalThread);
		//threadSpec.setNum(numThreads);	
		//for (int i = 0; i < numThreads; ++i)
		//	driverThreads.add(universalThread);
	}

	/**
	 * Constructs soot class that represents a universal thread using 
	 * if-else statement in its run method.
	 */
	protected SootClass genIfElseThread() {
		logger.info(
			"Constructing a universal driver thread using if-else stmt");
		SootClass driverThread = genThreadBasics("EnvDriverThread");

		SootMethod runMethod =
			new SootMethod(
				"run",
				new java.util.ArrayList(),
				VoidType.v(),
				Modifier.PUBLIC);

		Body runBody = genIfElseUniversalRunBody();
		runBody.setMethod(runMethod);

		runMethod.setActiveBody(runBody);

		driverThread.addMethod(runMethod);

		return driverThread;
	}

	/**
	 *
	 * Constructs soot class that represents a universal threas using switch statement
	 * in the body of its run method.   
	 *
	 */
	public SootClass genThread() {
		logger.info(
			"Constructing a universal driver thread using switch stmt");
		SootClass driverThread = genThreadBasics("EnvDriverThread");

		SootMethod runMethod =
			new SootMethod(
				"run",
				new java.util.ArrayList(),
				VoidType.v(),
				Modifier.PUBLIC);
		Body runBody = genUniversalRunBody();
		runBody.setMethod(runMethod);

		runMethod.setActiveBody(runBody);
		driverThread.addMethod(runMethod);
		return driverThread;
	}
	
	public void genMainFromSpec(Chain units){
	
	}
	
	public Body genUniversalRunBody() {

		Body runBody = new JavaBody();

		Chain units = runBody.getUnits();
		if (atomicSteps)
			units.add(
				JavaGr.newExprStmt(
					JavaGr.newStrExpr(JavaPrinter.getBeginAtomicCall())));

		List defaultPropositions = genDefaultPropositions();
		Iterator mi = defaultPropositions.iterator();
		JavaStmt switchStmt = null;
		JavaStmt caseStmt =
			JavaGr.newCaseStmt(null, JavaGr.newReturnVoidStmt(), null);
		String randomCall =
			JavaPrinter.getRandomIntCall(defaultPropositions.size());

		//we are walking through all of the methods except the last one: nonObservable
		for (int i = defaultPropositions.size() - 1; i >= 0; --i) {
			Proposition prop = (Proposition)mi.next();
			
			//figure out the call to the unit method
			JavaStmt methodCall = genPropositionStmt(prop);

			caseStmt =
				JavaGr.newCaseStmt(
					JavaGr.newStrExpr("" + i),
					JavaGr.newSequenceStmt(
						methodCall,
						JavaGr.newExprStmt(JavaGr.newStrExpr("break"))),
					caseStmt);
		}
		switchStmt =
			JavaGr.newSwitchStmt(JavaGr.newStrExpr("choice"), caseStmt);

		units.add(
			JavaGr.newWhileStmt(
				JavaGr.newStrExpr("true"),
				JavaGr.newSequenceStmt(
					JavaGr.newExprStmt(
						JavaGr.newStrExpr("int choice = " + randomCall)),
					switchStmt)));

		//this stmt is never reached in Java code, so we omit its creation
		//if(atomicStepsMode)
		//units.add(JavaGr.newExprStmt(JavaGr.newStrExpr(context+".endAtomic()")));

		return runBody;
	}


	public Body genIfElseUniversalRunBody() {

		Body runBody = new JavaBody();

		Chain units = runBody.getUnits();
		if (atomicSteps)
			units.add(
				JavaGr.newExprStmt(
					JavaGr.newStrExpr(JavaPrinter.getBeginAtomicCall())));
		List defaultPropositions = genDefaultPropositions();
		Iterator mi = defaultPropositions.iterator();
		JavaStmt stmt = null;
		
		//SootMethod publicMethod;
		//String methodCode;
		//SootClass declClass;
		//String declClassName;
		//String receiver = "";
		JavaStmt methodCall;
		//we are walking through all the methods except the last one: nonObservable
		//might want to fix later to have nonObservable as an option (?)
		int numProps = defaultPropositions.size()-1;
		for (int i = 0; i < numProps; i++) {
			Proposition prop = (Proposition)mi.next();

			methodCall = genPropositionStmt(prop);

			String chooseCall = JavaPrinter.getRandomBoolCall();

			if (i == 0)
				stmt =
					JavaGr.newIfStmt(JavaGr.newStrExpr(chooseCall), methodCall);
			else
				stmt =
					JavaGr.newIfElseStmt(
						JavaGr.newStrExpr(chooseCall),
						methodCall,
						stmt);

		}
		units.add(JavaGr.newWhileStmt(JavaGr.newStrExpr("true"), stmt));

		//this stmt is never reached in Java code, so we omit its creation
		//if(atomicStepsMode)
		//units.add(JavaGr.newExprStmt(JavaGr.newStrExpr(context+".endAtomic()")));

		return runBody;
	}
}
