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
import edu.ksu.cis.envgen.codegen.ast.stmt.JavaStmt;
import edu.ksu.cis.envgen.spec.*;

/** Generates drivers based on user-supplied regular expression assumptions. */
public class REDriverGenerator extends JavaDriverGenerator {

	public REDriverGenerator(){
		
	}
	
	public REDriverGenerator(UnitInfo unit, EnvInfo env) {

		this.unit = unit;
		this.env = env;
		
	}

	/**
	 * 
	 * Reads thread specifications one by one and synthesizes threads from them.
	 */
	public void genThreads() {
		
		//assert(userSpec!=null);
		
		//List threads = userSpec.getThreads();
		logger.info("generating threads");


		Iterator ti = threads.iterator();

		for (int i = 0; ti.hasNext(); ++i) {

			ThreadSpec threadSpec = (ThreadSpec) ti.next();
			SpecNode formula = threadSpec.getSpecification();
			assert formula != null;
			//List threadPropositions = threadSpec.getPropositions();
			String threadName = threadSpec.getName();
			SootClass threadClass;

			if (formula instanceof RENode) {
				RENode regFormula = (RENode) formula;
				
				logger.fine("EnvGenerator formula: " + regFormula);
				//if (formula==null) continue;
				regFormula.orCombine();

				//formula.seqCombine();
				//System.out.println("EnvGenerator opt formula: "+formula);

				threadClass = genThread(regFormula, threadName);
				threadSpec.setImplClass(threadClass);
				//driverThreads.add(getRegThread(regFormula, threadName));

			} else
				logger.severe("wrong kind of formula: "+formula.getClass());
		}
	}

	/**
	 * Constructs a soot class that represents a thread synthesized from a
	 * regular expression specification.
	 */
	public SootClass genThread(RENode formula, String threadName) {
		logger.info("Constructing a synthesized driver thread for a reg expr "+ formula);

		if (threadName == null)
			//threadName = "EnvDriverThread"+threadID;
			logger.severe("DriverGenerator, getRegSynthesizedThread: null threadName");

		SootClass driverThread = genThreadBasics(threadName);

		SootMethod runMethod = new SootMethod("run", new java.util.ArrayList(),
				VoidType.v(), Modifier.PUBLIC);
		Body runBody = genSynthesizedRunMethod(formula);

		runBody.setMethod(runMethod);

		runMethod.setActiveBody(runBody);
		driverThread.addMethod(runMethod);

		return driverThread;
	}

	public void genMainFromSpec(Chain mainUnits) {

		//get specification for the main thread
		//ThreadSpec mainThread = userSpec.getMainThread();

		if (mainThread != null) {
			SpecNode formula = mainThread.getSpecification();
			//List threadPropositions = mainThread.getPropositions();
			RENode regFormula = (RENode) formula;
			logger.fine("EnvGenerator formula: " + regFormula);
			//if (formula==null) continue;
			regFormula.orCombine();

			JavaStmt stmt = genRegStmt((RENode) formula, mainUnits, 0, 0);
			stmt = genAtomicPropStmt(stmt);
			mainUnits.add(stmt);
		}
	}

	public Body genSynthesizedRunMethod(RENode formula) {
		Body runBody = new JavaBody();
		Chain units = runBody.getUnits();
		
		JavaStmt stmt = genRegStmt(formula, units, 0, 0);
		stmt = genAtomicPropStmt(stmt);
		units.add(stmt);
		return runBody;
	}

	/**
	 * Creates a run method for a synthesized thread given a regular expression
	 * specification.
	 */
	public JavaStmt genRegStmt(RENode formula, Chain units, int c1, int c2) {
		// c1, c2 are counters used to generate fresh variables for choice and i
		// of the for-loop
		Iterator fi;
		RENode node;
		JavaStmt result = null;

		String randomBoolCall = JavaPrinter.getRandomBoolCall();

		switch (formula.getKind()) {
		case '|':
			// create a switch stmt
			int operandsNum = formula.getOperands().size();
			JavaStmt caseStmt = JavaGr.newCaseStmt(null, JavaGr
					.newReturnVoidStmt(), null);
			fi = formula.getOperands().iterator();
			for (int i = operandsNum - 1; fi.hasNext(); i--) {
				node = (RENode) fi.next();
				caseStmt = JavaGr.newCaseStmt(JavaGr.newStrExpr("" + i),
						JavaGr.newSequenceStmt(genRegStmt(node, units, c1++,
								c2++), JavaGr.newExprStmt(JavaGr
								.newStrExpr("break"))), caseStmt);

			}
			// might need to make "choice" + num
			result = JavaGr.newSequenceStmt(JavaGr.newExprStmt(JavaGr
					.newStrExpr("int choice" + c1 + "="
							+ JavaPrinter.getRandomIntCall(operandsNum - 1))),
					JavaGr.newSwitchStmt(JavaGr.newStrExpr("choice" + c1),
							caseStmt));
			return result;

		case 'S':
			result = JavaGr.newEmptyStmt();
			fi = formula.getOperands().iterator();
			while (fi.hasNext()) {
				node = (RENode) fi.next();
				result = JavaGr.newSequenceStmt(result, genRegStmt(node, units,
						c1++, c2++));
			}
			return result;

		case '^':
			if (formula.getSecondVal() < 0) {
				// this is exp{num} case
				// create a for-loop
				// for(int i=0;i<num;++i){ body }
				// put a statement into the body of the for-loop

				node = (RENode) (((RENode) formula).getOperands()
						.firstElement());
				result = JavaGr.newForStmt(JavaGr.newStrExpr("i<"
						+ formula.getVal()),
						genRegStmt(node, units, c1++, c2++));
				return result;
			}

			// this is exp{num1, num2} case
			// create a for-loop
			// for(int i=0;i<num1+Verify.random(num2-num1);++i){ body }
			// put exp into the body of the for-loop
			node = (RENode) (((RENode) formula).getOperands().firstElement());

			result = JavaGr.newForStmt(JavaGr.newStrExpr("i<"
					+ formula.getVal()
					+ "+"
					+ JavaPrinter.getRandomIntCall(formula.getSecondVal()
							- formula.getVal())), genRegStmt(node, units, c1++,
					c2++));
			return result;

		case '*':
			// create a do while(Verify.randomBool())
			// put a statement into the body
			node = (RENode) (((RENode) formula).getOperands().firstElement());
			result = JavaGr.newWhileStmt(JavaGr.newStrExpr(randomBoolCall),
					genRegStmt(node, units, c1++, c2++));
			return result;

		case '+':
			// create an if(Verify.randomBool())
			// put a statement into the body
			node = (RENode) (((RENode) formula).getOperands().firstElement());
			result = JavaGr.newDoWhileStmt(JavaGr.newStrExpr(randomBoolCall),
					genRegStmt(node, units, c1++, c2++));
			return result;

		case '?':
			// create a while(Verify.randomBool())
			// put a statement into the body
			node = (RENode) (((RENode) formula).getOperands().firstElement());
			result = JavaGr.newIfStmt(JavaGr.newStrExpr(randomBoolCall),
					genRegStmt(node, units, c1++, c2++));
			return result;

		case 'P':
			Proposition prop = formula.getProposition();
			result = genPropositionStmt(prop);
			return result;

		default:
			logger.severe("wrong kind of RNode: "+ formula);
		}
		return result;
	}
}