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
import soot.jimple.*;
import soot.util.*;

import edu.ksu.cis.envgen.analysis.*;
import edu.ksu.cis.envgen.analysis.data.*;
import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;
import edu.ksu.cis.envgen.codegen.ast.expr.InvokeExpr;
import edu.ksu.cis.envgen.codegen.ast.stmt.*;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.*;

/**
 * Generates stubs for environment methods
 * based on side-effects analysis.
 * 
 */
public class SEStubGenerator extends JavaStubGenerator{

	boolean mustSE = false;
	
	boolean returnSensitivity = false;
	
	//TODO: use chain constructors
	public SEStubGenerator(){
		
	}

	/** StubGenerator constructor that sets fields flowing from the
	 *  EnvGenerator.
	 */
	public SEStubGenerator(
		UnitInfo unit,
		EnvInfo env) {
			
		this.unit = unit;
		this.env = env;	
		
	}

	public void setOptions(Properties properties){
		//TODO: recheck/finish options
		
		String mustAnalysisStr = properties.getProperty("mustAnalysis");
		if(mustAnalysisStr != null)
			mustSE = Boolean.valueOf(mustAnalysisStr);
		
		String returnSensitivityStr = properties.getProperty("returnSensitivity");
		if(returnSensitivityStr != null)
			returnSensitivity = Boolean.valueOf(mustAnalysisStr);

	}

	/**
	 * Builds a stub based on analysis results.
	 *
	 */
	public void genStubBody(
		SootClass markedClass,
		SootMethod markedMethod) {
		
		if(! (assumptions instanceof SEAnalysisResults))
			logger.severe("Wrong type of assumptions!");
		SEAnalysisResults seAssumptions = (SEAnalysisResults)assumptions;

		//TODO: assert(markedMethod != null)
		String signature = markedMethod.getSignature();
		
		logger.finest("Create stub body for method prefixed signature: " + signature);

		//fix the method signature to contain no dummy prefix
		// ?? will this always be correct
		String methodSignature = "<" + signature.substring(2);
		
		
		logger.info("Create stub body for method recovered signature: "
					+ methodSignature);
		Type returnType = markedMethod.getReturnType();

		//create a body for this method
		Body body = new JavaBody();
		body.setMethod(markedMethod);
		Chain units = body.getUnits();

		if (atomicStepsMode)
			units.add(
				JavaGr.newExprStmt(
					JavaGr.newStrExpr(JavaPrinter.getBeginAtomicCall())));

		MultiSet newLocations = seAssumptions.getNewLocationsOf(methodSignature);
		buildAllocationStmts(markedMethod, newLocations, units);	
		
		MultiSet callBacks = seAssumptions.getCallBacksOf(methodSignature);
		buildCallBackStmts(markedMethod, callBacks, units);
		
		/*---------------------------------------------------*/
		/*  Print out must portion of side effects           */
		/*---------------------------------------------------*/
		if (mustSE) {
			DataFlowSet mustSideEffectsSummary =
				seAssumptions.getMustSideEffectsOf(methodSignature);
			units.add(
				getSideEffectsStmts(
					mustSideEffectsSummary,
					markedClass,
					false));
		}
		/*---------------------------------------------------*/
		/*   Print out may portion of side effects           */
		/*---------------------------------------------------*/
		MultiSet returnLocations =
			seAssumptions.getReturnLocationsOf(methodSignature);

		if (returnSensitivity) {
			MultiSet returnSideEffectsSummary =
				seAssumptions.getReturnSideEffectsOf(methodSignature);
			buildReturnSideEffectsStmts(
				returnSideEffectsSummary,
				returnLocations,
				units,
				markedClass,
				markedMethod);
		} else {
			DataFlowSet sideEffectsSummary =
				seAssumptions.getSideEffectsOf(methodSignature);
				
			units.add(getSideEffectsStmts(sideEffectsSummary, markedClass, true));

			//preprocess the return locations to be in one set
			if (!(returnType instanceof VoidType)) {
				MultiSet returnLocationsSet = new MultiSet();
				if (returnLocations != null) {
					MultiSet temp = null;
					for (Iterator ri = returnLocations.iterator();
						ri.hasNext();
						) {
						temp = (MultiSet) ri.next();
						returnLocationsSet.addAll(temp);
					}
				}
				units.add(getReturnStmts(markedClass, markedMethod, returnLocationsSet));
			}
		}
		if (atomicStepsMode)
			units.add(
				JavaGr.newExprStmt(
					JavaGr.newStrExpr(JavaPrinter.getEndAtomicCall())));

		markedMethod.setActiveBody(body);
	}
	
	/**
	 * 
	 */
	public void buildAllocationStmts(SootMethod markedMethod, MultiSet newLocations, Chain units) {

		logger.fine("new locations for: "+markedMethod +"\n"+ newLocations);
		if(newLocations == null)
			return;
		
		
		SymLocPath sym;
		Type symType;
		String symTypeStr;
		//String symCode;
		Root root;
		Value head;
		for (Iterator ni = newLocations.iterator(); ni.hasNext();) {
			sym = (SymLocPath) ni.next();
			//create an allocation statement
			
			root = sym.getRoot();
			head = root.getRoot();
			symType = root.getRootType();
			symTypeStr = symType.toString();
			logger.finest("Generating new location creation");
			//symCode = root.getCode();
			if (head instanceof soot.jimple.NewExpr) {
				units.add(
					JavaGr.newExprStmt(
						JavaGr.newDeclExpr(
							symTypeStr, root.getName(),new ValueExpr( new NewValue(symType, new ArrayList())))));
				//may need to figure out paramters to the constructor call ??
				
				
			} else if (head instanceof NewArrayExpr) {
				Type elemType = ((NewArrayExpr) head).getBaseType();
				//String elemTypeStr = vgen.getPackageAdjustedName(elemType);
				
				units.add(
					JavaGr.newExprStmt(
						JavaGr.newDeclExpr(
							symTypeStr, root.getName(),
							new ValueExpr(new NewArrayValue(elemType)))));
			}
		}
	}

	/**
	 * 
	 */
	public void buildCallBackStmts(SootMethod markedMethod, MultiSet callBacks, Chain units) {
		
		logger.info("callBacks for: "+markedMethod +"\n"+ callBacks);
		
		if(callBacks == null)
			return;
		
		SymLocValue callBack; 
		SootMethod sm;
		Stmt unit;
		String code;
		
		for (Iterator ni = callBacks.iterator(); ni.hasNext();) {
			callBack = (SymLocValue) ni.next();
			code = callBack.toString();
			//logger.info("************call back: "+callBack);
			//create a method call statement
			
			JavaStmt methodCall = JavaGr.newExprStmt(JavaGr.newStrExpr(code));
			units.add(methodCall);
		}
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

		//if (expr.throwsExceptions())
		//	methodCall = genTryCatchStmt(methodCall);

		return methodCall;
	}
	

	
	public void buildReturnSideEffectsStmts(
		MultiSet returnSideEffectsSummary,
		MultiSet returnLocations,
		Chain units,
		SootClass markedClass,
		SootMethod markedMethod) {
		
		//TODO: assert units!=null && markedMethod!=null
		logger.finer("createSideEffectsStmt with set: " + returnSideEffectsSummary);
		logger.finer("createSideEffectsStmt with return Locations: "
					+ returnLocations);
		

		if (returnSideEffectsSummary == null
			|| returnSideEffectsSummary.isEmpty()) {
			
			
			units.add(
				JavaGr.newExprStmt(
					JavaGr.newStrExpr("// no return sensitive effects")));
			// ?? return statement ??
			return;
		}

		Type returnType = markedMethod.getReturnType();

		if (!(returnType instanceof VoidType)
			&& returnSideEffectsSummary.size() != returnLocations.size())
			logger.severe("StubGenerator, createReturnSideEffectsStmt: sizes differ");

		DataFlowSet singleReturnSummary = null;
		MultiSet singleReturn = null;
		Iterator si = returnSideEffectsSummary.iterator();
		Iterator ri = returnLocations.iterator();

		units.add(
			JavaGr.newExprStmt(
				JavaGr.newStrExpr("// begin return sensitive may analysis")));

		if (returnSideEffectsSummary.size() == 1) {
			singleReturnSummary = (DataFlowSet) si.next();
			units.add(
				getSideEffectsStmts(singleReturnSummary, markedClass, true));

			if (!(returnType instanceof VoidType)) {
				singleReturn = (MultiSet) ri.next();
				units.add(getReturnStmts(markedClass, markedMethod, singleReturn));
			}
		} else {
			//create a nondeterministic choice
			int size = returnSideEffectsSummary.size();

			JavaStmt switchStmt = null;
			JavaStmt caseStmt = JavaGr.newCaseStmt(null, null, null);
			JavaStmt valStmt;
			//String randomCall = EnvPrinter.getRandomIntCall(size);

			for (int i = size - 1; i >= 0; --i) {
				singleReturnSummary = (DataFlowSet) si.next();
				valStmt =
					getSideEffectsStmts(singleReturnSummary, markedClass, true);
				if (!(returnType instanceof VoidType)) {
					singleReturn = (MultiSet) ri.next();
					valStmt =
						JavaGr.newSequenceStmt(
							valStmt,
							(getReturnStmts(markedClass, markedMethod, singleReturn)));
				}
				caseStmt =
					JavaGr.newCaseStmt(
						JavaGr.newStrExpr("" + i),
						valStmt,
						caseStmt);
			}

			String choice = "choiceReturn";
			switchStmt =
				JavaGr.newSwitchStmt(JavaGr.newStrExpr(choice), caseStmt);
			switchStmt =
				JavaGr.newSequenceStmt(
					JavaGr.newExprStmt(
						JavaGr.newStrExpr(
							"int "
								+ choice
								+ " = "
								+ JavaPrinter.getRandomIntCall(size - 1))),
					switchStmt);

			units.add(switchStmt);
			//create the default return 
			if (!(returnType instanceof VoidType))
				units.add(
					JavaGr.newReturnStmt(
						JavaGr.newValueExpr(
							new TopValue(returnType))));

		}
		units.add(
			JavaGr.newExprStmt(
				JavaGr.newStrExpr("// end return sensitive may analysis")));
	}

	/**
	 * Takes sideEffectsSummary table and builds assignment statements
	 * that describe it.
	 */
	public JavaStmt getSideEffectsStmts(
		DataFlowSet sideEffectsSummary,
		SootClass markedClass,
		boolean may) {

		
		String analysisType;

		if (may){
			analysisType = "may se";
			logger.info("may sideEffectsSummary: "+"\n" + sideEffectsSummary);

		}
		else{
			analysisType = "must se";
			logger.info("must sideEffectsSummary: "+"\n" + sideEffectsSummary);

		}

		if (sideEffectsSummary == null || sideEffectsSummary.isEmpty())
			return JavaGr.newExprStmt(
				JavaGr.newStrExpr("// no " + analysisType));

		JavaStmt result;
		SymLoc symLoc;
		SymLocValue symLocVal;
		Type symType;
		//int lId = 0; //used to create unique vars on the lhs
		int rId = 0; //used to create unique vars on the rhs
		SymLocValue lhs = null; //"no var";
		SymLocValue rhs = null; //"no val";

		result =
			(JavaGr.newExprStmt(JavaGr.newStrExpr("// begin " + analysisType)));
			
		Set keys = sideEffectsSummary.keySet();

		for (Iterator it = keys.iterator(); it.hasNext();) {
			//needs to be changed to SymLoc
			symLoc = (SymLoc) it.next();
			if (symLoc instanceof SymLocTop && ((SymLocTop)symLoc).isDummyLoc())
				continue;
				
			//if(symLoc.isEnvironmentLoc())
			//continue;

			//check if any fields need to be added
			//build JavaStmt for choose(lhs)

			symType = symLoc.getModifiedType();
			if (symType == null)
				//EnvPrinter.error("StubGenerator, createSideEffectsStmt:  modified type null for: "+symLoc);
				symType = symLoc.getType();

			lhs = symLoc;
			genMissingFields(symLoc, markedClass);
			logger.finest("sym loc code: " + lhs);

			//generate a statement lhs  = choose(rhs)  

			MultiSet values = (MultiSet) sideEffectsSummary.get(symLoc);
			//SymLocValue symLocVal = null;

			if (values.size() == 0) {
				logger.severe("no rhs");

				rhs =  new TopValue(symType); //vgen.getTopValName(symType);
				result =
					JavaGr.newSequenceStmt(
						result,
						getAssignmentStmt(lhs, rhs, may));

			} else if (values.size() == 1) {
				symLocVal = (SymLocValue) values.getFirst();
				//if this is SymLoc, then need to recheck fields

				//Value objVal = symLocVal.getValue(); 
				
				genMissingFields(symLocVal, markedClass);

				result =
					JavaGr.newSequenceStmt(
						result,
						getAssignmentStmt(lhs, symLocVal, may));
			} else {
				String rhsName = "r" + rId;
				rhs = new StrValue(rhsName);

				JavaStmt multipleValues =
					getMultipleValues(values, symType, rhsName, markedClass, rId);
				rId++;
				result = JavaGr.newSequenceStmt(result, multipleValues);

				result =
					JavaGr.newSequenceStmt(
						result,
						getAssignmentStmt(lhs, rhs, may));

			}
		}
		result =
			JavaGr.newSequenceStmt(
				result,
				JavaGr.newExprStmt(
					JavaGr.newStrExpr("// end " + analysisType)));
		return result;
	}

	/**
	 * Builds and assignment statement and make it a body of 
	 * if(choose()){} for the may analysis.
	 */
	public JavaStmt getAssignmentStmt(SymLocValue lhs, SymLocValue rhs, boolean may) {
		JavaStmt result =
			JavaGr.newExprStmt(
				JavaGr.newAssignExpr(new ValueExpr(lhs), "=", new ValueExpr(rhs)));
		if (may)
			result =
				JavaGr.newIfStmt(
					JavaGr.newStrExpr(JavaPrinter.getRandomBoolCall()),
					result);

		return result;
	}

	/**
	 * Creates a nondeterministic choice of returnLocations.
	 */
	public JavaStmt getMultipleValues(
		MultiSet values,
		Type symType,
		String var,
		SootClass markedClass,
		int rId) {
			
		Iterator vi = values.iterator();
		int size = values.size();
		SymLocValue symLocVal;
		//String code;
		String typeStr = symType.toString();
		//String defaultValueStr = vgen.getDefValName(symType);
		JavaStmt switchStmt = null;
		JavaStmt caseStmt = JavaGr.newCaseStmt(null, null, null);
		JavaStmt valStmt;
		//String randomCall = EnvPrinter.getRandomIntCall(size);
		//Value objVal;

		for (int i = size - 1; i >= 0; --i) {
			symLocVal = (SymLocValue) vi.next();
			
			//objVal = symLocVal.getValue(); 
			
			genMissingFields(symLocVal, markedClass);

			//code = symLocVal.getCode();
			valStmt = JavaGr.newExprStmt(JavaGr.newAssignExpr(new StrExpr(var), "=", new ValueExpr(symLocVal)));
			caseStmt =
				JavaGr.newCaseStmt(
					JavaGr.newStrExpr("" + i),
					valStmt,
					caseStmt);
		}
		String choice = "choiceR" + rId;

		switchStmt = JavaGr.newSwitchStmt(JavaGr.newStrExpr(choice), caseStmt);
		switchStmt =
			JavaGr.newSequenceStmt(
				JavaGr.newExprStmt(
					JavaGr.newStrExpr(
						"int "
							+ choice
							+ " = "
							+ JavaPrinter.getRandomIntCall(size - 1))),
				switchStmt);

		return JavaGr.newSequenceStmt(
			JavaGr.newExprStmt(JavaGr.newDeclExpr(typeStr, var, new ValueExpr(new DefaultValue(symType)))),
			switchStmt);
	}

	/**
	 * Inserts return statements into <code>units</code>. There may be several 
	 * return statements that are presented as a nondeterministic choice. This method
	 * is called only if returnType is not instanceof VoidType.
	 */
	public JavaStmt getReturnStmts(
		SootClass markedClass,
		SootMethod markedMethod,
		MultiSet returnLocations) {

		logger.finest("building return stmt for: "
				+ markedMethod
				+ " with returnLocations: "
				+ returnLocations);

		Type returnType = markedMethod.getReturnType();

		if (returnLocations == null || returnLocations.isEmpty()) {
			System.out.println(
				"??StubGenerator, createReturnStmt: no return locations");
			return (
				JavaGr.newReturnStmt(
					JavaGr.newValueExpr(new TopValue(returnType))));
		} //else {
			if (returnLocations.size() == 1) {
				SymLocValue symLocValue =
					(SymLocValue) returnLocations.getFirst();
					genMissingFields(symLocValue, markedClass);
				//String code = symLocValue.getCode();
				return (
					JavaGr.newReturnStmt(JavaGr.newValueExpr(symLocValue)));
			} //else {
				JavaStmt multipleReturns =
					getMultipleReturns(markedClass, returnLocations, returnType);
				return (multipleReturns);
			
		
	}

	/**
	 * Creates a nondeterministic choice of returnLocations.
	 */
	public JavaStmt getMultipleReturns(SootClass markedClass,
		MultiSet returnLocations,
		Type returnType) {
		Iterator ri = returnLocations.iterator();
		int size = returnLocations.size();
		SymLocValue symLocValue;
		//String code;
		JavaStmt switchStmt = null;
		JavaStmt caseStmt = JavaGr.newCaseStmt(null, null, null);
		JavaStmt retStmt;
		//String randomCall = EnvPrinter.getRandomIntCall(size);
		String typeStr = returnType.toString();

		for (int i = size - 1; i >= 0; --i) {
			symLocValue = (SymLocValue) ri.next();
			genMissingFields(symLocValue, markedClass);

			//code = symLocValue.getCode();
			retStmt = JavaGr.newExprStmt(JavaGr.newAssignExpr(new StrExpr("value"), "=", new ValueExpr(symLocValue)));
			caseStmt =
				JavaGr.newCaseStmt(
					JavaGr.newStrExpr("" + i),
					retStmt,
					caseStmt);
		}
		switchStmt =
			JavaGr.newSwitchStmt(JavaGr.newStrExpr("choice"), caseStmt);
		switchStmt =
			(JavaGr
				.newSequenceStmt(
					JavaGr.newExprStmt(
						JavaGr.newStrExpr(
							"int choice = "
								+ JavaPrinter.getRandomIntCall(size - 1))),
					switchStmt));

		switchStmt =
			JavaGr.newSequenceStmt(
				JavaGr.newExprStmt(
					JavaGr.newDeclExpr(
						typeStr, " value ", new ValueExpr(new DefaultValue(returnType)))),
				switchStmt);

		return JavaGr.newSequenceStmt(
			switchStmt,
			JavaGr.newExprStmt(JavaGr.newStrExpr("return value")));
	}


	/*--------------------------------------------------*/
	/*            Get Specification                     */
	/*--------------------------------------------------*/
	/**
	 * Maps results of the side-effects analysis
	 * into a convenient form.
	 */
	public void getSpec(List markedMethods) {
	

	}
}
