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

import edu.ksu.cis.envgen.codegen.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;


/**
 * A wrapper around ast constructs (stmt, expr) used as atomic propositions in the
 * specification language.
 *
 */
public class Proposition {
	
	JavaExpr expr;
	
	/** Label as given in the definitions section. */
	String label;
	
	String propCode;
	
	Logger logger = Logger.getLogger("envgen.spec");
	
	public Proposition(){
		
	}
	
	public Proposition(JavaExpr expr){
		this.expr = expr;
	}
	
	public JavaExpr getExpression(){
		return expr;
	}
	
	public  String getCode(JavaPrinter printer){
		return propCode;
	}
	
	public String getCode(){
		return propCode;
	}
	
	public void setCode(String code){
		this.propCode = code;
	}
	
	public String getLabel(){
	  return label;
	}

	public void setLabel(String lb){
	  label = lb;
	}

	public String toString(){
		if(expr == null)
			return "null";
		return expr.toString();
			
	}
	
	/** Returns true if code of <code>this</code> method call
	 * is the same as code for <code>obj</code>.
	 */
	public boolean equals(Proposition prop)
	{
	  return this.toString().equals(prop.toString());
	}
}
