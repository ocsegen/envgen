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

public class NewArrayValue extends SymLocValue {
	
	Type type;
	
	public NewArrayValue(Type type) {
		this.type = type;
	}
	

	public String toString() {
		return "new " + type+"[]";
	}

	public String getCode(JavaPrinter printer) {
		return "new " + type+"[Abstraction.TOP_INT]";
    }

}
