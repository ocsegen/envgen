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
package edu.ksu.cis.envgen.analysis.cg;

import java.util.*;
import java.util.logging.*;
import java.io.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

// import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraph;
import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.applinfo.*;
/*
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
//import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.CollectionTokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

//import edu.ksu.cis.indus.staticanalyses.tokens.SootValueTypeManager;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.processing.Context;
*/


/**
 * 
 * Call graph based on the Indus Object Flow Analysis (OFA).
 * Currently not used and commented out to compile without Indus.
 * 
 */
public class EnvCallGraphIndus extends EnvCallGraph {


	//ICallGraphInfo callGraphInfo;
	

	/** Set options using a config file */
	public void setOptions(Properties properties){
		//TODO: set options
		logger.warning("finish implementation");
		
	}


	public void buildCallGraph(ApplInfo applInfo) {
		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.env = applInfo.getEnv();
		unitMethods = unit.getMethods();
		this.hierarchy = hierarchy; 
		
		logger.info("------------Building call graph based on Indus OFA------------");
		logger.severe("finish implementation");
		
		/*
		if (ofaAnalysis) {

			//List unitMethods = getUnitMethods();
			logger.info("\n*******Unit methods: " + unitMethods);
			List entryMethods = getEntryMethods(unitMethods);
			logger.info("\n*******Entry methods: " + entryMethods);
			callGraphInfo = getCallGraph(unitMethods);
			logger.info("\n*******Done building call graph using ofa");

		} 
		*/
		logger.info("------------Done Building call graph based on Indus OFA------------");
				
	}

	/**
	 * Builds an OFA analysis-based call graph.
	 * 
	 * @param rootMethods
	 * @return
	 */

	/*
	public ICallGraphInfo getCallGraph(final Collection rootMethods) {
		
	
		final String _tagName = "SideEffect:FA";
		IValueAnalyzer aa =
			OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager(new SootValueTypeManager()), 
					new ExceptionFlowSensitiveStmtGraphFactory(
							ExceptionFlowSensitiveStmtGraphFactory.SYNC_RELATED_EXCEPTIONS,
							true));
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final PairManager _pairManager = new PairManager(false, true);
		CallGraphInfo cgi = new CallGraphInfo(new PairManager(false, true));
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		//final IThreadGraphInfo _tgi = new ThreadGraph(cgi, new CFGAnalysis(cgi, getBbm()), _pairManager);
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();

		_ssr.setStmtGraphFactory(new ExceptionFlowSensitiveStmtGraphFactory(
				ExceptionFlowSensitiveStmtGraphFactory.SYNC_RELATED_EXCEPTIONS,
				true));

		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		_cgipc.setAnalyzer(aa);
		_cgipc.setProcessingFilter(new CGBasedProcessingFilter(cgi));
		_cgipc.setStmtSequencesRetriever(_ssr);

		//final Map _info = new HashMap();
		//_info.put(ICallGraphInfo.ID, cgi);
		//_info.put(IThreadGraphInfo.ID, _tgi);
		//_info.put(PairManager.ID, _pairManager);
		//_info.put(IEnvironment.ID, aa.getEnvironment());
		//_info.put(IValueAnalyzer.ID, aa);

		//initialize();
		aa.analyze(new Environment(Scene.v()), rootMethods);

		_processors.clear();
		_processors.add(_callGraphInfoCollector);
		_pc.reset();
		_pc.driveProcessors(_processors);
		cgi.reset();
		cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());
		//writeInfo("CALL GRAPH:\n" + cgi.toString());
		
		return cgi;

	}
    */

	
	public List getTargetsOf(SootMethod sm) {
		assert(sm != null);
		
		List result = new ArrayList();
		
		/*
		List sites = getSitesOf(sm);

		if (sm.isPhantom() || sm.isAbstract() || sm.isNative()) {
			// No body to look at so no targets
			return result;
		}
		if (sites.isEmpty())
			return result;
		// Use a set as temporary structure to ensure unique targets
		Set targets = new HashSet();

		// The targets of a method is the union of the targets
		// for all invoke expressions in the method
		for (Iterator si = sites.iterator(); si.hasNext();) {
			Stmt s = (Stmt) si.next();
			targets.addAll(getTargetsOf(s));
		}
		// Transfer the results to a list to match return type
		result.addAll(targets);
		*/
		logger.severe("finish implementation");
		return result;
		
		
	}
	

	/**
	 * 
	 * @param site
	 * @return a collection of possible targets
	 */

	public Collection getTargetsOf(Stmt site, SootMethod sm) {

		//if (ofaAnalysis) {
		//	
			//String className = sm.getDeclaringClass().getName();

//			if (!processTargetPackage(sm))
//				return new ArrayList();
//
//			InvokeExpr ie = (InvokeExpr) ((Stmt) site).getInvokeExpr();
//			Context context = new Context();
//			context.setStmt(site);
//			context.setRootMethod(sm);
//			return callGraphInfo.getCallees(ie, context);
//		}
//
		logger.severe("finish implementation");
		return new ArrayList();
		

		
	}

	/**
	 * This method is called from EnvInterface to discover the environment
	 * methods
	 * 
	 * @param site
	 * @param expr
	 * @param receiverType
	 * @param sm
	 * @return
	 */

	public Collection getTargetsOf(Stmt site, InvokeExpr expr,
			Type receiverType, SootMethod sm) {

		//System.out.println("getTargetsof method: " +sm +", receiver type: "+receiverType);
		/*
		if (ofaAnalysis) {

			InvokeExpr ie = (InvokeExpr) ((Stmt) site).getInvokeExpr();
			Context context = new Context();
			context.setStmt(site);
			context.setRootMethod(sm);
			return callGraphInfo.getCallees(ie, context);
		}
		*/

		    logger.severe("finish implementation");
			return new ArrayList();
		//}
	}

	public List getTargetsOf(Stmt site){
		
		logger.severe("finish implementation");
		return new ArrayList();
	}
	
	public List getSitesOf(SootMethod sm){
		
		logger.warning("finish implementation");
		return new ArrayList();
	}

	
}
