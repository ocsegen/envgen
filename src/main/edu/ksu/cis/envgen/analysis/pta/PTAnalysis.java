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
package edu.ksu.cis.envgen.analysis.pta;

import java.util.*;
import java.util.logging.*;
import java.io.*;

import soot.*;
import soot.jimple.internal.*;
import soot.toolkits.graph.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.analysis.*;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.MultiSet;

/**
 *
 * Walks over methods and performs points-to analysis for 
 * each unvisited method.
 *
 */
public class PTAnalysis extends StaticAnalysis {

	PTAnalysisResults ptAnalysisResults;

	
	public PTAnalysis(ApplInfo applInfo) {

		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.callGraph = applInfo.getEnvCallGraph();
		ptAnalysisResults = new PTAnalysisResults();

	}

	public void setOptions(Properties properties){

		String chainLengthStr = properties.getProperty("chainLength");
		
		String preserveCallBacksStr = properties.getProperty("preserveCallBacks");
		if(preserveCallBacksStr != null)
			preserveCallBacks = Boolean.valueOf(preserveCallBacksStr);
		
		//TODO: finish
		//logger.info("finish implementation");
	}
	
	public PTAnalysisResults getPTAnalysisResults(){
		return ptAnalysisResults;
	}
	
	/**
	 * For each of the methods in the environment
	 * records a set of unit data side-effects.
	 *
	 */
	public AnalysisResults analyze(List<SootMethod> markedMethods) {

		HashSet<SootMethod> visited = new HashSet<SootMethod>();
		for (Iterator<SootMethod> mi = markedMethods.iterator(); mi.hasNext();)
			analyzeAliases(mi.next(), visited);
		
		return ptAnalysisResults;
	}

	/**
	 * Interprocedural, flow-sensitive, parameterized alias analysis.
	 *
	 */
	public void analyzeAliases(SootMethod externalMethod, HashSet<SootMethod> visited) {
		
		String methodSignature = externalMethod.getSignature();
		
		logger.info("\nAnalyzing method: "+ externalMethod);
		logger.fine("\nVisited: "+ visited);

		if (visited.contains(externalMethod)) {
			
			logger.fine("Already visited :" + externalMethod);
			//this is a cycle, for now we assume that this method
			//can return any location of our interest
			//if it matches the return type of the return value
			MultiSet returnsAll = getAllPossibleReturns(externalMethod);
			//callGraph.getMethodToReturnLocations().put(externalMethod, returnsAll);

			ptAnalysisResults.putReturnLocations(methodSignature, returnsAll);
			return;
		}
		
		logger.fine("Not visited :" + externalMethod);
		visited.add(externalMethod);

		//create appropriate mappings for this method
		ptAnalysisResults.putNewLocations(methodSignature,new MultiSet());
		
		ptAnalysisResults.putCallBacks(methodSignature, new MultiSet());

		//load the code for the method and step through it
		//JimpleBody jb = (JimpleBody)externalMethod.getBodyFromMethodSource("jb");
		Body jb = externalMethod.retrieveActiveBody();
		if (logger.isLoggable(Level.FINEST)) {
			System.out.println(
				"\nPrinting jimple for: " + externalMethod.toString());
			PrintWriter out = new PrintWriter(System.out, true);
			Printer.v().printTo(jb, out);
		}

		// set up the initial value  DataFlowSet_Alias_Init
		//FlowSet initDataFlowSet = new DataFlowSet();

		CompleteUnitGraph cfg = new CompleteUnitGraph(jb);

		// call Alias Analysis with the DataFlowSet_Alias_Init

		PTAnalysisTF alias =
			new PTAnalysisTF(
				cfg,
				externalMethod,
				new DataFlowSet(),
				callGraph,
				ptAnalysisResults,
				visited,
				this);

		//PatchingChain stmtList = jb.getUnits();
		//Unit last = (Unit)stmtList.getLast();
		visited.remove(externalMethod);
		//return ((Hashtable)alias.getFlowAfter(last));

		//create mapping from a method to its own PointsToAnalysis object that can be queried
		//by side-effects analysis
		ptAnalysisResults.putAliases(externalMethod, alias);

		//construct a mapping from method to its return locations
		MultiSet returnLocations =
			getReturnLocations(externalMethod, cfg, alias);

		
		logger.finer(
				"ReturnLocations of: "
					+ externalMethod
					+ ": "
					+ returnLocations);
		ptAnalysisResults.putReturnLocations(methodSignature, returnLocations);
		
		//DataFlowSet reads = getReadSummary(cfg, alias, false);
		//analysisResults.putReads(externalMethod.getSignature(), reads);

	}

	/**
	 * Builds a table assummming that <code>sm</code> can return 
	 * any location of our interest that matches the return type
	 * of the method.
	 * Is used to resolve cycles in the alias analysis.
	 */
	public MultiSet getAllPossibleReturns(SootMethod sm) {
		MultiSet returnsAll = new MultiSet();
		//check return type
		Type returnType = sm.getReturnType();
		SymLoc val = new SymLocTop(SymLocTop.UNKNOWN_LOC, returnType);
		//SymLocValue symLocValue = new SymLocValue(loc);
		returnsAll.add(val);
		//due to return-sensitivity, need to pack inside another set
		MultiSet outerReturnsAll = new MultiSet();
		outerReturnsAll.add(returnsAll);
		return outerReturnsAll;
	}

	/**
	 * Builds a list of sets of return values for externalMethod. If return sensitivity
	 * is not required, then all sets may be collapsed into one set.
	 */
	public MultiSet getReturnLocations(
		SootMethod externalMethod,
		CompleteUnitGraph cfg,
		PTAnalysisTF alias) {
		MultiSet returnLocations = new MultiSet();
		//this makes sense only if the return type of the method
		//is ref type
		Type returnType = externalMethod.getReturnType();
		if (returnType instanceof VoidType)
			return returnLocations;

		for (Iterator cfgi = cfg.iterator(); cfgi.hasNext();) {
			Unit s = (Unit) cfgi.next();
			if (s instanceof JReturnStmt) {
				DataFlowSet aliases =
					(DataFlowSet) alias.getFlowBefore((Unit) s);
				
				
				logger.finer("alias info before return: "+(DataFlowSet)aliases);
				Value val = ((JReturnStmt) s).getOp();
				MultiSet locs = getValues(val, aliases, returnType);
				returnLocations.add(locs);
			}
		}
		
		logger.finer(
			"??Return Locations for: "
				+ externalMethod
				+ " are: "
				+ returnLocations);
		return returnLocations;
	}

}
