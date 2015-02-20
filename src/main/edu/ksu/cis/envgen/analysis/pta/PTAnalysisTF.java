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

import java.util.*;
import java.util.logging.Logger;


import soot.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import edu.ksu.cis.envgen.analysis.DataFlowSet;
import edu.ksu.cis.envgen.analysis.ForwardFlowAnalysis;
import edu.ksu.cis.envgen.analysis.ValueSetPair;
import edu.ksu.cis.envgen.analysis.cg.EnvCallGraph;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.MultiSet;

/**
 * 
 * Transfer functions and merge operator for inter-procedural flow-sensitive parameterized
 * points-to analysis.
 * 
 */
public class PTAnalysisTF extends ForwardFlowAnalysis {
	
	/** Initial data flow set, empty for this analysis. */
	FlowSet initSet;

	/**
	 * Table containing unit classes. Used for identifying unit and environment
	 * components.
	 */
	// HashMap unitTable;
	/** Call graph that gets traversed in the top down order. */
	EnvCallGraph callGraph;

	/**
	 * Set that keeps track of visited methods, used to detect cycles in the
	 * call graph. When a cycle is detected, it is broken and the most general
	 * information is assumed. Foe example, for alias analysis, the method is
	 * assumed to return any location of our interest that matches the return
	 * type of method.
	 */
	HashSet visited;

	/**
	 * <code>Analysis</code> field,whose method <code>analyzeAliases()</code>
	 * gets recursively called for each of the targets at a call site.
	 */
	PTAnalysis analysis;

	PTAnalysisResults analysisResults;

	SootMethod method;

	boolean preserveCallBacks;
	
	Logger logger = Logger.getLogger("envgen.analysis.pta");

	protected Object newInitialFlow() {
		return initSet.clone();
	}

	protected Object entryInitialFlow() {
		return initSet.clone();
	}

	/**
	 * Defines flow equations by identifying transfer functions and its parts
	 * such kill and gen sets; implements generation of kill and gen sets for
	 * each of the relevant to the analysis statements. For example, for alias
	 * analysis, statements with method calls and assignment statements with
	 * locations of our interest on the right hand side are the only statements
	 * that are relevant.
	 */
	protected void flowThrough(Object inValue, Object unit, Object outValue) {
		FlowSet in = (DataFlowSet) inValue;
		FlowSet out = (DataFlowSet) outValue;
		FlowSet killSet = new DataFlowSet();
		FlowSet genSet = new DataFlowSet();
		// calculate kill and gen sets in terms of the data flow set flowing in

		if (((Stmt) unit).containsInvokeExpr())
			processCallSite((Stmt) unit, (DataFlowSet)in, genSet, killSet);

		if (unit instanceof JIdentityStmt)
			processIdentityStmt((Stmt) unit, genSet, killSet, (DataFlowSet) in);

		if (unit instanceof JAssignStmt)
			processAssignStmt((Stmt) unit, genSet, killSet, (DataFlowSet) in);

		
		logger.fine("\n\n**********Unit:     " + unit);
		// System.out.println("**********InSet: "+(DataFlowSet)in);
		// System.out.println("**********KillSet: "+killSet);
		// System.out.println("**********GenSet: "+genSet);
		
		// Perform kill
		// in.difference(killSet, in);
		// genSet.difference(killSet, genSet);

		// Perform generation
		// out.union(genSet, out);
		in.union(genSet, out);

		logger.fine("**********PointsTo flowThrough result set:\n"
					+ ((DataFlowSet) out));

		// System.out.println("@@@@@@@@@@@UnitToBeforeFlowMapping:\n"+
		// unitToBeforeFlow);
		// System.out.println("@@@@@@@@@@@UnitToAfterFlowMapping:\n"+
		// unitToAfterFlow);
	}

	/**
	 * Defines merge operator of the analysis; the merge operator is used to
	 * calculate information flowing into the node by combining data flow
	 * information flowing out of its predecessor nodes. For alias analysis
	 * union is used as a merge operator.
	 */
	protected void merge(Object in1, Object in2, Object out) {
		logger.fine("\n------Merging\n" + (FlowSet) in1
					+ "\n-----with\n" + (FlowSet) in2);
		FlowSet inSet1 = (FlowSet) in1;
		FlowSet inSet2 = (FlowSet) in2;
		FlowSet outSet = (FlowSet) out;

		inSet1.union(inSet2, outSet);
		logger.fine("outSet: " + (FlowSet) outSet);
	}

	/**
	 * Copies elements of <code>source</code> data flow set into
	 * <code>dest</code> data flow set.
	 */
	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source;
		FlowSet destSet = (FlowSet) dest;
		sourceSet.copy(destSet);
	}

	/**
	 * Generates aliases for identity statements, that are part of the jimple
	 * grammar.
	 */
	protected void processIdentityStmt(Stmt unit, FlowSet genSet,
			FlowSet killSet, DataFlowSet in) {
		SymLocValue symLoc; // symbolic location
		MultiSet set; // set holding symbolic locations
		ValueSetPair pair; // mapping that goes into the dataflowset
		Value lhs = ((JIdentityStmt) unit).getLeftOp();
		Value rhs = ((JIdentityStmt) unit).getRightOp();

		// keep track of parameters that are of RefType and ArrayType with
		// elements of the super unit type
		if (lhs instanceof JimpleLocal && rhs instanceof IdentityRef) {
			Type paramType = rhs.getType();

			if (analysis.processRefType(paramType)) {
				Root root = null;
				if (rhs instanceof ThisRef)
					root = new RootThis(rhs, paramType);

				else if (rhs instanceof ParameterRef)
					root = new RootParam(rhs, paramType);
				symLoc = new SymLocPath(root, paramType);
						
				set = new MultiSet();
				set.add(symLoc);
				pair = new ValueSetPair(lhs, set);
				logger.fine("*********GenSet: " + pair);

				in.add(pair, in);
			}
		}
	}

	protected void processAssignStmt(Stmt unit, FlowSet genSet,
			FlowSet killSet, DataFlowSet in) {
		Value lhs = ((JAssignStmt) unit).getLeftOp();
		Value rhs = ((JAssignStmt) unit).getRightOp();
		MultiSet locs = null; // set holding symbolic locations
		ValueSetPair pair; // mapping that goes into the dataflowset

		logger.fine("\nPointsTo Analysis assignment unit: " + unit);

		// if these are not objects or arrays, no need to track points-to for
		// them
		// since there will be no side effects to these locations
		Type type = lhs.getType();
		if (!analysis.processRefType(type)) {
			return;
		}
		// no need to keep track of complex reference variables
		// as they never serve as dereference varibales in the 3-address code

		// if(lhs instanceof JimpleLocal || lhs instanceof InstanceFieldRef ||
		// lhs instanceof ArrayRef)
		if (lhs instanceof JimpleLocal) {

			logger.fine("\nPointsTo Analysis copy statement: "
						+ unit);

			// if left hand side is in the set, then kill a pair
			// the kill set is not calculated, since "add" overwrites
			// the previous binding
			/*
			 * if(((DataFlowSet)in).containsKey(lhs) ) { if(debug > 1)
			 * System.out.println("\nPointsTo Analysis kill unit: "+unit);
			 * MultiSet set = new
			 * MultiSet((MultiSet)((DataFlowSet)in).get(lhs)); pair = new
			 * ValueSetPair(lhs, set); killSet.add(pair, killSet); if(debug > 1)
			 * System.out.println("killSet: "+killSet); }
			 */
			// get symbolic locations of the rhs
			if (rhs instanceof JimpleLocal) {
				locs = getLocsOfLocal(rhs, in);
			} else if (rhs instanceof StaticFieldRef) {
				locs = getLocsOfStaticFieldRef(rhs, in);
			} else if (rhs instanceof JInstanceFieldRef) {
				locs = getLocsOfInstanceFieldRef(rhs, in);
			} else if (rhs instanceof ArrayRef) {
				locs = getLocsOfArrayRef(rhs, in);
			} else if (rhs instanceof NullConstant) {
				locs = getLocsOfNull(rhs, in);
			} else if (rhs instanceof NewExpr) {
				locs = getLocsOfNewExpr(unit, rhs, in);
			} else if (rhs instanceof NewArrayExpr) {
				locs = getLocsOfNewArrayExpr(unit, rhs, in);
			} else if (rhs instanceof NewMultiArrayExpr) {
				locs = getLocsOfNewMultiArrayExpr(unit, rhs, in);
			} else if (rhs instanceof CastExpr) {
				locs = getLocsOfCastExpr(rhs, in);
			} else if (rhs instanceof InvokeExpr) {
				locs = getLocsOfInvokeExpr(unit, rhs, in);
			} else if (rhs instanceof StringConstant) {
				// ignore, not interested in se to these (?)
				locs = getLocsOfStringConst(rhs, in);

			} else {
				
				logger.severe("PointsToAnalysis, processAssignStmt: unhandled rhs: "
								+ rhs);
			}
			if (locs != null && !locs.isEmpty()) {
				pair = new ValueSetPair(lhs, locs);
				// genSet.add(pair, genSet);
				logger.fine("*********GenSet: " + pair);
					// overwrite previous binding for lhs
					// by directly inserting gen into in
					// this takes care of kill
				logger.fine("PointsToAnalysis adding new pair: "
							+ pair);
				
				in.add(pair, in);
				// if(debug > 1)
				// System.out.println("unit: " +unit + ", genSet: "+genSet);

			} else {
				// due to soot's dummy pass, this should be ok
				// EnvPrinter.error(
				// "PointsToAnalysis, processAssignStmt: no locations for: "
				// + rhs);
			}
		} else if (lhs instanceof JInstanceFieldRef) {
			updateHeapFieldRef(lhs, in);

		} else if (lhs instanceof ArrayRef) {
			updateHeapArrayRef(lhs, in);

		} else

		// EnvPrinter.error("PointsToAnalysis, processAssignStmt: unhandled lhs:
		// "+lhs);
		logger.fine("PointsToAnalysis, processAssignStmt: unhandled lhs: "
							+ lhs);
	}

	/**
	 * Returns symbolic locations of interest referred by a local.
	 */
	protected MultiSet getLocsOfLocal(Value rhs, DataFlowSet in) {
		MultiSet set = null; // set holding symbolic locations
		logger.finer("Local on the lhs");
		// if rhs is in the set, then generate set
		if (in.containsKey(rhs)) {
			logger.finer("\nPointsTo Analysis pointer to location of interest");
			// need to create a copy of this set
			set = new MultiSet((MultiSet) in.get(rhs));
		} else {
			logger.finer("PointsToAnalysis, getLocsOfLocal: dummy pass");
		}
		return set;
	}

	/**
	 * Returns symbolic locations of interest referred to by an InstanceField.
	 */
	protected MultiSet getLocsOfInstanceFieldRef(Value rhs, DataFlowSet in) {
		MultiSet set = new MultiSet(); // set holding symbolic locations
		SymLoc symLoc = null; // symbolic location

		logger.finer("\nInstance field ref on the right hand side of assignment");
		SootField field = ((JInstanceFieldRef) rhs).getField();
		Type fieldType = field.getType();
		Value base = ((JInstanceFieldRef) rhs).getBase();

		if (analysis.processRefType(fieldType)) {
			// if base in the set, then it's location of our interest
			if (in.containsKey(base)) {
				logger.finer("\nAlias Analysis: pointer to base unit");
				// get the symbolic locations for the base
				MultiSet aset = (MultiSet) in.get(base);
				SymLoc temp = null;
				Accessor accessor = null;
				for (Iterator ai = aset.iterator(); ai.hasNext();) {
					temp = (SymLoc) ai.next();
					accessor = new Accessor(field, fieldType);
					symLoc = analysis.getExtendedLoc(temp, accessor);
					set.add(symLoc);
				}
			} else {
				// the field is in the unit, base is not in the aliases table
				logger.finer("AliasAnalysis, getLocsOfInstanceFieldRef: dummy pass");
			}
		} else {
			symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, fieldType);
			set.add(symLoc);
		}
		return set;
	}

	/**
	 * Returns symbolic locations referred to by array expression.
	 */
	protected MultiSet getLocsOfArrayRef(Value rhs, DataFlowSet in) {
		MultiSet set = new MultiSet(); // set holding symbolic locations
		SymLoc symLoc = null;

		logger.fine("ArrayRef on rhs: "+rhs);

		Value base = ((ArrayRef) rhs).getBase();
		Value index = ((ArrayRef) rhs).getIndex();
		Type baseType = base.getType();
		Type elemType = ((ArrayType) baseType).baseType;
		
		logger.fine("Base type: "+baseType);
		logger.fine("Elem type: "+elemType);

		if (analysis.processRefType(elemType)) {
			if (in.containsKey(base)) {
				logger.fine("\nPointsTo Analysis base: " + base
							+ " in the table");
				// pull out all symbolic locations for this alias

				MultiSet aset = (MultiSet) in.get(base);
				logger.finer("\nAlias set for base: " + aset);

				SymLoc temp = null;
				Accessor accessor = null;
				for (Iterator ai = aset.iterator(); ai.hasNext();) {
					temp = (SymLoc) ai.next();
					accessor = new Accessor(0, elemType);
					symLoc = analysis.getExtendedLoc(temp, accessor);
					set.add(symLoc);
				}
			} else {
				// elem in the unit, base is not in the aliases table
				logger.finer("AliasAnalysis, getLocsOfArrayRef: dummy pass");
			}
		} else {
			//TODO: do we need to record the env loc?
			//TODO: elemType or baseType?
			//symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, baseType);
			symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, elemType);
			set.add(symLoc);
		}
		return set;
	}

	/**
	 * Returns symbolic locations referred to by a static field.
	 */
	protected MultiSet getLocsOfStaticFieldRef(Value rhs, DataFlowSet in) {
		MultiSet set = new MultiSet(); // set holding symbolic locations
		SymLoc symLoc; // symbolic location
		// check if this a static field of the unit
		SootField sf = ((StaticFieldRef) rhs).getField();
		Type fieldType = sf.getType();
		SootClass sc = sf.getDeclaringClass();
		Type baseType = sc.getType();
		if (analysis.processRefType(fieldType)) {
			logger.finer("\nPointsTo Analysis pointer to unit static field unit");
			// record this reference
			Root root = new RootGlobal(rhs, fieldType);
			symLoc = new SymLocPath(root, fieldType);

		} else
			symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, fieldType);

		set.add(symLoc);
		return set;
	}

	/**
	 * Returns a newly created symbolic location if it is of interest to the
	 * analysis.
	 */
	protected MultiSet getLocsOfNewExpr(Stmt unit, Value rhs, DataFlowSet in) {
		MultiSet set = new MultiSet(); // set holding symbolic locations
		SymLoc symLoc; // symbolic location
		logger.finer("Creation Cite");
		Type refType = ((NewExpr) rhs).getBaseType();

		// if(unitTable.containsKey(refType.toString()))
		if (analysis.processRefType(refType)) {
			logger.fine("Creation cite for unit type: "+refType);

			RootNewObject root = new RootNewObject(rhs, refType);
			root.setAllocationSite(unit);
			
			//if (refType.toString().indexOf('$') > 0)
			//	root.setIsInnerLoc(true);
				
			symLoc = new SymLocPath(root, refType);

			// create a new symbolic location recording the type and
			// creationIndex ???
			MultiSet newLocations = analysisResults.getNewLocationsOf(method
					.getSignature());
			// check if the set already contains the new object
			logger.finer("new locations:" + newLocations);
			if (!newLocations.contains(symLoc)) {
				logger.finer("inserting new  location: " + symLoc);
				int creationIndex = newLocations.size();
				root.setCreationIndex(creationIndex);
				newLocations.add(symLoc);
			} else {
				logger.finer("repeat new location: " + symLoc);
				// set it to be a summary node
				SymLocPath repeat = (SymLocPath) newLocations.get(symLoc);
				((RootNewObject) repeat.getRoot()).setSummaryNode(true);
				repeat.setSingular(false);

			}
		} else
			symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, refType);

		set.add(symLoc);
		return set;
	}

	protected MultiSet getLocsOfNewArrayExpr(Stmt unit, Value rhs,
			DataFlowSet in) {
		MultiSet set = new MultiSet(); // set holding symbolic locations
		SymLoc symLoc; // symbolic location

		logger.fine("Array creation cite: "+unit);
		
		Type baseType = ((NewArrayExpr) rhs).getType();
		Type elemType = ((NewArrayExpr) rhs).getBaseType();
		
		logger.fine("NewArrayRef type: " + baseType);
		logger.fine("Element type: " + elemType);
		
		// if(unitTable.containsKey(elemType.toString()))
		if (analysis.processRefType(elemType)) {
			logger.finer("Unit type creation cite");

			// create a new symbolic location recording the type and
			// creationIndex

			RootNewObject root = new RootNewObject(rhs, baseType);
			root.setAllocationSite(unit);
			symLoc = new SymLocPath(root, baseType);
			logger.finer("*************SymLoc:" + symLoc);

			// figure out the creation index

			MultiSet newLocations = analysisResults.getNewLocationsOf(method
					.getSignature());

			logger.finer("newLocations: " + newLocations);

			if (!newLocations.contains(symLoc)) {
				logger.finer("inserting new  location: " + symLoc);
				int creationIndex = newLocations.size();
				root.setCreationIndex(creationIndex);
				newLocations.add(symLoc);
				logger.finer("newLocations: " + newLocations);

			} else {
				logger.finer("repeat new location: " + symLoc);
				// set it to be a summary node
				SymLocPath repeat = (SymLocPath) newLocations.get(symLoc);
				((RootNewObject) repeat.getRoot()).setSummaryNode(true);

			}

		} else
			symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, baseType);

		set.add(symLoc);
		return set;
	}

	protected MultiSet getLocsOfNewMultiArrayExpr(Stmt unit, Value rhs,
			DataFlowSet in) {
		MultiSet set = new MultiSet(); // set holding symbolic locations
		SymLoc symLoc = null; // symbolic location

		logger.fine("Array creation cite");
		Type baseType = ((NewMultiArrayExpr) rhs).getType();
		logger.fine("NewMultiArrayRef type: " + baseType);

		ArrayType arrayType = ((NewMultiArrayExpr) rhs).getBaseType();

		Type elemType = arrayType.baseType;

		logger.finer("element type: " + elemType);
		// if(unitTable.containsKey(elemType.toString()))
		if (analysis.processRefType(elemType)) {
			logger.finer("Unit type creation cite");
			//EnvPrinter
			//		.error("PointsToAnalysis, gotLocsOfNewMultiArrayExpr: unhandled unit case");
			/*
			 * //create a new symbolic location recording the type and
			 * creationIndex
			 * 
			 * Root root = new Root(rhs, baseType); //figure out the creation
			 * index
			 * 
			 * MultiSet newLocations =
			 * callGraph.getNewLocationsOf(method.getSignature()); int
			 * creationIndex = newLocations.size();
			 * root.setCreationIndex(creationIndex);
			 * 
			 * symLoc = new SymLoc(root, baseType); newLocations.add(symLoc);
			 */
		} else
			symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, baseType);

		set.add(symLoc);
		return set;

	}

	/**
	 * Returns null symbolic location.
	 */
	protected MultiSet getLocsOfNull(Value rhs, DataFlowSet in) {
		MultiSet set = new MultiSet();
		Root root = new RootNull(rhs, NullType.v());
		SymLoc symLoc = new SymLocPath(root, NullType.v());
		set.add(symLoc);
		return set;
	}

	/**
	 * Returns environment location.
	 */
	protected MultiSet getLocsOfStringConst(Value rhs, DataFlowSet in) {
		MultiSet set = new MultiSet();
		// Root root = new Root(rhs);
		// root.setRootType(NullType.v());
		// SymLoc symLoc = new SymLoc(root, NullType.v());
		SymLoc symLoc = new SymLocTop(SymLocTop.ENVIRONMENT_LOC, rhs.getType());
		set.add(symLoc);
		return set;
	}

	protected MultiSet getLocsOfCastExpr(Value rhs, DataFlowSet in) {
		MultiSet set = null; // set holding symbolic locations
		logger.finer("Cast Expr on the rhs");
		Value ref = ((CastExpr) rhs).getOp();
		Type castType = ((CastExpr) rhs).getCastType();
		// if rhs is in the set, then generate set
		if (in.containsKey(ref)) {
			logger.finer("\nPointsTo Analysis pointer to location of interest");
			// need to create a copy of this set
			MultiSet aliases = (MultiSet) in.get(ref);
			logger.finer("Aliases: " + aliases);
			set = new MultiSet();
			SymLoc temp;
			for (Iterator ai = aliases.iterator(); ai.hasNext();) {
				temp = (SymLoc) ai.next();
				temp.setType(castType);

				logger.finer("&&&&&&&&&&&&&&&&&&Setting cast type for: "
									+ temp + " to " + castType);
				temp.setCastType(castType);
				logger.finer("Result of casting: " + temp);
				set.add(temp);
			}
		} else {
			logger.finer("AliasAnalysis, getLocsOfCastExpr: Dummy pass");
		}
		return set;

	}

	protected MultiSet getLocsOfInvokeExpr(Stmt unit, Value rhs, DataFlowSet in) {

		MultiSet set = null; // set holding symbolic locations
		SymLoc symLoc; // symbolic location
		Type returnType;

		logger.finer("PointsToAnalysis: InvokeExpr on rhs: " + rhs);
		InvokeExpr ie = (InvokeExpr) ((Stmt) unit).getInvokeExpr();
		returnType = ie.getMethod().getReturnType();

		if (ie instanceof InstanceInvokeExpr) {
			Value receiver = ((InstanceInvokeExpr) ie).getBase();
			if (!in.containsKey(receiver))
				// dummy pass
				return set;
		}
		set = new MultiSet();
		Collection targets = callGraph.getTargetsOf((Stmt) unit, method);

		logger.finer("targets: " + targets);
		if (targets.isEmpty()) {
			symLoc = new SymLocTop(SymLocTop.UNKNOWN_LOC, returnType);
			set.add(symLoc);
			return set;
		}

		for (Iterator ti = targets.iterator(); ti.hasNext();) {
			SootMethod called = (SootMethod) ti.next();
			if (called.isPhantom() || called.isNative() || called.isAbstract())
				continue;

			returnType = called.getReturnType();

			// incorporate info about the return locations
			MultiSet calledReturns = analysisResults
					.getReturnLocationsOf(called.getSignature());
			logger.finer("\nPointsToAnalysis: Return locations of: "
						+ called + ": " + calledReturns);

				// map return locations onto callee
			logger.finer("in data flow set: " + in);
			
			set.addAll(analysis.getMappedReturnLocs(unit, calledReturns, in));
		}

		// if(set.isEmpty())
		// EnvPrinter.error("PointsToAnalysis, getLocsOfInvokeExpr: empty
		// returns");
		return set;
	}

	protected void processCallSite(Stmt unit, DataFlowSet in, FlowSet genSet, FlowSet killSet) {
		// get targets of this site from the callGraph

		logger.fine("Processing call site: " + unit);

		// if (!callGraph.containsSite((Stmt) unit))
		// EnvPrinter.error(
		// "AliasAnalysis: Site " + (Stmt) unit + " is not in the graph");

		Collection targets = callGraph.getTargetsOf((Stmt) unit, method);
		logger.fine("With targets: " + targets);

		for (Iterator ti = targets.iterator(); ti.hasNext();) {
			SootMethod called = (SootMethod) ti.next();
			if (called.isPhantom() || called.isNative() || called.isAbstract())
				continue;
			
	
			if(preserveCallBacks){
				preserveCallBacks(in, method, unit, called);
			}
			
			
			// if alias summary info not available, call recursion
			// but only if this method hasn't been visited yet
			if (!analysisResults.getMethodToAliases().containsKey(called)) {
				// if(visited.contains(called))
				// this a cycle in the graph
				// taken care by analyzeAliases method
				logger.fine("\n****AliasAnalysis: Invoke Stmt: "
							+ unit + "\nCalled Method: " + called
							+ "\nVisited: " + visited);
				analysis.analyzeAliases(called, visited);
			} else {
				// no need to incorporate info
			}
		}
	}

	protected void preserveCallBacks(DataFlowSet in, SootMethod sm, Stmt unit, SootMethod called){
		if(analysis.processMethod(called)){
			
			logger.fine("CallBack: "+called+"\nattached to: "+sm);
			
			InvokeExpr ie = (InvokeExpr) ((Stmt) unit).getInvokeExpr();
			
			//get receiver and calculate its SymLocs
			MultiSet symReceiver = null;
			if (ie instanceof InstanceInvokeExpr) {
				Value receiver = ((InstanceInvokeExpr) ie).getBase();
				//if (!in.containsKey(receiver))
				symReceiver = (MultiSet)in.get(receiver);
				//logger.info("Receiver symlocs: "+symReceiver);
			}
			
			//get params and get their SymLocs
			List symArgs = new ArrayList();
			List args = ie.getArgs();
			//logger.info("args: "+args);
			Value tempArg;
			MultiSet tempSymLocs;
			for(Iterator ai = args.iterator(); ai.hasNext();){
				tempArg = (Value)ai.next();
				tempSymLocs = (MultiSet)in.get(tempArg);
				//logger.info("symLocArgs: "+tempSymLocs);
				symArgs.add(tempSymLocs);
			}
			
			String methodSignature = sm.getSignature();
			MultiSet callBacks = analysisResults.getCallBacksOf(methodSignature);
			//SymLocValue callBackValue = new CallBackValue(called, unit);
			SymLocValue callBackValue = new CallBackValue(symReceiver, called, symArgs);
			callBacks.add(callBackValue);
			//logger.info("CallBack: "+callBackValue+"\nattached to: "+sm);
			
		}
	}
	
	
	protected void updateHeapFieldRef(Value lhs, DataFlowSet in) {
		logger.finer("heap update through field reference: " + lhs);
		SootField field = ((JInstanceFieldRef) lhs).getField();
		Type fieldType = field.getType();
		Value base = ((JInstanceFieldRef) lhs).getBase();

		// field has to be in the unit
		if (analysis.processRefType(fieldType)) {
			// if base in the set, then it's location of our interest
			// the base has to be in the unit
			if (in.containsKey(base)) {

				// get the symbolic locations for the base
				MultiSet locs = (MultiSet) in.get(base);
				logger.finer("locations: " + locs);

				// check if this is a singular location
				if (analysis.isSingularLoc(locs)) {
					//TODO: finish
					//logger.severe("unhandled strong update through field reference: "
					//					+ lhs);

					logger.fine("unhandled strong update through field reference: "
											+ lhs);
					// find out locations whose prefix has changed and set them
					// to choose

				} else {

					// perform weak update, which amounts to no update
				}

			}
		}
	}

	protected void updateHeapArrayRef(Value lhs, DataFlowSet in) {
		Value base = ((ArrayRef) lhs).getBase();
		Value index = ((ArrayRef) lhs).getIndex();
		Type baseType = base.getType();
		Type elemType = ((ArrayType) baseType).baseType;

		if (analysis.processRefType(elemType)) {
			if (in.containsKey(base)) {
				// get the symbolic locations for the base
				MultiSet locs = (MultiSet) in.get(base);
				logger.finer("locations: " + locs);

				// check if this is a singular location
				if (analysis.isSingularLoc(locs)) {
					
					//TODO: finish
					//logger.severe("unhandled strong update through array reference: "
					//					+ lhs);
					
					logger.fine("unhandled strong update through array reference: "
							+ lhs);

					// find out locations whose prefix has changed and set them
					// to choose

				} else {

					// perform weak update, which amounts to no update
				}
			}
		}

	}

	/**
	 * Constructs an AliasAnalysis instance, sets its fields and launches the
	 * analysis calculation.
	 */
	PTAnalysisTF(UnitGraph g, SootMethod method, FlowSet initSet,
			EnvCallGraph callGraph, PTAnalysisResults pointsToResults,
			HashSet visited, PTAnalysis analysis) {
		
		super(g);
		
		this.method = method;
		if (initSet == null)
			logger.severe("Alias Analysis: initial set null");

		this.initSet = initSet;
		this.callGraph = callGraph;
		this.analysisResults = pointsToResults;
		this.visited = visited;
		this.analysis = analysis;
		this.preserveCallBacks = analysis.getPreserveCallBacks();
		// this.packageAnalysis = packageAnalysis;

		doAnalysis();
	}
	
	PTAnalysisTF(UnitGraph g) {
		
		super(g);
	}
}
