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
package edu.ksu.cis.envgen.spec.ltl2buchi;

import edu.ksu.cis.envgen.util.MultiSet;

/** Implementation of a buchi automaton node. A buchi automaton is a set of nodes. */
public class Node{
  /** Unique node id. */
  int id;

  /** The incoming edges represented by ids of nodes with outgoing
   * edges leading to this node. 
   */
  MultiSet incoming;

  /**
   * A set of formulas that must be true in this state and have not yet 
   * been processed.
   */
  MultiSet newF;

  /** Formulas that must be true in this state and have been processed. */
  MultiSet oldF;

    /** Formulas that must be true in all states that are immediate
     * successors of states satisfying formulas in oldF.
     */
  MultiSet nextF;

  /** Outgoing edges represented by ids of successor nodes. */
  MultiSet outgoing = new MultiSet();

  /** Set of atomic propositions that must be true in this state. */
  MultiSet propositions = new MultiSet(); 

  /** True if this is an accepting state. */
  //boolean accept; 

  public int getID(){ 
    return id;
  }

  /** Creates a node. */
  public Node(int pid, MultiSet pincoming, MultiSet pnewF, MultiSet poldF, 
	      MultiSet pnextF, MultiSet ppropositions){
    this.id = pid;
    this.incoming = (pincoming!=null)? new MultiSet(pincoming) : new MultiSet();
    this.newF = (pnewF!=null)? new MultiSet(pnewF) : new MultiSet();
    this.oldF = (poldF!=null)? new MultiSet(poldF) : new MultiSet(); 
    this.nextF = (pnextF!=null)? new MultiSet(pnextF) : new MultiSet();
    this.propositions = (ppropositions!=null)? new MultiSet(ppropositions) : new MultiSet();

  }

  public String toString(){
    return ("****id: "+id+"incoming: "+incoming+"must hold: "+propositions+"outgoing: "+outgoing);   
  }
}
