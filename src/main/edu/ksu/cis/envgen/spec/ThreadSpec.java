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

import soot.*;
import java.util.*;

/**
 *
 * Specification of a driver thread.
 */
public class ThreadSpec {
	
	/** Number of threads of this kind. */
	int num;
	
	/** Used to figure out a default name of the thread.*/ 
	int index;
	
	/** Thread name, optional. */
	String name;
	
	/** Specification in terms of LTL or regular expression formula. */
	SpecNode spec;

	//TODO: delete duplicates?
	//TODO: keep one copy of a proposition for all threads?
	/** List of atomic propositions used in thread's specification. */
	List propositions = new ArrayList(); 
	
	SootClass sc;
	
	public ThreadSpec(String name, int index, SpecNode spec){
		this.index = index;
		this.spec = spec;
		if (name != null)
			this.name = name;
		else
			this.name = "EnvDriverThread"+index;
	}
	
	public ThreadSpec(SootClass sc, int num){
		this.sc = sc;
		this.name = sc.getName();
		this.num = num;
	}
	
	public void setNum(int num){
		this.num = num;
	}
	
	public int getNum(){
		return num;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setSpecification(SpecNode spec){
		this.spec = spec;		
	}

	public SpecNode getSpecification(){
		return spec;
	}
	
	public void setPropositions(List propositions){
		this.propositions = propositions;
	}
	
	public List getPropositions(){
		return propositions;
	}
	
	
	public void setImplClass(SootClass sc){
		this.sc = sc;
	}
	
	public SootClass getImplClass(){
		return sc;
	}
	
	public void addProposition(Proposition prop){
		propositions.add(prop);
	}
	
	//public Type getType(){
	//	
	//	return sc.getType();
	//}
}
