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
package edu.ksu.cis.envgen.codegen.vals;

import java.util.*;

import soot.*;
import edu.ksu.cis.envgen.util.*;

public class CallBackValue extends SymLocValue {
	
	SootMethod sm;
	//Stmt unit;
	MultiSet receiver;
	List args;
	
	public CallBackValue(MultiSet receiver, SootMethod sm, List args) {
		this.receiver = receiver;
		this.sm = sm;
		this.args = args;
	}
	
	//public CallBackValue(SootMethod sm, Stmt unit) {
	//	this.sm = sm;
	//	this.unit = unit;
	//}

	public SootMethod getMethod(){
		return sm;
	}
	
	public String toString() {
		
		//return receiver +"." + sm.getName() + args;	
		return this.getCode();
	}

	public String getCode() {
			
		String receiverStr = getSingleValue(receiver);
		
		String smStr = sm.getName();
		
		//String argsStr = printer.printArgs(args);
		String argsStr = printArgs(args);
		
		return receiverStr +"." + smStr + argsStr;
    }
	
	public String printArgs(List args) {
		
		//assert(args != null);
		
		if(args == null)
			return "null";
	
		String result = "";
		int count = 0;
		MultiSet argsSet;
		SymLocValue val;
		String valStr;
		
		for (Iterator ai = args.iterator(); ai.hasNext();) {
			
			argsSet = (MultiSet)ai.next();
			valStr = getSingleValue(argsSet) ;
			if (count != 0)
				result = result + ", ";
			//if(val!=null)
			//	symLocValueStr = val.toString();
			//else
			//	symLocValueStr = "null";
			result = result + valStr;
			count++;
		}
		return "(" + result + ")";
	}
	
	public String getSingleValue(MultiSet valSet){
		String valueStr = null;
		SymLocValue singleValue = null;
		if(valSet == null)
			valueStr = "null";
		else if(valSet.isEmpty())
			valueStr = "empty";
		else if(valSet.size()==1){
			singleValue = (SymLocValue)valSet.elementAt(0);
			valueStr = singleValue.toString();
		}
		else {
			//TODO: finish
			//logger.warning("receiver has more than one symbolic location");
			logger.fine("receiver has more than one symbolic location");
			singleValue = (SymLocValue)valSet.elementAt(0);
			valueStr = singleValue.toString();
		}
		return valueStr;
	}
	
}
