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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;


import soot.*;

import edu.ksu.cis.envgen.analysis.*;
import edu.ksu.cis.envgen.analysis.pta.*;
import edu.ksu.cis.envgen.util.*;

/**
 * Mappings from methods to their points-to and side-effects summaries.
 *
 */
public class SEAnalysisResults extends AnalysisResults{

	PTAnalysisResults ptAnalysisResults;

	/** Mapping between method signatures and side-effects info.
	 *  Used to pull out info on a method based on its signature 
	 * rather than method's object, that might be different in other points of the program. */
	Map methodToSideEffects = new HashMap();

	Map methodToReturnSideEffects = new HashMap();

	Map methodToMustSideEffects = new HashMap();
	

	public SEAnalysisResults(PTAnalysisResults ptAnalysisResults){
		this.ptAnalysisResults = ptAnalysisResults;
	}

	/*-----------------------------------------------*/
	/*          To get tables                        */
	/*-----------------------------------------------*/
	
	public Map getMethodToSideEffects() {
		return methodToSideEffects;
	}

	public Map getMethodToMustSideEffects() {
		return methodToMustSideEffects;
	}

	public PTAnalysisResults getPTAnalysisResults(){
		return ptAnalysisResults;
	}

	/*-----------------------------------------------*/
	/*      To get info for one method               */
	/*-----------------------------------------------*/

	public PTAnalysisTF getAliasesOf(SootMethod sm) {
		return ptAnalysisResults.getAliasesOf(sm);
	}
	
	public MultiSet getNewLocationsOf(String sm) {
		return ptAnalysisResults.getNewLocationsOf(sm);
	}
	
	public MultiSet getCallBacksOf(String sm) {
		return ptAnalysisResults.getCallBacksOf(sm);
	}

	public MultiSet getReturnLocationsOf(String sm) {
		return ptAnalysisResults.getReturnLocationsOf(sm);
	}
	

	public DataFlowSet getSideEffectsOf(String sm) {
		return (DataFlowSet) methodToSideEffects.get(sm);
	}

	public DataFlowSet getMustSideEffectsOf(String sm) {
		return (DataFlowSet) methodToMustSideEffects.get(sm);
	}

	public MultiSet getReturnSideEffectsOf(String sm) {
		return (MultiSet) methodToReturnSideEffects.get(sm);
	}


	/*-----------------------------------------------*/
	/*      put info for one method               */
	/*-----------------------------------------------*/



	public void putSideEffects(String sm, DataFlowSet summary) {
		if (methodToSideEffects == null)
			methodToSideEffects = new HashMap();
		methodToSideEffects.put(sm, summary);

	}

	public void putMustSideEffects(String sm, DataFlowSet summary) {
		if (methodToMustSideEffects == null)
			methodToMustSideEffects = new HashMap();
		methodToMustSideEffects.put(sm, summary);
	}

	public void putReturnSideEffects(String sm, MultiSet summary) {
		if (methodToReturnSideEffects == null)
			methodToReturnSideEffects = new HashMap();
		methodToReturnSideEffects.put(sm, summary);
	}



	/*-----------------------------------------------*/
	/*      contains info for one method             */
	/*-----------------------------------------------*/



	public boolean containsSideEffects(String sm) {
		if (methodToSideEffects == null)
			return false;
		return methodToSideEffects.containsKey(sm);

	}
	
	public boolean containsMustSideEffects(String sm) {
		if (methodToMustSideEffects == null)
			return false;
		return methodToMustSideEffects.containsKey(sm);
	}

	public boolean containsReturnSideEffects(String sm) {
		if (methodToReturnSideEffects == null)
			return false;
		return methodToReturnSideEffects.containsKey(sm);
	}
	


	public void printAnalysisResults() {

		System.out.println(
			"-------------\nFinal may se results for methods is: \n"
				+ methodToSideEffects);
		System.out.println(
			"-------------\nFinal must se results for methods is: \n"
				+ methodToMustSideEffects);
		System.out.println(
			"-------------\nFinal return se results for methods is: \n"
				+ methodToReturnSideEffects);

	
	}

	public void printResultsToFile(String fileName) {

		FileWriter dumpFile = null;
		PrintWriter dumpOut = null;
		
		SootMethod method;
		String methodSignature;
		
		DataFlowSet mustSummary = null;
		DataFlowSet maySummary;
		MultiSet callBacks;
		
		Body jb;
		Map methodToAliases = ptAnalysisResults.getMethodToAliases();

		try {
			dumpFile = new FileWriter(fileName);
			dumpOut = new PrintWriter(dumpFile, true);
		} catch (Exception ex) {
		}

		
		for (Iterator it =  methodToAliases.keySet().iterator(); it.hasNext();) {
			
			method = (SootMethod) it.next();
			methodSignature = method.getSignature();
			
			jb = method.retrieveActiveBody();
			
			
			mustSummary = getMustSideEffectsOf(methodSignature);
			maySummary = getSideEffectsOf(methodSignature);
			callBacks = getCallBacksOf(methodSignature);
			
			try {
				dumpFile.write("\n\n***********************************");
				dumpFile.write("\nmethod name: " + method);

				dumpFile.write("\njimple: \n");
				//jb = method.retrieveActiveBody();
				Printer.v().printTo(jb, dumpOut);
				//jb.printTo(dumpOut);
				//out.flush();
				
				dumpFile.write("\n----------------------------------------");
				dumpFile.write("\ncallBacks:");
				dumpFile.write("\n" + callBacks);
				dumpFile.write("\nmustSummary:");
				dumpFile.write("\n" + mustSummary+"\n");
				dumpFile.write("\nmaySummary:");
				dumpFile.write("\n" + maySummary);
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
