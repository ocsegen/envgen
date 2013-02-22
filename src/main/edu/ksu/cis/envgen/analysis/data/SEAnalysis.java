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
package edu.ksu.cis.envgen.analysis.data;

import java.util.*;
import java.util.logging.*;
import java.io.*;


import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.*;


import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.analysis.*;
import edu.ksu.cis.envgen.analysis.pta.*;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.MultiSet;

/**
 * Invokes points-to analysis, walks over methods and performs side-effects analysis
 * for each unvisited method.
 *
 */
public class SEAnalysis extends StaticAnalysis {

	SEAnalysisResults seAnalysisResults;
	
	boolean returnSensitivity = false;
	boolean mustSE = false;
	
	
	public SEAnalysis(ApplInfo applInfo, PTAnalysisResults ptAnalysisResults) {

		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.callGraph = applInfo.getEnvCallGraph();
		seAnalysisResults = new SEAnalysisResults(ptAnalysisResults);

		//fill out the unit static fields list
		//globals = unit.findGlobals();
		
		logger.setLevel(Level.FINE);

	}
	
	public void setOptions(Properties properties){
		//TODO: finish
		logger.warning("finish implementation");
	}

	public SEAnalysisResults getSEAnalysisResults(){
		return seAnalysisResults;
	}

	/**
	 * For each of the methods in the environment
	 * records a set of unit data side-effects.
	 *
	 */
	public SEAnalysisResults analyze(List markedMethods) {

		 PTAnalysis pointsToAnalysis =
			new PTAnalysis(applInfo);

		HashSet visited = new HashSet();

		for (Iterator mi = markedMethods.iterator(); mi.hasNext();)
			pointsToAnalysis.analyzeAliases((SootMethod) mi.next(), visited);

		PTAnalysisResults ptAnalysisResults = pointsToAnalysis.getPTAnalysisResults();
		seAnalysisResults = new SEAnalysisResults(ptAnalysisResults);

		for (Iterator mi = markedMethods.iterator(); mi.hasNext();)
			analyzeSideEffects((SootMethod) mi.next(), visited);

		if (mustSE) {

			for (Iterator mi = markedMethods.iterator(); mi.hasNext();)
				analyzeMustSideEffects((SootMethod) mi.next(), visited);
		}

		return seAnalysisResults;
	}

	/**
	 * Interprocedural flow-sensitive, parameterized may side-effects analysis.
	 *
	 */
	public void analyzeSideEffects(
		SootMethod externalMethod,
		HashSet visited) {
		//we enter this method only if there is no mapping from
		//external method to its side-effects summary

		if (visited.contains(externalMethod)) {
			//has been visited but no summary
			//this is a cycle in the graph
			//assume the most general infor for the called method
			logger.fine("Cycle with method " + externalMethod);
			//create a mapping from called to a summary table
			//that changes every possible location

			DataFlowSet modifiesAll = getAllModifiedTable(externalMethod);
			seAnalysisResults.putSideEffects(
				externalMethod.getSignature(),
				modifiesAll);
			return;
		}

		logger.fine("Not visited :" + externalMethod);
		visited.add(externalMethod);

		//load the code for the method and step through it
		//JimpleBody jb = (JimpleBody)externalMethod.getBodyFromMethodSource("jb");
		Body jb = externalMethod.retrieveActiveBody();
		if (logger.isLoggable(Level.FINEST)) {
			System.out.println(
				"\nPrinting jimple for: " + externalMethod.toString());
			PrintWriter out = new PrintWriter(System.out, true);
			//jb.printTo(out, 0);
			Printer.v().printTo(jb, out);
		}

		// set up the initial value  DataFlowSet_Alias_Init

		//FlowSet initSideEffectsDataFlowSet = new DataFlowSet();

		CompleteUnitGraph cfg = new CompleteUnitGraph(jb);

		//check the results of the alias analysis

		// set up the initial value  DataFlowSet_Side_Init    
		// the set of symbolic locations is the same as for PointsToAnalysis
		// call Side-Effects Analysis with Alias analysis and DataFlowSet_Side_Init
		//SideEffectsAnalysis modifications = new SideEffectsAnalysis(cfg, initSideEffectsDataFlowSet, alias);

		PTAnalysisTF aliases =
			(PTAnalysisTF) seAnalysisResults.getAliasesOf(externalMethod);

		/*-------------------------------------------------------*/
		/*        Perform may analysis                           */
		/*-------------------------------------------------------*/

		DataFlowSet initDataFlowSet = new DataFlowSet();

		SEAnalysisTF mayModifications =
			new SEAnalysisTF(
				cfg,
				externalMethod,
				initDataFlowSet,
				callGraph,
				seAnalysisResults,
				visited,
				this,
				aliases,
				false);

		DataFlowSet maySummary = getSummary(cfg, mayModifications, false);
		seAnalysisResults.putSideEffects(
			externalMethod.getSignature(),
			maySummary);

		if (returnSensitivity) {
			MultiSet returnSummary = getReturnSummary(cfg, mayModifications);
			seAnalysisResults.putReturnSideEffects(
				externalMethod.getSignature(),
				returnSummary);

		}
		
		visited.remove(externalMethod);
	}

	/**
	* Interprocedural flow-sensitive, parameterized must side-effects analysis.
	*
	*/
	public void analyzeMustSideEffects(
		SootMethod externalMethod,
		HashSet visited) {

		//we enter this method only if there is no mapping from
		//external method to its side-effects summary

		if (visited.contains(externalMethod)) {
			//has been visited but no summary
			//this is a cycle in the graph
			//assume the most general infor for the called method
			logger.fine("Cycle with method " + externalMethod);
			//create a mapping from called to a summary table
			//that changes every possible location

			DataFlowSet modifiesNone = new DataFlowSet();
			//callGraph.getMethodToMustSideEffects().put(externalMethod,modifiesNone);
			seAnalysisResults.putMustSideEffects(
				externalMethod.getSignature(),
				modifiesNone);
			return;
		}
		logger.fine("Not visited :" + externalMethod);
		visited.add(externalMethod);

		//load the code for the method and step through it
		//JimpleBody jb = (JimpleBody)externalMethod.getBodyFromMethodSource("jb");
		Body jb = externalMethod.retrieveActiveBody();
		if (logger.isLoggable(Level.FINEST)) {
			System.out.println(
				"\nPrinting jimple for: " + externalMethod.toString());
			PrintWriter out = new PrintWriter(System.out, true);
			//jb.printTo(out, 0);
			Printer.v().printTo(jb, out);
		}

		// set up the initial value  DataFlowSet_Alias_Init

		//FlowSet initSideEffectsDataFlowSet = new DataFlowSet();

		CompleteUnitGraph cfg = new CompleteUnitGraph(jb);

		//check the results of the alias analysis

		// set up the initial value  DataFlowSet_Side_Init    
		// the set of symbolic locations is the same as for PointsToAnalysis
		// call Side-Effects Analysis with Alias analysis and DataFlowSet_Side_Init
		//SideEffectsAnalysis modifications = new SideEffectsAnalysis(cfg, initSideEffectsDataFlowSet, alias);

		PTAnalysisTF aliases = seAnalysisResults.getAliasesOf(externalMethod);

		/*-------------------------------------------------------*/
		/*        Perform must analysis                          */
		/*-------------------------------------------------------*/

		//create a dummy mapping

		DataFlowSet initDataFlowSet = new DataFlowSet();
		//maySummary.copy(initDataFlowSet);

		MultiSet dummySet = new MultiSet();
		//dummySet.add(new NullValue());
		ValueSetPair dummy =
			new ValueSetPair(new SymLocTop(SymLocTop.DUMMY_LOC, null), dummySet);

		initDataFlowSet.add(dummy, initDataFlowSet);
		logger.fine("initDataFlowSet: " + initDataFlowSet);

		SEAnalysisTF mustModifications =
			new SEAnalysisTF(
				cfg,
				externalMethod,
				initDataFlowSet,
				callGraph,
				seAnalysisResults,
				visited,
				this,
				aliases,
				true);

		//SideEffectsAnalysis mustModifications = new SideEffectsAnalysis(cfg, new DataFlowSet(), unitTable, callGraph, visited, this, aliases, chainLength, containerAnalysis, true);

		DataFlowSet mustSummary = getSummary(cfg, mustModifications, true);
		seAnalysisResults.putMustSideEffects(
			externalMethod.getSignature(),
			mustSummary);

		logger.fine(
				"Analysis, Must Side Effects for "
					+ externalMethod
					+ ": "
					+ mustSummary);

		/*-------------------------------------------------------*/
		/*        Calculate may - must                           */
		/*-------------------------------------------------------*/

		DataFlowSet maySummary =
			seAnalysisResults.getSideEffectsOf(externalMethod.getSignature());
		maySummary.difference(mustSummary, maySummary);

		if (returnSensitivity) {

			/*-------------------------------------------------------*/
			/*        Update returnSensitive - must                  */
			/*-------------------------------------------------------*/

			MultiSet returnSummary =
				seAnalysisResults.getReturnSideEffectsOf(
					externalMethod.getSignature());
			Iterator ri = returnSummary.iterator();
			DataFlowSet temp;
			while (ri.hasNext()) {
				temp = (DataFlowSet) ri.next();
				temp.difference(mustSummary, temp);
			}
		}
		visited.remove(externalMethod);
	}

	/**
	 * Builds a table assuming that sm can modifiy anything.
	 * Used to resolve cycles and recursion in side-effects analysis.
	 *
	 */
	public DataFlowSet getAllModifiedTable(SootMethod sm) {
		//if(debug > 0)
		System.out.println("building modifies all table for: " + sm);
		HashMap modified = new HashMap();
		//put static fields of the unit in the table

		/*
		SymLoc symLoc;
		SymLocValue val;
		MultiSet locs;
		MultiSet vals;
		Type baseType;
		Type modifiedType;

		
		SootField sf = null;
		(for Iterator fi = unitStaticFields.iterator();fi.hasNext();)
		{
			sf = (SootField)fi.next();
			baseType = sf.getDeclaringClass().getType();
			modifiedType = sf.getType();
			if( Modifier.isStatic(sf.getModifiers()) && Modifier.isPublic(sf.getModifiers()))
			{
			//create a pair field = {TOP}
			
			symLoc = new SymLoc(new Root(Jimple.v().newStaticFieldRef(sf)));
			symLoc.setType(baseType);
			symLoc.setModifiedType(modifiedType);
		
			val = new TopValue(modifiedType);
			vals = new MultiSet();
			vals.add(val);
			modified.put(symLoc, vals);
			
			}
		}
		*/
		//put parameters in the table

		/*
		
		List paramTypes = sm.getParameterTypes();
		Type paramType = null;
		SootClass paramTypeClass;
		int index = 0;
		for (Iterator pi = paramTypes.iterator(); pi.hasNext();) {
		
			paramType = (Type) pi.next();
			paramTypeClass = getClass(paramType);
			if (paramTypeClass != null) {
				if (callGraph.isUnitSuperType(paramTypeClass)) {
		
					symLoc =
						new SymLoc(
							new Root(
								Jimple.v().newParameterRef(paramType, index)),
							paramType);
					symLoc.setModifiedType(paramType);
		
					val = new TopValue(paramType);
					vals = new MultiSet();
					vals.add(val);
					modified.put(symLoc, vals);
				}
			}
			index++;
		}
		
		*/
		//need to add "this" ref ??
		return new DataFlowSet(modified);
	}
	/**
	 * Calculates sode-effects summary by combining all of the side-effects
	 * at exit points of the method.
	 */
	public DataFlowSet getSummary(
		CompleteUnitGraph cfg,
		SEAnalysisTF modifications,
		boolean must) {
		DataFlowSet summary = new DataFlowSet();

		DataFlowSet temp = null;
		int i = 0;
		for (Iterator cfgi = cfg.iterator(); cfgi.hasNext();) {
			Unit s = (Unit) cfgi.next();
			if (s instanceof ReturnStmt || s instanceof ReturnVoidStmt) {
				temp = (DataFlowSet) modifications.getFlowAfter((Unit) s);

				if (i == 0)
					summary.union(temp, summary);
				else {
					if (must)
						summary.intersection(temp, summary);
					else
						summary.union(temp, summary);
				}
				i++;
			}
		}
		return summary;
	}

	/**
	 * Calculates side-effects summary by combining all of the side-effects
	 * at exit points of the method.
	 */
	public MultiSet getReturnSummary(
		CompleteUnitGraph cfg,
		SEAnalysisTF modifications) {
		MultiSet summary = new MultiSet();
		DataFlowSet temp = null;
		for (Iterator cfgi = cfg.iterator(); cfgi.hasNext();) {
			Unit s = (Unit) cfgi.next();
			if (s instanceof ReturnStmt || s instanceof ReturnVoidStmt) {
				temp = (DataFlowSet) modifications.getFlowAfter((Unit) s);

				summary.add(temp);

			}
		}
		return summary;
	}

}
