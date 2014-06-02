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

import soot.*;
import edu.ksu.cis.envgen.codegen.JavaPrinter;

/**
 * 
 * Representation of a memory location for scalars.
 * 
 */

public class ConstValue extends SymLocValue {

	int intValue;

	//boolean top;

	public ConstValue(int intValue, Type type) {
		this.intValue = intValue;
		this.type = type;
	}

	//public ConstValue(boolean top, Type type)
	//{
	//	this.top = top;
	//	this.type = type;

	// }

	public String toString() {

		//if(top)
		//  return EnvPrinter.getTopNameVal(type);

		if (type instanceof IntType)
			return ("" + intValue);
		else if (type instanceof BooleanType) {
			if (intValue == 0)
				return "false";
			else if (intValue == 1)
				return "true";
			logger.severe("LocValue, toSring: wrong boolean value");

		}
		logger.severe("LocValue, toString: no string");
		return "never get here";
	}

	public String getCode(JavaPrinter printer) {
		//if(top)
		//  return EnvPrinter.getTopNameVal(type);

		if (type instanceof IntType)
			return ("" + intValue);
		else if (type instanceof BooleanType) {
			if (intValue == 0)
				return "false";
			else if (intValue == 1)
				return "true";
			logger.severe("LocValue, getCode: wrong boolean value");

		}
		logger.severe("LocValue, getCode: no string");
		return "never get here";
	}
}
