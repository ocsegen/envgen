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
package edu.ksu.cis.envgen.util;

import java.util.*;

import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.spec.*;
import edu.ksu.cis.envgen.spec.ltl2buchi.*;

//TODO: get rid of this class, after moving to java 1.5

/** 
 * Implementation of a set that
 * does comparisons of elements based on their
 * predefined actual types, not generic Object type.  Used in the Environment
 * generator to keep track of sets of Formulas, Nodes, Values, etc.
 */
public class MultiSet extends ArrayList {

	/** 
	 * Constructs a set from a given collection <code>c</code>. 
	 */
	public MultiSet(Collection c) {
		super(c);
	}

	/**
	 * Constructs an empty set. 
	 */
	public MultiSet() {
		super();
	}

	/**
	 * Looks for an element <code>obj</code>
	 * based on its real type, not
	 * generic Object type.
	 */

	public boolean contains(Object obj) {
		Iterator ci = this.iterator();
		Object temp = null;
		while (ci.hasNext()) {
			temp = ci.next();
			if (obj instanceof LTLNode) {
				//System.out.println("adding LTLNode");
				if (((LTLNode) obj).equals((LTLNode) temp))
					return true;
			}
			if (obj instanceof Node) {
				//System.out.println("adding Node");
				if (((Node) obj).equals((Node) temp))
					return true;
			}

			if (obj instanceof Proposition) {
				//System.out.println("adding Node");
				if (((Proposition) obj).equals((Proposition) temp))
					return true;
			}

			if (obj instanceof Intgr) {
				//System.out.println("adding Intgr");
				if (((Intgr) obj).equals((Intgr) temp))
					return true;
			}

			/*
			  if(obj instanceof SymLoc){
			  //System.out.println("adding Intgr");
			  if((((SymLoc)obj)).equals(((SymLoc)temp)))
			      return true;
			  }
			
			*/
			if (obj instanceof SymLocValue) {
				//System.out.println("adding Intgr");
				if (((SymLocValue) obj).equals((SymLocValue) temp))
					return true;
			}

		}
		return false;
	}
	
	public Object elementAt(int index){
		return get(index);
	}

	public Object get(Object obj) {
		Iterator ci = this.iterator();
		Object temp = null;
		while (ci.hasNext()) {
			temp = ci.next();
			if (obj instanceof LTLNode) {
				//System.out.println("adding LTLNode");
				if (((LTLNode) obj).equals((LTLNode) temp))
					return temp;
			}
			if (obj instanceof Node) {
				//System.out.println("adding Node");
				if (((Node) obj).equals((Node) temp))
					return temp;
			}

			if (obj instanceof Proposition) {
				//System.out.println("adding Node");
				if (((Proposition) obj).equals((Proposition) temp))
					return temp;
			}

			if (obj instanceof Intgr) {
				//System.out.println("adding Intgr");
				if (((Intgr) obj).equals((Intgr) temp))
					return temp;
			}

			/*
			  if(obj instanceof SymLoc){
			  //System.out.println("adding Intgr");
			  if((((SymLoc)obj)).equals(((SymLoc)temp)))
			      return temp;
			  }
			 */

			if (obj instanceof SymLocValue) {
				//System.out.println("adding Intgr");
				if (((SymLocValue) obj).equals((SymLocValue) temp))
					return temp;
			}

		}
		return null;
	}
	/** 
	 * Add <code>obj</code> to a set
	 * if it's not there and returns true,
	 * otherwise returns false.
	 */
	public boolean add(Object obj) {
		if (!contains(obj)) {
			super.add(obj);
			return true;
		}
		return false;
	}

	public void addLast(Object obj){
		super.add(obj);
	}

	public boolean addAll(Collection second) {
		Object temp;
		Iterator si = second.iterator();
		while (si.hasNext()) {
			temp = si.next();
			add(temp);

		}
		return true;
	}

	public Object getFirst(){
		return get(0);
	}
	
	public Object removeFirst(){
		return remove(0);
	}

	/**
	 * Removes <code>obj</code> fro a set.
	 */
	public boolean remove(Object obj) {
		Iterator ci = this.iterator();
		Object temp = null;
		for (int i = 0; ci.hasNext(); i++) {
			temp = ci.next();
			if (obj instanceof LTLNode) {
				//System.out.println("adding LTLNode");
				if (((LTLNode) obj).equals((LTLNode) temp)) {
					remove(i);
					return true;
				}
			}
			if (obj instanceof Node) {
				//System.out.println("adding Node");
				if (((Node) obj).equals((Node) temp)) {
					remove(i);
					return true;
				}
			}
			
			if (obj instanceof Proposition) {
				//System.out.println("adding Node");
				if (((Proposition) obj).equals((Proposition) temp)) {
					remove(i);
					return true;
				}
			}
			
			if (obj instanceof Intgr) {
				//System.out.println("adding Intgr");
				if (((Intgr) obj).equals((Intgr) temp)) {
					remove(i);
					return true;
				}
			}
			/*
			  if(obj instanceof SymLoc){
			  //System.out.println("adding Intgr");
			  if((((SymLoc)obj)).equals(((SymLoc)temp))){
			      remove(i);
			      return true;
			  }
			  }
			  */

			if (obj instanceof SymLocValue) {
				//System.out.println("adding Intgr");
				if (((SymLocValue) obj).equals((SymLocValue) temp)) {
					remove(i);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds <code>second</code> to this
	 * set, and returns the size if the resulting set.
	 */
	public int merge(MultiSet second) {

		Iterator si = second.iterator();
		while (si.hasNext()) {

			add(si.next());
		}
		return size();
	}

	public MultiSet intersect(MultiSet second) {

		MultiSet result = new MultiSet();

		Iterator si = second.iterator();
		Object temp;

		while (si.hasNext()) {
			temp = si.next();
			if (contains(temp))
				result.add(temp);

		}

		return result;

	}

	/**
	 * Removes elements of <code>second</code> from
	 * this set and returns the size
	 * of the resulting set.
	 */
	public int subtract(MultiSet second) {
		Iterator si = second.iterator();
		while (si.hasNext()) {
			remove(si.next());
		}
		return size();
	}

	/**
	 * Compares <code>second</code> set to this
	 * set elementwise, not pointerwise.
	 */
	public boolean equals(MultiSet second) {
		
		//System.out.println("COMPARING MULTISETS:");
		//System.out.println("this: " + this);
		//System.out.println("second: " + second);
		
		MultiSet temp = new MultiSet();
		temp.merge(this);
		if (size() == second.size() && size() == temp.merge(second)) {
			//System.out.println("MULTISETS EQUALS");
			return true;
		} else {
			//System.out.println("MULTISETS NOT EQUALS");
			return false;
		}
	}

	/**
	 * Constructs a string representation of this set.
	 */
	public String toString() {
		String temp = "[";
		Iterator si = this.iterator();
		while (si.hasNext()) {
			temp = temp.concat((si.next()).toString());
			if (si.hasNext())
				temp = temp.concat(",\n");
		}

		temp = temp.concat("]\n");
		return temp;
	}
}
