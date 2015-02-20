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
package edu.ksu.cis.envgen;

import java.util.HashMap;
import java.util.List;

import soot.*;
import edu.ksu.cis.envgen.analysis.cg.EnvCallGraph;
import edu.ksu.cis.envgen.applinfo.EnvHierarchy;
import edu.ksu.cis.envgen.applinfo.ModuleInfo;

public abstract class ApplInfo {
	protected ModuleInfo unit;
	protected ModuleInfo env;
	protected EnvCallGraph callGraph;
	protected EnvHierarchy envHierarchy;
	
	/** List of packages to ignore modeling */
	protected List<String> ignoredInfo;
	
	/** Additional relevant info */
	protected HashMap<String, SootMethod> relevantInfo;
	
	public ApplInfo(){}
	
	/*
	public ApplInfo(ModuleInfo unit, ModuleInfo env){
		this.unit = unit;
		this.env = env;
	}
	
	
	public ApplInfo(ModuleInfo unit, ModuleInfo env, EnvCallGraph callGraph){
		this(unit, env);
		this.callGraph = callGraph;
	}

	
	public ApplInfo(ModuleInfo unit, ModuleInfo env, EnvCallGraph callGraph, EnvHierarchy envHierarchy){
		this(unit, env, callGraph);
		this.envHierarchy = envHierarchy;
	}
	*/
	
	public void setUnit(ModuleInfo unit){
		this.unit = unit;
	}
	public ModuleInfo getUnit(){
		return unit;
	}
	public void setEnv(ModuleInfo env){
		this.env = env;
	}
	public ModuleInfo getEnv(){
		return env;
	}
	
	public void setEnvCallGraph(EnvCallGraph cg){
		this.callGraph = cg;
	}
	public EnvCallGraph getEnvCallGraph(){
		return callGraph;
	}
	
	public void setEnvHierarchy(EnvHierarchy eh){
		this.envHierarchy = eh;
	}
	public EnvHierarchy getEnvHierarchy(){
		return envHierarchy;
	}
	
	public void setRelevantInfo(HashMap<String, SootMethod> set){
		relevantInfo = set;
	}
	
	public HashMap<String, SootMethod> getRelevantInfo(){
		return relevantInfo;
	}
	
	
	public void setIgnoredInfo(List<String> ii){
		ignoredInfo = ii;
	}
	
	public List<String> getIgnoredInfo(){
		return ignoredInfo;
	}
	
	/** used to ignore modeling certain packages */
	public boolean isRelevantPackage(String p){
		if(ignoredInfo == null)
			return true;
		
		if(ignoredInfo.contains(p))
			return false;
		return true;	
	}
	
	//TODO: need 2 types of isRelevantType
	//used in PTA
	//public abstract boolean isRelevantRefType(Type type);
	//used in side-effects
	//public abstract boolean isRelevantRefOrScalarType(Type type);
	
	//used to track objects by type in points-to analysis
	public abstract boolean isRelevantType(Type type);
	
	//used to track objects by type in points-to analysis
	public abstract boolean isRelevantClass(SootClass sc);
	//used in call graph construction
	public abstract boolean isRelevantMethod(SootMethod sm);
	//used to track fields in Side-Effects analysis
	public abstract boolean isRelevantField(SootField sf);
	
	//additional methods that may be needed
	public boolean isRelevantCallBack(SootMethod sm){return false;}
	public boolean isReleventParam(Type type){return false;}
	public boolean isRelvantArray(Type elemType){return false;}
	public boolean isRelevantNew(Type type){return false;}
}
