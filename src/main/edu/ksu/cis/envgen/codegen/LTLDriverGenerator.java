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

import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;
import edu.ksu.cis.envgen.codegen.ast.stmt.JavaStmt;
import edu.ksu.cis.envgen.spec.*;
import edu.ksu.cis.envgen.spec.ltl2buchi.*;
import edu.ksu.cis.envgen.util.*;

/** Generates drivers based on user-supplied ltl assumptions. */
public class LTLDriverGenerator extends JavaDriverGenerator {

	boolean singleEventCond = false;

	Logger logger = Logger.getLogger("envgen.codegen");
	
	public LTLDriverGenerator(){
		
	}
	
	public LTLDriverGenerator(UnitInfo unit, EnvInfo env) {
		this.unit = unit;
		this.env= env;
		//this.unitInterface = unitInterface;
		//this.defaultPropositions = unitInterface.getDefaultPropositions();
		//this.defaultInstantiations = unitInterface.getDefaultInstantiations();
	}

	/**
	 * 
	 * Reads thread specifications one by one and synthesizes threads from them.
	 */
	public void genThreads() {

		//List threads = userSpec.getThreads();

		//List specs = astManager.getSpecifications();
		//List threadNames = astManager.getThreadNames();
		//List updatedDefaultMethods = null;

		//Iterator si = specs.iterator();
		//Iterator ti = threadNames.iterator();
		Iterator ti = threads.iterator();

		for (int i = 0; ti.hasNext(); ++i) {

			ThreadSpec threadSpec = (ThreadSpec) ti.next();
			SpecNode formula = threadSpec.getSpecification();
			List threadPropositions = threadSpec.getPropositions();
			String threadName = threadSpec.getName();
			SootClass threadClass;

			if (formula instanceof LTLNode) {
				LTLNode ltlFormula = LTLNode.pushNot((LTLNode) formula, true);

				//find the union of aProps and publicMethods elements
				List allProps = new MultiSet(threadPropositions);

				//deafultProps might have no labels entered
				//and they have default receiver objects
				//that could be different if spec instantiations happened

				
				//if (updatedDefaultMethods == null) 
				//	 updatedDefaultMethods = astManager.updateDefaultMethods(defaultPublicMethods);
				 
				//allProps.addAll(updatedDefaultMethods);
				
				logger.info("\nspecProps: " + threadPropositions);
				logger.info("\nallProps: " + allProps);
				logger.info("\nparsed specification: "+ (LTLNode) formula);
				logger.info("\nneg propogated specification: "+ ltlFormula);

				Graph graph = new Graph(allProps.size());
				graph.setNodeID(1);
				//reset, 0 is reserved for the initial state
				MultiSet buchi = null;

				if (singleEventCond) {
					List defaultPropositions = genDefaultPropositions();
					//change singleEventCondition to use allProps list

					LTLNode singleEventFormula = LTLNode
							.singleEventCondition(defaultPropositions);
					//System.out.println("Single Event Condition:
					// "+singleEventFormula);
					singleEventFormula = LTLNode.pushNot(singleEventFormula,
							true);
					//System.out.println("PushNot: "+singleEventFormula);
					//conjunct this formula with the single event condition
					LTLNode combinedSpec = new LTLNode('&', 0, ltlFormula,
							singleEventFormula);
					//System.out.println("Combined spec: "+combinedSpec);
					buchi = graph.createGraph(combinedSpec);
				} else
					buchi = graph.createGraph(ltlFormula);
				
				logger.fine("\nbuchi: " + buchi);
				//single event cond will be taken care by fsa construction
				//System.out.println("allProps size: "+allProps.size());

				if (buchi.isEmpty())
					logger.severe("DriverGenerator: no states in the buchi automaton");

				FSA fsa = new FSA(buchi, allProps.size());
				System.out.println("fsa: " + fsa);
				//append to the end of the list
				threadClass = genThread(fsa, allProps, threadName);
				threadSpec.setImplClass(threadClass);
				//driverThreads.add(getLTLThread(fsa, allProps, threadName));
			} else
				logger.severe("wrong kind of formula");
		}
	}

	public void genMainFromSpec(Chain units) {
		//TODO: finish
	}

	/**
	 * Constructs a soot class that represents a thread synthesized from an LTL
	 * specification.
	 */
	protected SootClass genThread(FSA fsa, List propositions, String threadName) {
		//int index;
		logger.fine("Constructing a synthesized driver thread from LTL specification.");

		if (threadName == null)
			//threadName = "EnvDriverThread"+threadID;
			logger.severe("null threadName");
		SootClass driverThread = genThreadBasics(threadName);

		SootMethod runMethod = new SootMethod("run", new java.util.ArrayList(),
				VoidType.v(), Modifier.PUBLIC);

		Body runBody = genSynthesizedRunBody(fsa, propositions);
		runBody.setMethod(runMethod);
		runMethod.setActiveBody(runBody);
		driverThread.addMethod(runMethod);
		return driverThread;
	}

	public Body genSynthesizedRunBody(FSA fsa, List propositions) {

		Body runBody = new JavaBody();
		Chain units = runBody.getUnits();
		if (atomicSteps)
			units.add(JavaGr.newExprStmt(JavaGr.newStrExpr(JavaPrinter
					.getBeginAtomicCall())));
		units.add(JavaGr.newExprStmt(JavaGr.newStrExpr("int state = "
				+ fsa.getInitialState())));

		JavaStmt switchStmt = null;
		JavaStmt caseStmt = JavaGr.newCaseStmt(null,
				JavaGr.newReturnVoidStmt(), null);
		FSAState[] states = fsa.getStates();
		FSAState tempState = null;

		for (int i = fsa.getMaxSize() - 1; i >= 0; i--) {
			tempState = states[i];
			JavaStmt nestedSwitch = null;
			if (tempState != null) {
				JavaStmt nestedCase = JavaGr.newCaseStmt(null, JavaGr
						.newReturnVoidStmt(), null);
				Iterator ti = tempState.getTransitions().iterator();
				Transition tr;

				for (int j = tempState.getTransitions().size() - 1; j >= 0
						&& ti.hasNext(); j--) {
					tr = (Transition) ti.next();
					if (tr.getActionNum() < propositions.size()) {
						Proposition prop = (Proposition) propositions.get(tr
								.getActionNum());
						//figure out the call to the unit method
						JavaStmt methodCall = genPropositionStmt(prop);

						if ((tempState.getTransitions().size()) == 1) {
							//in case we have only one option
							//there is no need to generate a switch stmt
							nestedCase = JavaGr
									.newSequenceStmt(
											JavaGr.newSequenceStmt(
															methodCall,
															JavaGr.newExprStmt(JavaGr.newAssignExpr(
																					new StrExpr("state"), "=", new StrExpr(""+ tr
																											.getNextState())))),
											JavaGr.newExprStmt(JavaGr
													.newStrExpr("break")));
						} else
							nestedCase = JavaGr
									.newCaseStmt(
											JavaGr.newStrExpr("" + j),
											JavaGr
													.newSequenceStmt(
															JavaGr
																	.newSequenceStmt(
																			methodCall,
																			JavaGr.newExprStmt(JavaGr
																							.newAssignExpr(
																									new StrExpr("state"), "=", new StrExpr(""
																													+ tr
																															.getNextState())))),
															JavaGr
																	.newExprStmt(JavaGr
																			.newStrExpr("break"))),
											nestedCase);
					}
					if ((tempState.getTransitions().size()) == 1)
						nestedSwitch = nestedCase;
					else
						//make code specific to OCSEGen: Open Components and Systems as well (?)
						nestedSwitch = JavaGr.newSequenceStmt(JavaGr
								.newSwitchStmt(JavaGr.newStrExpr(JavaPrinter
										.getRandomIntCall(tempState
												.getTransitions().size() - 1)),
										nestedCase), JavaGr.newExprStmt(JavaGr
								.newStrExpr("break")));
				}
			}
			if (nestedSwitch != null)
				caseStmt = JavaGr.newCaseStmt(JavaGr.newStrExpr("" + i),
						nestedSwitch, caseStmt);

		}
		switchStmt = JavaGr.newSwitchStmt(JavaGr.newStrExpr("state"), caseStmt);
		units.add(JavaGr.newWhileStmt(JavaGr.newStrExpr("true"), switchStmt));

		//this stmt is never reached in Java code, so we omit its creation
		//if(atomicStepsMode)
		//units.add(JavaGr.newExprStmt(JavaGr.newStrExpr(context+".endAtomic()")));

		return runBody;
	}
}