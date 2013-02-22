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


/** Data structure to hold an LTL formula. */
public class LTLNode extends SpecNode{

 /**
  * Kinds of node:
  * 'P' atomic proposition, 
  * 'G' globally,
  * 'E' eventually (finally), 
  * '!' negation,              
  * 'X' next,                  
  * 'T' true,                 
  * 'F' false,                 
  * '&' and,                   
  * '|' or,                    
  * 'U' until,                 
  * 'V' dual of until.       
  */ 
  char kind;

  /** Left operand, used for binary operators such as &, |, etc. */
  LTLNode left;

  /** Right operand, used for binary operators such as &, |, etc. */
  LTLNode right;  

  public LTLNode(char op, int v, LTLNode l, LTLNode r){
    this.kind = op;
    this.val = v;
    this.left = l;
    this.right = r;
  }

  /** Default constructor. */
  public LTLNode(){
    this.left = null;
    this.right = null;
  }

  /** Copy constructor. */
  public LTLNode(LTLNode f){
    if(f== null){
      return;
    }
    this.kind = f.kind;
    switch(f.kind){
          case 'P':
		  this.val = f.val;
		  return;
          case '!':
          case 'X':
	          this.left = null;
		  this.right = new LTLNode(f.right);
		  return;
          case 'T':
          case 'F':
	          this.left = null;
		  this.right = null;
		  return;
          case '&':
          case '|':
          case 'U':
          case 'V':
	          this.left = new LTLNode(f.left);
		  this.right = new LTLNode(f.right);
		  return;

          default: logger.severe("LTLNode copy constr: unknown kind"+kind);

    }
  }


  /**
   * Builds an LTL formuls that corresponds to the single event condition, 
   * takes as an input a universe of propositions, and builds a formula
   * that states that no two propositions from the universe can hold at the
   * same time.
   */
  public static LTLNode singleEventCondition(java.util.List propositions){
    //one proposition is extra - a non-observable action (p0)
    LTLNode disjunction = new LTLNode('P', 0, null, null);
    LTLNode rightDisjunction = null;

    LTLNode conjunction = new LTLNode('!', 0, null, new LTLNode('&', 0, new LTLNode('P', propositions.size()-1, null, null), new LTLNode('P', propositions.size(), null, null)));
    //System.out.println("first conjunct: "+conjunction);
    LTLNode rightConjunction = null;

    LTLNode result = null;
    System.out.println("propositions size = "+propositions.size());

    for(int i=1; i<propositions.size()+1; i++)
    {
      rightDisjunction = new LTLNode('P',i, null, null);
      //System.out.println("rightDisjunction: "+rightDisjunction);
      disjunction = new LTLNode('|', 0, disjunction, rightDisjunction);
      //System.out.println("disjunction: "+disjunction);
    }

    for(int i=0; i<propositions.size()-1; i++){
      for(int j=i+1; (j<propositions.size()+1); j++){

	  rightConjunction = new LTLNode('!', 0, null, new LTLNode('&', 0, new LTLNode('P', i, null, null), new LTLNode('P', j, null, null)));
	  //System.out.println("rightConjunction: "+rightConjunction);
	  conjunction = new LTLNode('&', 0, conjunction, rightConjunction);
	  //System.out.println("conjunction: "+ conjunction);

      }
    }
    //need to return Globally(disjuction & conjunction), rewrite G as V using
    //rule Gp = f V p 
    return new LTLNode('V', 0, new LTLNode('f', 0, null, null), new LTLNode('&', 0, disjunction, conjunction));
  }
  
  public boolean equals(Object f){
	assert(f!=null);
    if(this.kind!=((LTLNode)f).kind)
      return false;

    switch(kind){
          case 'P':
	          return (val == ((LTLNode)f).val);

          case '!':
	          return (right.val == ((LTLNode)f).right.val);

          case 'X':
	          return (right.equals(((LTLNode)f).right));

          case 'T':
          case 'F':
	          return true;
          case '&':
          case '|':
	          return ((left.equals(((LTLNode)f).left) && right.equals(((LTLNode)f).right)) || 
			  (left.equals(((LTLNode)f).right) && right.equals(((LTLNode)f).left)));
          case 'U': 
          case 'V':
	          return (left.equals(((LTLNode)f).left)&&right.equals(((LTLNode)f).right));
          default: logger.severe("LTLNode equals: unknown kind "+kind);

    }
    return false;
  }

  /**
   * Pushes negations inside the formula, until they appear only in front
   * of atomic propositions.
   */
  public static  LTLNode pushNot(LTLNode f, boolean b){
    if(!b){    //false means need to push the ! inside
      switch(f.getKind()){
        case 'P': return new LTLNode('!', 0, null, f);

        case 'T': f.setKind('F');
	          return f;
        case 'F': f.setKind('T');
	          return f;

        case '!': return pushNot(f.getRight(), true);
        case 'X': f.setRight(pushNot(f.getRight(), false));
	          return f;	          
	   
        case '&': f.setKind('|');
	          f.setLeft(pushNot(f.getLeft(), false));
		  f.setRight(pushNot(f.getRight(), false));
		  return f;

        case '|': f.setKind('&');
	          f.setLeft(pushNot(f.getLeft(), false));
		  f.setRight(pushNot(f.getRight(), false));
		  return f;

        case 'U': f.setKind('V');
	          f.setLeft(pushNot(f.getLeft(), false));
		  f.setRight(pushNot(f.getRight(), false));
		  return f; 

        case 'V': f.setKind('U');
	          f.setLeft(pushNot(f.getLeft(), false));
		  f.setRight(pushNot(f.getRight(), false));
		  return f;
		  
        default:  System.out.println("SEVERE: Parser pushNot: "+ f.getKind());
      }
    }
    else{
      switch(f.getKind()){
        case '!': return pushNot(f.getRight(), false);
        case 'V':
        case 'U':
        case '|':
        case '&': f.setLeft(pushNot(f.getLeft(), true));
        case 'X': f.setRight(pushNot(f.getRight(), true));
        case 'P': 
        case 'T': 
        case 'F': return f;

        default:  System.out.println("SEVERE: pushNot: "+ f.getKind());
      }

    }
    return null;
  }
 
  public LTLNode getRight(){
    return right;
  }
    
  public void setRight(LTLNode f){
    right = f;
  }
    

  public LTLNode getLeft(){
    return left;
  }

  public void setLeft(LTLNode f){
    left = f;
  }

  public char getKind(){
    return kind;
  } 

  public void setKind(char c){
    kind = c;
  }
  public String toString(){
	    String temp = "";
	    switch(kind){
	          case 'P':
			  temp = " "+val;
			  break;
	          case '!':
	          case 'X':
		          temp ="("+kind+ " ("+right+"))";
			  break;
	          case 'T':
	          case 'F':
		          temp =""+kind;
			  break;
	          case '&':
	          case '|':
	          case 'U':
	          case 'V':
		          temp = "("+left+")"+kind+"("+right+")";
			  break;

	          default: logger.severe("LTLNode toString: unknown kind "+kind);
	    }    
	    return temp;
	  }
}
