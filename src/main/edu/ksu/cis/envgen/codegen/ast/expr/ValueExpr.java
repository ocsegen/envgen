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
package edu.ksu.cis.envgen.codegen.ast.expr;

import java.io.FileWriter;

import edu.ksu.cis.envgen.codegen.JavaPrinter;
import edu.ksu.cis.envgen.codegen.vals.*;

public class ValueExpr extends JavaExpr{
	SymLocValue value;
	
	public ValueExpr(SymLocValue value){
		assert value!= null;
		this.value = value;
	}

	public boolean throwsExceptions(){
		return false;
	}
	
	public String toString(){
		return value.toString();
	}
	
	public String getCode(JavaPrinter printer){
		
		return value.getCode(printer);
	}

	public void printToFile(FileWriter file, JavaPrinter printer, int shift){
		String code = value.getCode(printer);
		printf(file, code);
	}
}
