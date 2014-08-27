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

import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.applinfo.*;

/**
 * 
 * Mappings from callsites to their possible targets, from targets to their
 * callsites.
 */
public abstract class EnvCallGraph /* extends InvokeGraph */{

	ModuleInfo unit;

	/** List of analyzable unit methods. */ 
	List unitMethods;
	
	ModuleInfo env;
	
	ApplInfo applInfo;
	
	CallGraph callGraph = null;
	
	/** List of analyzable environment methods immediately called from the unit.*/
	//List envMethods;

	/**
	 * Root of the call graph, an artificial dummy node needed for a common
	 * start.
	 */
	//SootMethod root;

	/** Class Hierarchy needed for virtual method call resolution. */
	Hierarchy hierarchy;

	protected Properties properties;

	int numTargets;

	int numMethods;
	
	Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis.cg");
	
	public abstract void setOptions(Properties properties);
	
	public abstract void buildCallGraph(ApplInfo applInfo);
	
	public abstract List getTargetsOf(SootMethod sm);

	public abstract  Collection<SootMethod> getTargetsOf(Stmt site, SootMethod sm);
	
	public abstract Collection<SootMethod> getTargetsOf(Stmt site, InvokeExpr expr,
			Type receiverType, SootMethod sm);
	
	public abstract  List<SootMethod> getTargetsOf(Stmt site);
	
	public abstract List getSitesOf(SootMethod sm); 
	
	// TODO: implement transitive closure
	public List<SootMethod> getCallersOf(SootMethod sm) {

		List<SootMethod> result = new ArrayList<SootMethod>();
		Iterator<Edge> callers = callGraph.edgesInto(sm);

		SootMethod caller;
		Edge edge;
		while (callers.hasNext()) {
			edge = callers.next();
			caller = edge.src();
			System.out.println("caller of: " + sm + "   :   " + caller);
			result.add(caller);
		}

		return result;
	}
	

	/**
	 * 
	 * Quick package test: if the environment includes only lib classes, the
	 * unit doesn't include any lib classes, and none of the unit classes
	 * inherit from the env classes (except for Object), then the env method
	 * can't see any of the fields of the unit classes. No need to process such
	 * target.
	 * 
	 */
	public boolean processTarget(SootMethod target) {

		if ((target.isPhantom() || target.isAbstract() || target.isNative()))
			return false;


		// check if this is a unit class
		//if (unit.containsClass(className)) {
		//	logger.fine("************" + declClass
		//				+ " is a unitType");
		//	return true;
		//}

		// check if this class is a supertype of any of the unit classes
		//if (unit.isModuleSuperType(declClass)) {
		//	logger.fine("************" + declClass
		//				+ " is a unitSuperType");
		//	return true;
		//}
		
		
		return applInfo.isRelevantMethod(target);

		// what follows is the coarse implementation of package analysis
		// that works well only for user-defined unit

		//if (unit.includesLibClasses())
		//	return true;

		//if (Util.isLibraryMethod(target))
		//	return true;

		//return true;
	}


	/**
	 * Prints the graph in terms of methods and call sites they contain.
	 */

	public void printGraph() {
		HashSet visited = new HashSet();
		visited.addAll(unitMethods);
		//List targets = getTargetsOf(root);
		System.out.println("unit Methods: " + unitMethods);
		for (Iterator ti = unitMethods.iterator(); ti.hasNext();)
			recursivePrintGraph((SootMethod) ti.next(), visited);
		numMethods = visited.size();

	}

	/**
	 * Recursive method that is used to walk the graph tree.
	 * 
	 */
	private void recursivePrintGraph(SootMethod sm, HashSet visited) {
		visited.add(sm);
		// if(debug > 0)
		// System.out.println("\nContainer: "+sm);
		List sites = getSitesOf(sm);
		System.out.println("\tSites:" + sites);
		for (Iterator si = sites.iterator(); si.hasNext();) {
			Stmt s = (Stmt) si.next();
			// if(debug > 0)
			System.out.println("\tSite " + s + " with targets:\n"
					+ getTargetsOf(s));
		}
		for (Iterator si = sites.iterator(); si.hasNext();) {
			Stmt s = (Stmt) si.next();
			/*
			 * if(callGraph.containsSite(s))
			 * System.out.println("!!!!!!!!!!!contains site "+s); else
			 * System.out.println("!!!!!!!!!!!doesn't contain site "+s);
			 */
			List targets = getTargetsOf(s);
			for (Iterator ti = targets.iterator(); ti.hasNext();) {
				SootMethod target = (SootMethod) ti.next();
				if (!visited.contains(target))
					recursivePrintGraph(target, visited);
				else {
					// if(debug > 0)
					// System.out.println("\nContainer "+target+" already
					// visited");
				}
			}
		}
		// visited.remove(sm);

	}

	/*
	public List getTargetsOfRoot() {
		return getTargetsOf(root);
	}
	*/
	
	public List<SootMethod> getAllEntryMethods(){
		List<SootMethod> result = new ArrayList<SootMethod>();
		SootClass internalClass = null;
		SootMethod internalMethod = null;
		
		for (Iterator<SootClass> it = unit.getClasses().iterator(); it.hasNext();) {
			internalClass = it.next();
			List<SootMethod> methodList = internalClass.getMethods();
			
			for(Iterator<SootMethod> im = methodList.iterator(); im.hasNext();){
				internalMethod = im.next();
				//we do not analyze entry methods that are abstract, native or phantom
				//TODO: find implementations of abstract methods?
				if(internalMethod.isConcrete()){
					result.add(internalMethod);
				}
			}
			
			//result.addAll(methodList);
		}
		
		return result;
		
	}

	public List<SootMethod> getMainEntryMethods(List<SootMethod> methods) {
		List<SootMethod> result = new ArrayList<SootMethod>();
		SootMethod temp;
		for (Iterator<SootMethod> it = methods.iterator(); it.hasNext();) {
			temp = it.next();
			if (temp.getName().equals("main"))
				result.add(temp);
		}
		if (result.isEmpty())
			logger.severe("getEntryMethods: no entry methods");
		logger.info("entry methods: " + result);
		return result;
	}
	
	public CallGraph getCallGraph(){
		return callGraph;
	}
	
	public List getEnvMethods() {
		
		List envMethods = new ArrayList();
		//List secondLevel = getTargetsOfRoot();
		for (Iterator si = unitMethods.iterator(); si.hasNext();) {
			List thirdLevel = getTargetsOf((SootMethod) (si.next()));
			envMethods.addAll(thirdLevel);
		}
		// marked methods are the top level environment methods
		envMethods.removeAll(unitMethods);
		// if (debug > 0)
		logger.fine("-----------Top Level Environment methods: "
				+ envMethods);
		return envMethods;
	}


	public void getStatistics() {
		// Chain contextClasses = (Scene.v()).getContextClasses();
		Chain contextClasses = (Scene.v()).getClasses();
		Chain applClasses = (Scene.v()).getApplicationClasses();
		Chain classes = (Scene.v()).getClasses();
		Chain libClasses = (Scene.v()).getLibraryClasses();

		System.out.println("\nStats: Scene classes: " + classes);
		System.out.println("\nStats: Scene context classes: " + contextClasses);
		System.out.println("\nStats: Scene appl classes: " + applClasses);
		System.out.println("\nStats: Scene lib classes: " + libClasses);

		System.out.println("\nStats: Scene classes size: " + classes.size());
		System.out.println("\nStats: Scene context classes size: "
				+ contextClasses.size());
		System.out.println("\nStats: Scene appl classes size: "
				+ applClasses.size());
		System.out.println("\nStats: Scene lib classes size: "
				+ libClasses.size());

		System.out.println("Stats: numMethods: " + numMethods);
		System.out.println("Stats: Targets inserted: " + numTargets);

		/*
		 * System.out.println("***************"); Chain loaded =
		 * (Scene.v().getClasses()); for(Iterator li = loaded.iterator();
		 * li.hasNext();) { SootClass l = (SootClass)li.next();
		 * if(l.isContextClass()) System.out.println(l+" is context");
		 * if(l.isApplicationClass()) System.out.println(l+" is application");
		 * if(l.isLibraryClass()) System.out.println(l+" is library");
		 *  }
		 */
	}
	
	public String toString(){
		return callGraph.toString();
	}

}
