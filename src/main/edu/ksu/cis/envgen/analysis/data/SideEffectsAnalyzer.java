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
import java.util.logging.Logger;

import soot.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.analysis.AnalysisResults;
import edu.ksu.cis.envgen.analysis.pta.*;
import edu.ksu.cis.envgen.applinfo.*;

/**
 * Implements methods shared by points-to and side-effects analyses.
 * 
 */
public class SideEffectsAnalyzer extends AssumptionsAcquirer {
	/** Unit classes. */
	public ModuleInfo unit;
	
	public ModuleInfo env;
	
	public ApplInfo applInfo; 

	Properties properties;
	
	boolean unitAnalysis = false;
	
	boolean mustSE = false;
	
	boolean printToFile =  false;
	String printFileName = null;

	
	public Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis");
	
	
	public SideEffectsAnalyzer(){	
	}
	
	
	public void setOptions(Properties properties){
		
		this.properties = properties;
		
		String unitAnalysisStr = properties.getProperty("unitAnalysis");
		if(unitAnalysisStr != null)
			unitAnalysis = Boolean.valueOf(unitAnalysisStr);
		
		String mustAnalysisStr = properties.getProperty("mustAnalysis");
		if(mustAnalysisStr != null)
			mustSE = Boolean.valueOf(mustAnalysisStr);
		
		String printToFileStr = properties.getProperty("printToFile");
		if(printToFileStr!=null){
			printToFile = true;
			printFileName = printToFileStr;
		}
		
		//TODO: finish
		logger.fine("finish implementation");
	}
	
	
	public Assumptions acquireAssumptions(ApplInfo info) {
		assert info != null;
		
		this.applInfo = info;
		this.unit = info.getUnit();
		this.env = info.getEnv();
		
		List<SootMethod> markedMethods = null;
		//TODO: synch with how env methods are filled
		//if(unitAnalysis)
		//	markedMethods = unit.getMethods();
		//else 
			markedMethods = env.getMethods();
		
		logger.info("\nMethods under analysis: \n" + markedMethods);
		
		 PTAnalysis pta =
				new PTAnalysis(applInfo);
		 pta.setOptions(properties);

		HashSet<SootMethod> visited = new HashSet<SootMethod>();

		for (Iterator<SootMethod> mi = markedMethods.iterator(); mi.hasNext();)
			pta.analyzeAliases(mi.next(), visited);

		PTAnalysisResults ptAnalysisResults = pta.getPTAnalysisResults();
			
		SEAnalysis se = new SEAnalysis(applInfo, ptAnalysisResults);
		se.setOptions(properties);
		//set analysis results
		//se.setAnalysisResults(analysisResults);

		for (Iterator<SootMethod> mi = markedMethods.iterator(); mi.hasNext();)
			se.analyzeSideEffects(mi.next(), visited);

		if (mustSE) {

			for (Iterator<SootMethod> mi = markedMethods.iterator(); mi.hasNext();)
				se.analyzeMustSideEffects(mi.next(), visited);
		}
		AnalysisResults seAnalysisResults = se.getSEAnalysisResults();
		if (printToFile)
			seAnalysisResults.printResultsToFile(printFileName);
			

		return seAnalysisResults;
	}
}
