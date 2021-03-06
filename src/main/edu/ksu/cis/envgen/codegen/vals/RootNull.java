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

import edu.ksu.cis.envgen.codegen.JavaPrinter;
import soot.*;

/**
 * Representation of a null value.
 * 
 */
public class RootNull extends Root {

	/** Type of value. */
	Value nullValue;

	public RootNull(Value value, Type type) {
		//this.nullValue = nullValue;
		super(value, type);
	}


	public String toString() {
		return "null";
	}

	public String getCode(JavaPrinter printer) {

		return "null";
	}

}
