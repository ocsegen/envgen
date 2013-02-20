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
package edu.ksu.cis.envgen.spec;

import java.util.logging.Logger;

/** Data structure for recording ltl formula or regular expression.  Holds fields and
 *  methods that are common for ltl and reg nodes: attributes and methods of 
 *  proposition nodes.
 */ 
public class SpecNode{

  /** String value of the proposition. Used for display
   * purposes. */
  Proposition proposition;

  /** 
   *Value that corresponds to the index of proposition
   * as it is recorded in the universe vector of propositions. 
   */
     int val;
     
     Logger logger = Logger.getLogger("envgen.spec");


	public void setProposition(Proposition p){
		this.proposition = p;
	}


  /** Returns the string representation of the proposition for 
   * the proposition node. */
  public Proposition getProposition(){
    return proposition;
  }

  /** Returns the index of the proposition in the universe 
   * of propositions.
   */
  public int getVal(){
  return val;
  } 
}
