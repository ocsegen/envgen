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
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import edu.ksu.cis.envgen.*;

/**
 * 
 * Call graph based on SPARK Points-To-Analysis (PTA). Requires whole-program mode.
 * 
 */
public class EnvCallGraphSpark extends EnvCallGraph {

	
	/** Set options using a config file */
	public void setOptions(Properties properties){
		
		this.properties = properties;
		
		//String packageNameStr = properties.getProperty("analysisPackageName");
		//if(packageNameStr != null)
		//	analysisPackageName = packageNameStr;
		
		
		String mainClassName = properties.getProperty("mainClass");
		SootClass mainClass = null;
		if (mainClassName != null) {
			mainClass = Scene.v().getSootClass(mainClassName);
			Scene.v().setMainClass(mainClass);
		}
		else
			logger.severe("provide main class");
		
		//TODO: finish
		logger.warning("finish implementation");
		
	}
	

	public void buildCallGraph(ApplInfo applInfo) {	
		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.env = applInfo.getEnv();
		unitMethods = unit.getMethods();
		this.hierarchy = hierarchy; 

		    logger.info("------------Building call graph based on SPARK PTA------------");
		    
			//Options.v().set_keep_line_number(true);
			//Options.v().set_whole_program(true);
			Options.v().setPhaseOption("cg", "verbose:false");
			
			soot.Scene.v().loadNecessaryClasses();
			soot.Scene.v().setEntryPoints(EntryPoints.v().all());
			
			setSparkPointsToAnalysis();
			
		    logger.info("------------Done Building call graph based on SPARK PTA------------");
			
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
	    logger.info("Target of: "+site+": "+result);
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


	public void setSparkPointsToAnalysis() {
		System.out.println("[spark] Starting analysis ...");
				
		HashMap opt = new HashMap();
		opt.put("enabled","true");
		opt.put("verbose","false");
		opt.put("ignore-types","false");          
		opt.put("force-gc","false");            
		opt.put("pre-jimplify","false");          
		opt.put("vta","false");                   
		opt.put("rta","false");                   
		opt.put("field-based","false");           
		opt.put("types-for-sites","false");        
		opt.put("merge-stringbuffer","true");   
		opt.put("string-constants","false");     
		opt.put("simulate-natives","true");      
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");            
		opt.put("simplify-offline","false");    
		opt.put("simplify-sccs","false");        
		opt.put("ignore-types-for-sccs","false");
		opt.put("propagator","worklist");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");         
		opt.put("double-set-new","hybrid");
		opt.put("dump-html","false");           
		opt.put("dump-pag","false");             
		opt.put("dump-solution","false");        
		opt.put("topo-sort","false");           
		opt.put("dump-types","true");             
		opt.put("class-method-var","true");     
		opt.put("dump-answer","false");          
		opt.put("add-tags","false");             
		opt.put("set-mass","false"); 		
		
		SparkTransformer.v().transform("",opt);
		
		System.out.println("[spark] Done!");
	}
}
