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
import edu.ksu.cis.envgen.codegen.JavaPrinter;

public class NewValue extends SymLocValue {
	
	String typeStr;
	//Type type;
	List args;
	
	//public NewValue(Type type) {
	//	this.type = type;
	//}
	
	public NewValue(Type type, List args) {
		this.type = type;
		this.args = args;
	}
	
	public NewValue(String type, List args) {
		this.typeStr = type;
		this.args = args;
	}

	public String toString() {
		logger.warning("finish printing args");
		if(type != null)
			return "new " + type + args;
			
		return "new " + typeStr + args;
	}

	public String getCode(JavaPrinter printer) {
		//logger.severe("finish printing args");
		
		if(type!=null){
			String typeS = printer.getPackageAdjustedType(type.toString());
			return "new " + typeS + printer.printArgs(args);
		}
		
		String typeS = printer.getPackageAdjustedType(typeStr);
		return "new " + typeS + printer.printArgs(args);
    }

}
