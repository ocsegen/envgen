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
import java.util.logging.Level;
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
import edu.ksu.cis.envgen.analysis.pta.PTAnalysisTF;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.MultiSet;

/**
 * Implementation of interprocedural,
 * flow-sensitive, parameterized side-effects analysis.  
 * Defines transfer functions.
 *
 */
public class SEAnalysisTF extends ForwardFlowAnalysis {
	/** Initial data flow set, empty for this analysis. */
	protected FlowSet initSet;

	/**
	 * Alias analysis results that are used by the side-effects
	 * analysis to resolve aliases.
	 */
	protected PTAnalysisTF alias;

	/** Call graph that gets traversed in the top down order. */
	protected EnvCallGraph callGraph;

	protected SEAnalysisResults seAnalysisResults;

	/** Set that keeps track of visited methods, used to detect cycles
	 * in the call graph. When a cycle is detected, it is broken and the most general
	 * information is assumed.  For example, for side-effects analysis, the method is
	 * assumed to modifies all possible locations of our interest.
	 */
	protected HashSet visited;
	protected SootMethod method;

	/** <code>Analysis</code> field,whose method <code>analyzeSideEffects()</code>
	 * gets recursively called for each of the targets at a call site.
	 */
	protected SEAnalysis analysis;

	protected boolean mustSE;
	
	protected Logger logger = Logger.getLogger("envgen.analysis.data");

	/**
	 * Initially data flow set is empty.
	 */
	protected Object newInitialFlow() {
		return initSet.clone();
	}

	protected Object entryInitialFlow() {
		return initSet.clone();
	}

	/**
	 * Implementation of transfer functions.  
	 */
	protected void flowThrough(Object inValue, Object unit, Object outValue) {
		FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;
		//generate kill and gen sets
		FlowSet killSet, genSet;

		killSet = new DataFlowSet();
		genSet = new DataFlowSet();

		if (((Stmt) unit).containsInvokeExpr()) {
			//sanity check
			//if (!callGraph.containsSite((Stmt) unit))
			//	EnvPrinter.error("Site " + unit + " is not in the graph");

			processCallSite((Stmt) unit, genSet, killSet, (DataFlowSet) in);
		}

		if (unit instanceof JAssignStmt) {
			// we are interested in assignment statements that have a field ref 
			// and array access on the lhs
			processAssignStmt((Stmt) unit, genSet, killSet, (DataFlowSet) in);
		}

		
		logger.fine("\n\n**********Unit:     " + unit);
		logger.fine("**********OutSet:    " + (DataFlowSet) in);
			//System.out.println("**********KillSet:  "+killSet);
			//System.out.println("**********GenSet:   "+genSet);
		
		// Perform kill
		//in.difference(killSet, out);

		// Perform generation
		in.union(genSet, out);
		logger.fine(
				"\n**********Modifications flowThrough result: "
					+ ((DataFlowSet) out));
	}

	/**
	 * Merge operator.
	 */
	protected void merge(Object in1, Object in2, Object out) {
		logger.fine("Merge!");
		FlowSet inSet1 = (FlowSet) in1;
		FlowSet inSet2 = (FlowSet) in2;
		FlowSet outSet = (FlowSet) out;

		if (mustSE) {
			logger.fine("Intersection! Out set: " + outSet);
			
			inSet1.intersection(inSet2, outSet);
		} else {
			logger.fine("Union!");
			inSet1.union(inSet2, outSet);
		}
	}

	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source;
		FlowSet destSet = (FlowSet) dest;
		sourceSet.copy(destSet);
	}

	public void processAssignStmt(
		Stmt unit,
		FlowSet genSet,
		FlowSet killSet,
		DataFlowSet in) {
		Value lhs = ((JAssignStmt) unit).getLeftOp();
		Value rhs = ((JAssignStmt) unit).getRightOp();
		MultiSet locs = null;
		MultiSet values;
		SootField field;
		Type modifiedType = null;
		ValueSetPair pair;

		logger.fine("\nSideEffects assignment unit: " + unit);

		//get alias infor for this statement
		DataFlowSet aliasSet = (DataFlowSet) alias.getFlowBefore(unit);
		logger.fine("\nPointsTo Data before the unit: " + aliasSet);

		if (lhs instanceof InstanceFieldRef) {
			field = ((InstanceFieldRef) lhs).getField();
			modifiedType = field.getType();
			locs = getLocsOfInstanceFieldRef(lhs, aliasSet);
		}
		if (lhs instanceof StaticFieldRef) {

			field = ((StaticFieldRef) lhs).getField();
			modifiedType = field.getType();
			locs = getLocsOfStaticFieldRef(lhs, aliasSet);
		}
		if (lhs instanceof ArrayRef) {
			Value base = ((ArrayRef) lhs).getBase();
			//figure out the element type
			Type baseType = base.getType();
			modifiedType = ((ArrayType) baseType).baseType;
			locs = getLocsOfArrayRef(lhs, aliasSet);
		}

		if (locs != null && (!locs.isEmpty())) {
			values = analysis.getValues(rhs, aliasSet, modifiedType);
			if (analysis.isSingularLoc(locs)) {
				SymLoc loc = (SymLoc) locs.getFirst();

				pair = new ValueSetPair(loc, values);
				//System.out.println("*********GenSet: "+pair);
				//genSet.add(pair, genSet);
				//System.out.println("SEAnalysis adding new pair: "+pair);
				in.add(pair, in);

			} else {
				SymLoc temp;
				for (Iterator li = locs.iterator(); li.hasNext();) {
					temp = (SymLoc) li.next();
					pair = new ValueSetPair(temp, values);
					//System.out.println("*********GenSet: "+pair);
					//genSet.add(pair, genSet);
					//System.out.println("SEAnalysis adding new pair: "+pair);
					in.addWeak(pair, in);
				}
			}
		}
	}

	public MultiSet getLocsOfInstanceFieldRef(
		Value lhs,
		DataFlowSet aliasSet) {
		SymLoc symLoc;
		MultiSet locs = new MultiSet();
		logger.fine("InstanceFieldRef on the lhs: "+lhs);

		SootField field = ((InstanceFieldRef) lhs).getField();
		if(analysis.processRefOrScalarField(field)){
		  Type fieldType = field.getType();
		  Value base = ((InstanceFieldRef) lhs).getBase();
		  Type baseType = base.getType();
		  Accessor accessor = new Accessor(field, fieldType);
		  return getExtendedLocs(base, baseType, fieldType, accessor, aliasSet);
		}
		return new MultiSet();
	}

	public MultiSet getLocsOfArrayRef(Value lhs, DataFlowSet aliasSet) {
		SymLoc symLoc;
		MultiSet locs = new MultiSet();

		logger.fine("ArrayRef on lhs: "+lhs);
		Value base = ((ArrayRef) lhs).getBase();
		Value index = ((ArrayRef) lhs).getIndex();
		//figure out the element type
		Type baseType = base.getType();
		Type elemType = ((ArrayType) baseType).baseType;
		
		logger.fine("ArrayRef base type: " + baseType);
		logger.fine("Elem type: " + elemType);
		
		Accessor accessor = new Accessor(0, elemType);
		return getExtendedLocs(base, elemType, baseType, accessor, aliasSet);
	}

	public MultiSet getLocsOfStaticFieldRef(Value lhs, DataFlowSet aliasSet) {
		SymLoc symLoc;
		MultiSet locs = new MultiSet();
		logger.fine("Static field ref on the lhs");
		//check if this a static field of the unit
		SootField sf = ((StaticFieldRef) lhs).getField();
		Type fieldType = sf.getType();
		Type baseType = sf.getDeclaringClass().getType();
		//check whether this field ref needs to be processed
		//if (analysis.processRefOrScalarType(fieldType)) {
		if (analysis.processRefOrScalarField(sf)) {
			if (analysis.processRefType(baseType)) {
				//record this modification
				Root root = new RootGlobal(lhs, fieldType);
				symLoc = new SymLocPath(root, fieldType);
				symLoc.setModifiedType(fieldType);
				locs.add(symLoc);
			}

		} else {
			//this is a field in the environment or a field 
			//that doesn't need to be tracked
			//no change needs to be recorded
			logger.fine("env static field");
		}
		return locs;
	}

	protected void processCallSite(
		Stmt unit,
		FlowSet genSet,
		FlowSet killSet,
		DataFlowSet in) {
		
		
		//get targets of this site from the callGraph
		Collection targets = callGraph.getTargetsOf(unit, method);
		SootMethod called;
		String calledSignature;

		String callerSignature = method.getSignature();

		MultiSet newLocations =
			seAnalysisResults.getNewLocationsOf(callerSignature);

		for (Iterator ti = targets.iterator(); ti.hasNext();) {
			called = (SootMethod) ti.next();
			calledSignature = called.getSignature();

			if ((called.isPhantom()
				|| called.isNative()
				|| called.isAbstract())) {
				//nothing happens, this method doesn't have a body
				logger.warning("Abstract, phantom, or native method: " + called);
				return;
			}
			
			//if this is a callback, skip analysis
			MultiSet callbacks = seAnalysisResults.getCallBacksOf(callerSignature);
			
			CallBackValue callBackValue;
			SootMethod tempMethod;
			String tempMethodSignature;
			for(Iterator ci = callbacks.iterator(); ci.hasNext();){
				callBackValue = (CallBackValue)ci.next();
				tempMethod = callBackValue.getMethod();
				tempMethodSignature = tempMethod.getSignature();
				if(tempMethodSignature.equals(calledSignature)){
					return;
				}
			}
			
			//if side-effects summary info not available, call recursion
			Map methodToSideEffects;
			if (mustSE)
				methodToSideEffects =
					seAnalysisResults.getMethodToMustSideEffects();
			else
				methodToSideEffects = seAnalysisResults.getMethodToSideEffects();

			//if this an analyzable method, and it hasn't been analyzed, analyze it
			if (!methodToSideEffects.containsKey(calledSignature)) {
				//if(visited.contains(called))
				//this is a cycle in the graph
				//this case is taken care of inside analyzeAliases
				logger.fine(
						"\n****SideEffectsAnalysis Invoke Stmt: "
							+ unit
							+ "\nCalled Method: "
							+ called
							+ "\nVisited: "
							+ visited);
				if (mustSE)
					analysis.analyzeMustSideEffects(called, visited);
				else
					analysis.analyzeSideEffects(called, visited);

			}
			//at this point we should have a mapping for called
			if (!methodToSideEffects.containsKey(calledSignature))
				logger.severe("expected a mapping for: " + called);

			//incorporate the summary information into the genSet of this statement
			logger.fine("methodToSideEffects structure: " + methodToSideEffects);
			DataFlowSet summary =
				(DataFlowSet) methodToSideEffects.get(calledSignature);
			logger.fine(
					"--------- need to update "
						+ unit
						+ " with summary of "
						+ called);
			logger.fine("summary of called: " + summary);
			
			MultiSet mappings = getMappedSummaryOfCalledMethod(unit, summary);
			logger.fine("\n---------Summary mappings: \n" + mappings);
			ValueSetPair temp;
			for (Iterator mi = mappings.iterator(); mi.hasNext();) {
				temp = (ValueSetPair) mi.next();
				in.add(temp, in);
				logger.fine("\nout set: " + in);
			}

			//update the allocation sites of this method with 
			//the allocation sites of the called method

			//SootMethod caller = callGraph.getDeclaringMethod(unit);

			//System.out.println("??????????\nSide-effects analysis method: "+method);
			//System.out.println("Declaring method: "+caller);

			MultiSet newLocationsOfCalled =
				seAnalysisResults.getNewLocationsOf(calledSignature);
			newLocations.addAll(newLocationsOfCalled);
		}
	}

	
	
	/**
	 * For each entry loc->{values} in the summary, builds a mapping
	 * mappedLoc -> {mapped values}, packs them into one set and returns.
	 */
	protected MultiSet getMappedSummaryOfCalledMethod(
		Stmt unit,
		DataFlowSet summary) {
		MultiSet mappings = new MultiSet();
		SymLoc loc;
		SymLoc temp;
		MultiSet values;
		MultiSet mappedLocs;
		Iterator mi;
		MultiSet mappedVals;
		ValueSetPair pair;
		DataFlowSet aliasSet = (DataFlowSet) alias.getFlowBefore(unit);
		Set keys = summary.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			loc = (SymLoc) it.next();
			logger.fine("Mapping this location: " + loc);

			values = (MultiSet) summary.get(loc);

			mappedVals = analysis.getMappedVals(unit, values, aliasSet);

			mappedLocs = analysis.getMappedLocs(unit, loc, aliasSet);
			if (mappedLocs != null && !(mappedLocs.isEmpty())) {
				for (mi = mappedLocs.iterator(); mi.hasNext();) {
					temp = (SymLoc) mi.next();
					pair = new ValueSetPair(temp, mappedVals);
					mappings.add(pair);
				}
			}
		}
		return mappings;
	}

	/**
	 * Extends locations pointed to by base with accessor.
	 */
	protected MultiSet getExtendedLocs(
		Value base,
		Type baseType,
		Type modType,
		Accessor accessor,
		DataFlowSet aliasSet) {
		MultiSet locs = new MultiSet();
		SymLoc symLoc = null;

		//check whether this field/array access  needs to be processed
		if (analysis.processRefOrScalarType(modType)) {
			logger.info("unit field of type: " + modType);
			//check whether the base of this field ref is in the alias table
			if (aliasSet.containsKey(base)) {
				logger.fine("\nSideEffects base: " + base + " in the table");
				//pull out all symbolic locations for this alias
				//and change their value to TOP for now
				MultiSet set = (MultiSet) aliasSet.get(base);
				if (mustSE) {
					//if the variable may point to more than
					//one location, this can't go into the must analysis
					//check if any other situations need to be taken care of ??
					if (set.size() > 1)
						return locs;
				}

				logger.fine("\nAlias set for base: " + set);
				SymLoc temp = null;
				for (Iterator<SymLoc> is = set.iterator(); is.hasNext();) {
					temp = is.next();
					if (temp instanceof SymLocPath) {
						symLoc = getExtendedLoc((SymLocPath)temp, accessor);
						symLoc.setModifiedType(modType);
						symLoc.setType(baseType);
						locs.add(symLoc);
					}
				}
			} else {
				//there is no record for the base of this field
				//create an unknown location with field
				//TODO: recheck, this might create garbage
				symLoc = new SymLocTop(SymLocTop.UNKNOWN_LOC, baseType);
				symLoc.setModifiedAccessor(accessor);
				symLoc.setModifiedType(modType);
				locs.add(symLoc);
			}
		} else {
			//this is a field in the environment or a field 
			//that doesn't need to be tracked
			//no change needs to be recorded
			logger.fine("env field");
		}
		return locs;
	}

	/**
	 * Appends a modified accessor to temp.
	 *
	 */
	public SymLoc getExtendedLoc(SymLocPath symLoc, Accessor accessor) {
		SymLoc extLoc = new SymLocPath(symLoc);
		extLoc.setModifiedAccessor(accessor);
		return extLoc;
	}

	public SEAnalysisTF(
		UnitGraph g,
		SootMethod method,
		FlowSet initSet,
		EnvCallGraph callGraph,
		SEAnalysisResults seAnalysisResults,
		HashSet visited,
		SEAnalysis analysis,
		PTAnalysisTF aliases,
		boolean mustSE) {
		super(g);
		this.method = method;
		this.initSet = initSet;
		this.callGraph = callGraph;
		this.seAnalysisResults = seAnalysisResults;
		this.alias = aliases;
		this.visited = visited;
		this.analysis = analysis;
		this.mustSE = mustSE;
		
		logger.setLevel(Level.FINE);

		doAnalysis();
	}

	/**
	 * Returns true if l.f=val or l[i]=val need to be recorded by the 
	 * side-effects analysis. fieldType is the type of f (or array element type),
	 * baseType is the type of l.
	 */
	/*
	public boolean processField(Type fieldType, Type baseType)
	{
	if(containerAnalysis)
	    return true;
	if(fieldType instanceof BaseType) 
	{
	
	    if(fieldType instanceof RefType)
	    {
		if(unitTable.containsKey(fieldType.toString()))
		{
		    if(debug > 0)
			System.out.println("processField: unit field");
		    return true;
		}
	    }      
	    
	    else if(unitTable.containsKey(baseType.toString()))
	    {
		if(debug > 0)
		    System.out.println("processField: non ref field of the unit class");
		return true;
	    }
	}
	
	if(fieldType instanceof ArrayType)
	{
	    //figure out the element type
	    Type elemType = ((ArrayType)fieldType).baseType;
	    if(processField(elemType, baseType))
		return true;
	
	}
	return false;
	}
	*/
}
