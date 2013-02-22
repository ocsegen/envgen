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

/** Imlementation of an integer class, used
 * to keep track of sets of ids and comparison based on
 * the value of <code>num</code>.
 */
public class Intgr{
  int num;

  public Intgr(Intgr n){
    num = n.num;
  }

  public Intgr(int n){
    num = n;
  }
  
  public int getNum(){
    return num;
  }

  public boolean equals(Intgr second){
    //System.out.println("inside Intgr equals: \n");;
    return (second.num == this.num);
  }

  public String toString(){
    return ""+num;
  }
}
