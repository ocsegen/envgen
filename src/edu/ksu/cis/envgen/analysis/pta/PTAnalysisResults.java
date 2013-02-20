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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import soot.*;
import soot.jimple.Stmt;
import soot.util.Chain;

import edu.ksu.cis.envgen.analysis.*;
import edu.ksu.cis.envgen.analysis.pta.PTAnalysisTF;
import edu.ksu.cis.envgen.util.*;

/**
 * Mappings from methods to their points-to summaries.
 *
 */
public class PTAnalysisResults extends AnalysisResults {

	//TODO: refactor using PTMethodSummary
	Map methodToAliases = new HashMap();

	/** Mapping between method signatures and return locations. */
	Map methodToReturnLocations = new HashMap();

	Map methodToNewLocations = new HashMap();
	
	Map methodToCallBacks = new HashMap();


	/*-----------------------------------------------*/
	/*          To get tables                        */
	/*-----------------------------------------------*/

	
	public Map getMethodToAliases() {
		return methodToAliases;
	}
	
	public Map getMethodToCallBacks() {
		return methodToCallBacks;
	}
	
	/*-----------------------------------------------*/
	/*      To get info for one method               */
	/*-----------------------------------------------*/

	
	public PTAnalysisTF getAliasesOf(SootMethod sm) {
		return (PTAnalysisTF) methodToAliases.get(sm);
	}
	

	public MultiSet getNewLocationsOf(String sm) {
		return (MultiSet) methodToNewLocations.get(sm);
	}

	public MultiSet getReturnLocationsOf(String sm) {
		return (MultiSet) methodToReturnLocations.get(sm);
	}

	public MultiSet getCallBacksOf(String sm) {
		return (MultiSet) methodToCallBacks.get(sm);
	}

	/*-----------------------------------------------*/
	/*      put info for one method               */
	/*-----------------------------------------------*/

	
	public void putAliases(SootMethod sm, PTAnalysisTF alias) {
		if (methodToAliases == null)
			methodToAliases = new HashMap();
		methodToAliases.put(sm, alias);
	}
	

	public void putNewLocations(String sm, MultiSet locations) {
		if (methodToNewLocations == null)
			methodToNewLocations = new HashMap();
		methodToNewLocations.put(sm, locations);
	}

	public void putReturnLocations(String sm, MultiSet locations) {
		if (methodToReturnLocations == null)
			methodToReturnLocations = new HashMap();
		methodToReturnLocations.put(sm, locations);
	}

	public void putCallBacks(String sm, MultiSet summary) {
		if (methodToCallBacks == null)
			methodToCallBacks = new HashMap();
		methodToCallBacks.put(sm, summary);
	}


	/*-----------------------------------------------*/
	/*      contains info for one method             */
	/*-----------------------------------------------*/

	
	public boolean containsAliases(SootMethod sm) {
		if (methodToAliases == null)
			return false;
		return methodToAliases.containsKey(sm);
	}
	

	public boolean containsNewLocations(String sm) {
		if (methodToNewLocations == null)
			return false;
		return methodToNewLocations.containsKey(sm);
	}

	public boolean containsReturnLocations(String sm) {
		if (methodToReturnLocations == null)
			return false;
		return methodToReturnLocations.containsKey(sm);
	}

	public boolean containsCallBacks(String sm) {
		if (methodToCallBacks == null)
			return false;
		return methodToCallBacks.containsKey(sm);
	}


	public void printResults() {
	
			System.out.println(
				"-------------\nFinal return locations for methods is: \n"
					+ methodToReturnLocations);
			System.out.println(
				"-------------\nFinal new locations for methods is: \n"
					+ methodToNewLocations);	
	}

	public void printResultsToFile(String fileName) {

		FileWriter dumpFile = null;
		PrintWriter dumpOut = null;

		Map methodToAliases = getMethodToAliases();
		//Hashtable methodToMustSideEffects = callGraph.getMethodSignatureToMustSideEffects();
		//Hashtable methodToSideEffects = callGraph.getMethodSignatureToSideEffects();
		//Hashtable methodToReturnLocations = callGraph.getMethodToReturnLocations();
		SootMethod method;
		String methodSignature;
		PTAnalysisTF aliasSummary = null;
		DataFlowSet aliasSet = null;
		DataFlowSet mustSummary = null;
		DataFlowSet maySummary;
		MultiSet returnLocations;
		MultiSet newLocations;
		Body jb;
		Stmt lastUnit;

		try {
			dumpFile = new FileWriter(fileName);
			dumpOut = new PrintWriter(dumpFile, true);
		} catch (Exception ex) {
		}

		for (Iterator it = methodToAliases.keySet().iterator();
			it.hasNext();
			) {
			method = (SootMethod) it.next();
			methodSignature = method.getSignature();

			aliasSummary = (PTAnalysisTF)methodToAliases.get(method);
			jb = method.retrieveActiveBody();
			lastUnit = (Stmt)((Chain)jb.getUnits()).getLast();
			
			aliasSet = (DataFlowSet) aliasSummary.getFlowAfter(lastUnit);
			

			try {
				dumpFile.write("\n\n***********************************");
				dumpFile.write("\nmethod name: " + method);

				dumpFile.write("\njimple: \n");
				//jb = method.retrieveActiveBody();
				Printer.v().printTo(jb, dumpOut);
				//jb.printTo(dumpOut);
				//out.flush();

				dumpFile.write("\n----------------------------------------");
				dumpFile.write("\naliases after the last stmt:");
				dumpFile.write("\n" + aliasSet);
				
				dumpFile.write("\nsummary:");
				
				dumpFile.write("\n----------------------------------------");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		try {
			dumpFile.close();
		} catch (Exception ex) {
		}
	}

}
