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
package edu.ksu.cis.envgen.codegen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Type;
import edu.ksu.cis.envgen.codegen.ast.expr.JavaExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.ValueExpr;
import edu.ksu.cis.envgen.codegen.vals.TopValue;

/** Code generation routines used by both driver and stub generators. */
public class CodeGenUtil {
	/** 
	  * Generates a list of top values for a list of <code>types</code>. 
	  */
	public static List genDefaultArgs(List types) {
		
		assert(types != null);
		List result = new ArrayList();
	
		Type type;
		JavaExpr val;
		for (Iterator ai = types.iterator(); ai.hasNext();) {
			
			type = (Type)ai.next();
			val = new ValueExpr(new TopValue(type));
			result.add(val);
			
		}
		return result;
	}
}
