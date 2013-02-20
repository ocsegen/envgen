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
package edu.ksu.cis.envgen.codegen.ast.stmt;

import java.io.FileWriter;

import edu.ksu.cis.envgen.codegen.JavaPrinter;
import edu.ksu.cis.envgen.codegen.ast.expr.JavaExpr;

public class ThrowStmt extends JavaStmt{
	JavaExpr expr;
	
	public ThrowStmt(JavaExpr expr){
		this.expr = expr;
	}

	public void printToFile(FileWriter file, int shift){
		logger.warning("finish");
	}
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift){
		logger.warning("finish");
	}
}
