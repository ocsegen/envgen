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
package edu.ksu.cis.envgen.analysis;

import java.util.*;
import java.util.logging.Logger;

import soot.*;

import edu.ksu.cis.envgen.*;

/**
 * Mappings from methods to their summaries.
 *
 */
public abstract class AnalysisResults extends StubAssumptions {
	public Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis");

	Map<SootMethod, MethodSummary> methodToSummarries = new HashMap<SootMethod, MethodSummary>();

	public Map<SootMethod, MethodSummary> getMethodSummaries(){
		return methodToSummarries;
	}
	
	public MethodSummary getSummaryOf(SootMethod sm){
		return (MethodSummary)methodToSummarries.get(sm);
	}
	
	
	public void putSummary(SootMethod sm, MethodSummary summary){
		methodToSummarries.put(sm, summary);
	}
	

	public boolean containsSummary(SootMethod sm) {
		if (methodToSummarries == null)
			return false;
		return methodToSummarries.containsKey(sm);
	}
	
	public void printResults() {
		System.out.println(
			"-------------\nMethod summaries are: \n"+ methodToSummarries);
	}


	public abstract void printResultsToFile(String fileName);
}
