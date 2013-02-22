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
import soot.jimple.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.analysis.cg.EnvCallGraph;
import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.MultiSet;

/**
 * Implements methods shared by points-to and side-effects analyses.
 * 
 */
public abstract class StaticAnalysis{
	/** Unit classes. */
	public ModuleInfo unit;
	
	public ModuleInfo env;
	
	public ApplInfo applInfo;

	/** Needed for virtual call resolution.  */
	public EnvCallGraph callGraph;
	
	/**
	 * Integer field that keeps track of value k in k-limiting. The value can be
	 * varied by the user. The larger the value, the more precise information
	 * will be produced by the analysis.
	 */
	public int chainLength = 1; 

	public boolean unitAnalysis = false;  
	
	public boolean preserveCallBacks = false;
	
	public Logger logger = Logger.getLogger("edu.ksu.cis.envgen.analysis");
	
	public abstract void setOptions(Properties properties);
	
	/*
	public Assumptions acquireAssumptions(ApplInfo info) {
		assert info != null;
		
		this.unit = info.getUnit();
		this.env = info.getEnv();
		this.callGraph = info.getEnvCallGraph();
		
		
		List markedMethods = null;
		if(unitAnalysis)
			markedMethods = unit.getMethods();
		else 
			markedMethods = env.getMethods();
		
		logger.info("\nMethods under analysis: \n" + markedMethods);

		return analyze(markedMethods);
		
	}
	*/
	
	public abstract AnalysisResults analyze(List markedMethods);
	
	public boolean getPreserveCallBacks(){
		return preserveCallBacks;
	}
	
	/*
	public AnalysisResults getAnalysisResults(){
		return analysisResults;
	}
	
	public void setAnalysisResults(AnalysisResults results){
		this.analysisResults = results;
	}
	*/
	
	/**
	 * Maps symLoc of the called method into a location of the caller.
	 */

	public MultiSet getMappedReturnLocs(Stmt unit, MultiSet locations,
			DataFlowSet aliasSet) {
		MultiSet symLocSet;
		//MultiSet valsSet;
		MultiSet locsSet;
		MultiSet mappings = new MultiSet();
		//MultiSet temp;
		for (Iterator li = locations.iterator(); li.hasNext();) {
			symLocSet = (MultiSet) li.next();
			locsSet = getMappedLocs(unit, symLocSet, aliasSet);
			// locsSet = convertValsToLocs(valsSet);
			// check if mapping is needed
			mappings.addAll(locsSet);
		}
		return mappings;
	}

	/** Given an invoke expr and param, figures out the arg to plug in. */
	public MultiSet getMappedLocs(Stmt unit, SymLoc param, DataFlowSet aliasSet) {

		logger.finer("Looking for arg in stmt: " + unit+ ", with param: " + param);
		
		MultiSet mappings = new MultiSet();
		if (param instanceof SymLocPath) {

			Root root = ((SymLocPath)param).getRoot();
			
			//System.out.println("param location: " + param + ", with root: "+ root);
			Type rootType = root.getRootType();

			InvokeExpr ie = (InvokeExpr) unit.getInvokeExpr();
			List args = ie.getArgs();

			if (root instanceof RootThis  || ((root instanceof RootNewObject) && root.isInnerLoc())) {

				//System.out.println("*** inner or this root: " + root);
				// need to look at the instance on which this method
				// was called and update it
				// map this = receiver
				if (ie instanceof InstanceInvokeExpr) {
					Value instance = ((InstanceInvokeExpr) ie).getBase();
					mappings.addAll(getMappedArg(param, instance, aliasSet));
				} else if(ie instanceof StaticInvokeExpr){
					logger.finer("?? no arg for static invoke");
					mappings.add(param);
				}
				else	
					logger.severe("unknown invoke: "+ie);

			}
			
			
			//TODO: why can't we drop else
			else if (root instanceof RootParam) {
			
				int index = ((RootParam) root).getIndex();

				logger.finer("paramref root: " + root);
				// generate a pair where argNum = set of values of paramNum
				// map param = arg

				Value arg = (Value) args.get(index);
				// if this location is of our interest
				mappings.addAll(getMappedArg(param, arg, aliasSet));
			}

			else {
				// no need to map this location, it's not a param or this, or
				// inner class
				
				logger.fine("no need to map this location: " + param);
				mappings.add(param);
			}
		} else {
			// no need to map this location, it doesn't have a root
			logger.fine("no need to map this location: " + param);
			mappings.add(param);
		}

		return mappings;
	}

	/** Since arg may point to several locations, need to map each of them. */

	public MultiSet getMappedArg(SymLoc param, Value arg, DataFlowSet aliasSet) {

		
		logger.fine("getMappedArg: plugging arg :" + arg+ " into param: " + param);


		// map arg onto a set of locations
		MultiSet result = new MultiSet();

		if (arg instanceof NullConstant) {
			result.add(new SymLocPath(new RootNull(arg, NullType.v()), NullType.v()));
			return result;
		}

		// fix to handle strings better?
		if (arg instanceof StringConstant) {
			result.add(new SymLocTop(SymLocTop.ENVIRONMENT_LOC, arg.getType()));
			return result;
		}

		if (aliasSet.containsKey(arg)) {
			// pull out all symbolic locations for this alias
			// and change their value to TOP for now
			MultiSet set = (MultiSet) aliasSet.get(arg);

			logger.finer("Alias set for base: " + set);
			for (Iterator is = set.iterator(); is.hasNext();) {
				// This should always be a symbolic location
				SymLoc temp = (SymLoc) is.next();

				// temp is the location arg points to

				SymLoc mappedLoc = getMappedLocation(param, temp);

				if (mappedLoc != null)
					result.add(mappedLoc);
			}
		} else
			/*
			 * EnvPrinter.error( "Analysis, getMappedArg: " + arg + " not in
			 * aliasSet: " + aliasSet);
			 */

			result.add(new SymLocTop(SymLocTop.UNKNOWN_LOC, arg.getType()));
		// if (result.isEmpty())
		// EnvPrinter.error("Analysis, getMappedArg: empty mapping");

		return result;
	}

	/**
	 * Plugging an argument into a param placeholder, param = arg (works for
	 * this = receiver as this is just another parameter).
	 */

	public SymLoc getMappedLocation(SymLoc param, SymLoc arg) {

		
		logger.fine("getMappedLocation:  plugging arg: "
					+ arg + " into param: " + param);
		// take fields of a param and plug in an arg into the head of the param
	
		if(param instanceof SymLocPath){
				return getMappedPathParam((SymLocPath)param, arg);
		}

		else if (param instanceof SymLocTop) {	
				return getMappedTopParam((SymLocTop)param, arg);
			
		} else
			logger.severe("unknown param type: " + param);
		return null;
	}

	
	public SymLoc getMappedPathParam(SymLocPath param, SymLoc arg){
		
		if(arg instanceof SymLocPath){
			return getMappedPathParam(param, (SymLocPath)arg);
		}
		else if(arg instanceof SymLocTop){
			return getMappedPathParam(param, (SymLocTop)arg);
		}
		else
			logger.severe("wrong type of arg: "+arg);
		return null;
		
	}
	
	/** Plugging an arg into a parameter access path */
	public SymLoc getMappedPathParam(SymLocPath param, SymLocPath arg) {

		logger.fine("getMappedPathParam: plugging arg :" + arg
				+ " into param: " + param);
		
		Root root = param.getRoot();
		
		Type baseType = param.getType();
		List accessors = param.getAccessors();
		Accessor modifiedAccessor = param.getModifiedAccessor();
		Type modifiedType = param.getModifiedType();
		
		
	
		SymLocPath mappedLoc = new SymLocPath(arg); 
		
		
		if(param.isInnerLoc()){
			
			mappedLoc.addAccessor(new Accessor(root));
		}
		
		
		if (arg.isConcreteLoc()) {
			
			int argLength = arg.getChainLength();
			int paramLength = param.getChainLength();
			
			
			logger.finer("ArgLength: "+argLength+", paramLength: "+paramLength);
			
			
			//mappedLoc = new SymLocPath(arg);
	
			if(param.isConcreteLoc()){
				//two outcomes 
			
				if((argLength + paramLength) <= chainLength){
					
					//add all accessors
					mappedLoc.addAccessors(accessors);
				}
				else{
					//add some accessors, set to reachable
					int dif = chainLength - argLength;
					for (int i = 0; i < dif; i++){
						mappedLoc.addAccessor((Accessor)accessors.get(i));
					}
					mappedLoc.setKind(SymLocPath.REACHABLE_LOC);
					
				}
			}
			//additionally, if param is reachable, the result is reachable
			if (param.isReachableLoc()) {
				//append some accessors from param, make it reachable
				mappedLoc.setKind(SymLocPath.REACHABLE_LOC);
			}
		}
		
		//arg reachable, param either concrete or reachable
		else if (arg.isReachableLoc()) {
			
			//mappedLoc = new SymLocPath(arg);
			mappedLoc.setKind(SymLocPath.REACHABLE_LOC);
		
		} 
		
		
		mappedLoc.setType(baseType);
		if (modifiedAccessor != null) {
			mappedLoc.setModifiedAccessor(modifiedAccessor);
			mappedLoc.setModifiedType(modifiedType);
		}
		
		logger.fine("Resulting loc: "+mappedLoc);
		return mappedLoc;

	}
	
	public SymLoc getMappedPathParam(SymLocPath param, SymLocTop arg){
		
		//returns top for now, could return top.f....f for more precision
		Type baseType = param.getType();
		
		SymLocTop mappedLoc = new SymLocTop(SymLocTop.UNKNOWN_LOC, baseType);
		
		Accessor modifiedAccessor = param.getModifiedAccessor();
		if (modifiedAccessor != null) {
			mappedLoc.setModifiedAccessor(modifiedAccessor);
			mappedLoc.setModifiedType(param.getModifiedType());
		}
		return mappedLoc;
	}

	/** Plugging an arg into a top access path for param */
	public SymLoc getMappedTopParam(SymLoc param, SymLoc arg) {

		//always returns TOP regardless the arg
		
		Type baseType = param.getType();

		SymLoc mappedLoc = new SymLocTop(SymLocTop.UNKNOWN_LOC, baseType);
		
		Accessor modifiedAccessor = param.getModifiedAccessor();
		if (modifiedAccessor != null) {
			mappedLoc.setModifiedAccessor(modifiedAccessor);
			mappedLoc.setModifiedType(param.getModifiedType());
		}
		return mappedLoc;

	}

	public MultiSet getMappedVals(Stmt unit, MultiSet values,
			DataFlowSet aliasSet) {
		
		logger.fine("getMappedVales: Mapping values set: " + values);

		MultiSet mappedValues = new MultiSet();
		SymLocValue temp;
		//boolean top;
		//Value value;

		for (Iterator vi = values.iterator(); vi.hasNext();) {
			temp = (SymLocValue) vi.next();
			
			logger.finer("Mapping individual value: " + temp);
			// top = temp.getTop();
			// value = temp.getValue();
			// System.out.println("with value: "+value);
			if (temp instanceof SymLoc) {
				
				logger.finer("symLoc value: " + temp);
				// map this value
				MultiSet mappedLocs = getMappedLocs(unit, (SymLoc) temp,
						aliasSet);
				// convert these locs into values
				// MultiSet mapped = convertLocsToVals(mappedLocs);
				mappedValues.addAll(mappedLocs);

			} else {
				mappedValues.add(temp);
			}
		}
		
		if(mappedValues.isEmpty())
			logger.severe("Mapped Values empty");
		
		logger.finer("mapped set: " + mappedValues);

		return mappedValues;
	}

	public MultiSet getMappedLocs(Stmt unit, MultiSet values,
			DataFlowSet aliasSet) {
		
		logger.fine("getMappedLocs: mapping locs set: " + values);

		MultiSet mappedValues = new MultiSet();
		Iterator vi = values.iterator();
		SymLocValue temp;
		boolean top;
		Value value;

		while (vi.hasNext()) {
			temp = (SymLocValue) vi.next();
			
			logger.finer("mapping individual loc: " + temp);
			// top = temp.getTop();
			// value = temp.getValue();
			// System.out.println("with value: "+value);
			if (temp instanceof SymLoc) {
				
				logger.finer("symLoc value: " + temp);
				// map this value
				MultiSet mappedLocs = getMappedLocs(unit, (SymLoc) temp,
						aliasSet);
				// convert these locs into values
				// MultiSet mapped = convertLocsToVals(mappedLocs);
				mappedValues.addAll(mappedLocs);

			}
			else
				logger.severe("expected SymLoc, got: "+temp);
			// else{
			// mappedValues.add(temp);
			// }
		}
		
		if(mappedValues.isEmpty())
			logger.severe("mapped locs empty");
		
		logger.fine("result mapped set: " + mappedValues);
		return mappedValues;
	}

	public boolean addField(Type modifiedType) {
		if (chainLength >= 1 || !(modifiedType instanceof RefLikeType))
			return true;
		return false;
	}

	/**
	 * 
	 * 
	 */
	public MultiSet getValues(Value rhs, DataFlowSet aliasSet, Type fieldType) {
		MultiSet result = new MultiSet();
		SymLocValue val = null;
		
		logger.fine("??getValues of: " + rhs + " of type: "+ fieldType);

		if(fieldType instanceof RefLikeType){
			if (rhs instanceof NullConstant) {
				val = new SymLocPath(new RootNull(rhs, rhs.getType()), fieldType);
				result.add(val);
			}
			// if this is an object
			else if (aliasSet.containsKey(rhs)) {
				// find to what locations the rhs point to
				MultiSet rhsSet = (MultiSet) aliasSet.get(rhs);
				 
				if(rhsSet.isEmpty())
					logger.severe("getValues: no locations on the rhs");
				
				
					SymLoc rhsLoc;
					for (Iterator ri = rhsSet.iterator(); ri.hasNext();) {
						val = (SymLoc) ri.next();
						// val = new SymLocValue(rhsLoc);
						result.add(val);
					}
				
			} 
			else{ 
				val = new SymLocTop(SymLocTop.UNKNOWN_LOC, fieldType);
				result.add(val);
			}
			
		}
		else if (fieldType instanceof PrimType){
			if (fieldType instanceof IntType && rhs instanceof IntConstant) {
				int intVal = ((IntConstant) rhs).value;
				val = new ConstValue(intVal, fieldType);
				result.add(val);
			} else if (fieldType instanceof BooleanType
					&& rhs instanceof IntConstant) {
				int intVal = ((IntConstant) rhs).value;
				val = new ConstValue(intVal, fieldType);
				result.add(val);

			} else {
				
				logger.finer("???Building top value for: " + rhs
							+ " with type: " + fieldType);
				val = new TopValue(fieldType);
				result.add(val);
			}
		}
		

		return result;
	}
	
	
	public SymLoc getExtendedLoc(SymLoc symLoc, Accessor accessor) {
		if(symLoc instanceof SymLocPath)
			return getExtendedLoc((SymLocPath)symLoc, accessor);
		else
			return getExtendedLoc((SymLocTop)symLoc, accessor);
	}
	


	
	/**
	 * Extends a symbolic location with a field or array access expression.
	 */

	public SymLocPath getExtendedLoc(SymLocPath symLoc, Accessor accessor) {
		
		logger.fine("Adding accessor: "+accessor +" to symLoc: "+symLoc);
		
		SymLocPath extLoc = null;
		
		/*
		// reference from inner object to the enclosing object
		if (accessor.getName().startsWith("this")) {
			if(symLoc.isInnerLoc()){
				//return the enclosing loc
				extLoc = symLoc.getEnclosingLoc();
				return extLoc;
			}
			
		}
		*/
		

		Type type = accessor.getType();
				
		// check the length of the fields chain
		// make sure it's <= chainLength
		
		logger.finer("\nThe chain legth of " + symLoc + ": "+ symLoc.getChainLength());

		if (symLoc.isConcreteLoc()) {
			if (symLoc.getChainLength() <= chainLength) {
				
					
				logger.finer("------------unit field, unit base < chainLength");
				extLoc = new SymLocPath(symLoc);
				extLoc.addAccessor(accessor);
			} else {
				// this is beyond the limit of the analysis
				// convert to reachable
				
					
				logger.finer("-----------unit field, unit base = chain length");
				extLoc = new SymLocPath(symLoc);
				extLoc.setKind(SymLocPath.REACHABLE_LOC);
			}
		} else if (symLoc.isReachableLoc()) {
			extLoc = new SymLocPath(symLoc);
		}

		extLoc.setType(type);
		return extLoc;
	}

	public SymLoc getExtendedLoc(SymLocTop symLoc, Accessor accessor) {
		//EnvPrinter.error("getExtendedLoc: finish extending the top location");
		//return null;
		
		
			// no need to add a field
			logger.finer("---------------unit field, unknown location base");
			SymLoc extLoc = new SymLocTop(SymLocTop.UNKNOWN_LOC, accessor.getType());
			// set the type to the type of the field
			return extLoc;
	
		
	}
	
	/* Checks whether a set of locations represents a singular location */
	public boolean isSingularLoc(MultiSet locs) {
		if (locs.size() > 1)
			return false;
		// check the element
		// if choose or reachable then false
		SymLoc loc = (SymLoc) locs.getFirst();
		if (loc.isSingular())
			return true;
		return false;
	}
	

	/**
	 * Used in points-to analysis.
	 */
	public boolean processRefType(Type type) {
		return applInfo.isRelevantType(type);

		/*
		if (type instanceof RefType) {
	
			SootClass typeClass = ((RefType) type).getSootClass();
			return applInfo.isRelevantClass(typeClass);

		}
		if (type instanceof ArrayType) {
			// figure out the element type
			Type elemType = ((ArrayType) type).baseType;
			if (processRefType(elemType))
				return true;

		}

		return false;
		*/
	}
	
	/**
	 * Used in points-to analysis to determine call backs.
	 */
	public boolean processMethod(SootMethod sm) {
		//return applInfo.isRelevantCallBack(sm);
		return applInfo.isRelevantMethod(sm);
	}
	

	/**
	 * Used in se analysis.
	 */
	public boolean processRefField(SootField sf) {

		return applInfo.isRelevantField(sf);
	}

	
	/**
	 * Used in side-effects analysis.
	 */
	
	public boolean processRefOrScalarType(Type type) {
		//TODO: implement using applInfo.isRelevant
		if (type instanceof RefType) {
			return processRefType(type);
		}

		if (type instanceof ArrayType) {
			// figure out the element type
			Type elemType = ((ArrayType) type).baseType;
			if (processRefOrScalarType(elemType))
				return true;

		}

		if (type instanceof PrimType)
			return true;

		return false;
	}
	

	/**
	 * Used in side-effects analysis.
	 */

	public boolean processRefOrScalarField(SootField sf) {
		//Type fieldType = sf.getType();
		//return processRefOrScalarType(fieldType);
		return applInfo.isRelevantField(sf);
		
	}

}
