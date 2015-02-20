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
import soot.toolkits.scalar.*;

import edu.ksu.cis.envgen.util.MultiSet;

/**
 * Keeps mapping between references and a set
 * of locations they may refer to. Used in points-to
 * and side-effects.
 * 
 */
public class DataFlowSet implements FlowSet {

	/**
	 * Adds <code>obj</code> into <code>dest</code> set,
	 * requires <code>obj</code> to be of type ValueSetPair.
	 */

	HashMap<Value, MultiSet> map = new HashMap<Value, MultiSet>();
	Logger logger = Logger.getLogger("envgen.analysis.stat");

	public DataFlowSet() {

	}

	public DataFlowSet(HashMap<Value, MultiSet> map) {
		this.map = map;
	}

	public void add(Object obj, FlowSet dest) {

		Value key = ((ValueSetPair) obj).getLeft();
		MultiSet value = ((ValueSetPair) obj).getRight();

		//System.out.println("Adding key: "+key);

		if (this.containsKey(key)) {
			//need to overwrite previous value
			Value existingKey = this.getKey(key);
			((DataFlowSet) dest).put(existingKey, value);
		} else {
			((DataFlowSet) dest).put(key, value);
		}

		//System.out.println("Result: "+ (DataFlowSet)dest);

	}

	/**
	 * Adds a mapping without overwriting previous results, used for weak update.
	 * 
	 */
	public void addWeak(Object obj, FlowSet dest) {

		Value key = ((ValueSetPair) obj).getLeft();
		MultiSet value = ((ValueSetPair) obj).getRight();

		if (this.containsKey(key)) {
			//need to overwrite previous value
			Value existingKey = this.getKey(key);

			// need to merge the symbolic locations set for the temp
			// construct the new set of symbolic locations
			// remove temp and insert with the new set

			MultiSet existingSet = (MultiSet) this.get(existingKey);
			logger.fine(
					"\nexisting set: "
						+ existingSet
						+ "\nadditional set: "
						+ value);

			MultiSet mergedSet = new MultiSet(existingSet);

			mergedSet.merge(value);
			logger.fine("\nsuccessfully merged into: " + mergedSet);
			//if(!destSyms.equals(tempSyms))
			 ((DataFlowSet) dest).put(existingKey, mergedSet);

		} else {
			((DataFlowSet) dest).put(key, value);
		}

	}

	public void add(Object obj) {
		logger.severe("DataFlowSet, add: not implemented");
	}

	/**
	 * Returns true if <code>this</code> data flow set contains
	 * an element that is structurally identical to <code>obj</code>.
	 */
	public boolean containsKey(java.lang.Object obj) {
		logger.finer("DataFlowSet, containsKey: " + obj);
		Value temp;
		if (obj == null)
			logger.severe("DataFlowSet, containsKey: null object");

		Set<Value> keys = this.keySet();

		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			temp = it.next();

			if (obj instanceof Value) {
				if (temp.toString()
					.equals((((Value) obj).toString())))
					return true;
			}
		}
		return false;

	}

	/**
	 * Returns the first element from <code>this</code> data flow set
	 * that is structurally identical to <code>obj</code> (not necessarily
	 * the same pointer).
	 */
	public Value getKey(java.lang.Object obj) {
		Value temp;
		if (obj == null)
			logger.severe("DataFlowSet, getKey: null object");

		Set<Value> keys = this.keySet();
		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			temp = it.next();

			if (obj instanceof Value) {
				if (temp.toString()
					.equals((((Value) obj).toString())))
					return temp;
			}
		}
		return null;

	}

	/**
	 * Copies elements of <code>this</code> set into <code>dest</code> set.
	 * requires this != null
	 * requires dest != null
	 * 
	 */
	public void copy(FlowSet dest) {
		Value tempVar;
		MultiSet tempSyms;

		assert (this != null);
		assert (dest != null);

		//clear the destination set before adding elements of this
		if (!dest.isEmpty())
			dest.clear();

		Set<Value> keys = this.keySet();
		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			tempVar =  it.next();
			tempSyms = (MultiSet) this.get(tempVar);
			((DataFlowSet) dest).put(tempVar, tempSyms);
		}
	}

	/**
	 * Walks through the <code>other</code> set and subtracts its elements 
	 * from <code>this</code> set,
	 * writing the result into <code>dest</code>.
	 * @requires other != null && dest!=null
	 */
	public void difference(FlowSet other, FlowSet dest) {
		assert (other != null);
		assert (dest != null);
		
		Value temp, key;
		MultiSet otherSyms, tempSyms, destSyms;
		
		if (dest != this)
			this.copy(dest);

		logger.fine(
				"\nDataFlowSet difference, \n"
					+ this
					+ "\n\n minus \n\n"
					+ other);

		Set<Value> keys = ((DataFlowSet) other).keySet();
		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			temp = it.next();
			// if doesn't contain, no need to subtract
			if (((DataFlowSet) dest).containsKey(temp)) {
				key = ((DataFlowSet) dest).getKey(temp);
				// need to construct a new set of symbolic locations
				// that equals to difference between sets of
				// key and tem

				otherSyms = (MultiSet) ((DataFlowSet) other).get(temp);
				tempSyms = (MultiSet) ((DataFlowSet) dest).get(key);
				logger.fine(
						"\nkey value: "
							+ temp
							+ "\nsubtract dest: "
							+ tempSyms
							+ "\nsecond set: "
							+ otherSyms);

				//copy the set of key into new set
				destSyms = new MultiSet(tempSyms);

				destSyms.subtract(otherSyms);
				logger.fine(
						"\nsuccessfully subtracted into: " + destSyms);

				if (destSyms.isEmpty()) {
					//remove this key from the data flow set

					 ((DataFlowSet) dest).removeKey(key);
				} else {
					//overwite the previous binding with new set
					 ((DataFlowSet) dest).put(key, destSyms);
				}

			}

		}
		logger.fine(
				"\n-------Difference Final dest data flow set: "
					+ (DataFlowSet) dest);
	}

	public void difference(FlowSet other) {
		logger.severe("DataFlowSet, difference: not implemented");
	}

	public void intersection(FlowSet other, FlowSet dest) {
		
		assert (other != null);
		assert (dest != null);
		
		Value temp, key;
		MultiSet otherSyms, tempSyms, destSyms;

		logger.fine("\nDataFlowSet Intersection1, \nthis: "
					+ this
					+ "\nother: "
					+ other
					+ "\ndest: "
					+ dest);

		//if one of the sets is empty
		//set the intersection to be the 
		//non empty set
		if (this.isEmpty()) {
			other.copy(dest);
			return;
		}
		if (other.isEmpty()) {
			this.copy(dest);
			return;
		}

		//if (dest != this)
		//	this.copy(dest);

		//to avoid concurrent modification exception, we create a new set

		DataFlowSet result = new DataFlowSet();

		logger.fine(
				"\nDataFlowSet Intersection2, \nthis: "
					+ this
					+ "\nother: "
					+ other
					+ "\ndest: "
					+ dest);

		Set<Value> keys = ((DataFlowSet) this).keySet();

		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			temp = it.next();

			if (((DataFlowSet) other).containsKey(temp)) {
				key = ((DataFlowSet) other).getKey(temp);
				// need to intersect the symbolic locations set for the temp
				// construct the new set of symbolic locations

				otherSyms = (MultiSet) ((DataFlowSet) this).get(temp);
				tempSyms = (MultiSet) ((DataFlowSet) other).get(key);
				logger.fine(
						"\nkey value: "
							+ temp
							+ "\nintersect: "
							+ tempSyms
							+ "\nsecond set: "
							+ otherSyms);

				destSyms = tempSyms.intersect(otherSyms);

				logger.fine(
						"\nsuccessfully intersected into: " + destSyms);
				if (!destSyms.isEmpty())
					 ((DataFlowSet) result).put(key, destSyms);
				//else {
				//	((DataFlowSet) dest).removeKey(temp);
				//}
			} //else {
			//((DataFlowSet) dest).removeKey(temp);

			//}

		}

		result.copy(dest);

		logger.fine(
				"\n-------Intersection Final dest data flow set: "
					+ (DataFlowSet) dest);

	}

	public void intersection(FlowSet other) {
		logger.severe("DataFlowSet, intersection: not implemented");
	}

	public java.lang.Object emptySet() {
		logger.severe("DataFlowSet, emptySet: not implemented");
		return null;
	}

	public void remove(java.lang.Object obj, FlowSet dest) {

		logger.severe("DataFlowSet, remove: not implemented");
	}

	public void remove(java.lang.Object obj) {
		logger.severe("DataFlowSet, remove: not implemented");
	}

	public void removeKey(Object key) {
		map.remove(key);

	}

	public java.util.Iterator iterator() {
		logger.severe("DataFlowSet, iterator: not implemented");
		return null;

	}

	public java.util.List toList() {
		logger.severe("DataFlowSet, toList: not implemented");
		return null;
	}

	/**
	 * Walks through the <code>other<code> set and adds its elements to
	 * <code>this</code> set, writng the result into <code>dest</code>.
	 * @requires other != null && dest != null
	 * 
	 *
	 */
	public void union(FlowSet other, FlowSet dest) {
		
		assert (other != null);
		assert (dest != null);
		
		Value temp, key;
		MultiSet otherSyms, tempSyms, destSyms;

		if (dest != this)
			this.copy(dest);

		logger.fine("\nDataFlowSet union, \n" + this +"\n\n union \n\n" + other);

		Set<Value> keys = ((DataFlowSet) other).keySet();

		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			temp = it.next();

			if (!((DataFlowSet) dest).containsKey(temp)) {
				// need to add temp into the destination set
				((DataFlowSet) dest).put(temp, ((DataFlowSet) other).get(temp));
			} else {
				key = ((DataFlowSet) dest).getKey(temp);
				// need to merge the symbolic locations set for the temp
				// construct the new set of symbolic locations
				// remove temp and insert with the new set
				otherSyms = (MultiSet) ((DataFlowSet) other).get(temp);
				tempSyms = (MultiSet) ((DataFlowSet) dest).get(key);
				logger.fine(
						"\nkey value: "
							+ temp
							+ "\nmerge dest: "
							+ tempSyms
							+ "\nsecond set: "
							+ otherSyms);

				destSyms = new MultiSet(tempSyms);

				destSyms.merge(otherSyms);
				logger.fine(
						"\nsuccessfully merged into: " + destSyms);
				//if(!destSyms.equals(tempSyms))
				 ((DataFlowSet) dest).put(key, destSyms);

			}
		}
		logger.fine(
				"\n-------Union Final dest data flow set: "
					+ (DataFlowSet) dest);

	}

	public void union(FlowSet other) {

		logger.severe("DataFlowSet, union: not implemented");
	}

	/** Compares based on the content regardless of order.
	 */

	public boolean equals(Object second) {
		if (!(second instanceof DataFlowSet))
			logger.severe("DataFlowSet, equals, wrong type parameter!!!!");
		Set<Value> f = this.keySet();
		logger.fine(
				"DATA FLOW SET :" + this +" EQUALS WITH SECOND?: " + second);
		Set<Value> s = ((DataFlowSet) second).keySet();

		if (f.size() == s.size()) {
			Iterator<Value> fi = f.iterator();
			Value fVal;
			Value fKey;
			Value sKey;

			MultiSet sMultiSet;
			MultiSet fMultiSet;
			while (fi.hasNext()) {
				fVal = fi.next();
				if (((DataFlowSet) second).containsKey(fVal)) {
					fKey = this.getKey(fVal);
					sKey = ((DataFlowSet) second).getKey(fVal);
					fMultiSet = (MultiSet) this.get(fKey);
					sMultiSet = (MultiSet) (((DataFlowSet) second).get(sKey));
					if (!sMultiSet.equals(fMultiSet))
						return false;
				} else
					return false;

			}
			return true;

		} else
			return false;

	}

	public boolean contains(Object obj) {
		return containsKey(obj);
	}

	public HashMap getMap() {
		return map;
	}

	public String toString() {
		return map.toString();
	}

	/*-------------------------------------------------------------*/
	/*         Methods from HashMap interface                      */
	/*-------------------------------------------------------------*/

	public Object put(Object key, Object value) {
		return map.put((Value)key, (MultiSet)value);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public int size() {
		return map.size();
	}

	public Set<Value> keySet() {
		return map.keySet();
	}
	public boolean isEmpty() {
		return map.isEmpty();
	}
	/**
	 * Clears the data flow set so it contains no entries.
	 */
	public void clear() {

		map.clear();
	}

	/** Creates a shallow copy of <code>this</code> set. */

	public FlowSet clone() {
		//EnvPrinter.error("DataFlowSet, clone: not implemented");
		//System.out.println("Calling clone on "+this);

		Value key = null;
		MultiSet values = null;
		HashMap<Value, MultiSet> resultMap = new HashMap<Value,MultiSet>();

		Set<Value> keys = this.keySet();
		for (Iterator<Value> it = keys.iterator(); it.hasNext();) {
			key = it.next();
			values = (MultiSet) this.get(key);
			resultMap.put(key, values);
		}

		DataFlowSet result = new DataFlowSet(resultMap);

		//System.out.println("cloned set: "+result);		
		return result;
	}

}
