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

import java.util.*;
import java.util.logging.Logger;
import java.io.*;

import edu.ksu.cis.envgen.codegen.JavaPrinter;
import edu.ksu.cis.envgen.spec.LTLNode;
import edu.ksu.cis.envgen.spec.parser.EnvGenParser;
import edu.ksu.cis.envgen.util.MultiSet;

/** This class implements costruction of a buchi automaton from an LTL formula. */
public class Graph{

  /** Unique node identification. */
  int nodeID;

  /** Set of nodes that represent a buchi automaton. */
  MultiSet buchi;

  /** Number of operations in the system. */  
  int numActions;
  
  Logger logger = Logger.getLogger("envgen.spec.ltl2buchi");

  public Graph(int num){
    numActions = num;
    nodeID = 1;
    buchi = new MultiSet();
  }

  /** 
   * Creates the first node of the buchi automaton that has 
   * node 0 in incoming set and <code>f</code>
   * in a set of new (not yet processed) formulas; expands
   * the first node.
   */
  public MultiSet createGraph(LTLNode f){
    if(f == null){
      logger.severe("Graph, createGraph: null formula");
    }
    MultiSet buchi = new MultiSet();
    //the constructor creates new sets for all null parameters
    Node init = new Node(nodeID++, null, null, 
                         null, null, null);

    //set the incoming node 0 (the dummy initial node)
    init.incoming.add(new Intgr(0));
    //put the original formula into a set of unprocessed fromulas
    init.newF.add(f);
    return expand(init, buchi);
  }

  /**
   * Expands node <code>cnd</code> and adds processed nodes
   * into the buchi automaton <code>set</code>.
   */
  public MultiSet expand(Node cnd, MultiSet set){

    Node nd;
    assert(cnd!=null);

    logger.fine("Graph, expand, node: "+cnd+ ", with new formulas: "+cnd.newF);

    //if no formulas left in the new set
    //this node if fully processed and ready to be added to the set
    if(cnd.newF.size()==0){
      nd = find(cnd, set);
      if(nd!=null){
        nd.incoming.merge(cnd.incoming);
        return set;
      }
      
      set.add(cnd);
      nd = new Node(nodeID++, new MultiSet(),cnd.nextF, null, null, null);
      nd.incoming.add(new Intgr(cnd.id));
      return expand(nd,set);
    } 

    LTLNode temp = (LTLNode)cnd.newF.removeFirst();
    logger.fine("Graph, expand, first new formula: "+temp);

    assert(temp!=null);  
    assert(cnd.oldF!=null);
    
    Node nd2;
    LTLNode leftFormula = temp.getLeft();
    LTLNode rightFormula = temp.getRight();

    switch(temp.getKind()){
        case 'P': // atom
	          if(!searchNeg(temp, cnd.oldF)){
	            cnd.oldF.add(temp);
		    cnd.propositions.add(temp);
		    //System.out.println("Graph, expand prop node: "+cnd);
	            return expand(cnd, set);
		  }  
		  return set;
		  
        case '!': // negation

	          if(rightFormula == null || rightFormula.getKind()!='P')
		    logger.severe("Graph, expand: bad unrolled formula");
		  if(!searchNeg(temp, cnd.oldF)){
	            cnd.oldF.add(temp);
		    cnd.propositions.add(temp);
		    //System.out.println("Graph, expand prop node: "+cnd);
	            return expand(cnd, set);
		  }  
		  return set;

        case 'X': // next
	          cnd.oldF.add(temp);
		  cnd.nextF.add(rightFormula);
	          return expand(cnd, set);

        case 'T': // true
	          cnd.oldF.add(temp);
	          return expand(cnd, set);

        case 'F': // false
	          return set;

        case '&': // and
	          cnd.oldF.add(temp);
		  if(!cnd.oldF.contains(leftFormula))
		    cnd.newF.add(leftFormula);
		  if(!cnd.oldF.contains(rightFormula))
		    cnd.newF.add(rightFormula);
		  return expand(cnd, set);

        case '|': // or
	          cnd.oldF.add(temp);
	          nd2 = new Node(nodeID++, cnd.incoming, cnd.newF, cnd.oldF, cnd.nextF, cnd.propositions);
		  if(!cnd.oldF.contains(rightFormula))
		    nd2.newF.add(rightFormula);
		  if(!cnd.oldF.contains(leftFormula))
		    cnd.newF.add(leftFormula);

		  return expand(nd2, expand(cnd, set));
		  
        case 'U': // until
	          cnd.oldF.add(temp);
	          nd2 = new Node(nodeID++, cnd.incoming, cnd.newF, cnd.oldF, cnd.nextF, cnd.propositions);
		  if(!cnd.oldF.contains(rightFormula))
	            nd2.newF.add(rightFormula);
		  if(!cnd.oldF.contains(leftFormula))
		    cnd.newF.add(leftFormula);
		  cnd.nextF.add(temp);

		  return expand(nd2, expand(cnd, set));		  

        case 'V': // dual of until
                  // (left V right)
                  // right is added to the new of both nodes
	          // left is added to the new of nd1
                  // (left V right) is added to the of nd2

	          //process the old formula
	          cnd.oldF.add(temp);

		  //cnd is n1
		  if(!cnd.oldF.contains(rightFormula))
		    cnd.newF.add(rightFormula);

		  //make a copy, right is added to both 
	          nd2 = new Node(nodeID++, cnd.incoming, cnd.newF, cnd.oldF, cnd.nextF, cnd.propositions);
		  
		  nd2.nextF.add(temp);

		  if(!cnd.oldF.contains(leftFormula))
		    cnd.newF.add(leftFormula);

		  return expand(nd2, expand(cnd, set));
        default:
	          logger.severe("Graph, expand: wrong operator: "+temp.getKind());
    }
    return null;
  }

  public Node find(Node nd, MultiSet set){
    Node temp = null;
    Iterator si = set.iterator();
    while(si.hasNext())
    {
      temp = (Node)si.next();
      if(nd.oldF.equals(temp.oldF) && nd.nextF.equals(temp.nextF))
	return temp;
    }
    return null;
  }

  public boolean searchNeg(LTLNode f, MultiSet set){
    Iterator si = set.iterator();
    LTLNode temp = null;
    if(f.getKind() == 'P'){
      while(si.hasNext()){
        temp = (LTLNode) si.next();
	if(temp.getKind() == '!' && temp.getRight().getVal() == f.getVal())
	  return true;
      }
      return false;
    }
    if(f.getKind() == '!'){
      while(si.hasNext()){
        temp = (LTLNode) si.next();
	if(temp.getKind() == 'P' && temp.getVal() == f.getRight().getVal())
	  return true;
      }
      return false;      
    }
    logger.severe("Graph, searchNeg: bad node supplied");
    return false;
  }
  public MultiSet getInitial(MultiSet buchi){
    MultiSet initial = new MultiSet();
    Node temp = null;
    if(buchi == null)
      logger.severe("Graph, getInitial: no buchi automaton");
    Iterator bi = buchi.iterator();
    while(bi.hasNext()){
      temp = (Node)bi.next();
      if(temp.incoming.contains(new Intgr(0)))
      	initial.add(temp);
    }
    return initial;
  }

  public void setOutgoing(MultiSet buchi){
    Intgr tempID ;
    Node tempNodeParent = null;
    Node tempNodeChild = null;
    if(buchi == null)
      logger.severe("Graph, setOutgoig: no buchi automaton");
    Iterator bi1 = buchi.iterator();
    for(int i=0; bi1.hasNext(); i++){
      tempNodeParent = (Node)bi1.next();
      tempID = new Intgr(tempNodeParent.getID());
      //System.out.println("*** tempNodeParent: "+tempID);
      Iterator bi2 = buchi.iterator();
      for(int j=0; bi2.hasNext(); j++){
	tempNodeChild = (Node)bi2.next();
	//System.out.println("*** tempNodeChild: "+tempNodeChild.getID());
	//System.out.println("*** tempNodeChild.incoming: "+tempNodeChild.incoming);
	if(tempNodeChild.incoming.contains(tempID)){
	  //add the child node to the outgoing of the parent node
	  tempNodeParent.outgoing.add(new Intgr(tempNodeChild.getID()));
 	}
      }
    }
  }

  public void setAccepting(MultiSet buchi){

  }
    /*
  public void optimize(MultiSet buchi){
    int size;
    Iterator bi1, bi2;
    Node 
    do{
      size = buchi.size()
      bi1 = buchi.iterator();
      for(int i=0, bi1.hasNext(); i++)
      {
	
	bi2 = buchi.iterator();
	for(int j=0; bi2.hasNext(); j++){

	}
      }
    } while(buchi.size()!=size)
    }*/

  public MultiSet getFSA(MultiSet buchi){
    if( buchi == null) 
      logger.severe("Graph, getFSA: no buchi automaton");
    MultiSet fsa = new MultiSet();
    
 
    return fsa;
  }

  public void setNodeID(int n){
    nodeID = n;
  }

}









