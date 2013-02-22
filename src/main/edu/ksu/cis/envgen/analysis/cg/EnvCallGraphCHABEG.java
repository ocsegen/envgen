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
import soot.jimple.*;
import soot.jimple.internal.*;

import edu.ksu.cis.envgen.*;

/**
 * 
 * Call graph based on Class Hierarchy Analysis (CHA).
 * 
 */
public class EnvCallGraphCHABEG extends EnvCallGraph {

	InvokeGraph invokeGraph = new InvokeGraph();
	
	public EnvCallGraphCHABEG() {}
	
	/** Set options using a config file */
	public void setOptions(Properties properties){
		
		//TODO: finish
		logger.warning("finish implementation");
		
	}
	
	
	/*-----------------------------------------------*/
	/* Build Call Graph */
	/*-----------------------------------------------*/

	public void buildCallGraph(ApplInfo applInfo) {
		this.applInfo = applInfo;
		this.unit = applInfo.getUnit();
		this.env = applInfo.getEnv();
		unitMethods = unit.getMethods();
		this.hierarchy = hierarchy; 

		logger.info("------------Building call graph based on Soot CHA------------");
		buildCHACallGraph();
		
		logger.info("------------Done Building call graph based on Soot CHA------------");
	}
	


	/**
	 * Builds a call graph starting with a dummy root at the top, unit methods
	 * at the second level down, methods that get called inside the unit methods
	 * at the 3d level down. 3d level contains methods that get called inside
	 * the unit including the ones that are part of the unit. To identify top
	 * level environment methods, go through 3d level methods of the call graph
	 * and identify those whose declaring class is not part of the unit.
	 */
	private void buildCHACallGraph() {
		SootClass internalClass;
		SootMethod internalMethod;

		logger.severe("finish implementation");

		logger.info("unitMethods: "+unitMethods);
		// EnvPrinter.printTable(unitTable);

		// create dummy method - root of the graph
		SootMethod root = new SootMethod("root", new LinkedList(), NullType.v());

		for (Iterator it = unit.getClasses().iterator(); it.hasNext();) {
			internalClass = (SootClass) it.next();
			System.out.println("internalClass "+internalClass);

			List internalList = internalClass.getMethods();
			System.out.println("internalMethods: "+internalList);
			if (internalList.isEmpty()) {
				logger.fine("CallGraph, buildCallGraph: No internal methods for class "
								+ internalClass.getName());
				continue;
			}

			// dummy site for each of the internal classes
			Stmt site = new JNopStmt();
			invokeGraph.addSite(site, root);

			for (Iterator ii = internalList.iterator(); ii.hasNext();) {
				internalMethod = (SootMethod) ii.next();
				System.out.println("internalMethod "+internalMethod);

				if ((internalMethod.isPhantom() || internalMethod.isAbstract() || internalMethod
						.isNative()))
					continue;

				 if (processTarget(internalMethod)) {
				logger.info("CallGraph, buildCallGraph: process method: "
									+ internalMethod);
				invokeGraph.addTarget(site, internalMethod);

				// targets++;

				List sites = invokeGraph.getSitesOf(internalMethod);
				if (sites.isEmpty()) {
				logger.info("CallGraph, buildCallGraph: The method "
										+ internalMethod
										+ " has no sites or not in the graph");
					recursiveBuildCallGraph(internalMethod);
				}
				// java.util.List sites = callGraph.getSitesOf(internalMethod);
				// System.out.println("\nSites of "+internalMethod+": "+sites);
				// java.util.List targets =
				// callGraph.getTargetsOf(internalMethod);
				// System.out.println("\nTargets of "+internalMethod+":
				// "+targets);
				// }
			}
			}
		}
			
	}

	/**
	 * Builds a call graph starting with a dummy root at the top, unit methods
	 * at the second level down, methods that get called inside the unit methods
	 * at the 3d level down. 3d level contains methods that get called inside
	 * the unit including the ones that are part of the unit. To identify top
	 * level environment methods, go through 3d level methods of the call graph
	 * and identify those whose declaring class is not part of the unit.
	 */
	private void buildCHACallGraphShort() {
		SootClass internalClass;
		SootMethod internalMethod;

		logger.info("----------building a CHA-based call graph");

		logger.info("unitMethods: "+unitMethods);
		// EnvPrinter.printTable(unitTable);

		// create dummy method - root of the graph
		//root = new SootMethod("root", new LinkedList(), NullType.v());

		for (Iterator it = unitMethods.iterator(); it.hasNext();) {
			//internalClass = (SootClass) it.next();
			// System.out.println("!!!!!class "+internalClass);

			//List internalList = internalClass.getMethods();
			//if (internalList.isEmpty()) {
			//	logger.fine("CallGraph, buildCallGraph: No internal methods for class "
			//					+ internalClass.getName());
			//	continue;
			//}

			// dummy site for each of the internal classes
			//Stmt site = new JNopStmt();
			//addSite(site, root);

			//for (Iterator ii = internalList.iterator(); ii.hasNext();) {
				internalMethod = (SootMethod) it.next();
				//System.out.println("!!!!!Method "+internalMethod);

				//if ((internalMethod.isPhantom() || internalMethod.isAbstract() || internalMethod
				//		.isNative()))
				//	continue;

				// if (processTarget(internalMethod)) {
				logger.info("CallGraph, buildCallGraph: process method: "
									+ internalMethod);
				//addTarget(site, internalMethod);

				// targets++;

				List sites = invokeGraph.getSitesOf(internalMethod);
				if (sites.isEmpty()) {
				logger.info("CallGraph, buildCallGraph: The method "
										+ internalMethod
										+ " has no sites or not in the graph");
					recursiveBuildCallGraph(internalMethod);
				}
				// java.util.List sites = callGraph.getSitesOf(internalMethod);
				// System.out.println("\nSites of "+internalMethod+": "+sites);
				// java.util.List targets =
				// callGraph.getTargetsOf(internalMethod);
				// System.out.println("\nTargets of "+internalMethod+":
				// "+targets);
				// }
			//}
		}

	}

	
	/**
	 * Recursive function that gets called from buildCallGraph to update the
	 * callGraph
	 */
	private void recursiveBuildCallGraph(SootMethod sm) {
		logger.info("recursiveBuildCallGraph for: " + sm);
		
		
		PrintWriter out = new PrintWriter(System.out, true);

		// load the code for the method and step through it
		// JimpleBody jb = (JimpleBody)sm.getBodyFromMethodSource("jb");
		// Body jb = sm.retrieveActiveBody();
		Body jb = null;
		if(sm.hasActiveBody())
			jb = sm.retrieveActiveBody();
		else{
			logger.severe("no active body for: "+sm);
		    //jb = (JimpleBody)sm.getBodyFromMethodSource("jb");
			//SootClass needsLoading = sm.getDeclaringClass();
			//Scene.v().loadClassAndSupport(needsLoading.getName());
			//needsLoading.setApplicationClass();
			//SootResolver.v().resolveClass(needsLoading.getName(), SootClass.BODIES);
			//hierarchy = new Hierarchy();
			//jb = sm.retrieveActiveBody();
			
		}

		

		if (logger.isLoggable(Level.FINE)) {
		
			logger.fine("\nPrinting jimple for: " + sm.toString());
			Printer.v().printTo(jb, out);
		}

		// get its code and update the graph

		PatchingChain stmtList = jb.getUnits();
		for (Iterator si = stmtList.iterator(); si.hasNext();) {
			Stmt s = (Stmt) si.next();

			// identify all possible statements that might have invoke expr

			if (s.containsInvokeExpr()) {
				// System.out.println("\n"+s);

				InvokeExpr ie = (InvokeExpr) s.getInvokeExpr();
				if (ie instanceof InstanceInvokeExpr) {
					Type receiverType = ((InstanceInvokeExpr) ie).getBase()
							.getType();

					invokeGraph.addSite(s, sm);

					logger.fine("\t\tAdded " + s + " to " + sm);
					if (receiverType instanceof RefType) {
						// System.out.println("\nResolving call");
						SootClass sc = ((RefType) receiverType).getSootClass();
						SootMethod smethod = ie.getMethod();
						List targetList = null;
						// ?? Check why dispatch fails sometimes!!!
						try {
							targetList = hierarchy.resolveAbstractDispatch(sc,
									smethod);

						} catch (Exception e) {
							System.out.println(e);
						}
						if (targetList != null) {
							Iterator targetsIt = targetList.iterator();

							while (targetsIt.hasNext()) {
								SootMethod target = (SootMethod) targetsIt
										.next();
								addTargetToSite(s, target, sm);

							}
						}
					}
				} else if (ie instanceof StaticInvokeExpr) {
					invokeGraph.addSite(s, sm);

					logger.fine("\t\tAdded " + s + " to " + sm);
					SootMethod target = ie.getMethod();
					addTargetToSite(s, target, sm);
				}
				/*
				 * //repetitition else if (ie instanceof SpecialInvokeExpr) {
				 * addSite(s, sm);
				 * 
				 * 
				 * if(debug > 0) System.out.println("\t\tAdded "+s+" to "+sm);
				 * SootMethod target =
				 * hierarchy.resolveSpecialDispatch((SpecialInvokeExpr)ie, sm);
				 * addTargetToSite(s, target, sm);
				 *  }
				 */

			}
		}
	}

	public List getTargetsOf(SootMethod sm) {
		assert(sm != null);
		
		List result = new ArrayList();
		List sites = invokeGraph.getSitesOf(sm);

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
		return result;
	}

	/**
	 * 
	 * @param site
	 * @return a collection of possible targets
	 */

	public Collection getTargetsOf(Stmt site, SootMethod sm) {

			//TODO: check why a site may not be in the graph
			try{

			return getTargetsOf(site);
			}catch(Exception e){
				logger.warning("Site is not part of invoke graph: "+sm);
				return new ArrayList();
			}

		//}
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

			try{
			return hierarchy.resolveAbstractDispatch(((RefType) receiverType)
					.getSootClass(), expr.getMethod());
			}catch(Exception e){e.printStackTrace();
			System.out.println("failed resolving: "+expr.getMethod());}
			return new ArrayList();
		
	}

	/**
	 * Checks if the <code>target</code> needs to be processed, adds it
	 * <code>site</code>, and calls <code>recursiveBuildCallGraph
	 * </code>
	 * for <code>target</code>.
	 */
	private void addTargetToSite(Stmt s, SootMethod target, SootMethod sm) {
		if (!target.getDeclaringClass().isApplicationClass()) {
			SootClass sc = (Scene.v()).getSootClass(target.getDeclaringClass()
					.getName());
			sc.setApplicationClass();
			target = sc.getMethod(target.getName(), target.getParameterTypes(),
					target.getReturnType());
		}

		// System.out.println("\t\tAdding " + target +" to "+sm);
		// if(callGraph.containsSite(s))
		// System.out.println("\t\t*******Site " + s +" is found!!!");

		// System.out.println("\t\tAdding " + target +" to "+ s);

		if (processTarget(target)) {
			
			invokeGraph.addTarget(s, target);
			numTargets++;
			// System.out.println("\t\tTargets of "+sm+":
			// "+callGraph.getTargetsOf(sm));
			// System.out.println("\t\tTargets of "+s+":
			// "+callGraph.getTargetsOf(s));
			// need to analyze each of the called methods
			List sites = invokeGraph.getSitesOf(target);
			if (sites.isEmpty()) {
				logger.fine("* * * * The method " + target
							+ " has no sites or not in the graph");
				recursiveBuildCallGraph(target);
			}

		}
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
