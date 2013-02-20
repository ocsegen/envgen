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

/** Implementation of a state of a finite state automaton. */
public class FSAState{
  /** Keeps track of transitions and the states where they lead to. */
  MultiSet transitions; 

  /** Incoming nodes. */
  MultiSet incoming;

    /** Outgoing nodes. */ 
  MultiSet outgoing;

  public FSAState(){
    transitions = new MultiSet();
    incoming = new MultiSet();
    outgoing = new MultiSet();
  }

  /** Returns a set of transitions from this state. */
  public MultiSet getTransitions(){
      return transitions;
  }

  public String toString(){
    return "incoming: "+incoming+"\noutgoing: "+outgoing+"\ntransitions: "+transitions;
    
  }
}
