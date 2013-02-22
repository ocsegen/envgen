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

/** Representation of a transition of a finite state automata. */
public class Transition{
  /** Action taken on this transition. */
  int actionNum;

  /** The state the transition goes to. */
  int nextState;

  // String param;

  public Transition(int a, int n){
    actionNum = a;
    nextState = n;
  }

    /*
  public Transition(int a, int n, String p){
    actionNum = a;
    nextState = n;
    param = p;
  }
    */

  public int getActionNum()
  {
    return actionNum;
  }

  public int getNextState()
  {
    return nextState;
  }

  public String toString(){
    return "(p"+actionNum+" s"+nextState+") ";
  }
}
