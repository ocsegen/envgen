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
import java.util.logging.Logger;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;

/**
 * User specifications for drivers and stubs.
 * 
 */
public class UserSpec extends Assumptions{
	
	//TODO: separate driver specs from stub specs ?
	

	Map definitions = new HashMap();

	/** Unit class instantiations as defined in a specification. */
	List initProps = new ArrayList();
	
	/** List of specifications of driver threads other than the main */
	List threads =  new ArrayList();
	
	ThreadSpec thread;
	
	/** Specification of the main thread */
	ThreadSpec mainThread;
	
	List stubs = new ArrayList();
	
	Logger logger = Logger.getLogger("envgen.spec");

	public List getThreads() {
		return threads;
	}

	public Map getDefinitions() {
		return definitions;
	}


	public List getInitProps() {
		return initProps;
	}

	public ThreadSpec getMainThread(){
		return mainThread;
	}
	
	public List getStubs(){
		return stubs;
	}

	public void addThread(){
		int threadIndex = threads.size();
		thread = new ThreadSpec(null, threadIndex, null);	
	    threads.add(thread);
	}

	/*
	public void addThread(String name, SpecNode spec){
		System.out.println("TypeChecker: adding new thread: "+name);
		int threadIndex = threads.size();
		thread = new ThreadSpec(name, threadIndex, spec);	
		threads.add(thread);

	}
	*/
	
	public void addThread(SootClass implClass, int num){
		thread = new ThreadSpec(implClass, num);
		threads.add(thread);
	}
	
	public void setThreadNum(int num){
		thread.setNum(num);
	}
	
	public void setThreadName(String name){
		thread.setName(name);
		if(name.equals("Main")){
			mainThread = thread;
			//threads.remove(thread);
		}
	}
	
	public void setThreadSpecification(SpecNode spec){
		logger.info("setting thread spec to: "+spec);
		thread.setSpecification(spec);
		if(thread.getName().equals("Main")){
			//set the init section
			List mainProps = thread.getPropositions();
			Proposition prop;
			JavaExpr expr;
			for(Iterator ii = mainProps.iterator(); ii.hasNext(); ){
				prop = (Proposition)ii.next();
				expr = prop.getExpression();
				
				if(expr instanceof TypeDeclExpr){
					String varName = ((TypeDeclExpr)expr).getName();
					initProps.add(prop);
				}
			}
			
		}
	}

	
	public void addProposition(Proposition prop, SpecNode node) {
		//check if specPropositions already contains this proposition

		//int lastIndex = specPropositions.size()-1;
		//MultiSet threadPropositions = (MultiSet) specPropositions.get(lastIndex);
		
		//System.out.println("TypeChecker: adding proposition: "+prop);
		thread.addProposition(prop);

		/*
		if(threadPropositions.contains(prop)){
		
				//node.val = i;
			
		}
		else{
			threadPropositions.add(prop);
			//set the proposition value for the proposition node
			node.val = threadPropositions.size() - 1;
			if (debug)
				System.out.println(
					"Value of " + prop + " is " + (threadPropositions.size() - 1));
			//System.out.println("Val of the node: "+node.val);

		
		}
		*/
	}

	public void addInitProposition(Proposition prop){
		initProps.add(prop);
	}

	//TODO: refine
	public boolean containsVar(String name, Type type){
		
		Proposition alloc;
		JavaExpr expr;
		for (Iterator ii = initProps.iterator(); ii.hasNext();) {
			alloc = (Proposition) ii.next();
			expr = alloc.getExpression();
			
			String objName;
			String objType;
			if(expr instanceof TypeDeclExpr){
				//check the name
				objName = ((TypeDeclExpr)expr).getName();
				objType =  ((TypeDeclExpr)expr).getTypeStr();
				if (objName.equals(name) && objType.equals(type.toString())) 

					return true;
			
			}
		}
		return false;
	}
	
	//TODO: refine
	public String getVar(String type){
		
		Proposition alloc;
		JavaExpr expr;
		for (Iterator ii = initProps.iterator(); ii.hasNext();) {
			alloc = (Proposition) ii.next();
			expr = alloc.getExpression();
			
			String objName;
			String objType;
			if(expr instanceof TypeDeclExpr){
				//check the name
				//objName = ((TypeDeclExpr)expr).getName();
				objType =  ((TypeDeclExpr)expr).getTypeStr();
				if (objType.equals(type)) 

					return ((TypeDeclExpr)expr).getName();
			}
		}
		return "";
	}
	
	//TODO: refine
	public boolean containsVar(String name){
		
		Proposition alloc;
		JavaExpr expr;
		for (Iterator ii = initProps.iterator(); ii.hasNext();) {
			alloc = (Proposition) ii.next();
			expr = alloc.getExpression();
			
			String objName;
			String objType;
			if(expr instanceof TypeDeclExpr){
				//check the name
				objName = ((TypeDeclExpr)expr).getName();
				objType =  ((TypeDeclExpr)expr).getTypeStr();
				if (objName.equals(name) ) 

					return true;
			}
		}
		return false;
	}
	
	public void addDefinition(String key, Proposition prop){
		definitions.put(key, prop);
	}
	
	public void addDefinition(String key, Object value){
		definitions.put(key, value);
	}
	
	//public void addSpecification(ParserNode spec){
	//	thread.setSpecification(spec);
	//}

	
	public void addStubSpec(StubSpec spec){
		stubs.add(spec);
	}
}
