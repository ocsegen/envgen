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
 * Representation of a top value of a primitive type (meaning, the analysis has to assume 
 * the most general info about the value).
 * 
 */
public class TopValue extends SymLocValue {
	
	public TopValue(Type type) {
		//if(type instanceof PrimType)
			this.type = type;
		//else
		//	logger.severe("TopValue: create SymLocTop for type: "+type);
	}

	public String toString() {
		return "TOP";
	}

	public String getCode(JavaPrinter printer) {
		return printer.printTopValue(type);
    }
}
