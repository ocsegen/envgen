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

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import edu.ksu.cis.envgen.*;

/**
 * 
 * Call graph based on Class Hierarchy Analysis (CHA).
 * 
 */
public class EnvCallGraphCHA extends EnvCallGraph {

	public EnvCallGraphCHA() {}
	
	/** Set options using a config file */
	public void setOptions(Properties properties){
		
		this.properties = properties;
	
		//String packageNameStr = properties.getProperty("analysisPackageName");
		//if(packageNameStr != null)
		//	analysisPackageName = packageNameStr;
		
		//TODO: finish
		logger.fine("finish implementation");
		
	}
	

	public void buildCallGraph(ApplInfo applInfo) {
		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.env = applInfo.getEnv();
		unitMethods = unit.getMethods();
		this.hierarchy = hierarchy; 

		logger.info("------------Building call graph based on Soot CHA------------");
		
		buildCHACallGraphAsTransformer();
		
		logger.info("------------Done Building call graph based on Soot CHA------------");
	}
	
	public void buildCHACallGraphAsTransformer() {

		// InvokeGraph graph = ClassHierarchyAnalysis.newInvokeGraph();
		// InvokeGraph graph = getGraph();

		
		Options.v().set_include_all(true);
		Options.v().set_app(true);
		soot.Scene.v().loadNecessaryClasses();
		//soot.Scene.v().setEntryPoints(EntryPoints.v().all());
		
		
		HashMap opt = new HashMap();
		//opt.put("include-all","true");
		//opt.put("app","true");
		opt.put("enabled","true");
		opt.put("full-resolver","true");
		opt.put("verbose", false);
		
		List entryPoints = getAllEntryMethods();
		logger.info("entry points: "+entryPoints);
		Scene.v().setEntryPoints(entryPoints);
		
		
		CHATransformer.v().transform("", opt);
		
		callGraph = Scene.v().getCallGraph();	
				
	}

	public List getTargetsOf(SootMethod sm) {
		
		logger.warning("finish implementation");
		List result = new ArrayList();
		return result;
		
	}
	
	/**
	 * 
	 * @param site
	 * @return a collection of possible targets
	 */
	public Collection getTargetsOf(Stmt site, SootMethod sm) {

		List result = new ArrayList();
	    Iterator it =  callGraph.edgesOutOf(site);
	    
	    SootMethod target;
	    Edge edge;
	    while(it.hasNext()){
	    	edge = (Edge)it.next();
	    	target = edge.tgt();
	    	if(processTarget(target))
	    		result.add(target);
	    }
	    logger.fine("Target of: "+site+"   :   "+result);
	    return result;
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
		//TODO: eliminate this method
		return getTargetsOf(site, sm);
		
	}
	
	public List getTargetsOf(Stmt site){
		List result = new ArrayList();
		logger.severe("finish implementation");
		return result;
	}
	
	public List getSitesOf(SootMethod sm){
		logger.warning("finish implementation");
		List result = new ArrayList();
		return result;
	}

	
}
